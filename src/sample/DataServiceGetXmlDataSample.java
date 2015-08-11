package sample;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.text.SimpleDateFormat;

import javax.xml.namespace.QName;

import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;

import sun.misc.BASE64Encoder;
/**
 * 利用axis2从DataExchange服务查询数据样例
 * 
 * 本样例将根据Client端发送的参数条件到DataExchange服务查询对应的业务XML数据。
 * 执行过程同步等待，若过程中出错，将抛出异常信息，否则返回查询数据。
 * 
 * @author XIEYSH
 * @version V1.0
 */
public class DataServiceGetXmlDataSample {
	/**
	 * Timestamp类型格式化
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ThreadLocal<SimpleDateFormat> dateTimeLocal = new ThreadLocal() {
	    protected SimpleDateFormat initialValue() {
	      return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S.Z");
	    }
	};
	/**
	 * Date类型格式化
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static ThreadLocal<SimpleDateFormat> dateLocal = new ThreadLocal() {
	    protected SimpleDateFormat initialValue() {
	      return new SimpleDateFormat("yyyy-MM-dd");
	    }
	};
	/**
	 * Time类型格式化
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static ThreadLocal<SimpleDateFormat> timeLocal = new ThreadLocal() {
	    protected SimpleDateFormat initialValue() {
	      return new SimpleDateFormat("HH:mm:ss");
	    }
	};
	/**
	 * 样例主方法
	 */
	@SuppressWarnings("rawtypes")
	public static void main(String[] args) throws Exception {
		// DataExchange服务地址，如果不在本机，请修改成对应IP地址
		// String url = "http://59.175.218.200:9090/dxservice";
		String url = "http://127.0.0.1:8881/dxservice";
		// DataExchange服务登录用户名，默认为admin
		String username = "admin";
		// DataExchange服务登录用户名，默认为1
		String password = "1";
		// 任务OID，定义的数据查询任务业务标识，可以到“任务”页面查看
		String taskOId = "hbjyweb_webservice_cb20_xml_task";
		// 添加查询条件
		Map<String, Object> params = new HashMap<String, Object>();
		// 查询条件设置，例：AAB004变量名称，'湖北%'是本次要查询的变量值，表示查询AAB004 like '湖北%'的数据
		params.put("AAB003", "118262782%");
		params.put("AAB007", "%");//组织机构代码和营业执照要求至少必录一项，为空的那个直接传%
		params.put("AAB004", "1湖北%");
		params.put("AAF036_1", "19800101");//起始时间
		params.put("AAF036_2", "20141231");//终止时间
		params.put("AAE022", "42%");
		// 客户端应用名称，可以为空
		String appName = "湖北省交换区数据交换服务器";
		// 请求超时时间
		int timeout = 20000;
		// 请求方式。post或者get
		QName methodQName = new QName("ns", "get");
		
		String endpoint = url+"/svcs/DataService";
		RPCServiceClient serviceClient = new RPCServiceClient();
		EndpointReference targetEPR = new EndpointReference(endpoint);
		// 请求信息设置
		Options rpcOptions = serviceClient.getOptions();
		rpcOptions.setTimeOutInMilliSeconds(timeout);
		rpcOptions.setTo(targetEPR);
		if ((username != null) && (!(username.equals("")))) {
			rpcOptions.setProperty("javax.xml.rpc.security.auth.username", username);
		}
	    if ((password != null) && (!(password.equals("")))) {
	    	rpcOptions.setProperty("javax.xml.rpc.security.auth.password", password);
	    }
	    if (username != null) {
	    	Hashtable<String, String> hashTb = new Hashtable<String, String>();
	    	String auth = username + ":" + ((password == null) ? "" : password);
	    	hashTb.put("RPC_CONNECTION_AUTHORIZATION", bytesToBASE64NoSpace(auth.getBytes("utf-8")));
         // 校验用用户名密码
	    	hashTb.put("username", "admin");
	    	hashTb.put("password", "1");
	    	rpcOptions.setProperty("HTTP_HEADERS", hashTb);
		}
		Class[] returnTypes = { Object.class };
		// 传入参数设置
	    Object[] arguments = new Object[3];
	    arguments[0] = appName;
	    arguments[1] = taskOId;
	    arguments[2] = serializable(params);
	    long s = System.currentTimeMillis();
	    Object[] response = serviceClient.invokeBlocking(methodQName, arguments, returnTypes);
	    if (response == null) {
	    	throw new Exception("请求目标失败，响应对象为空。");
	    }
	    Object ret = null;
	    ret = getInvokeValue(response);
	    String xml = (String)handleReturnValue(ret);
	    System.out.println(xml);
	    long e = System.currentTimeMillis();
		System.out.println("查询数据耗时：" + (e - s) + "毫秒。");
	}
	
