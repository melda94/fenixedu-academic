package middleware.studentMigration;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import middleware.middlewareDomain.MWAluno;
import middleware.middlewareDomain.MWBranch;
import middleware.middlewareDomain.MWDegreeTranslation;
import middleware.middlewareDomain.MwEnrolment;
import middleware.persistentMiddlewareSupport.IPersistentMWAluno;
import middleware.persistentMiddlewareSupport.IPersistentMWBranch;
import middleware.persistentMiddlewareSupport.IPersistentMWDegreeTranslation;
import middleware.persistentMiddlewareSupport.IPersistentMWEnrolment;
import middleware.persistentMiddlewareSupport.IPersistentMiddlewareSupport;
import middleware.persistentMiddlewareSupport.OJBDatabaseSupport.PersistentMiddlewareSupportOJB;
import middleware.persistentMiddlewareSupport.exceptions.PersistentMiddlewareSupportException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import Dominio.Enrolment;
import Dominio.EnrolmentEvaluation;
import Dominio.IBranch;
import Dominio.ICurricularCourse;
import Dominio.ICurricularCourseScope;
import Dominio.ICursoExecucao;
import Dominio.IDegreeCurricularPlan;
import Dominio.IDisciplinaExecucao;
import Dominio.IEnrolment;
import Dominio.IEnrolmentEvaluation;
import Dominio.IExecutionPeriod;
import Dominio.IFrequenta;
import Dominio.IStudent;
import Dominio.IStudentCurricularPlan;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.IFrequentaPersistente;
import ServidorPersistente.IPersistentCurricularCourse;
import ServidorPersistente.IPersistentCurricularCourseScope;
import ServidorPersistente.IPersistentStudent;
import ServidorPersistente.ISuportePersistente;
import ServidorPersistente.OJB.SuportePersistenteOJB;
import Util.EnrolmentEvaluationState;
import Util.EnrolmentEvaluationType;
import Util.EnrolmentState;
import Util.TipoCurso;

/**
 * @author Nuno Nunes (nmsn@rnl.ist.utl.pt)
 */

public class UpdateStudentEnrolments
{

	private static IExecutionPeriod executionPeriod = null;
	private static int enrolmentNotWritten = 0;
	private static int enrolmentWritten = 0;
	private static int curricularCoursesNotFound = 0;
	private static int curricularCourseScopesNotFound = 0;
	private static int attendsNotFound = 0;
	private static int attendsUpdated = 0;

	public static void main(String args[]) throws Exception
	{
		IPersistentMiddlewareSupport mws = PersistentMiddlewareSupportOJB.getInstance();
		IPersistentMWAluno persistentAluno = mws.getIPersistentMWAluno();
		IPersistentMWEnrolment persistentEnrolment = mws.getIPersistentMWEnrolment();
		SuportePersistenteOJB sp = SuportePersistenteOJB.getInstance();

		System.out.println("Reading Students ....");
		sp.iniciarTransaccao();
		executionPeriod = sp.getIPersistentExecutionPeriod().readActualExecutionPeriod();

		List result = persistentAluno.readAll();
		sp.confirmarTransaccao();
		System.out.println("Updating " + result.size() + " student Curriculums ...");

		Iterator iterator = result.iterator();
		while (iterator.hasNext())
		{
			MWAluno oldStudent = (MWAluno) iterator.next();
			try
			{
				sp.iniciarTransaccao();
				// Read The middleware Enrolments
				oldStudent.setEnrolments(persistentEnrolment.readByStudentNumber(oldStudent.getNumber()));
				UpdateStudentEnrolments.updateStudentEnrolment(oldStudent, sp);
				sp.confirmarTransaccao();
			} catch (Exception e)
			{}
		}

		ReportEnrolment.report(new PrintWriter(System.out, true));
	}

