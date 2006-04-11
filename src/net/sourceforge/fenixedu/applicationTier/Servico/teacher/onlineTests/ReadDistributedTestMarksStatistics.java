/*
 * Created on 12/Fev/2004
 *  
 */
package net.sourceforge.fenixedu.applicationTier.Servico.teacher.onlineTests;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sourceforge.fenixedu.applicationTier.Service;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.InvalidArgumentsServiceException;
import net.sourceforge.fenixedu.dataTransferObject.ExecutionCourseSiteView;
import net.sourceforge.fenixedu.dataTransferObject.InfoExecutionCourse;
import net.sourceforge.fenixedu.dataTransferObject.SiteView;
import net.sourceforge.fenixedu.dataTransferObject.onlineTests.InfoDistributedTestWithTestScope;
import net.sourceforge.fenixedu.dataTransferObject.onlineTests.InfoSiteStudentsTestMarksStatistics;
import net.sourceforge.fenixedu.domain.onlineTests.DistributedTest;
import net.sourceforge.fenixedu.domain.onlineTests.StudentTestQuestion;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;
import net.sourceforge.fenixedu.persistenceTier.onlineTests.IPersistentStudentTestQuestion;
import net.sourceforge.fenixedu.util.tests.CorrectionFormula;

/**
 * @author Susana Fernandes
 * 
 */
public class ReadDistributedTestMarksStatistics extends Service {

	public SiteView run(Integer executionCourseId, Integer distributedTestId)
			throws FenixServiceException, ExcepcaoPersistencia {

		InfoSiteStudentsTestMarksStatistics infoSiteStudentsTestMarksStatistics = new InfoSiteStudentsTestMarksStatistics();

		DistributedTest distributedTest = rootDomainObject.readDistributedTestByOID(distributedTestId);
		if (distributedTest == null)
			throw new InvalidArgumentsServiceException();

		IPersistentStudentTestQuestion persistentStudentTestQuestion = persistentSupport
				.getIPersistentStudentTestQuestion();
        Set<StudentTestQuestion> studentTestQuestions = distributedTest.findStudentTestQuestionsOfFirstStudentOrderedByTestQuestionOrder();

		List<String> correctAnswersPercentageList = new ArrayList<String>();
		List<String> partiallyCorrectAnswersPercentage = new ArrayList<String>();
		List<String> wrongAnswersPercentageList = new ArrayList<String>();
		List<String> notAnsweredPercentageList = new ArrayList<String>();
		List<String> answeredPercentageList = new ArrayList<String>();

		DecimalFormat df = new DecimalFormat("#%");
        int numOfStudent = persistentStudentTestQuestion.countNumberOfStudents(distributedTest);
		for (StudentTestQuestion studentTestQuestion : studentTestQuestions) {
			if (studentTestQuestion.getCorrectionFormula().getFormula().intValue() == CorrectionFormula.FENIX) {

				correctAnswersPercentageList.add(df.format(persistentStudentTestQuestion
						.countCorrectOrIncorrectAnswers(studentTestQuestion.getTestQuestionOrder(),
								studentTestQuestion.getTestQuestionValue(), true, distributedTest
										.getIdInternal())
						* java.lang.Math.pow(numOfStudent, -1)));
				wrongAnswersPercentageList.add(df.format(persistentStudentTestQuestion
						.countCorrectOrIncorrectAnswers(studentTestQuestion.getTestQuestionOrder(),
								studentTestQuestion.getTestQuestionValue(), false, distributedTest
										.getIdInternal())
						* java.lang.Math.pow(numOfStudent, -1)));

				int partially = persistentStudentTestQuestion.countPartiallyCorrectAnswers(
						studentTestQuestion.getTestQuestionOrder(), studentTestQuestion
								.getTestQuestionValue(), distributedTest.getIdInternal());
				if (partially != 0)
					partiallyCorrectAnswersPercentage.add(df.format(partially
							* java.lang.Math.pow(numOfStudent, -1)));
				else
					partiallyCorrectAnswersPercentage.add(new String("-"));

			}
			int responsed = persistentStudentTestQuestion.countResponsedOrNotResponsed(
					studentTestQuestion.getTestQuestionOrder(), true, distributedTest.getIdInternal());

			notAnsweredPercentageList.add(df.format((numOfStudent - responsed)
					* java.lang.Math.pow(numOfStudent, -1)));
			answeredPercentageList.add(df.format(responsed * java.lang.Math.pow(numOfStudent, -1)));
		}
		infoSiteStudentsTestMarksStatistics.setCorrectAnswersPercentage(correctAnswersPercentageList);
		infoSiteStudentsTestMarksStatistics
				.setPartiallyCorrectAnswersPercentage(partiallyCorrectAnswersPercentage);
		infoSiteStudentsTestMarksStatistics.setWrongAnswersPercentage(wrongAnswersPercentageList);
		infoSiteStudentsTestMarksStatistics.setNotAnsweredPercentage(notAnsweredPercentageList);
		infoSiteStudentsTestMarksStatistics.setAnsweredPercentage(answeredPercentageList);
		infoSiteStudentsTestMarksStatistics.setInfoDistributedTest(InfoDistributedTestWithTestScope
				.newInfoFromDomain(distributedTest));
		infoSiteStudentsTestMarksStatistics
				.setExecutionCourse((InfoExecutionCourse) infoSiteStudentsTestMarksStatistics
						.getInfoDistributedTest().getInfoTestScope().getInfoObject());

		SiteView siteView = new ExecutionCourseSiteView(infoSiteStudentsTestMarksStatistics,
				infoSiteStudentsTestMarksStatistics);
		return siteView;
	}
}