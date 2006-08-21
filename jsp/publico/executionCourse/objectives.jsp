<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<html:xhtml/>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/taglibs-datetime.tld" prefix="dt" %>

<h2>
	<bean:message key="link.objectives"/>
</h2>

<bean:define id="executionPeriod" name="executionCourse" property="executionPeriod" type="net.sourceforge.fenixedu.domain.ExecutionPeriod"/>

<logic:iterate id="entry" name="executionCourse" property="curricularCoursesIndexedByCompetenceCourse">
	<bean:define id="competenceCourse" name="entry" property="key"/>
	<logic:equal name="competenceCourse" property="curricularStage.name" value="APPROVED">
		<h3>
			<bean:write name="competenceCourse" property="name"/>
			<br/>
			<logic:iterate id="curricularCourse" name="entry" property="value" indexId="i">
				<logic:notEqual name="i" value="0"><br/></logic:notEqual>
				<bean:define id="degree" name="curricularCourse" property="degreeCurricularPlan.degree"/>
				<bean:message bundle="ENUMERATION_RESOURCES" name="degree" property="degreeType.name"/>
				<bean:message key="label.in"/>
				<bean:write name="degree" property="nome"/>
			</logic:iterate>
		</h3>
		<blockquote>
			<h4>
				<bean:message key="label.generalObjectives"/>
			</h4>
			<logic:present name="competenceCourse" property="objectives">
				<bean:write name="competenceCourse" property="objectives" filter="false"/>
			</logic:present>
			<logic:notEmpty name="competenceCourse" property="objectivesEn">
				<br/>
				<h4>
					<bean:message key="label.generalObjectives.eng"/>
				</h4>
				<bean:write name="competenceCourse" property="objectivesEn" filter="false"/>
			</logic:notEmpty>
		</blockquote>
	</logic:equal>
</logic:iterate>

	<logic:iterate id="curricularCourse" type="net.sourceforge.fenixedu.domain.CurricularCourse"
			name="executionCourse" property="curricularCoursesSortedByDegreeAndCurricularCourseName">
		<bean:define id="degree" name="curricularCourse" property="degreeCurricularPlan.degree"/>
		<logic:notEqual name="curricularCourse" property="isBolonha" value="true">
			<% net.sourceforge.fenixedu.domain.Curriculum curriculum = curricularCourse.findLatestCurriculumModifiedBefore(executionPeriod.getExecutionYear().getEndDate()); %>
			<% net.sourceforge.fenixedu.domain.Curriculum lastCurriculum = curricularCourse.findLatestCurriculum(); %>
			<% request.setAttribute("curriculum", curriculum); %>
			<% request.setAttribute("lastCurriculum", lastCurriculum); %>

				<h3>
					<bean:write name="curricularCourse" property="name"/>
					<br/>
					<bean:message bundle="ENUMERATION_RESOURCES" name="degree" property="degreeType.name"/>
					<bean:message key="label.in"/>
					<bean:write name="degree" property="nome"/>
				</h3>
				<blockquote>
					<logic:present name="curriculum">
						<h4>
							<bean:message key="label.generalObjectives"/>
						</h4>
						<bean:write name="curriculum" property="generalObjectives" filter="false"/>
						<logic:notEmpty name="curriculum" property="generalObjectivesEn">
							<br/>
							<h4>
								<bean:message key="label.generalObjectives.eng"/>
							</h4>
							<bean:write name="curriculum" property="generalObjectivesEn" filter="false"/>
						</logic:notEmpty>
						<logic:notEmpty name="curriculum" property="operacionalObjectives">
							<h4>
								<bean:message key="label.operacionalObjectives"/>
							</h4>
							<bean:write name="curriculum" property="operacionalObjectives" filter="false"/>
						</logic:notEmpty>
						<logic:notEmpty name="curriculum" property="operacionalObjectivesEn">
							<br/>
							<h4>
								<bean:message key="label.operacionalObjectives.eng"/>
							</h4>
							<bean:write name="curriculum" property="operacionalObjectivesEn" filter="false"/>
						</logic:notEmpty>
					</logic:present>
					<logic:notPresent name="curriculum">
						<bean:message key="message.objectives.not.defined"/>
					</logic:notPresent>
				</blockquote>
		</logic:notEqual>

		<br/>
		<br/>
	</logic:iterate>