	public static void updateStudentEnrolment(MWAluno oldStudent, SuportePersistenteOJB sp) throws Exception
	{
		try
		{
			IPersistentStudent persistentStudent = sp.getIPersistentStudent();

			// Read Fenix Student
			IStudent student = persistentStudent.readByNumero(oldStudent.getNumber(), TipoCurso.LICENCIATURA_OBJ);

			if (student == null)
			{
				System.out.println("Error Reading Fenix Student! Student Number [" + oldStudent.getNumber() + "]");
				return;
			}

			IStudentCurricularPlan studentCurricularPlan = sp.getIStudentCurricularPlanPersistente().readActiveByStudentNumberAndDegreeType(student.getNumber(), TipoCurso.LICENCIATURA_OBJ);

			if (studentCurricularPlan == null)
			{
				System.out.println("Error Reading Student Curricular Plan! Student Number [" + oldStudent.getNumber() + "]");
				return;
			}

			List studentEnrolments = sp.getIPersistentEnrolment().readAllByStudentCurricularPlan(studentCurricularPlan);

			// Find the Enrolments That No Longer Exist
			List enrolments2Annul = getEnrolments2Annul(oldStudent, studentEnrolments, oldStudent.getEnrolments(), sp);

			// Find The New Enrolments
			List enrolments2Write = getEnrolments2Write(studentEnrolments, oldStudent.getEnrolments(), studentCurricularPlan, sp);

			// Annul the Enrolments
			annulEnrolments(enrolments2Annul, sp);

			// Create The New Enrolments
			writeEnrolments(enrolments2Write, studentCurricularPlan, oldStudent, sp);

		} catch (Exception e)
		{
			System.out.println("Error Migrating Student " + oldStudent.getNumber() + " enrolments");
			System.out.println("Aluno " + oldStudent.getNumber());
			System.out.println("Degree " + oldStudent.getDegreecode());
			System.out.println("Branch " + oldStudent.getBranchcode());
			e.printStackTrace();
			throw new Exception(e);
		}

	}

	/**
	 * @param enrolments2Write
	 * @param sp
	 */
	private static void writeEnrolments(
		List enrolments2Write,
		IStudentCurricularPlan studentCurricularPlan,
		MWAluno oldStudent,
		SuportePersistenteOJB sp)
		throws Exception
	{
		Iterator iterator = enrolments2Write.iterator();
		while (iterator.hasNext())
		{
			final MwEnrolment mwEnrolment = (MwEnrolment) iterator.next();

			// Get the Degree Of the Student
			IDegreeCurricularPlan degreeCurricularPlan = getDegreeCurricularPlan(mwEnrolment.getDegreecode(), studentCurricularPlan, sp);

			if (degreeCurricularPlan == null)
			{
				System.out.println("Error ! Degree Curricular Plan Not Found !");
				throw new Exception();
			}

			// Get The Branch (This could be the student branch or the curricular course branch
			IBranch branch = getBranch(mwEnrolment.getDegreecode(), mwEnrolment.getBranchcode(), degreeCurricularPlan, sp);

			if (branch == null)
			{
				System.out.println("Error ! Branch Not Found !");
				throw new Exception();
			}

			// Get the Curricular Course 
			List curricularCourses =
				sp.getIPersistentCurricularCourse().readbyCourseCodeAndDegreeCurricularPlan(
					StringUtils.trim(mwEnrolment.getCoursecode()),
					degreeCurricularPlan);
			ICurricularCourse curricularCourse = null;

			if (curricularCourses.size() != 1)
			{
				// if the result size is greater than 1 then check if the Branch Code match in Any
				if (curricularCourses.size() > 1)
				{
					System.out.println("Several Curricular Courses with Code found " + mwEnrolment.getCoursecode() + " found for Degree " + mwEnrolment.getDegreecode());
					curricularCoursesNotFound++;
					enrolmentNotWritten++;
					return;
				} else
				{ // size == 0
					// Try to read by CourseCode Only (this will assume that all the Degree Curricular Plan name ends with "2003/2004"
					IPersistentCurricularCourse curricularCourseDAO = sp.getIPersistentCurricularCourse();
					curricularCourses = curricularCourseDAO.readbyCourseCode(StringUtils.trim(mwEnrolment.getCoursecode()));

					if (curricularCourses.size() == 1)
					{
						curricularCourse = (ICurricularCourse) curricularCourses.get(0);
					} else if (curricularCourses.size() > 1)
					{
						if (hasDiferentDegrees(curricularCourses))
						{
							curricularCourse = getCurricularCourseFromAnotherDegree(mwEnrolment, sp);
							if (curricularCourse == null)
							{
								return;
							}
						} else
						{
							curricularCourse = (ICurricularCourse) curricularCourses.get(0);
						}
					} else
					{
						ReportEnrolment.addCurricularCourseNotFound(mwEnrolment.getCoursecode(),
							mwEnrolment.getDegreecode().toString(),
							mwEnrolment.getNumber().toString());
						return;
					}
				}
			} else // curricularCourses.size() == 1
			{
				curricularCourse = (ICurricularCourse) curricularCourses.get(0);
			}

			// Get the Curricular Course Scope
			ICurricularCourseScope curricularCourseScope =
				getCurricularCourseScopeToEnrollIn(studentCurricularPlan, mwEnrolment, curricularCourse, branch, sp);

			if (curricularCourseScope == null)
			{
				return;
			}

			IEnrolment enrolment = createEnrolment(studentCurricularPlan, sp, curricularCourseScope);

			// Update the Corresponding Attend if it exists
			IFrequenta attend = updateAttend(curricularCourse, executionPeriod, enrolment, mwEnrolment, sp);
			if (attend == null)
			{
//				System.out.println("Student " + mwEnrolment.getNumber() + " has no Attend for " + mwEnrolment.getCoursecode() + " from Degree " + mwEnrolment.getDegreecode());
				attendsNotFound++;
				return;

			}
		}
	}

