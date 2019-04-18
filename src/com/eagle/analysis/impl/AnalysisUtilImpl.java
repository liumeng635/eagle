package com.eagle.analysis.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.eagle.analysis.AnalysisUtil;
import com.eagle.common.ConfigFactory;
import com.eagle.dataSource.DBUtil;
import com.eagle.entity.ColumnInfo;
import com.eagle.entity.ParamInfo;
import com.eagle.exception.EagleException;
import com.eagle.loader.ConfigLoader;
import com.eagle.utils.LogUtil;
/**
 * 解析工具
 * @author 刘猛
 *
 */
public class AnalysisUtilImpl implements AnalysisUtil {
	private final static String SEPERATOR = "\\.";
	private final static String REGEX_CUB = "(?<=\\#\\{)(.+?)(?=\\})";
	private final static String REGEX_DOLR = "(?<=\\$\\{)(.+?)(?=\\})";
	private final static String CUB_START = "#{";
	private final static String DOLR_START = "${";
	private final static String PARAM_END = "}";
	private final static String MAPPERS = "mappers";
	private final static String MAPPER = "mapper";
	private final static String NAMESPACE = "namespace";
	private final static String RESOURCE = "resource";
	private final static String STATEMENT = "statement";
	private final static String RESULTTYPE = "resultType";
	private final static String PARAMTYPE = "paramType";
	private final static String SQL = "sql";
	private final static String IF = "if";
	private final static String TEST = "test";
	private final static String EQ = "==";
	private final static String NE = "!=";
	private final static String AND = "and";
	private final static String OR = "or";
	private static Connection conn = null;
	private static Statement stmt = null;
	private static PreparedStatement pstmt = null;
	private static ResultSet rs = null;
	private static LogUtil log = LogUtil.getInstance();

