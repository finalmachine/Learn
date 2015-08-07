package com.gbi.commons.net.http;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.gbi.commons.config.Params;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class ProxiesHttpClient extends BasicHttpClient {

	private static AtomicInteger index = new AtomicInteger(0);
	private static ArrayList<String> proxypool = new ArrayList<>();
	private static ReadWriteLock lock = new ReentrantReadWriteLock(false);

	private String _tag = null;

	public ProxiesHttpClient() {
		lock.writeLock().lock();
		if (proxypool.isEmpty()) {
			reloadProxyList();
		} else {
			System.out.println("pool size:" + proxypool.size());
		}
		lock.writeLock().unlock();
	}

	public ProxiesHttpClient(String tag) {
		if (tag == null || tag.trim().length() == 0) {
			throw new IllegalArgumentException("tag must be valid");
		}
		_tag = tag;
		lock.writeLock().lock();
		if (proxypool.isEmpty()) {
			reloadProxyList();
		} else {
			System.out.println(proxypool.size());
		}
		lock.writeLock().unlock();
	}

	protected void changeProxy() {
		lock.readLock().lock();
		String proxyStr = proxypool.get(index.incrementAndGet() % proxypool.size());
		if (index.get() < 0) {
			index.set(0);
		}
		super.setProxy(proxyStr.split(":")[0], proxyStr.split(":")[1]);
		lock.readLock().unlock();
	}

	@Override
	protected void prepare(HttpMethod method, String uri, Map<String, String> extraHeaders,
			Map<String, String> data) {
		if (lastStatus != 302) {
			changeProxy();
		}
		super.prepare(method, uri, extraHeaders, data);
	}

	public void removeCurrentProxy() {
		lock.writeLock().lock();
		if (proxypool.size() > 300) {
			proxypool.remove(proxy.getHostName() + ":" + proxy.getPort());
			if (proxypool.isEmpty()) {
				reloadProxyList();
			} else {
				System.out.println("remain:" + proxypool.size());
			}
		}
		lock.writeLock().unlock();
		proxy = null;
	}

	/**
	 * 该方法必须在写锁开启的情况下使用
	 */
	protected void reloadProxyList() {
		MongoClient mongo = null;
		try {
			mongo = new MongoClient(Params.MongoDB.PROXIES.host, Params.MongoDB.PROXIES.port);
		} catch (UnknownHostException e) {
			System.err.println("mongo proxies unreachable");
		}
		DBObject query = new BasicDBObject();
		if (_tag != null) {
			query.put("tag", _tag);
		}
		DBObject key = new BasicDBObject().append("_id", 1);
		DBCursor cursor = mongo.getDB(Params.MongoDB.PROXIES.database).getCollection("proxies").find(query,
				key);
		for (DBObject o : cursor) {
			proxypool.add((String) o.get("_id"));
		}
		mongo.close();
		if (proxypool.isEmpty()) {
			throw new RuntimeException("no useful proxy in mongo");
		}
		System.out.println("load proxy list form db, count: " + proxypool.size());
	}

	public static void main(String[] args) {
		ProxiesHttpClient client1 = new ProxiesHttpClient("CN");
		BasicHttpResponse r = client1.get("http://localhost:8080");
		if (r == null) {
			client1.removeCurrentProxy();
		}
		client1.close();
	}
}
