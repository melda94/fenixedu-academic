/*
 * CursoExecucaoOJB.java
 *
 * Created on 2 de Novembro de 2002, 21:17
 */

package ServidorPersistente.OJB;

/**
 *
 * @author  rpfi
 */

import java.util.Iterator;
import java.util.List;

import org.odmg.QueryException;

import Dominio.CursoExecucao;
import Dominio.ICurso;
import Dominio.ICursoExecucao;
import Dominio.IExecutionYear;
import Dominio.IPlanoCurricularCurso;
import Dominio.ITurma;
import ServidorPersistente.ExcepcaoPersistencia;
import ServidorPersistente.ICursoExecucaoPersistente;

public class CursoExecucaoOJB
	extends ObjectFenixOJB
	implements ICursoExecucaoPersistente {

	public void lockWrite(ICursoExecucao cursoExecucao)
		throws ExcepcaoPersistencia {
		super.lockWrite(cursoExecucao);
	}

	public void delete(ICursoExecucao executionDegree) throws ExcepcaoPersistencia {
		// Delete all Classes associated
		List classes = SuportePersistenteOJB.getInstance().getITurmaPersistente().readByExecutionDegree(executionDegree);
		
		Iterator iterator = classes.iterator();
		while(iterator.hasNext()){
			SuportePersistenteOJB.getInstance().getITurmaPersistente().delete((ITurma) iterator.next());
		}
		super.delete(executionDegree);
	}

	public void deleteAll() throws ExcepcaoPersistencia {
		try {
			String oqlQuery = "select all from " + CursoExecucao.class.getName();
			query.create(oqlQuery);
			List result = (List) query.execute();
			lockRead(result);
			Iterator iterator = result.iterator();
			while(iterator.hasNext()){
				delete((ICursoExecucao) iterator.next());
			}
		} catch (QueryException ex) {
			throw new ExcepcaoPersistencia(ExcepcaoPersistencia.QUERY, ex);
		}
	}

	/**
	 * 
	 * @see ServidorPersistente.ICursoExecucaoPersistente#readBySigla(String)
	 * @deprecated 
	 */
	public ICursoExecucao readBySigla(String sigla)
		throws ExcepcaoPersistencia {
		try {
			ICursoExecucao cursoExecucao = null;
			String oqlQuery =
				"select all from " + CursoExecucao.class.getName();
			oqlQuery += " where curso.sigla = $1";
			query.create(oqlQuery);
			query.bind(sigla);
			List result = (List) query.execute();
			lockRead(result);
			if (result.size() != 0)
				cursoExecucao = (ICursoExecucao) result.get(0);
			return cursoExecucao;
		} catch (QueryException ex) {
			throw new ExcepcaoPersistencia(ExcepcaoPersistencia.QUERY, ex);
		}
	}

	/**
	 * @see ServidorPersistente.ICursoExecucaoPersistente#readByExecutionYear(Dominio.IExecutionYear)
	 */
	public List readByExecutionYear(IExecutionYear executionYear)
		throws ExcepcaoPersistencia {
		try {
			ICursoExecucao cursoExecucao = null;
			String oqlQuery =
				"select all from " + CursoExecucao.class.getName();
			oqlQuery += " where executionYear.year = $1";
			query.create(oqlQuery);
			query.bind(executionYear.getYear());
			List result = (List) query.execute();
			lockRead(result);
			return result;
		} catch (QueryException e) {
			e.printStackTrace(System.out);
			throw new ExcepcaoPersistencia(ExcepcaoPersistencia.QUERY, e);
		}
	}

	/**
	 * @see ServidorPersistente.ICursoExecucaoPersistente#readByDegreeAndExecutionYear(Dominio.ICurso, Dominio.IExecutionYear)
	 */
	public ICursoExecucao readByDegreeCurricularPlanAndExecutionYear(
		IPlanoCurricularCurso degreeCurricularPlan,
		IExecutionYear executionYear)
		throws ExcepcaoPersistencia {
		try {
			ICursoExecucao cursoExecucao = null;
			String oqlQuery =
				"select all from " + CursoExecucao.class.getName();
			oqlQuery += " where executionYear.year = $1"
				+ " and curricularPlan.name = $2 "
				+ " and curricularPlan.curso.sigla = $3";
			query.create(oqlQuery);

			query.bind(executionYear.getYear());
			query.bind(degreeCurricularPlan.getName());
			query.bind(degreeCurricularPlan.getCurso().getSigla());

			List result = (List) query.execute();
			lockRead(result);
			if (result.size() != 0)
				return (ICursoExecucao) result.get(0);
			else
				return null;
		} catch (QueryException e) {
			throw new ExcepcaoPersistencia(ExcepcaoPersistencia.QUERY, e);
		}
	}

	/**
	 * @see ServidorPersistente.ICursoExecucaoPersistente#readByDegreeAndExecutionYear(Dominio.ICurso, Dominio.IExecutionYear)
	 */
	public ICursoExecucao readByDegreeInitialsAndNameDegreeCurricularPlanAndExecutionYear(
		String degreeInitials,
		String nameDegreeCurricularPlan,
		IExecutionYear executionYear)
		throws ExcepcaoPersistencia {
		try {
			ICursoExecucao cursoExecucao = null;
			String oqlQuery =
				"select all from " + CursoExecucao.class.getName();
			oqlQuery += " where executionYear.year = $1"
				+ " and curricularPlan.name = $2 "
				+ " and curricularPlan.curso.sigla = $3";
			query.create(oqlQuery);

			query.bind(executionYear.getYear());
			query.bind(nameDegreeCurricularPlan);
			query.bind(degreeInitials);

			List result = (List) query.execute();
			lockRead(result);
			if (result.size() != 0)
				return (ICursoExecucao) result.get(0);
			else
				return null;
		} catch (QueryException e) {
			throw new ExcepcaoPersistencia(ExcepcaoPersistencia.QUERY, e);
		}
	}

	/**
	 * @see ServidorPersistente.ICursoExecucaoPersistente#readByCursoAndAnoLectivo(Dominio.ICurso, java.lang.String)
	 */
	public ICursoExecucao readByCursoAndAnoLectivo(
		ICurso curso,
		String anoLectivo)
		throws ExcepcaoPersistencia {
		return null;
	}

}
