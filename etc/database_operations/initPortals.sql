
select @RootModule:=ROOT_DOMAIN_OBJECT.KEY_ROOT_MODULE from ROOT_DOMAIN_OBJECT;
select @RootPortal:=ROOT_DOMAIN_OBJECT.KEY_ROOT_PORTAL from ROOT_DOMAIN_OBJECT;

drop table IF EXISTS MODULES_TO_TRANSFER;
create table MODULES_TO_TRANSFER
  select child.ID_INTERNAL as KEY_MODULE, parent.ID_INTERNAL as 'KEY_PARENT_MODULE', NODE.NODE_ORDER, NODE.VISIBLE, NODE.ASCENDING, child.CONTENT_ID
    from CONTENT as child
      inner join NODE on NODE.KEY_CHILD = child.ID_INTERNAL
      inner join CONTENT as parent on parent.ID_INTERNAL = NODE.KEY_PARENT
    where child.OJB_CONCRETE_CLASS = 'net.sourceforge.fenixedu.domain.functionalities.Module';

alter table MODULES_TO_TRANSFER
  add column KEY_SECTION int(11) default null,
  add column KEY_PARENT_SECTION int(11) default null;

insert into CONTENT (OJB_CONCRETE_CLASS, KEY_ROOT_DOMAIN_OBJECT, NAME, CREATION_DATE, CONTENT_ID)
  select 'net.sourceforge.fenixedu.domain.Section', MODULES_TO_TRANSFER.KEY_MODULE, CONTENT.NAME, now(), MD5(MODULES_TO_TRANSFER.CONTENT_ID)
    from MODULES_TO_TRANSFER, CONTENT
    where MODULES_TO_TRANSFER.KEY_MODULE = CONTENT.ID_INTERNAL;

update MODULES_TO_TRANSFER, CONTENT
  set MODULES_TO_TRANSFER.KEY_SECTION = CONTENT.ID_INTERNAL
  where MODULES_TO_TRANSFER.KEY_MODULE = CONTENT.KEY_ROOT_DOMAIN_OBJECT;

update MODULES_TO_TRANSFER, CONTENT
  set MODULES_TO_TRANSFER.KEY_PARENT_SECTION = CONTENT.ID_INTERNAL
  where MODULES_TO_TRANSFER.KEY_PARENT_MODULE = CONTENT.KEY_ROOT_DOMAIN_OBJECT;

update MODULES_TO_TRANSFER
  set MODULES_TO_TRANSFER.KEY_PARENT_SECTION = @RootPortal
  where MODULES_TO_TRANSFER.KEY_PARENT_SECTION is null;

insert into NODE (OJB_CONCRETE_CLASS, KEY_ROOT_DOMAIN_OBJECT, KEY_PARENT, KEY_CHILD, NODE_ORDER, VISIBLE, ASCENDING)
  select 'net.sourceforge.fenixedu.domain.contents.ExplicitOrderNode', KEY_MODULE, KEY_PARENT_SECTION, KEY_SECTION, NODE_ORDER, VISIBLE, ASCENDING
    from MODULES_TO_TRANSFER;

insert into NODE (OJB_CONCRETE_CLASS, KEY_ROOT_DOMAIN_OBJECT, KEY_PARENT, KEY_CHILD, NODE_ORDER, VISIBLE, ASCENDING)
  select 'net.sourceforge.fenixedu.domain.contents.ExplicitOrderNode', child.ID_INTERNAL, MODULES_TO_TRANSFER.KEY_SECTION, child.ID_INTERNAL, NODE.NODE_ORDER, NODE.VISIBLE, NODE.ASCENDING
    from CONTENT as child
      inner join NODE on NODE.KEY_CHILD = child.ID_INTERNAL
      inner join CONTENT as parent on parent.ID_INTERNAL = NODE.KEY_PARENT
      inner join MODULES_TO_TRANSFER on MODULES_TO_TRANSFER.KEY_MODULE = parent.ID_INTERNAL
    where child.OJB_CONCRETE_CLASS = 'net.sourceforge.fenixedu.domain.functionalities.Functionality';

update CONTENT set KEY_ROOT_DOMAIN_OBJECT = 1;
update NODE set KEY_ROOT_DOMAIN_OBJECT = 1;

