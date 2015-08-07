package com.gbi.commons.base.service;

import com.gbi.commons.net.http.BasicHttpClient;
import com.gbi.commons.net.http.BasicHttpResponse;

public class ProxyTest {
	
	public static void test1() {// IPv4: "202.119.25.227"
		BasicHttpClient browser = new BasicHttpClient();
		browser.setProxy("185.6.55.52", 8080);
	//	BasicHttpResponse resp = browser.get("http://www.shanghai.gov.cn/newshanghai/img/color-logo-hd.png");
		BasicHttpResponse resp = browser.get("http://www.google.com");
		if (resp != null) {
			System.out.println(resp.getMd5());
		} else {
			System.out.println("null");
		}
		browser.close();
	}
	
	public static void main(String[] args) {
		test1();
	}
}