	/**
	 * 字符数组转64位byte字符串
	 */
	public static String bytesToBASE64NoSpace(byte[] b) {
	    if ((b == null) || (b.length <= 0)) {
	    	return null;
	    }
	    String r = new BASE64Encoder().encode(b);
	    return r.replaceAll("\\s", "");
	}
	/**
	 * 按指定格式拼写传入参数字符串
	 */
	@SuppressWarnings("rawtypes")
	public static String serializable(Map<String, Object> params) throws Exception {
	    if (params == null)
	      return null;
	    String key = null;
	    Object value = null;
	    StringBuffer sb = new StringBuffer();
	    sb.append("<maps>");
	    for (Iterator localIterator = params.entrySet().iterator(); localIterator.hasNext(); ) { Map.Entry entry = (Map.Entry)localIterator.next();
	      sb.append("<map>");
	      key = (String)entry.getKey();
	      if ((key == null) || (key.equals(""))) {
	    	  throw new Exception("参数Key值不能为空。");
	      }
	      value = entry.getValue();

	      sb.append("<key>");
	      sb.append("<![CDATA[");
	      sb.append(key);
	      sb.append("]]>");
	      sb.append("</key>");

	      sb.append("<value>");
	      sb.append(objectToString(value));
	      sb.append("</value>");
	      sb.append("</map>");
	    }
	    sb.append("</maps>");
	    return sb.toString();
	}
	/**
	 * 对象类型转字符串类型
	 */
	private static String objectToString(Object obj) throws Exception {
	    if (obj == null) {
	    	return "";
	    }
	    if (obj instanceof String) {
	      return "<![CDATA[" + obj.toString() + "]]>";
	    }
	    if (obj instanceof Number) {
	      return obj.toString();
	    }
	    if (obj instanceof Timestamp) {
	      return formatDateTime(obj);
	    }
	    if (obj instanceof Time) {
	      return formatTime(obj);
	    }
	    if (obj instanceof Date) {
	      return formatDate(obj);
	    }
	    if (obj instanceof Boolean) {
	      return obj.toString();
	    }
	    if (obj instanceof Character) {
	      return "<![CDATA[" + obj.toString() + "]]>";
	    }
	    throw new Exception("不支持的参数对象类型:" + obj.getClass());
	}
	/**
	 * Date类型格式化
	 */
	public static String formatDate(Object date) {
	    return ((SimpleDateFormat)dateLocal.get()).format((Date)date);
	}
	/**
	 * Time类型格式化
	 */
	public static String formatTime(Object date) {
	    return ((SimpleDateFormat)timeLocal.get()).format((Date)date);
	}
	/**
	 * Timestamp类型格式化
	 */
	public static String formatDateTime(Object date) {
	    return ((SimpleDateFormat)dateTimeLocal.get()).format((Date)date);
	}
	/**
	 * 根据返回response取得查询结果
	 */
	@SuppressWarnings("unchecked")
	private static Object getInvokeValue(Object[] response) {
	    Object res = response[0];
	    if (res == null) {
	    	return null;
	    }
	    if (res instanceof OMTextImpl) {
	    	OMTextImpl omText = (OMTextImpl)res;
	    	return omText.getText();
	    }
	    List<String> retValue = new ArrayList<String>();
	    if (res instanceof OMElementImpl) {
	    	OMElementImpl omElement = (OMElementImpl)res;
	    	Iterator<OMElementImpl> it = omElement.getParent().getChildren();
	    	while (it.hasNext()) {
	    		OMElementImpl text = (OMElementImpl)it.next();
	    		retValue.add(text.getText());
	    	}
	    }
	    int len = retValue.size();
	    String[] retStr = new String[len];
	    for (int i = 0; i < len; ++i) {
	    	retStr[i] = ((String)retValue.get(i));
	    }

	    return retStr;
	}
	/**
	 * 根据查询结果.格式化返回结果
	 */
	private static Object handleReturnValue(Object ret) throws Exception {
		if ((ret != null) && (ret instanceof String[])) {
			String codeStr;
			int code;
			String[] back = (String[])ret;
			if (back.length == 1) {
				codeStr = back[0];
				code = Integer.valueOf(codeStr).intValue();
				if (code > 0) {
					throw new Exception("未知异常。");
				}
				return null;
			}
			if (back.length >= 2) {
				codeStr = back[0];
				code = Integer.valueOf(codeStr).intValue();
				if (code > 0) {
					throw new Exception(back[1]);
				}
				if (back.length == 2) {
					return back[1];
				}
				if (back.length > 2) {
					String[] str = new String[back.length - 1];
					System.arraycopy(back, 1, str, 0, str.length);
					return str;
				}
			}
		}
		return ret;
	}
}
