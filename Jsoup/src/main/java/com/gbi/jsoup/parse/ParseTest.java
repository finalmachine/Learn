package com.gbi.jsoup.parse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ParseTest {
	
	private static void parseHtmlString() {
		String html = "<html><head><title>First parse</title></head>"
				+ "<body> Parsed HTML into a doc. </body></html>";
		Document doc = Jsoup.parse(html);
		System.out.println(doc.title());
	}
	
	private static void parseHtmlFile() throws IOException {
		File input = new File("./file/local.html");
		Document doc = Jsoup.parse(input, "UTF-8", "http://www.baidu.com/");
		System.out.println(doc.title());
	}
	
	private static void getUrlHttp() throws IOException {
		Document doc = Jsoup.connect("http://www.baidu.com").get();
		System.out.println(doc.title());
	}

	private static void getUrlHttps() throws IOException {
		Document doc = Jsoup.connect("https://www.baidu.com").get();
		System.out.println(doc);
	}

	private static void postUrlHttp() throws IOException {
		Map<String, String> data = new HashMap<String, String>();
		data.put("_a_", "1");
		data.put("<b>", "2");
		Document doc = Jsoup.connect("http://localhost:8080/test/servlet/GeneralServlet").data(data).post();
		System.out.println(doc);
	}
	
	public static void main(String[] args) throws IOException {
		parseHtmlString();
		parseHtmlFile();
		getUrlHttp();
		getUrlHttps();
		postUrlHttp();
	}
}
