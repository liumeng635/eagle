package com.eagle.loader;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class XmlLoader {
	public static Document doc;
	static Logger logger = Logger.getLogger(XmlLoader.class);
	
	@SuppressWarnings("static-access")
	public XmlLoader(File file){
		doc = read(file);
	}
	@SuppressWarnings("static-access")
	public XmlLoader(InputStream in){
		doc = read(in);
	}
	@SuppressWarnings("static-access")
	public XmlLoader(String fileName){
		doc = read(getInputStream(fileName));
	}
	
	public FileInputStream getInputStream(String fileName){
		FileInputStream in = null;
		try {
			in = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return in;
	}
	public Document read(File file){
		Document doc = null;
		SAXReader reader = new SAXReader();
		try {
			doc = reader.read(file);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
	public Document read(InputStream in){
		Document doc = null;
		SAXReader reader = new SAXReader();
		try {
			doc = reader.read(in);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return doc;
	}
}
