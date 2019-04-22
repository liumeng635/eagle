package com.eagle.utils;

import org.apache.log4j.Logger;

public class LogUtil {
	private static Logger log = Logger.getLogger(LogUtil.class);
	
	private static LogUtil instance = null;
	
	private LogUtil(){}
	
	public static LogUtil getInstance(){
		if(instance == null){
			synchronized (LogUtil.class) {
				LogUtil temp = instance;
				if(temp == null){
					temp = new LogUtil();
					instance = temp;
				}
			}
		}
		return instance;
	}
	
	public void logInfo(String msg){
		log.info(msg);
	}
	public void logInfo(String msg,Throwable t){
		log.info(msg,t);
	}
	public void logInfo(Throwable t){
		log.info(t);
	}
	public void logWarn(String msg){
		log.warn(msg);
	}
	public void logWarn(String msg,Throwable t){
		log.warn(msg,t);
	}
	public void logWarn(Throwable t){
		log.warn(t);
	}
	public void logFatal(String msg){
		log.fatal(msg);
	}
	public void logFatal(String msg,Throwable t){
		log.fatal(msg,t);
	}
	public void logFatal(Throwable t){
		log.fatal(t);
	}
	public void logError(String msg){
		log.error(msg);
	}
	public void logError(String msg,Throwable t){
		t.printStackTrace();
		log.error(msg,t);
	}
	public void logError(Throwable t){
		t.printStackTrace();
		log.error(t);
	}
}
