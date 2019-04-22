package com.eagle.excutor.impl;

import com.eagle.analysis.AnalysisExcutor;
import com.eagle.common.ConfigFactory;
import com.eagle.excutor.ExcutorService;

/**
 * @author 刘猛
 */
public class ExcutorServiceImpl implements ExcutorService{
	AnalysisExcutor excutor = ConfigFactory.getAnalysisExcutor();
	private static ExcutorServiceImpl excutorService = null;
	private ExcutorServiceImpl(){}
	public static ExcutorServiceImpl getInstance(){
		if(excutorService == null){
			synchronized (ExcutorServiceImpl.class) {
				ExcutorServiceImpl exc = excutorService;
				if(exc == null){
					exc = new ExcutorServiceImpl();
					excutorService = exc;
				}
			}
		}
		return excutorService;
	}
	@Override
	public Object selectForList(String sqlStatement, Object paramObj) {
		return excutor.selectForList(sqlStatement,paramObj);
	}

	@Override
	public Object selectForList(String sqlStatement, Object paramObj,
			int skipResults, int maxResults) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object selectForObject(String sqlStatement, Object paramObj) {
		return excutor.selectForObject(sqlStatement,paramObj);
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
