package com.eagle.exception;


public class EagleException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	public EagleException(String msg){super(msg);}
	public EagleException(String msg,Throwable e){super(msg,e);}
	public EagleException(Throwable e){super(e);}

}
