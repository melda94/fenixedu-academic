create table `INQUIRY_TEMPLATE_INQUIRY_BLOCK` (`OID_INQUIRY_TEMPLATE` bigint unsigned, `OID_INQUIRY_BLOCK` bigint unsigned, primary key (OID_INQUIRY_TEMPLATE, OID_INQUIRY_BLOCK), index (OID_INQUIRY_TEMPLATE), index (OID_INQUIRY_BLOCK)) ENGINE=InnoDB, character set latin1;
alter table `INQUIRY_BLOCK` drop key OID_INQUIRY, drop OID_INQUIRY;
 
#curricular 1sem
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5909874999297, 5884105195521);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5909874999297, 5884105195522);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5909874999297, 5884105195523);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5909874999297, 5884105195524);
#curricular 2sem
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5909875000497, 5884105195521);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5909875000497, 5884105195522);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5909875000497, 5884105195523);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5909875000497, 5884105195524);
#delegate
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5931349836177, 5884105196321);
#student teacher 1sem
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5935644803074, 5884105195525);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5935644803074, 5884105195526);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5935644803074, 5884105195527);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5935644803074, 5884105196521);
#student teacher 2sem
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5935644804274, 5884105195525);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5935644804274, 5884105195526);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5935644804274, 5884105195527);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5935644804274, 5884105196521);
#teacher 1sem
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5944234738265, 5884105196921);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5944234738265, 5884105196922);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5944234738265, 5884105196923);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(5944234738265, 5884105196924);
#results 1sem
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6008659247305, 5884105195721);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6008659247305, 5884105195722);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6008659247305, 5884105195723);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6008659247305, 5884105195921);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6008659247305, 5884105195922);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6008659247305, 5884105196121);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6008659247305, 5884105196721);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6008659247305, 5884105197521);
#regent 1sem
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6098853561121, 5884105197121);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6098853561121, 5884105197122);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6098853561121, 5884105197123);
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6098853561121, 5884105197124);
# coordinator 1sem
insert into INQUIRY_TEMPLATE_INQUIRY_BLOCK values(6116033430505, 5884105197321);

update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326337 where OID_INQUIRY_QUESTION=5866925328737;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326338 where OID_INQUIRY_QUESTION=5866925328738;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326339 where OID_INQUIRY_QUESTION=5866925328739;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326340 where OID_INQUIRY_QUESTION=5866925328740;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326341 where OID_INQUIRY_QUESTION=5866925328741;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326342 where OID_INQUIRY_QUESTION=5866925328742;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326343 where OID_INQUIRY_QUESTION=5866925328743;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326344 where OID_INQUIRY_QUESTION=5866925328744;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5927054868489 where OID_INQUIRY_QUESTION=5927054870889;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326346 where OID_INQUIRY_QUESTION=5866925328746;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326347 where OID_INQUIRY_QUESTION=5866925328747;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326348 where OID_INQUIRY_QUESTION=5866925328748;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326349 where OID_INQUIRY_QUESTION=5866925328749;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326350 where OID_INQUIRY_QUESTION=5866925328750;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326351 where OID_INQUIRY_QUESTION=5866925328751;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326352 where OID_INQUIRY_QUESTION=5866925328752;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5927054868497 where OID_INQUIRY_QUESTION=5927054870897;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966610 where OID_INQUIRY_QUESTION=5914169969010;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966611 where OID_INQUIRY_QUESTION=5914169969011;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966612 where OID_INQUIRY_QUESTION=5914169969012;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966613 where OID_INQUIRY_QUESTION=5914169969013;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966614 where OID_INQUIRY_QUESTION=5914169969014;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966615 where OID_INQUIRY_QUESTION=5914169969015;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966616 where OID_INQUIRY_QUESTION=5914169969016;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966617 where OID_INQUIRY_QUESTION=5914169969017;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966618 where OID_INQUIRY_QUESTION=5914169969018;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966619 where OID_INQUIRY_QUESTION=5914169969019;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966620 where OID_INQUIRY_QUESTION=5914169969020;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966621 where OID_INQUIRY_QUESTION=5914169969021;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966622 where OID_INQUIRY_QUESTION=5914169969022;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966623 where OID_INQUIRY_QUESTION=5914169969023;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966624 where OID_INQUIRY_QUESTION=5914169969024;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966625 where OID_INQUIRY_QUESTION=5914169969025;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966626 where OID_INQUIRY_QUESTION=5914169969026;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966627 where OID_INQUIRY_QUESTION=5914169969027;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966628 where OID_INQUIRY_QUESTION=5914169969028;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966629 where OID_INQUIRY_QUESTION=5914169969029;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966630 where OID_INQUIRY_QUESTION=5914169969030;

update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966631 where OID_INQUIRY_QUESTION=5914169969031;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326376 where OID_INQUIRY_QUESTION=5866925328776;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326377 where OID_INQUIRY_QUESTION=5866925328777;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326378 where OID_INQUIRY_QUESTION=5866925328778;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326379 where OID_INQUIRY_QUESTION=5866925328779;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5866925326380 where OID_INQUIRY_QUESTION=5866925328780;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966637 where OID_INQUIRY_QUESTION=5914169969037;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966638 where OID_INQUIRY_QUESTION=5914169969038;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966639 where OID_INQUIRY_QUESTION=5914169969039;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966640 where OID_INQUIRY_QUESTION=5914169969040;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966641 where OID_INQUIRY_QUESTION=5914169969041;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966642 where OID_INQUIRY_QUESTION=5914169969042;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966643 where OID_INQUIRY_QUESTION=5914169969043;
update QUESTION_ANSWER set OID_INQUIRY_QUESTION=5914169966644 where OID_INQUIRY_QUESTION=5914169969044;
