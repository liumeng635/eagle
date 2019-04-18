package com.eagle.loader;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.dom4j.Element;
/**
 * 配置解析器
 * @author 刘猛
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
	 * 根据标签得到text
	 * @param tag
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getValByConfigTag(String tag){
		Element obj = root.element(tag);
		return (T)obj.getTextTrim();
	}
	/**
	 * 得到属性值
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