	/**
	 * @param curricularCourse
	 * @param executionPeriod2
	 * @param sp
	 * @return
	 */
	private static IFrequenta updateAttend(
		ICurricularCourse curricularCourse,
		IExecutionPeriod executionPeriod2,
		IEnrolment enrolment,
		MwEnrolment mwEnrolment,
		SuportePersistenteOJB sp)
		throws ExcepcaoPersistencia
	{
		IDisciplinaExecucao executionCourse =
			sp.getIDisciplinaExecucaoPersistente().readbyCurricularCourseAndExecutionPeriod(curricularCourse, executionPeriod);
		IFrequenta attend = null;
		if (executionCourse == null)
		{
			ReportEnrolment.addExecutionCourseNotFound(
				mwEnrolment.getCoursecode(),
				mwEnrolment.getDegreecode().toString(),
				mwEnrolment.getNumber().toString());
		} else
		{
			IStudent student = sp.getIPersistentStudent().readByNumero(mwEnrolment.getNumber(), TipoCurso.LICENCIATURA_OBJ);
			attend = sp.getIFrequentaPersistente().readByAlunoAndDisciplinaExecucao(student, executionCourse);

			if (attend != null)
			{
				sp.getIFrequentaPersistente().simpleLockWrite(attend);
				attend.setEnrolment(enrolment);
				attendsUpdated++;
			}

		}
		return attend;
	}

