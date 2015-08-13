package com.gbi.mongodb2.monitor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;

import com.gbi.commons.config.Params;
import com.gbi.commons.net.http.BasicHttpClient;
import com.gbi.commons.net.http.BasicHttpResponse;
import com.gbi.mongodb2.copy.CopyUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MonitorTest {
	
	private static MongoClient client = null;
	
	private static DBCollection getCollection(String name) {
		return client.getDB(Params.MongoDB.TEST.database).getCollection(name);
	}
	
	@Before
	private static void open() throws Exception {
		client = new MongoClient(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
	}
	
	@After
	private static void close() throws Exception {
		client.close();
	}
	
	public static void test_cd() throws Exception {
		String table = "source_cd";
		CopyUtil.copyCollection("source", table);
		CopyUtil.copyCollection("source.log", table + ".log");

		DBCollection c = getCollection(table);
		DBObject people1 = new BasicDBObject();
		people1.put("_id", 4);
		people1.put("name", "尹志平");
		people1.put("age", 20);
		c.save(people1);
		c.remove(new BasicDBObject("_id", 1));
		
		DBMonitor m = new DBMonitor(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		m.open();
		m.log(Params.MongoDB.TEST.database,	table);
		m.close();
		System.out.println("insert:" + m.getInsert());
		System.out.println("create:" + m.getCreate());
		System.out.println("delete:" + m.getDelete());
		System.out.println("change:" + m.getUpdate());
	}
	
	public static void test_cu() throws Exception {
		String table = "source_cu";
		CopyUtil.copyCollection("source", table);
		CopyUtil.copyCollection("source.log", table + ".log");
		
		DBCollection c = getCollection(table);
		DBObject people1 = new BasicDBObject();
		people1.put("_id", 4);
		people1.put("name", "尹志平");
		people1.put("age", 20);
		c.save(people1);
		
		people1.put("age", 22);
		c.save(people1);
		
		DBMonitor m = new DBMonitor(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		m.open();
		m.log(Params.MongoDB.TEST.database,	table);
		m.close();
		System.out.println("insert:" + m.getInsert());
		System.out.println("create:" + m.getCreate());
		System.out.println("delete:" + m.getDelete());
		System.out.println("change:" + m.getUpdate());
	}
	
	public static void test_cud() throws Exception {
		String table = "source_cud";
		CopyUtil.copyCollection("source", table);
		CopyUtil.copyCollection("source.log", table + ".log");
		
		DBCollection c = getCollection(table);
		DBObject people1 = new BasicDBObject();
		people1.put("_id", 4);
		people1.put("name", "尹志平");
		people1.put("age", 20);
		c.save(people1);
		
		people1.put("age", 22);
		c.save(people1);
		
		c.remove(people1);
		
		DBMonitor m = new DBMonitor(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		m.open();
		m.log(Params.MongoDB.TEST.database,	table);
		m.close();
		System.out.println("insert:" + m.getInsert());
		System.out.println("create:" + m.getCreate());
		System.out.println("delete:" + m.getDelete());
		System.out.println("change:" + m.getUpdate());
	}

	public static void test_dc() throws Exception {
		String table = "source_dc";
		CopyUtil.copyCollection("source", table);
		CopyUtil.copyCollection("source.log", table + ".log");
		
		DBCollection c = getCollection(table);
		c.remove(new BasicDBObject("_id", 1));
		
		DBObject people1 = new BasicDBObject();
		people1.put("_id", 1);
		people1.put("name", "尹志平");
		people1.put("age", 20);
		c.save(people1);
		
		DBMonitor m = new DBMonitor(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		m.open();
		m.log(Params.MongoDB.TEST.database,	table);
		m.close();
		System.out.println("insert:" + m.getInsert());
		System.out.println("create:" + m.getCreate());
		System.out.println("delete:" + m.getDelete());
		System.out.println("change:" + m.getUpdate());
	}
	
	public static void test_d_c() throws Exception {
		String table = "source_d_c";
		CopyUtil.copyCollection("source", table);
		CopyUtil.copyCollection("source.log", table + ".log");
		
		DBCollection c = getCollection(table);
		c.remove(new BasicDBObject("_id", 1));
		
		DBMonitor m = new DBMonitor(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		m.open();
		m.log(Params.MongoDB.TEST.database,	table);
		m.close();
		System.out.println("insert:" + m.getInsert());
		System.out.println("create:" + m.getCreate());
		System.out.println("delete:" + m.getDelete());
		System.out.println("change:" + m.getUpdate());
		
		DBObject people1 = new BasicDBObject();
		people1.put("_id", 1);
		people1.put("name", "尹志平");
		people1.put("age", 20);
		c.save(people1);

		m.open();
		m.log(Params.MongoDB.TEST.database,	table);
		m.close();
		System.out.println("insert:" + m.getInsert());
		System.out.println("create:" + m.getCreate());
		System.out.println("delete:" + m.getDelete());
		System.out.println("change:" + m.getUpdate());
	}
	
	public static void test_du() throws Exception {
		String table = "source_du";
		CopyUtil.copyCollection("source", table);
		CopyUtil.copyCollection("source.log", table + ".log");
		
		DBCollection c = getCollection(table);
		c.remove(new BasicDBObject("_id", 1));

		DBObject people1 = c.findOne(new BasicDBObject("_id", 2));
		people1.put("age", 23);
		c.save(people1);

		DBMonitor m = new DBMonitor(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		m.open();
		m.log(Params.MongoDB.TEST.database,	table);
		m.close();
		System.out.println("insert:" + m.getInsert());
		System.out.println("create:" + m.getCreate());
		System.out.println("delete:" + m.getDelete());
		System.out.println("change:" + m.getUpdate());
	}
	
	public static void GrabYoudaili() {
		DBCollection collection = client.getDB(Params.MongoDB.TEST.database).getCollection("proxy_5");
		BasicHttpClient browser = new BasicHttpClient();
		String[] urls = new String[] { "http://www.youdaili.net/Daili/guowai/list_4.html" };
		int count = 0;
		for (String url : urls) {
			BasicHttpResponse response = browser.get(url);
			if (response == null) {
				browser.close();
				throw new RuntimeException("有代理国外代理访问失败");
			}
			// 抓取首页 >
			Elements lines = response.getDocument().select("ul.newslist_line>li>a");
			for (Element line : lines) {
				response = browser.get(line.absUrl("href"));
				if (response == null) {
					System.err.println("丢失一个网页");
					continue;
				}
				while (true) {
					Document dom = response.getDocument();
					List<TextNode> textNodes = response.getDocument().select("div.cont_font>p").first().textNodes();
					Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)@([^@#]*)#(【匿】){0,1}([^#]*)");
					for (TextNode textNode : textNodes) {
						Matcher m = pattern.matcher(textNode.text());
						if (m.find()) {
							DBObject proxy = collection.findOne(new BasicDBObject("_id", m.group(1) + ":" + m.group(2)));
							if (proxy == null) {
								proxy = new BasicDBObject();
								proxy.put("_id", m.group(1) + ":" + m.group(2));
								proxy.put("IPv4", m.group(1));
								proxy.put("port", m.group(2));
								proxy.put("protocol", m.group(3));
								proxy.put("type", m.group(4) == null ? "" : "anonymous");
								proxy.put("source", "youdaili");
								proxy.put("location", m.group(5));
							} else {
								proxy.put("protocol", m.group(3));
								proxy.put("type", m.group(4) == null ? "" : "anonymous");
								proxy.put("source", "youdaili");
								proxy.put("location", m.group(5));
							}
							collection.save(proxy);
							++count;
						}
					}
					Element a = dom.select("ul>pagelist>li>a:containsOwn(下一页)").first();
					if (a == null) {
						break;
					} else {
						if ("#".equals(a.attr("href"))) {
							break;
						} else {
							response = browser.get(a.absUrl("href"));
						}
					}
				}
			}
			// 抓取首页 <
		}
		System.out.println("网站:有代理 共捕获数据 " + count + " 条");
		browser.close();
	}
	
	public static void log_test1() throws Exception {
		DBMonitor m = new DBMonitor(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		m.open();
		m.log(Params.MongoDB.TEST.database,	"proxy");
		m.close();
		System.out.println("insert:" + m.getInsert());
		System.out.println("create:" + m.getCreate());
		System.out.println("delete:" + m.getDelete());
		System.out.println("change:" + m.getUpdate());
	}
	
	public static void recovery_test1() throws Exception {
		DBMonitor m = new DBMonitor(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		m.open();
		m.recovery(1438851189650L, Params.MongoDB.TEST.database, "proxy");
		m.close();
	}

	public static void main(String[] args) throws Exception {
	//	open();
	//	GrabYoudaili();
	//	close();
	//	log_test1();
		recovery_test1();
	}
}
