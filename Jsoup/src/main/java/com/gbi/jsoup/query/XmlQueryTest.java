package com.gbi.jsoup.query;

import java.io.File;
import java.io.FileInputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import com.gbi.commons.util.file.ResourceUtil;

public class XmlQueryTest {
	
	private static void test1() throws Exception {
		System.out.println("------test1----->\n");
		File file = ResourceUtil.getTestFile(XmlQueryTest.class, "test1.xml");
		Document dom = Jsoup.parse(new FileInputStream(file), "UTF-8", "", Parser.xmlParser());
		System.out.println(dom);
		System.out.println("<-----test1------\n");
	}

	public static void main(String[] args) throws Exception {
		test1();
	}
}
