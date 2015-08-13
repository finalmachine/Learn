package com.gbi.mongodb2.find;

import java.net.UnknownHostException;
import java.util.Scanner;

import com.gbi.commons.config.Params;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class FindTest {

	public static void findTest1() {
		MongoClient client = null;
		try {
			client = new MongoClient(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DB database = client.getDB("123");//sParams.MongoDB.TEST.database
		DBCollection collection = database.getCollection("source");

		DBObject ref = new BasicDBObject();
		DBObject key = new BasicDBObject();
		key.put("_id", 1);
		
		DBCursor cursor = collection.find(ref, key);
		while (cursor.hasNext()) {
			DBObject dbObject = (DBObject) cursor.next();
			System.out.println(dbObject);
		}

		cursor = collection.find(ref, key);
		while (cursor.hasNext()) {
			DBObject dbObject = (DBObject) cursor.next();
			System.out.println(dbObject);
		}
		cursor.close();
		
		client.close();
	}
	
	public static void findTest2() {
		MongoClient client = null;
		try {
			client = new MongoClient("192.168.0.242", 27001);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DB database = client.getDB("core");
		DBCollection collection = database.getCollection("institution_basic");
		
		DBObject query = new BasicDBObject();
		query.put("_id", 92015582);

		DBCursor cursor = collection.find(query);
		System.out.println(cursor.count());
		System.out.println(cursor.size());

		client.close();
	}
	
	public static void findAndwrite() {
		MongoClient client = null;
		try {
			client = new MongoClient("192.168.0.252", 27017);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DB database = client.getDB("Metrix");
		DBCollection collection1 = database.getCollection("PapersPubmedGrab");
		DBCollection collection2 = database.getCollection("PapersPubmedGrab.use");

		DBObject query = new BasicDBObject();
	//	query.put("content.Publish_Year", "2015");
		DBObject keys = new BasicDBObject();
		keys.put("_id", 1);
		keys.put("content.authorList", 1);
		DBObject orderBy = new BasicDBObject();
		orderBy.put("_id", -1);

		DBCursor cursor = collection1.find(query, keys).sort(orderBy).limit(100000);
		System.out.println(cursor.size());
		int i = 0;
		while (cursor.hasNext()) {
			BasicDBObject element = (BasicDBObject)cursor.next();
			DBObject insert = new BasicDBObject();
			insert.put("_id", element.get("_id"));
			insert.put("authorList", ((DBObject) element.get("content")).get("authorList"));
			System.out.println(element);
			collection2.insert(insert);
		//	collection2.insert(element);
			System.out.println(++i);
		}
		client.close();
	}

	public static void main(String[] args) {
		findTest1();
	}
}
