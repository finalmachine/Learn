package com.gbi.mongodb2.delete;

import java.net.UnknownHostException;

import com.gbi.commons.config.Params;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class DeleteUtil {
	
	public static void deleteOneTestData() {
		MongoClient client = null;
		try {
			client = new MongoClient(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DBCollection collection = client.getDB(Params.MongoDB.TEST.database).getCollection("source_d");
		collection.remove(new BasicDBObject("_id", 1));
		
		client.close();
	}
	
	public static void main(String[] args) {
		deleteOneTestData();
	}
}
