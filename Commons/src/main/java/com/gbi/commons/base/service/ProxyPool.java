package com.gbi.commons.base.service;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.gbi.commons.config.Params;
import com.gbi.commons.net.amqp.MsgConsumers;
import com.gbi.commons.net.amqp.MsgProducer;
import com.gbi.commons.net.amqp.MsgWorker;
import com.gbi.commons.net.amqp.MsgWorkerFactory;
import com.gbi.commons.net.http.BasicHttpClient;
import com.gbi.commons.net.http.BasicHttpResponse;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class ProxyPool {

	private static final String queueName = "ProxyPool";
	private static final Map<String, String> checkSubject = new HashMap<>();
	// private static final Map<String, String> subjectMd5 = new HashMap<>();
	private static MongoClient client = null;
	private static DBCollection collection = null;
	private static MsgProducer<String> producer = null;

	private static void init() {
		try {
			client = new MongoClient(Params.MongoDB.PROXIES.host, Params.MongoDB.PROXIES.port);
			collection = client.getDB(Params.MongoDB.PROXIES.database).getCollection("proxies");
			producer = new MsgProducer<>(queueName);
		} catch (UnknownHostException | TimeoutException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		checkSubject.put("US", "http://www.google.com");
		checkSubject.put("CN", "http://www.shanghai.gov.cn/newshanghai/img/color-logo-hd.png"); // 百度logo

		BasicHttpClient c = new BasicHttpClient();
		c.setProxy(Params.StableProxy.ZHITAO.host, Params.StableProxy.ZHITAO.port);
		c.close();
	}

	private static void exit() {
		producer.close();
		client.close();
	}

	// 抓取有代理的代理服务器地址
	public static void GrabYoudaili() {
		BasicHttpClient browser = new BasicHttpClient();
		String[] urls = new String[] { "http://www.youdaili.net/Daili/guowai/",
				"http://www.youdaili.net/Daili/guonei/" };
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

	public static void checkProxyPool() {
		DBCursor cursor = collection.find(new BasicDBObject(), new BasicDBObject("_id", 1));
		for (DBObject proxyInfo : cursor) {
			producer.send((String) proxyInfo.get("_id"));
		}
		cursor.close();
		new MsgConsumers(queueName, 20, new MsgWorkerFactory<String>() {
			@Override
			public MsgWorker<String> newWorker() {
				return socketAddr -> checkProxy(socketAddr);
			}
		}).run();
	}

	private static boolean checkProxy(String socketAddr) {
		System.out.println("begin " + socketAddr);
		BasicHttpClient browser = new BasicHttpClient();
		browser.setProxy(socketAddr.split(":")[0], socketAddr.split(":")[1]);
		BasicDBList tag = new BasicDBList();
		BasicDBList delay = new BasicDBList();
		for (String key : checkSubject.keySet()) {
			long beginTime = System.currentTimeMillis();
			BasicHttpResponse r = browser.get(checkSubject.get(key));
			if (r == null) {
				continue;
			}
			long endTime = System.currentTimeMillis();
			tag.add(key);
			delay.add(endTime - beginTime);
		}
		System.out.println(socketAddr + " out");
		if (tag.size() == 0) {
			System.out.println(socketAddr + " 没什么用");
			collection.remove(new BasicDBObject("_id", socketAddr));
		} else {
			DBObject proxyInfo = collection.findOne(new BasicDBObject("_id", socketAddr));
			proxyInfo.put("tag", tag);
			proxyInfo.put("delay", delay);
			proxyInfo.put("updateTime", Calendar.getInstance().getTime());
			collection.save(proxyInfo);
		}
		browser.close();
		System.out.println("end " + socketAddr);
		return true;
	}

	public static void main(String[] args) {
		init();
		GrabYoudaili();
		checkProxyPool();
		exit();
	}
}
