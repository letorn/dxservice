package dxservice;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;

import sun.misc.BASE64Encoder;

public class Client {

	private RPCServiceClient service = new RPCServiceClient();
	private Call call;
	private int callTimeout = 90000;

	public Client(String dxservice, String username, String password) {
		try {
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(dxservice);
			call.setTimeout(callTimeout);
			call.setUseSOAPAction(true);
			call.setProperty(HTTPConstants.REQUEST_HEADERS, encodeAuth(username, password));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Client(String dxservice, String username, String password, String operation) {
		try {
			call = (Call) service.createCall();
			call.setTargetEndpointAddress(dxservice);
			call.setTimeout(callTimeout);
			call.setUseSOAPAction(true);
			call.setProperty(HTTPConstants.REQUEST_HEADERS, encodeAuth(username, password));
			call.setOperationName(new QName("ns", operation));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setOperationName(String opName) {
		call.setOperationName(opName);
	}

	public void setOperationName(String namespaceURI, String localPart) {
		call.setOperationName(new QName(namespaceURI, localPart));
	}

	public Object[] execute(Object... params) {
		try {
			String[] strs = (String[]) call.invoke(params);
			int statusCode = strs[0] != null && strs[0].matches("\\d") ? Integer.valueOf(strs[0]) : -1;
			if (statusCode == 0) {
				return new Object[] { HttpServletResponse.SC_OK, strs.length > 1 ? strs[1] : "" };
			} else {
				return new Object[] { statusCode, strs[1] };
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			if (e.getCause() instanceof SocketTimeoutException) {
				return new Object[] { HttpServletResponse.SC_REQUEST_TIMEOUT, "Request Timeout" };
			} else if (e.getCause() instanceof ConnectException) {
				return new Object[] { HttpServletResponse.SC_NOT_FOUND, "Not Found" };
			} else {
				String message = e.getMessage() != null ? e.getMessage() : "";
				if (message.contains("(404)Not Found") || message.contains("could not find a target service")) {
					return new Object[] { HttpServletResponse.SC_NOT_FOUND, "Not Found" };
				} else if (message.contains("(503)Service Unavailable")) {
					return new Object[] { HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Service Unavailable" };
				} else {
					return new Object[] { -1, "Unkonwn" };
				}
			}
		}
	}

	public void setTimeout(int timeout) {
		call.setTimeout(timeout);
	}

	private static Object encodeAuth(String username, String password) {
		Hashtable<String, String> hashTb = new Hashtable<String, String>();
		try {
			String auth = username + ":" + ((password == null) ? "" : password);
			hashTb.put("RPC_CONNECTION_AUTHORIZATION", new BASE64Encoder().encode(auth.getBytes("utf-8")));
			hashTb.put("username", username);
			hashTb.put("password", password);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return hashTb;
	}
	
}
