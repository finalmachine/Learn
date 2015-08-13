package com.gbi.mongodb2.compare;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class DBCollectionComparator {
	public static boolean compareTwoCollection(String host1, int port1, String db1, String collection1,
			String host2, int port2, String db2, String collection2) {
		DBCursor c1 = null, c2 = null; 
		try {
			c1 = new MongoClient(host1, port1).getDB(db1).getCollection(collection1).find().sort(new BasicDBObject("_id", 1));
			c2 = new MongoClient(host2, port2).getDB(db2).getCollection(collection2).find().sort(new BasicDBObject("_id", 1));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		while (c1.hasNext() && c2.hasNext()) {
			DBObject o1 = c1.next();
			DBObject o2 = c2.next();
			if (!o1.equals(o2)) {
				System.out.println(o1);
				System.out.println(o2);
				return false;
			}
		}
		if (c1.hasNext() == c2.hasNext()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(compareTwoCollection("127.0.0.1", 27017, "TEST", "proxy", "127.0.0.1", 27017, "TEST", "proxy_5"));
	}
}