insert into AVAILABILITY_POLICY (OJB_CONCRETE_CLASS, KEY_ROOT_DOMAIN_OBJECT, KEY_ACCESSIBLE_ITEM, EXPRESSION, TARGET_GROUP, KEY_CONTENT)
  select AVAILABILITY_POLICY.OJB_CONCRETE_CLASS, 1, AVAILABILITY_POLICY.KEY_ACCESSIBLE_ITEM, AVAILABILITY_POLICY.EXPRESSION, AVAILABILITY_POLICY.TARGET_GROUP, MODULES_TO_TRANSFER.KEY_SECTION 
    from AVAILABILITY_POLICY
      inner join MODULES_TO_TRANSFER on MODULES_TO_TRANSFER.KEY_MODULE = AVAILABILITY_POLICY.KEY_CONTENT;
   
update AVAILABILITY_POLICY AP, CONTENT C set AP.KEY_CONTENT=C.ID_INTERNAL, C.KEY_AVAILABILITY_POLICY=AP.ID_INTERNAL WHERE AP.KEY_ACCESSIBLE_ITEM=C.OLD_ID_INTERNAL AND (C.OJB_CONCRETE_CLASS LIKE '%Section' OR C.OJB_CONCRETE_CLASS LIKE '%Item' OR C.OJB_CONCRETE_CLASS LIKE '%Module');

 
insert into AVAILABILITY_POLICY(OJB_CONCRETE_CLASS, KEY_ROOT_DOMAIN_OBJECT, KEY_CONTENT, EXPRESSION, TARGET_GROUP) select AP.OJB_CONCRETE_CLASS, '1', C1.ID_INTERNAL, AP.EXPRESSION, AP.TARGET_GROUP FROM CONTENT C, CONTENT C1, NODE N, AVAILABILITY_POLICY AP WHERE C.OJB_CONCRETE_CLASS LIKE '%Module' AND C1.OJB_CONCRETE_CLASS LIKE '%Section' AND C1.NAME=C.NAME AND N.KEY_CHILD=C1.ID_INTERNAL AND N.KEY_PARENT=@RootPortal AND C.KEY_AVAILABILITY_POLICY = AP.ID_INTERNAL;

update AVAILABILITY_POLICY AP, CONTENT C set C.KEY_AVAILABILITY_POLICY=AP.ID_INTERNAL WHERE AP.KEY_CONTENT=C.ID_INTERNAL AND C.KEY_AVAILABILITY_POLICY IS NULL;

-- Connnect MetaDomainObjectPortals

create TEMPORARY TABLE ORDER_TABLE (CHILD_ID INTEGER, NODE_ORDER INTEGER auto_increment, KEY(NODE_ORDER));


insert into ORDER_TABLE(CHILD_ID) select C.ID_INTERNAL FROM CONTENT C WHERE OJB_CONCRETE_CLASS LIKE '%MEtaDomainObjectPortal';

insert into NODE(OJB_CONCRETE_CLASS, KEY_ROOT_DOMAIN_OBJECT,KEY_PARENT,KEY_CHILD,NODE_ORDER,VISIBLE,ASCENDING) select 'net.sourceforge.fenixedu.domain.contents.ExplicitOrderNode', '1', @RootPortal, C.ID_INTERNAL, (SELECT count(*) +1 FROM NODE WHERE KEY_PARENT=@RootPortal)+OT.NODE_ORDER, '1','1' FROM CONTENT C, ORDER_TABLE OT WHERE C.OJB_CONCRETE_CLASS LIKE '%MetaDomainObjectPortal' AND OT.CHILD_ID = C.ID_INTERNAL;

drop TEMPORARY TABLE ORDER_TABLE;




update CONTENT, AVAILABILITY_POLICY SET CONTENT.KEY_AVAILABILITY_POLICY=NULL WHERE CONTENT.KEY_AVAILABILITY_POLICY=AVAILABILITY_POLICY.ID_INTERNAL AND EXPRESSION LIKE '%homepage%';
delete FROM AVAILABILITY_POLICY WHERE EXPRESSION LIKE '%homepage%';
