package com.eagle.sqlClient.impl;

import com.eagle.common.ConfigFactory;
import com.eagle.exception.EagleException;
import com.eagle.excutor.ExcutorService;
import com.eagle.sqlClient.SqlMapClient;

/**
 * ³Ö¾Ã²ã²Ù×÷Àà
 * @author ÁõÃÍ
 *
 */
public class SqlMapClientImpl implements SqlMapClient{
	/*³Ö¾Ã²ã²Ù×÷Ö´ÐÐÆ÷*/
	private ExcutorService excutor = ConfigFactory.getExcutor();
	private static SqlMapClientImpl sqlClient = null;
	private SqlMapClientImpl(){}
	
	public static SqlMapClientImpl getInstance(){
		if(sqlClient == null){
			synchronized (SqlMapClientImpl.class) {
				SqlMapClientImpl exc = sqlClient;
				if(exc == null){
					exc = new SqlMapClientImpl();
					sqlClient = exc;
				}
			}
		}
		return sqlClient;
	}

	@Override
	public Object selectForList(String sqlStatement, Object paramObj)
			throws EagleException {
		return excutor.selectForList(sqlStatement,paramObj);
	}

	@Override
	public Object selectForList(String sqlStatement, Object paramObj,
			int skipResults, int maxResults) throws EagleException {
		return excutor.selectForList(sqlStatement, paramObj,skipResults, maxResults);
	}

	@Override
	public Object selectForObject(String sqlStatement, Object paramObj)
			throws EagleException {
		return excutor.selectForObject(sqlStatement, paramObj);
	}

	@Override
	public Object insert(String sqlStatement, Object paramObj)
			throws EagleException {
		return excutor.insert(sqlStatement, paramObj);
	}

	@Override
	public int update(String sqlStatement, Object paramObj)
			throws EagleException {
		return excutor.update(sqlStatement, paramObj);
	}

	@Override
	public int delete(String sqlStatement, Object paramObj)
			throws EagleException {
		return excutor.delete(sqlStatement, paramObj);
	}

}
