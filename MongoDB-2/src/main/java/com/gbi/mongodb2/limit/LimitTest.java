package com.gbi.mongodb2.limit;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class LimitTest {
	
	private static void limitTest() {
		MongoClient client = null;
		try {
			client = new MongoClient("127.0.0.1", 27017);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DB database = client.getDB("test");
		DBCollection collection = database.getCollection("test_table1");
		
		DBCursor cursor = collection.find().skip(0).limit(1);
		System.out.println(cursor.size());
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			System.out.println(obj);
		}
		
		client.close();
	}
	
	public static void main(String[] args) {
		limitTest();
	}
}
