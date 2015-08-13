package com.gbi.jsoup.common;

import com.gbi.commons.net.http.BasicHttpClient;
import com.gbi.commons.net.http.BasicHttpResponse;

public class ProxyTest {
	
	public static void proxyTest1() {
		BasicHttpClient client = new BasicHttpClient();
		client.setProxy("192.168.0.116", 1080);
		try {
			System.out.println(client.get("http://www.google.com").getDocument());
		} catch (Exception e) {
			e.printStackTrace();
		}
		client.close();
	}
	
	public static void proxyTest2() {
		BasicHttpClient client = new BasicHttpClient();
		client.setProxy("113.53.230.154",3129);
		try {
			BasicHttpResponse response = client.get("http://maps.google.com/maps/api/geocode/json?address=KYUNG+HEE+UNIVERSITY+EAST+WEAST+NEO+MEDICALCENTER+SEOUL+KOREA+REPUBLIC+OF+US&language=en");
			System.out.println(client.getLastStatus());
			System.out.println(new String(response.getContent()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		client.close();
	}

	public static void main(String[] args) {
		proxyTest2();
	}
}
