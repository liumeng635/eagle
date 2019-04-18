package com.eagle.analysis;

public interface AnalysisExcutor {
	Object selectForList(final String sqlStatement,final Object paramObj);
	Object selectForList(final String sqlStatement,final Object paramObj,final int skipResults,int maxResults);
	Object selectForObject(final String sqlStatement,final Object paramObj);
	Object insert(final String sqlStatement,final Object paramObj);
	int update(final String sqlStatement,final Object paramObj);
	int delete(final String sqlStatement,final Object paramObj);
}
