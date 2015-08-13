package com.gbi.mongodb2.update;

import java.net.UnknownHostException;

import com.gbi.commons.config.Params;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class UpdateUtil {
	
	public static void updateSomeArgs() {
		MongoClient client = null;
		try {
			client = new MongoClient(Params.MongoDB.TEST.host, Params.MongoDB.TEST.port);
		} catch (UnknownHostException e) {
			System.err.println("can't connect to the server");
			e.printStackTrace();
			return;
		}
		
		DBCollection collection = client.getDB(Params.MongoDB.TEST.database).getCollection("source_u");
		
		BasicDBObject people1 = new BasicDBObject();
		people1.put("_id", 1);
		people1.put("name", "张无忌");
		people1.put("age", 20);
		people1.put("gender", "male");
		people1.put("level", 3);
		collection.save(people1);

		BasicDBObject people2 = new BasicDBObject();
		people2.put("_id", 2);
		people2.put("name", "韦一笑");
		collection.save(people2);

		BasicDBObject people3 = new BasicDBObject();
		people3.put("_id", 3);
		people3.put("name", "周芷若");
		people3.put("age", 22);
		collection.save(people3);
		
		client.close();
	}
	
	public static void main(String[] args) {
		updateSomeArgs();
	}
}