	private static IEnrolment createEnrolment(
		IStudentCurricularPlan studentCurricularPlan,
		SuportePersistenteOJB sp,
		ICurricularCourseScope curricularCourseScope)
		throws ExcepcaoPersistencia
	{
		// Create the Enrolment
		IEnrolment enrolment = new Enrolment();
		sp.getIPersistentEnrolment().simpleLockWrite(enrolment);
		enrolment.setCurricularCourseScope(curricularCourseScope);
		enrolment.setEnrolmentEvaluationType(EnrolmentEvaluationType.NORMAL_OBJ);
		enrolment.setEnrolmentState(EnrolmentState.ENROLED);
		enrolment.setExecutionPeriod(executionPeriod);
		enrolment.setStudentCurricularPlan(studentCurricularPlan);

		// Create The Enrolment Evaluation
		IEnrolmentEvaluation enrolmentEvaluation = new EnrolmentEvaluation();
		sp.getIPersistentEnrolmentEvaluation().simpleLockWrite(enrolmentEvaluation);

		enrolmentEvaluation.setCheckSum(null);
		enrolmentEvaluation.setEmployee(null);
		enrolmentEvaluation.setEnrolment(enrolment);
		enrolmentEvaluation.setEnrolmentEvaluationState(EnrolmentEvaluationState.TEMPORARY_OBJ);
		enrolmentEvaluation.setEnrolmentEvaluationType(EnrolmentEvaluationType.NORMAL_OBJ);
		enrolmentEvaluation.setExamDate(null);
		enrolmentEvaluation.setGrade(null);
		enrolmentEvaluation.setGradeAvailableDate(null);
		enrolmentEvaluation.setObservation(null);
		enrolmentEvaluation.setPersonResponsibleForGrade(null);
		enrolmentEvaluation.setWhen(null);

		enrolmentWritten++;
		return enrolment;
	}

	/**
	 * @param mwEnrolment
	 * @param sp
	 * @return
	 */
	private static ICurricularCourse getCurricularCourseFromAnotherDegree(final MwEnrolment mwEnrolment, SuportePersistenteOJB sp)
		throws ExcepcaoPersistencia
	{
		IFrequentaPersistente attendDAO = sp.getIFrequentaPersistente();
		ICurricularCourse curricularCourse = null;
		List attendList = attendDAO.readByStudentNumberInCurrentExecutionPeriod(mwEnrolment.getNumber());
		List attendsWithCurricularCourseCode = (List) CollectionUtils.select(attendList, new Predicate()
		{
			public boolean evaluate(Object input)
			{
				IFrequenta attend = (IFrequenta) input;

				String courseCode = mwEnrolment.getCoursecode();
				
				List associatedCurricularCourses = attend.getDisciplinaExecucao().getAssociatedCurricularCourses();
				Iterator iterator = associatedCurricularCourses.iterator();
				while (iterator.hasNext())
				{
					ICurricularCourse curricularCourse = (ICurricularCourse) iterator.next();
					if (curricularCourse.getCode().equals(courseCode))
					{
						return true;
					}
				}
				return false;
			}
		});

		if (attendsWithCurricularCourseCode.size() > 0)
		{
			List associatedCurricularCourses =
				((IFrequenta) attendsWithCurricularCourseCode.get(0)).getDisciplinaExecucao().getAssociatedCurricularCourses();

			curricularCourse = (ICurricularCourse) associatedCurricularCourses.get(0);
		} else
		{
			ReportEnrolment.addFoundCurricularCourseInOtherDegrees(
				mwEnrolment.getCoursecode(),
				mwEnrolment.getDegreecode().toString(),
				mwEnrolment.getNumber().toString());
		}
		return curricularCourse;
	}