	static {

		try {
			conn = DBUtil.getDataSource().getConnection();
		} catch (SQLException e) {
			log.logInfo("error get sql connection!", e);
			e.printStackTrace();
		} catch (Exception e) {
			log.logInfo("error get sql connection!", e);
			e.printStackTrace();
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object getResult(Map map,Object param) throws SQLException,
			Exception {
		Object returnResult = null;
		String paramType = (String) map.get(PARAMTYPE);//参数类型
		String resultType = (String) map.get(RESULTTYPE);//结果类型
		List<ParamInfo> cubList = (List<ParamInfo>) map.get("cubList");//sql参数注入信息
		String sql = (String) map.get(SQL);//sql
		List<ColumnInfo> colunmInfos = new ArrayList<ColumnInfo>();//sql值列
		pstmt = conn.prepareStatement(sql);
		log.logInfo("excute sql : " + sql);
		System.out.println("excute sql : " + sql);
		if(cubList != null){
			for(ParamInfo paramInfo : cubList){
				pstmtSet(paramInfo, param, pstmt);
			}
		}
		rs = pstmt.executeQuery();
		ResultSetMetaData data = rs.getMetaData();
		for (int i = 1; i < data.getColumnCount(); i++) {
			ColumnInfo info = new ColumnInfo();
			info.setColumnName(data.getColumnName(i));
			info.setColumnType(data.getColumnTypeName(i));
			colunmInfos.add(info);
		}

		if (isMap(resultType)) {// ====如果返回类型是map=======
			List<Map> rsList = new ArrayList<Map>();
			while (rs.next()) {
				Map mp = new HashMap();
				for (ColumnInfo column : colunmInfos) {
					mp.put(column.getColumnName(),
							rs.getObject(column.getColumnName()));
				}
			}
			returnResult = rsList;
		}else if(isInt(resultType) || isString(resultType)){//如果是int or String孤值返回
			List rsList = new ArrayList();
			Object value = null;
			if(rs.next()){
				 value = rs.getObject(1);
			}
			if(isInt(resultType)){
				 rsList.add(Integer.valueOf(value+""));
			}
			if(isString(resultType)){
				rsList.add(returnResult = (String)value);
			}
			returnResult = rsList;
		}else {// =====如果返回类是Entity====
			List rsList = new ArrayList();
			while (rs.next()) {
				Class<?> clz = Class.forName(resultType);
				Field[] fields = clz.getDeclaredFields();
				Object obj = clz.newInstance();
				for (Field f : fields) {
					f.setAccessible(true);
					String attrName = f.getName();
					for (ColumnInfo column : colunmInfos) {
						String columnName = getColumnName(column
								.getColumnName());
						if (StringUtils.equals(attrName.toLowerCase(),
								columnName)) {
							Object value = rs.getObject(column.getColumnName());
							setProperty(obj, attrName, value);
							break;
						}
					}
				}
				rsList.add(obj);
			}
			returnResult = rsList;
		}
		return returnResult;
	}
	
	/**
	 * sql注入参数
	 * @param paramInfo
	 * @param param
	 * @param pstmt
	 * @throws SQLException 
	 * @throws EagleException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 * @throws InstantiationException 
	 * @throws SecurityException 
	 * @throws ClassNotFoundException 
	 */
	private void pstmtSet(ParamInfo paramInfo,Object param,PreparedStatement pstmt) throws SQLException, IllegalArgumentException, IllegalAccessException, SecurityException, InstantiationException, ClassNotFoundException{
		Class<?> clazz = param.getClass();
		if(isMap(clazz.getName())){
			Map<?,?> map = (Map<?,?>)param;
			pstmt.setObject(paramInfo.getIndex(), map.get(paramInfo.getParamName()));
		}else{
			Field[] fls = clazz.getDeclaredFields();
			if(!existsProperty(paramInfo.getParamName(), fls)){
				throw new EagleException(clazz.getName()+" has no property "+paramInfo.getParamName());
			}
			for(Field fld : fls){
				if(StringUtils.equals(fld.getName(), paramInfo.getParamName())){
					fld.setAccessible(true);
					Object value = fld.get(param);
					pstmt.setObject(paramInfo.getIndex(),value);
					break;
				}
			}
		}
	}

	/**
	 * 解析指定的sqlmapper配置返回sql执行信息
	 * @param sqlStatement
	 * @param obj
	 * @return
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalArgumentException 
	 * @throws EagleException
	 * @throws Exception
	 */
	public Map<?, ?> getSqlStatement(String sqlStatement, Object obj) throws IllegalArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException{
		String sqlMapper = "";
		String sqlId = "";
		if (StringUtils.isEmpty(sqlStatement)) {
			throw new EagleException("sqlStatement不能为空！");
		} else {
			Pattern p = Pattern.compile(SEPERATOR);
			Matcher m = p.matcher(sqlStatement);
			if (!m.find()) {
				throw new EagleException(
						"sqlStatement格式不正确！应为：{mapperNamespace}.{sqlId}");
			} else {
				String[] argArrs = sqlStatement.split(SEPERATOR);
				String namespace = argArrs[0];// sqlmapper命名空间
				sqlId = argArrs[1];// 指定sql的id
				/**
				 * 解析配置文件
				 */
				ConfigLoader loder = ConfigFactory.getSqlConfigLoader();
				List<Element> elements = loder.getRootElements(loder.root,
						MAPPERS, MAPPER);
				for (Element e : elements) {
					try {
						String namespaceConfig = e.attributeValue(NAMESPACE);
						if (StringUtils.equals(namespace, namespaceConfig)) {
							sqlMapper = e.attributeValue(RESOURCE);
							break;
						}
					} catch (Exception e1) {
						throw new EagleException(
								"error parsing xml file of eagle.xml,some errors may exsists in mapper node which namespace is "
										+ namespace, e1);
					}
				}
			}
		}
		return analysisMapper(sqlMapper, sqlId, obj);
	}

	/**
	 * 解析mapper返回sql相关信息
	 * @param resource
	 * @param sqlId
	 * @param obj
	 * @return
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @throws EagleException
	 * @throws IllegalAccessException
	 * @throws InstantiationException 
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	private Map<?,?> analysisMapper(String resource, String sqlId,
			Object obj) throws IllegalArgumentException,
			ClassNotFoundException,IllegalAccessException, InstantiationException {
		String parent = ConfigFactory.resource.substring(0,
				ConfigFactory.resource.lastIndexOf("/") + 1);
		ConfigLoader loder = ConfigFactory.getConfigLoader(parent + resource);
		List<Element> statements = loder.root.elements(STATEMENT);
		Element matchElement = null;
		for (Element e : statements) {
			String idAttr = e.attributeValue("id");
			if (StringUtils.equals(sqlId, idAttr)) {
				matchElement = e;
				break;
			}
		}
		Map map = new HashMap();
		map.put(RESULTTYPE, matchElement.attributeValue(RESULTTYPE));
		map.put(PARAMTYPE, matchElement.attributeValue(PARAMTYPE));
		String prefixSql = matchElement.getText().replace("\n\t", "");
		/**
		 * 动态sql判断条件
		 */
		List<Element> ifs = matchElement.elements(IF);
		for (Element e : ifs) {
			String test = e.attributeValue(TEST);
			// 拼接复合条件的sql =======to do
			prefixSql += "";
		}
		// 根据SQLString中的参数返回处理后的sql
		map.putAll(returnSql(prefixSql, obj,
				matchElement.attributeValue(PARAMTYPE)));
		return map;
	}

	/**
	 * 解析sql并处理条件参数
	 * @param sql
	 * @param obj
	 * @param paramType
	 * @return
	 * @throws ClassNotFoundException
	 * @throws EagleException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map<?, ?> returnSql(String sql, Object obj,
			String paramType) throws ClassNotFoundException,
			IllegalArgumentException, IllegalAccessException, InstantiationException {
		
		Map map = new HashMap();
		
		if(existsParam(sql) && obj == null){
			throw new EagleException("sql含有条件参数！而传入的参数对象却为空！");
		}
		if(existsParam(sql) && obj != null){
			Class<?> paramClazz = obj.getClass();
			Class<?> configClazz = Class.forName(paramType);
			if (paramClazz != configClazz && (!isMap(paramClazz.getName()) && !isMap(paramType))) {
				log.logInfo("传入的参数类型与配置的类型不一致！");
				throw new EagleException("传入的参数类型与配置的类型不一致！");
			} else {
				// ------获取#{}集合------
				List<ParamInfo> cubList = extractVal(sql, REGEX_CUB);
				// 将匹配#{}替换成占位符？
				sql = processSqlStr_Cub(cubList, sql);
				map.put("cubList", cubList);

				// -----获取${}集合------
				List<ParamInfo> dolrList = extractVal(sql, REGEX_DOLR);
				// 将${}群不替换成传入的参数
				sql = processSqlStr_Dolr(dolrList, sql, obj);
			}
		}
		map.put("sql", sql);
		return map;
	}
	
	/**
	 * 将匹配#{}替换成占位符？
	 * @param cubList
	 * @param sql
	 */
	private String processSqlStr_Cub(List<ParamInfo> cubList,String sql){
		for (ParamInfo pf : cubList) {
			sql = sql.replace(CUB_START + pf.getParamName() + PARAM_END,"?");
		}
		return sql;
	}
	/**
	 * 将${}群不替换成传入的参数
	 * @param dolrList
	 * @param sql
	 * @param obj
	 * @throws EagleException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException 
	 * @throws SecurityException 
	 * @throws InstantiationException 
	 */
	private String processSqlStr_Dolr(List<ParamInfo> dolrList,String sql,Object obj) throws IllegalArgumentException, IllegalAccessException, SecurityException, ClassNotFoundException, InstantiationException{
		Class<?> paramClazz = obj.getClass();
		for (ParamInfo pf : dolrList) {
			if (!isMap(obj.getClass().getName())) {// 参数类型不是map的时候
				Field[] fields = paramClazz.getDeclaredFields();
				if (!existsProperty(pf.getParamName(), fields)) {
					throw new EagleException(paramClazz.getName()
							+ " has no property:"
							+ pf.getParamName());
				}
				for (Field f : fields) {
					f.setAccessible(true);
					if (StringUtils.equals(pf.getParamName(), f.getName())) {
						Object value = f.get(obj);
						sql = sql.replace(DOLR_START + pf.getParamName()
								+ PARAM_END, value+"");
					}
				}

			} else {// 参数类型是map
				Map<?, ?> mp = (Map<?, ?>) obj;
				for (Map.Entry<?, ?> entry : mp.entrySet()) {
					if (mp.containsKey(pf.getParamName())) {
						sql = sql.replace(DOLR_START + entry.getKey()
								+ PARAM_END, entry.getValue() + "");
					}
				}
			}

		}
		return sql;
	}

	/**
	 * 属性是否存在
	 * @param property
	 * @param fs
	 * @return
	 */
	private boolean existsProperty(String property, Field[] fs) {
		boolean is = false;
		for (Field f : fs) {
			if (StringUtils.equals(f.getName(), property)) {
				is = true;
				break;
			}
		}
		return is;
	}
	/**
	 * 判断sql是否有入参
	 * @param sql
	 * @return
	 */
	private boolean existsParam(String sql){
		boolean is = false;
		if(sql.indexOf("$")>0){
			is = true;
		}
		if(sql.indexOf("#")>0){
			is = true;
		}
		return is;
	}

	/**
	 * 匹配指定字符串之间的内容
	 * @param sql
	 * @param regex
	 * @return
	 */
	private List<ParamInfo> extractVal(String sql, String regex) {
		List<ParamInfo> list = new ArrayList<ParamInfo>();
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sql);
		int index = 0;
		ParamInfo pinfo = null;
		while (m.find()) {
			pinfo = new ParamInfo();
			pinfo.setParamName(m.group());
			pinfo.setIndex(++index);
			list.add(pinfo);
		}
		return list;
	}
	
