package com.eagle.dataSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.eagle.common.ConfigFactory;
import com.eagle.loader.ConfigLoader;

public class DBUtil {
	private final static String DATASOURCE_TAG = "dataSource";
	private final static String CONTEXTPATH_TAG = "context-path";
	private final static String PROPERTY_TAG = "property";
	/**
	 * 数据库信息标签
	 */
	/*数据库驱动类*/
	private final static String DRIVERCLASSNAME_ATTR = "driverClassName";
	private final static String URL_ATTR = "url";
	private final static String USERNAME_ATTR = "username";
	private final static String PASSWORD_ATTR = "password";
	private final static String INITIALSIZE_ATTR = "initialSize";
	private final static String MAXACTIVE_ATTR = "maxActive";
	private final static String MINIDLE_ATTR = "minIdle";
	private final static String MAXWAIT_ATTR = "maxWait";
	private final static String POOLPREPAREDSTATEMENTS_ATTR = "poolPreparedStatements";
	private final static String MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE_ATTR = "maxPoolPreparedStatementPerConnectionSize";
	private final static String VALIDATIONQUERY_ATTR = "validationQuery";
	private final static String TESTONBORROW_ATTR = "testOnBorrow";
	private final static String TESTONRETURN_ATTR = "testOnReturn";
	private final static String TESTWHILEIDLE_ATTR = "testWhileIdle";
	private final static String TIMEBETWEENEVICTIONRUNSMILLIS_ATTR = "timeBetweenEvictionRunsMillis";
	private final static String MINEVICTABLEIDLETIMEMILLIS = "minEvictableIdleTimeMillis";
	private final static String REMOVEABANDONED_ATTR = "removeAbandoned";
	private final static String REMOVEABANDONEDTIMEOUT_ATTR = "removeAbandonedTimeout";
	private final static String LOGABANDONED_ATTR = "logAbandoned";
	private final static String MAXOPENPREPAREDSTATEMENTS_ATTR = "maxOpenPreparedStatements";
	
	private static Properties properties = new Properties();
	
	static{
		initConnectInfo();
	}
	
	/**
	 * 初始化数据连接信息
	 */
	private static void initConnectInfo(){
		ConfigLoader loder = ConfigFactory.getSqlConfigLoader();
		Element environment = loder.root.element("environment");
		List<Element> elements = loder.getRootElements(environment,DATASOURCE_TAG,PROPERTY_TAG);//xml方式
		Element element = loder.getRootElements(environment,DATASOURCE_TAG,CONTEXTPATH_TAG).get(0);//properties文件的方式
		if(elements != null && elements.size()>0){
			for(Element e : elements){
				String attr = e.attributeValue("name");
				String value = e.attributeValue("value");
				if(StringUtils.equals(attr,DRIVERCLASSNAME_ATTR)){
					properties.put(DRIVERCLASSNAME_ATTR, value);
				}
				else if(StringUtils.equals(attr,URL_ATTR)){
					properties.put(URL_ATTR, value);
				}
				else if(StringUtils.equals(attr,USERNAME_ATTR)){
					properties.put(USERNAME_ATTR, value);
				}
				else if(StringUtils.equals(attr,PASSWORD_ATTR)){
					properties.put(PASSWORD_ATTR, value);
				}
				else if(StringUtils.equals(attr,INITIALSIZE_ATTR)){
					properties.put(INITIALSIZE_ATTR, value);
				}
				else if(StringUtils.equals(attr,MAXACTIVE_ATTR)){
					properties.put(MAXACTIVE_ATTR, value);
				}
				else if(StringUtils.equals(attr,MINIDLE_ATTR)){
					properties.put(MINIDLE_ATTR, value);
				}
				else if(StringUtils.equals(attr,MAXWAIT_ATTR)){
					properties.put(MAXWAIT_ATTR, value);
				}
				else if(StringUtils.equals(attr,POOLPREPAREDSTATEMENTS_ATTR)){
					properties.put(POOLPREPAREDSTATEMENTS_ATTR, value);
				}
				else if(StringUtils.equals(attr,MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE_ATTR)){
					properties.put(MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE_ATTR, value);
				}
				else if(StringUtils.equals(attr,VALIDATIONQUERY_ATTR)){
					properties.put(VALIDATIONQUERY_ATTR, value);
				}
				else if(StringUtils.equals(attr,TESTONBORROW_ATTR)){
					properties.put(TESTONBORROW_ATTR, value);
				}
				else if(StringUtils.equals(attr,TESTONRETURN_ATTR)){
					properties.put(TESTONRETURN_ATTR, value);
				}
				else if(StringUtils.equals(attr,TESTWHILEIDLE_ATTR)){
					properties.put(TESTWHILEIDLE_ATTR, value);
				}
				else if(StringUtils.equals(attr,MINEVICTABLEIDLETIMEMILLIS)){
					properties.put(MINEVICTABLEIDLETIMEMILLIS, value);
				}
				else if(StringUtils.equals(attr,REMOVEABANDONED_ATTR)){
					properties.put(REMOVEABANDONED_ATTR, value);
				}
				else if(StringUtils.equals(attr,TIMEBETWEENEVICTIONRUNSMILLIS_ATTR)){
					properties.put(TIMEBETWEENEVICTIONRUNSMILLIS_ATTR, value);
				}
				else if(StringUtils.equals(attr,REMOVEABANDONEDTIMEOUT_ATTR)){
					properties.put(REMOVEABANDONEDTIMEOUT_ATTR, value);
				}
				else if(StringUtils.equals(attr,LOGABANDONED_ATTR)){
					properties.put(LOGABANDONED_ATTR, value);
				}
				else if(StringUtils.equals(attr,MAXOPENPREPAREDSTATEMENTS_ATTR)){
					properties.put(MAXOPENPREPAREDSTATEMENTS_ATTR, value);
				}
			}
		}
		
		if(element != null){//properties 文件方式
			try {
				properties.load(DBUtil.class.getResourceAsStream(element.getText()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * 不存在的情况下默认一些配置
		 */
		setDefault();
	}
	private static void setDefault(){
		if(!properties.containsKey(INITIALSIZE_ATTR)){
			properties.put(INITIALSIZE_ATTR, "0");
		}
		if(!properties.containsKey(MAXACTIVE_ATTR)){
			properties.put(MAXACTIVE_ATTR, "8");
		}
		if(!properties.containsKey(POOLPREPAREDSTATEMENTS_ATTR)){
			properties.put(POOLPREPAREDSTATEMENTS_ATTR, "false");
		}
		if(!properties.containsKey(MAXOPENPREPAREDSTATEMENTS_ATTR)){
			properties.put(MAXOPENPREPAREDSTATEMENTS_ATTR, "-1");
		}
		if(!properties.containsKey(TESTONBORROW_ATTR)){
			properties.put(TESTONBORROW_ATTR, "true");
		}
		if(!properties.containsKey(TESTONRETURN_ATTR)){
			properties.put(TESTONRETURN_ATTR, "false");
		}
		if(!properties.containsKey(TESTWHILEIDLE_ATTR)){
			properties.put(TESTWHILEIDLE_ATTR, "false");
		}
	}
	
	public static DataSource getDataSource() throws Exception{
		DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);
		return dataSource;
	}
	
	public static Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	
	public static void main(String[] args) {
		try {
			DBUtil.getDataSource().getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
