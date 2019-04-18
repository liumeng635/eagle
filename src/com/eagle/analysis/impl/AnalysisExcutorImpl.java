package com.eagle.analysis.impl;

import java.util.List;

import com.eagle.analysis.AnalysisExcutor;
import com.eagle.analysis.AnalysisUtil;
import com.eagle.exception.EagleException;
import com.eagle.utils.LogUtil;
/**
 * AnalysisExcutorImpl¿‡
 * @author ¡ı√Õ
 *
 */
public class AnalysisExcutorImpl implements AnalysisExcutor{
	private static LogUtil log = LogUtil.getInstance();
	AnalysisUtil au = new AnalysisUtilImpl();
	private static AnalysisExcutorImpl excutor = null;
	private AnalysisExcutorImpl(){}
	public static AnalysisExcutorImpl getInstance(){
		if(excutor == null){
			synchronized (AnalysisExcutorImpl.class) {
				AnalysisExcutorImpl exc = excutor;
				if(exc == null){
					exc = new AnalysisExcutorImpl();
					excutor = exc;
				}
			}
		}
		return excutor;
	}

	@Override
	public Object selectForList(String sqlStatement, Object paramObj) {
		return au.getDatas(sqlStatement, paramObj);
	}

	@Override
	public Object selectForList(String sqlStatement, Object paramObj,
			int skipResults, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object selectForObject(String sqlStatement, Object paramObj){
		List<?> list = (List<?>)au.getDatas(sqlStatement, paramObj);
		if(list.size()>1){
			try {
				log.logInfo("query for single result,but returns too many results!");
				throw new EagleException("query for single result,but returns too many results!");
			} catch (EagleException e) {}
		}
		return list.get(0);
	}

	@Override
	public Object insert(String sqlStatement, Object paramObj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(String sqlStatement, Object paramObj) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int delete(String sqlStatement, Object paramObj) {
		// TODO Auto-generated method stub
		return 0;
	}

}
