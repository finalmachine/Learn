package com.gbi.commons.model;

import java.util.HashMap;
import java.util.Map;

import com.gbi.commons.net.http.HttpMethod;

public class SimpleHttpErrorInfo {
	private String url;
	private Map<String, String> data;
	private HttpMethod method;
	private String info;
	
	public SimpleHttpErrorInfo(String url, String info) {
		this.url = url;
		this.data = null;
		this.method = HttpMethod.GET;
		this.info = info;
	}
	
	public SimpleHttpErrorInfo(String url, String info, HttpMethod method, Map<String, String> data) {
		this.url = url;
		this.data = new HashMap<String, String>(data);
		this.method = method;
		this.info = info;
	}
	
	public String getInfo() {
		return info;
	}
	
	@Override
	public String toString() {
		return	"URL:" + url + ",\n" +
				"DATA:" + data + ",\n" +
				"METHOD:" + method + ",\n" +
				"INFO:" + info + "\n";
	}
}