	/**
	 * @param mwEnrolment
	 * @param curricularCourse
	 * @param sp
	 * @return
	 */
	private static ICurricularCourseScope getCurricularCourseScopeToEnrollIn(
		IStudentCurricularPlan studentCurricularPlan,
		MwEnrolment mwEnrolment,
		ICurricularCourse curricularCourse,
		IBranch branch,
		SuportePersistenteOJB sp)
		throws ExcepcaoPersistencia
	{
		boolean sameDegree = true;
		IPersistentCurricularCourseScope curricularCourseScopeDAO = sp.getIPersistentCurricularCourseScope();

		List curricularCourseScopes = null;

		if (!curricularCourse.getDegreeCurricularPlan().equals(studentCurricularPlan.getDegreeCurricularPlan()))
		{
			curricularCourseScopes = curricularCourseScopeDAO.readByCurricularCourse(curricularCourse);	
			return (ICurricularCourseScope) curricularCourseScopes.get(0);
		}

		curricularCourseScopes =
			curricularCourseScopeDAO.readByCurricularCourseAndYearAndSemester(
				curricularCourse,
				mwEnrolment.getCurricularcourseyear(),
				mwEnrolment.getCurricularcoursesemester());
		ICurricularCourseScope curricularCourseScope = null;

		if ((curricularCourseScopes == null) || (curricularCourseScopes.isEmpty()))
		{

			// Try to read by Course and Year only
			curricularCourseScopes = curricularCourseScopeDAO.readByCurricularCourseAndYear(curricularCourse, mwEnrolment.getCurricularcourseyear());

			if ((curricularCourseScopes == null) || (curricularCourseScopes.isEmpty()))
			{
				// Se a disciplina da inscricao nao e do curso do aluno e nao conseguimos encontrar: 
				//  - por ano e semestre
				//  - por ano
				// Entao vamods tentar ler so por semestre que aparece na inscricao e se exisir apenas 1
				// e a que procuramos 
				if (!sameDegree)
				{
					curricularCourseScopes =
						curricularCourseScopeDAO.readByCurricularCourseAndSemester(curricularCourse, mwEnrolment.getCurricularcoursesemester());
				}

				if ((curricularCourseScopes != null) && (curricularCourseScopes.size() == 1))
				{
					curricularCourseScope = (ICurricularCourseScope) curricularCourseScopes.get(0);
				} else
				{
					ReportEnrolment.addCurricularCourseScopeNotFound(mwEnrolment.getCoursecode(),
						mwEnrolment.getDegreecode().toString(),
						mwEnrolment.getNumber().toString(),
						mwEnrolment.getCurricularcourseyear().toString(),
						mwEnrolment.getCurricularcoursesemester().toString(),
						mwEnrolment.getBranchcode().toString());
					return null;
				}
			} else
			{
				// See if a Scope exists for this branch
				curricularCourseScope = findScopeForBranch(curricularCourseScopes, branch);

				if (curricularCourseScope == null)
				{
					curricularCourseScope = findScopeForBranch(curricularCourseScopes, studentCurricularPlan.getBranch());
				}

				// If there's no Scope, try to find the "empty" scope
				if (curricularCourseScope == null)
				{
					curricularCourseScope = findScopeForBranch(curricularCourseScopes, null);
				}

				// if we can't find a scope and the degree of the course is diferent from the student's, them we ignore the branch
				if ((curricularCourseScope == null)
					&& (!studentCurricularPlan.getDegreeCurricularPlan().getDegree().equals(curricularCourse.getDegreeCurricularPlan().getDegree())))
				{
					curricularCourseScope = (ICurricularCourseScope) curricularCourseScopes.get(0);
				}

				// If still we cannot find a scope ...
				if (curricularCourseScope == null)
				{
					System.out.println("Error Reading Curricular Course Scope after finding scopes by Curricular Course and Year!! ");
					System.out.println("Numero do Aluno " + mwEnrolment.getNumber());
					System.out.println("Codigo Curso " + mwEnrolment.getDegreecode());
					System.out.println("Codigo Disciplina " + mwEnrolment.getCoursecode());
					System.out.println("Codigo Ramo " + mwEnrolment.getBranchcode());
					System.out.println("Ano Curricular " + mwEnrolment.getCurricularcourseyear());
					System.out.println("Semestre Disciplina " + mwEnrolment.getCurricularcoursesemester());
					System.out.println("Curricular Course ID " + curricularCourse.getIdInternal());

					System.out.println("---------");

					curricularCourseScopesNotFound++;
					enrolmentNotWritten++;

					return null;
				}

			}

		} else
		{

			if (curricularCourseScopes.size() == 1)
			{
				curricularCourseScope = (ICurricularCourseScope) curricularCourseScopes.get(0);
			}

			// There is a list of Scopes for This Curricular Course, year and semester
			// See if a Scope exists for this branch
			if (curricularCourseScope == null)
			{
				curricularCourseScope = findScopeForBranch(curricularCourseScopes, branch);
			}

			if (curricularCourseScope == null)
			{
				curricularCourseScope = findScopeForBranch(curricularCourseScopes, studentCurricularPlan.getBranch());
			}

			// If there's no Scope, try to find the "empty" scope
			if (curricularCourseScope == null)
			{
				curricularCourseScope = findScopeForBranch(curricularCourseScopes, null);
			}

			// if we can't find a scope and the degree of the course is diferent from the student's, them we ignore the branch
			if ((curricularCourseScope == null)
				&& (!studentCurricularPlan.getDegreeCurricularPlan().getDegree().equals(curricularCourse.getDegreeCurricularPlan().getDegree())))
			{
				curricularCourseScope = (ICurricularCourseScope) curricularCourseScopes.get(0);
			}

			// If still we cannot find a scope ...
			if (curricularCourseScope == null)
			{
				System.out.println("Error Reading Curricular Course Scope after finding scopes by Curricular Course, Year and Semester!! ");
				System.out.println("Numero do Aluno " + mwEnrolment.getNumber());
				System.out.println("Codigo Curso " + mwEnrolment.getDegreecode());
				System.out.println("Codigo Disciplina " + mwEnrolment.getCoursecode());
				System.out.println("Codigo Ramo " + mwEnrolment.getBranchcode());
				System.out.println("Ano Curricular " + mwEnrolment.getCurricularcourseyear());
				System.out.println("Semestre Disciplina " + mwEnrolment.getCurricularcoursesemester());
				System.out.println("Curricular Course ID " + curricularCourse.getIdInternal());
				System.out.println("---------");

				curricularCourseScopesNotFound++;
				enrolmentNotWritten++;

				return null;
			}
		}

		return null;
	}

