package com.eagle.sqlClient;

import com.eagle.exception.EagleException;

public interface SqlMapClient {
	Object selectForList(final String sqlStatement,final Object paramObj) throws EagleException;
	Object selectForList(final String sqlStatement,final Object paramObj,final int skipResults,int maxResults) throws EagleException;
	Object selectForObject(final String sqlStatement,final Object paramObj)throws EagleException;
	Object insert(final String sqlStatement,final Object paramObj)throws EagleException;
	int update(final String sqlStatement,final Object paramObj)throws EagleException;
	int delete(final String sqlStatement,final Object paramObj)throws EagleException;
}
