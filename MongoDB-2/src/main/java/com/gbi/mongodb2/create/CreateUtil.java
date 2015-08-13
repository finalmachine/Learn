package com.gbi.mongodb2.create;

import java.net.UnknownHostException;

import com.gbi.commons.config.Params;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class CreateUtil {
	public static void createTestInfo() {
		MongoClient client = null;
		try {
			client = new MongoClient(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DBCollection collection = client.getDB(Params.MongoDB.TEST.database).getCollection("source");
		
		BasicDBObject people1 = new BasicDBObject();
		people1.put("_id", 1);
		people1.put("name", "张无忌");
		people1.put("age", 20);
		collection.save(people1);

		BasicDBObject people2 = new BasicDBObject();
		people2.put("_id", 2);
		people2.put("name", "韦一笑");
		people2.put("age", 20);
		collection.save(people2);

		BasicDBObject people3 = new BasicDBObject();
		people3.put("_id", 3);
		people3.put("name", "周芷若");
		people3.put("age", 20);
		collection.save(people3);
		
		client.close();
	}
	
	public static void addTestInfo_c() {
		MongoClient client = null;
		try {
			client = new MongoClient(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DBCollection collection = client.getDB(Params.MongoDB.TEST.database).getCollection("source_c");
		
		BasicDBObject people3 = new BasicDBObject();
		people3.put("_id", 4);
		people3.put("name", "尹志平");
		people3.put("age", 20);
		collection.save(people3);
		
		client.close();
	}
	
	public static void addTestInfo_cd() {
		MongoClient client = null;
		try {
			client = new MongoClient(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DBCollection collection = client.getDB(Params.MongoDB.TEST.database).getCollection("source_cd");
		
		BasicDBObject people3 = new BasicDBObject();
		people3.put("_id", 4);
		people3.put("name", "尹志平");
		people3.put("age", 20);
		System.out.println(collection.save(people3));
		
		client.close();
	}
	
	public static void main(String[] args) {
	//	createTestInfo();
	//	addTestInfo_c();
		addTestInfo_cd();
	}
}