	/**
	 * @param curricularCourses
	 * @return
	 */
	private static boolean hasDiferentDegrees(List curricularCourses)
	{
		int numberOfDegrees = CollectionUtils.getCardinalityMap(curricularCourses).size();
		return (numberOfDegrees > 1);
	}

	/**
	 * @param curricularCourseScopes
	 * @param branch
	 * @return
	 */
	private static ICurricularCourseScope findScopeForBranch(List curricularCourseScopes, IBranch branch)
	{
		Iterator iterator = curricularCourseScopes.iterator();
		while (iterator.hasNext())
		{
			ICurricularCourseScope curricularCourseScope = (ICurricularCourseScope) iterator.next();

			if (branch == null)
			{
				if (((curricularCourseScope.getBranch().getCode().equals("")) && (curricularCourseScope.getBranch().getName().equals("")))
					|| (curricularCourseScope.getBranch().getName().startsWith("CURSO DE")))
				{
					return curricularCourseScope;
				}
			} else
			{
				if (curricularCourseScope.getBranch().equals(branch))
				{
					return curricularCourseScope;
				}
			}
		}
		return null;
	}

	/**
	 * @param studentEnrolments
	 * @param list
	 * @return
	 */
	private static List getEnrolments2Write(
		List studentEnrolments,
		List oldEnrolments,
		IStudentCurricularPlan studentCurricularPlan,
		SuportePersistenteOJB sp)
		throws Exception
	{
		List result = new ArrayList();

		Iterator oldEnrolmentIterator = oldEnrolments.iterator();

		while (oldEnrolmentIterator.hasNext())
		{
			MwEnrolment mwEnrolment = (MwEnrolment) oldEnrolmentIterator.next();

			// Get the Degree Of the Curricular Course
			IDegreeCurricularPlan degreeCurricularPlan = getDegreeCurricularPlan(mwEnrolment.getDegreecode(), studentCurricularPlan, sp);

			if (degreeCurricularPlan == null)
			{
				System.out.println("Error ! Degree Curricular Plan Not Found !");
				throw new Exception();
			}

			// Get The Branch for This Curricular Course
			IBranch branch = getBranch(mwEnrolment.getDegreecode(), mwEnrolment.getBranchcode(), degreeCurricularPlan, sp);

			if (branch == null)
			{
				System.out.println("Error ! Branch Not Found !");
				throw new Exception();
			}

			// Check if The Enrolment exists
			if (!enrolmentExistsOnFenix(mwEnrolment, degreeCurricularPlan, branch, studentEnrolments, sp))
			{
				result.add(mwEnrolment);
			}

		}
		return result;
	}