	/**
	 * 成员属性赋值
	 * @param obj
	 * @param name
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws EagleException
	 */
	private void setProperty(Object obj, String name, Object value)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Class<?> clz = obj.getClass();
		String methodName = returnSetMethodName(name);
		Method[] ms = clz.getMethods();
		for (Method m : ms) {
			if (StringUtils.equals(m.getName(), methodName)) {
				if (m.getParameterTypes().length == 1) {
					Class<?> clazzParameterType = m.getParameterTypes()[0];
					setFieldValue(clazzParameterType, value, m, obj);
				}
			}
		}
	}

	/**
	 * 根据属性名得到设值方法
	 * @param name
	 * @return
	 */
	private String returnSetMethodName(String name) {
		name = name.replace(name.charAt(0) + "",(name.charAt(0) + "").toUpperCase());
		return "set" + name;
	}

	/**
	 * 为成员变量赋值
	 * @param paramType
	 * @param value
	 * @param m
	 * @param obj
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws EagleException
	 */
	private void setFieldValue(Class<?> paramType, Object value, Method m,
			Object obj) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		if (value != null) {
			if (!StringUtils.isEmpty(value.toString())) {
				if (paramType == Integer.class) {
					value = new Integer(value.toString());
					m.invoke(obj, value);
					return;
				}

				if (paramType == String.class) {
					m.invoke(obj, value.toString());
					return;
				}
				if (paramType == BigDecimal.class) {
					value = new BigDecimal(value.toString());
					m.invoke(obj, value);
					return;
				}
				if (paramType == BigInteger.class) {
					value = new BigInteger(value.toString());
					m.invoke(obj, value);
					return;
				}
				if (paramType == Boolean.class) {
					value = new Boolean(value.toString());
					m.invoke(obj, value);
					return;
				}
				if (paramType == Byte.class) {
					value = new Byte(value.toString());
					m.invoke(obj, value);
					return;
				}
				if (paramType == Character.class) {
					if (value.toString().length() > 1)
						throw new EagleException("字符类型赋值出现错误，原因可能是字段值不正确！");
					else
						value = Character.valueOf(value.toString().charAt(0));
					m.invoke(obj, value);
					return;
				}
				if (paramType == Float.class) {
					value = new Float(value.toString());
					m.invoke(obj, value);
					return;
				}
				if (paramType == Double.class) {
					value = new Double(value.toString());
					m.invoke(obj, value);
					return;
				}
				if (paramType == Long.class) {
					value = new Long(value.toString());
					m.invoke(obj, value);
					return;
				}
				if (paramType == Short.class) {
					value = new Short(value.toString());
					m.invoke(obj, value);
					return;
				}
			}
			if (paramType == Date.class) {
				if (!StringUtils.isEmpty(value.toString())) {
					value = new Date(((java.sql.Date) value).getTime());
					m.invoke(obj, value);
				}
				return;
			}
		}
	}

	public Object getDatas(String namespace, Object obj) {
		Object result = null;
		try {
			result = getResult(getSqlStatement(namespace, obj),obj);
		} catch (SQLException e) {
			log.logInfo("", e);
			e.printStackTrace();
		} catch (EagleException e) {
			log.logInfo("", e);
			e.printStackTrace();
		} catch (Exception e) {
			log.logInfo("", e);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 查询出来的字段处理
	 * @param param
	 * @return
	 */
	private String getColumnName(String param) {
		if (StringUtils.isEmpty(param))return param;
		return param.indexOf("_") > 0 ? param.replaceAll("_", "").toLowerCase(): param.toLowerCase();
	}

	/**
	 * 判断是否是map
	 * @param type
	 * @return
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private boolean isMap(String type) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		boolean is = false;
		if(type.indexOf(".")<0){
			if(type.indexOf("map")>0){
				is = true;
			}
		}else{
			Object obj = Class.forName(type).newInstance();
			if (obj instanceof Map) {
				is = true;
			}
		}
		return is;
	}
	/**
	 * 
	 * @param paramType
	 * @return
	 * @throws ClassNotFoundException
	 */
	private boolean isString(String paramType) throws ClassNotFoundException{
		boolean is = false;
		if(paramType.indexOf(".")<0){
			if(StringUtils.equals("String", paramType)){
				is = true;
			}
		}else{
			Class<?> clazz = Class.forName(paramType);
			if(clazz == String.class){
				is = true;
			}
		}
		return is;
	}
	/**
	 * 
	 * @param paramType
	 * @return
	 * @throws ClassNotFoundException
	 */
	private boolean isInt(String paramType) throws ClassNotFoundException{
		boolean is = false;
		if(paramType.indexOf(".")<0){
			if(StringUtils.equals("int", paramType)){
				is = true;
			}
		}else{
			Class<?> clazz = Class.forName(paramType);
			if(clazz == Integer.class){
				is = true;
			}
		}
		return is;
	}

}
