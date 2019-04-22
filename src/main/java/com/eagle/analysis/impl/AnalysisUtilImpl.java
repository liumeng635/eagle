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
 * sql解析器
 * @author liumeng
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

	private Object getResult(Map map,Object param) throws SQLException,
			Exception {
		Object returnResult = null;
		String paramType = (String) map.get(PARAMTYPE);
		String resultType = (String) map.get(RESULTTYPE);
		List<ParamInfo> cubList = (List<ParamInfo>) map.get("cubList");
		String sql = (String) map.get(SQL);
		List<ColumnInfo> colunmInfos = new ArrayList<ColumnInfo>();
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

		/**如果是map参数**/
		if (isMap(resultType)) {
			List<Map> rsList = new ArrayList<>();
			while (rs.next()) {
				Map mp = new HashMap();
				for (ColumnInfo column : colunmInfos) {
					mp.put(column.getColumnName(),
							rs.getObject(column.getColumnName()));
				}
			}
			returnResult = rsList;
		/*int or String*/
		}else if(isInt(resultType) || isString(resultType)){
			List rsList = new ArrayList();
			Object value = null;
			if(rs.next()){
				 value = rs.getObject(1);
			}
			if(isInt(resultType)){
				 rsList.add(Integer.valueOf(value+""));
			}
			if(isString(resultType)){
				rsList.add(value);
			}
			returnResult = rsList;
		}else {
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
	 * sql执行
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
	 * 获取sqlStatement
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
			throw new EagleException("sqlStatement????????");
		} else {
			Pattern p = Pattern.compile(SEPERATOR);
			Matcher m = p.matcher(sqlStatement);
			if (!m.find()) {
				throw new EagleException(
						"sqlStatement??????????????{mapperNamespace}.{sqlId}");
			} else {
				String[] argArrs = sqlStatement.split(SEPERATOR);
				String namespace = argArrs[0];
				sqlId = argArrs[1];
				/**
				 * 解析sql xml
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
	 * 解析sql mapper
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
		ConfigLoader loder = ConfigFactory.getConfigLoader(resource);
		List<Element> statements = loder.root.elements(STATEMENT);
		Element matchElement = null;
		for (Element e : statements) {
			String idAttr = e.attributeValue("id");
			if (StringUtils.equals(sqlId, idAttr)) {
				matchElement = e;
				break;
			}
		}
		Map map = new HashMap<>();
		map.put(RESULTTYPE, matchElement.attributeValue(RESULTTYPE));
		map.put(PARAMTYPE, matchElement.attributeValue(PARAMTYPE));
		String prefixSql = matchElement.getText().replace("\n\t", "");
		/**
		 * ???sql?ж?????
		 */
		List<Element> ifs = matchElement.elements(IF);
		for (Element e : ifs) {
			String test = e.attributeValue(TEST);
			// ????????????sql =======to do
			prefixSql += "";
		}
		// ????SQLString?е?????????????sql
		map.putAll(returnSql(prefixSql, obj,
				matchElement.attributeValue(PARAMTYPE)));
		return map;
	}

	/**
	 * ????sql??????????????
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
			throw new EagleException("sql?????????????????????????????????");
		}
		if(existsParam(sql) && obj != null){
			Class<?> paramClazz = obj.getClass();
			Class<?> configClazz = Class.forName(paramType);
			if (paramClazz != configClazz && (!isMap(paramClazz.getName()) && !isMap(paramType))) {
				log.logInfo("????????????????????????????");
				throw new EagleException("????????????????????????????");
			} else {
				// ------???#{}????------
				List<ParamInfo> cubList = extractVal(sql, REGEX_CUB);
				// ?????#{}?滻???λ????
				sql = processSqlStr_Cub(cubList, sql);
				map.put("cubList", cubList);

				// -----???${}????------
				List<ParamInfo> dolrList = extractVal(sql, REGEX_DOLR);
				// ??${}????滻?????????
				sql = processSqlStr_Dolr(dolrList, sql, obj);
			}
		}
		map.put("sql", sql);
		return map;
	}
	
	/**
	 * ?????#{}?滻???λ????
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
	 * ??${}????滻?????????
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
			if (!isMap(obj.getClass().getName())) {// ???????????map?????
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

			} else {// ??????????map
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
	 * ??????????
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
	 * ?ж?sql????????
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
	 * ???????????????????
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
	 * ?????????
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
	 * ????????????????????
	 * @param name
	 * @return
	 */
	private String returnSetMethodName(String name) {
		name = name.replace(name.charAt(0) + "",(name.charAt(0) + "").toUpperCase());
		return "set" + name;
	}

	/**
	 * ???????????
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
					if (value.toString().length() > 1) {
						throw new EagleException("???????????????????????????????????");
					} else {
						value = Character.valueOf(value.toString().charAt(0));
					}
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
					value = new Date(((Date) value).getTime());
					m.invoke(obj, value);
				}
				return;
			}
		}
	}

	@Override
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
	 * ???????????δ???
	 * @param param
	 * @return
	 */
	private String getColumnName(String param) {
		if (StringUtils.isEmpty(param)) {
			return param;
		}
		return param.indexOf("_") > 0 ? param.replaceAll("_", "").toLowerCase(): param.toLowerCase();
	}

	/**
	 * ?ж??????map
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