	/**
	 * @param mwEnrolment
	 * @param degreeCurricularPlan
	 * @param branch
	 * @param studentEnrolments
	 * @param sp
	 * @return
	 */
	private static boolean enrolmentExistsOnFenix(
		MwEnrolment mwEnrolment,
		IDegreeCurricularPlan degreeCurricularPlan,
		IBranch branch,
		List studentEnrolments,
		SuportePersistenteOJB sp)
	{

		Iterator iterator = studentEnrolments.iterator();
		while (iterator.hasNext())
		{
			IEnrolment enrolment = (IEnrolment) iterator.next();
			if (
			 (enrolment
				.getCurricularCourseScope()
				.getCurricularCourse()
				.getCode()
				.equalsIgnoreCase(
					StringUtils.trim(
						mwEnrolment
							.getCoursecode()))
				&& (enrolment.getExecutionPeriod().equals(executionPeriod))))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param enrolments2Annul
	 * @param sp
	 */
	private static void annulEnrolments(List enrolments2Annul, SuportePersistenteOJB sp) throws ExcepcaoPersistencia
	{

		Iterator iterator = enrolments2Annul.iterator();
		while (iterator.hasNext())
		{
			IEnrolment enrolment = (IEnrolment) iterator.next();

			// Find the Attend
			IDisciplinaExecucao executionCourse =
				sp.getIDisciplinaExecucaoPersistente().readbyCurricularCourseAndExecutionPeriod(
					enrolment.getCurricularCourseScope().getCurricularCourse(),
					executionPeriod);
			if (executionCourse == null)
			{
				continue;
			}
			IFrequenta attend =
				sp.getIFrequentaPersistente().readByAlunoAndDisciplinaExecucao(enrolment.getStudentCurricularPlan().getStudent(), executionCourse);

			if (attend != null)
			{
				sp.getIFrequentaPersistente().simpleLockWrite(attend);
				attend.setEnrolment(null);
			}

			// Delete EnrolmentEvalutaion
			Iterator evaluations = enrolment.getEvaluations().iterator();
			while (evaluations.hasNext())
			{
				IEnrolmentEvaluation enrolmentEvaluation = (IEnrolmentEvaluation) evaluations.next();
				sp.getIPersistentEnrolmentEvaluation().delete(enrolmentEvaluation);
			}

			// Delete Enrolment
			sp.getIPersistentEnrolment().delete(enrolment);

		}

	}

	/**
	 * @param studentEnrolments
	 * @param list
	 * @return A list with the Curricular Courses to Annul
	 */
	private static List getEnrolments2Annul(MWAluno oldStudent, List studentEnrolments, List oldEnrolments, SuportePersistenteOJB sp)
		throws Exception
	{
		List result = new ArrayList();

		Iterator fenixEnrolments = studentEnrolments.iterator();

		while (fenixEnrolments.hasNext())
		{
			IEnrolment enrolment = (IEnrolment) fenixEnrolments.next();

			// Check if The Enrolment exists on the old System
			if (!enrolmentExistsOnAlmeidaServer(enrolment, oldEnrolments, sp))
			{
				result.add(enrolment);
			}
		}

		return result;
	}

	/**
	 * @param enrolment
	 * @param oldEnrolments
	 * @param sp
	 * @return
	 */
	private static boolean enrolmentExistsOnAlmeidaServer(IEnrolment enrolment, List oldEnrolments, SuportePersistenteOJB sp)
	{
		Iterator iterator = oldEnrolments.iterator();
		while (iterator.hasNext())
		{
			MwEnrolment mwEnrolment = (MwEnrolment) iterator.next();

			// To read an mw_Enrolment we need the student number, the Course Code, the Semester and the enrolment year
			if ((mwEnrolment.getNumber().equals(enrolment.getStudentCurricularPlan().getStudent().getNumber()))
				&& (StringUtils
					.trim(mwEnrolment.getCoursecode())
					.equals(
						enrolment
							.getCurricularCourseScope()
							.getCurricularCourse()
							.getCode()))
				)
			{
				return true;
			}

		}
		return false;
	}

	/**
	 * 
	 * @param degreeCode
	 * @param studentCurricularPlan
	 * @param sp
	 * @return
	 * @throws PersistentMiddlewareSupportException
	 * @throws ExcepcaoPersistencia
	 */
	private static IDegreeCurricularPlan getDegreeCurricularPlan(
		Integer degreeCode,
		IStudentCurricularPlan studentCurricularPlan,
		ISuportePersistente sp)
		throws PersistentMiddlewareSupportException, ExcepcaoPersistencia
	{
		IPersistentMiddlewareSupport mws = PersistentMiddlewareSupportOJB.getInstance();
		IPersistentMWDegreeTranslation persistentMWDegreeTranslation = mws.getIPersistentMWDegreeTranslation();

		MWDegreeTranslation mwDegreeTranslation = persistentMWDegreeTranslation.readByDegreeCode(degreeCode);

		ICursoExecucao executionDegree =
			sp.getICursoExecucaoPersistente().readByDegreeNameAndExecutionYearAndDegreeType(
				mwDegreeTranslation.getDegree().getNome(),
				executionPeriod.getExecutionYear(),
				TipoCurso.LICENCIATURA_OBJ);

		if (executionDegree == null)
		{
			System.out.println("[ERROR] The Degree Has no Execution in " + executionPeriod.getExecutionYear().getYear());
			return null;
		} else
		{
			if (!studentCurricularPlan.getDegreeCurricularPlan().equals(executionDegree.getCurricularPlan()))
			{
				System.out.println("[ERROR] The Student Has Changed his Degree ! " + studentCurricularPlan.getStudent().getNumber());
				return null;
			} else
			{
				return executionDegree.getCurricularPlan();
			}
		}
	}

	private static IBranch getBranch(Integer degreeCode, Integer branchCode, IDegreeCurricularPlan degreeCurricularPlan, ISuportePersistente sp)
		throws PersistentMiddlewareSupportException, ExcepcaoPersistencia
	{
		IBranch branch = null;

		IPersistentMiddlewareSupport mws = PersistentMiddlewareSupportOJB.getInstance();
		IPersistentMWBranch persistentBranch = mws.getIPersistentMWBranch();

		// Get the old BRanch
		sp.clearCache();
		MWBranch mwbranch = persistentBranch.readByDegreeCodeAndBranchCode(degreeCode, branchCode);

		// Get the new one		
		if (mwbranch == null)
		{
			System.out.println("Curso " + degreeCode);
			System.out.println("Ramo " + branchCode);
		}

		branch = sp.getIPersistentBranch().readByDegreeCurricularPlanAndBranchName(degreeCurricularPlan, mwbranch.getDescription());

		if (branch == null)
		{
			branch = sp.getIPersistentBranch().readByDegreeCurricularPlanAndBranchName(degreeCurricularPlan, "");
		}

		if (branch == null)
		{
			System.out.println("DCP " + degreeCurricularPlan.getName());
			System.out.println("Ramo Inexistente " + mwbranch);
		}

		return branch;
	}

}
