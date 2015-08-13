package com.gbi.jsoup.connect;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

public class ConnectTest {

	public static void connectResponse() {
		try {
			Connection connect = Jsoup.connect("http://www.baidu.com");
			Response resp = connect.method(Method.GET).timeout(100000).execute();
			System.out.println(resp.contentType());
			System.out.println(resp.cookies());
			System.out.println(resp.headers());
			System.out.println(resp.body());
		} catch (HttpStatusException e) {
			System.err.println("status:" + e.getStatusCode());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		connectResponse();
	}
}