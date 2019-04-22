package com.eagle.common;

import com.eagle.analysis.AnalysisExcutor;
import com.eagle.analysis.impl.AnalysisExcutorImpl;
import com.eagle.excutor.ExcutorService;
import com.eagle.excutor.impl.ExcutorServiceImpl;
import com.eagle.loader.ConfigLoader;
import com.eagle.sqlclient.SqlMapClient;
import com.eagle.sqlclient.impl.SqlMapClientImpl;

public class ConfigFactory {
	public static String resource = "/eagle.xml";
	private static ConfigLoader sqlConfigLoader = new ConfigLoader(ConfigFactory.class.getResourceAsStream(resource));
	private ConfigFactory(){}
	
	public static ConfigLoader getSqlConfigLoader(){
		return sqlConfigLoader == null ? new ConfigLoader(ConfigFactory.class.getResourceAsStream(resource)) : sqlConfigLoader;
	}
	
	public static ConfigLoader getConfigLoader(String resource){
		return new ConfigLoader(ConfigFactory.class.getResourceAsStream(resource));
	}
	
	public static ExcutorService getExcutor(){
		return ExcutorServiceImpl.getInstance();
	}
	public static AnalysisExcutor getAnalysisExcutor(){
		return AnalysisExcutorImpl.getInstance();
	}
	public static SqlMapClient getSqlMapClient(){
		return SqlMapClientImpl.getInstance();
	}
	
}
