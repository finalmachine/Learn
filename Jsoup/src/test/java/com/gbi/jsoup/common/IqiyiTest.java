package com.gbi.jsoup.common;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class IqiyiTest {

	private static void dongmanTest() throws IOException {
		Document dom = Jsoup
				.connect("http://www.iqiyi.com/dongman/")
				.userAgent(
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36")
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
				.header("Accept-Encoding", "gzip, deflate, sdch")
				.header("Accept-Language", "zh-CN,zh;q=0.8")
				.header("Connection", "keep-alive")
			//	.header("Cookie", "QC008=1434867478.1434867478.1434867478.1; QC005=29ead5c52b1c4d2c4e6e8178d0bfe8dd; P00004=-744360007.1434867511.c1ae5f78fc; QC006=2r5pqde1zfsb5gte4wh04cl3; Hm_lvt_53b7374a63c37483e5dd97d78d9bb36e=1434867478; T00404=164426b534f914213bef3218b15c5e03; QC124=0%7C3")
				.header("Host", "www.iqiyi.com")
				.header("If-Modified-Since:Sun", "21 Jun 2015 03:56:07 GMT")
				.get();
		System.out.println(dom);
	}

	public static void main(String[] args) throws IOException {
		dongmanTest();
	}
}
