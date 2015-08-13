package com.gbi.mongodb2.copy;

import java.net.UnknownHostException;

import com.gbi.commons.config.Params;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class CopyUtil {
	
	public static final String suffix = ".copy"; 
	
	public static void copyCollection(String collectionName) {
		MongoClient client = null;
		try {
			client = new MongoClient(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DB database = client.getDB(Params.MongoDB.TEST.database);
		DBCollection collection1 = database.getCollection(collectionName);
		DBCollection collection2 = database.getCollection(collectionName + suffix);
		
		collection2.drop();
		DBCursor cursor = collection1.find();
		while (cursor.hasNext()) {
			DBObject object = (DBObject) cursor.next();
			collection2.insert(object);
		}
		
		client.close();
		System.out.println("copy " + collectionName + " finished");
	}
	
	public static void copyCollection(String collectionFrom, String collectionTo) {
		MongoClient client = null;
		try {
			client = new MongoClient(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DB database = client.getDB(Params.MongoDB.TEST.database);
		DBCollection collection1 = database.getCollection(collectionFrom);
		DBCollection collection2 = database.getCollection(collectionTo);
		
		collection2.drop();
		DBCursor cursor = collection1.find();
		while (cursor.hasNext()) {
			DBObject object = (DBObject) cursor.next();
			collection2.insert(object);
		}
		
		client.close();
		System.out.println("copy " + collectionFrom + " finished");
	}
	
	public static void main(String[] args) {
	//	copyCollection("source", "source_cd");
	//	copyCollection("source.log", "source_cd.log");
		copyCollection("proxy");
	}
}
