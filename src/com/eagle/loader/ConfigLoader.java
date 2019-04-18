package com.eagle.loader;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.dom4j.Element;
/**
 * ���ý�����
 * @author ����
 *
 */
public class ConfigLoader extends XmlLoader{
	public Element root = doc.getRootElement();
	
	public ConfigLoader(File file){
		super(file);
	}
	public ConfigLoader(String fileName){
		super(fileName);
	}
	public ConfigLoader(InputStream in){
		super(in);
	}
	/**
	 * ���ݱ�ǩ�õ�text
	 * @param tag
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValByConfigTag(String tag){
		Element obj = root.element(tag);
		return (T)obj.getTextTrim();
	}
	/**
	 * �õ�����ֵ
	 * @param tag
	 * @param attribute
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAttributeVal(String tag,String attribute){
		Element obj = root.element(tag);
		return (T)obj.attributeValue(attribute);
	}
	
	@SuppressWarnings("unchecked")
	public List<Element> getRootElements(Element root,final String parentTag,final String childTag){
		Element obj = root.element(parentTag);
		List<Element> roots = obj.elements(childTag);
		return roots;
	}
	

}
