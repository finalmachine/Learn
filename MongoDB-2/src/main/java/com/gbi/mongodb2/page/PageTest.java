package com.gbi.mongodb2.page;



import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class PageTest {

	public static void test1() throws Exception {
		MongoClient client = new MongoClient();
		DBCollection collection = client.getDB("NAVISUS").getCollection("Payment_France");

		int pageSize = 10;
		DBObject order = new BasicDBObject();
		order.put("_id", 1);
		DBObject info = null;

		DBCursor cursor = collection.find().sort(order).limit(pageSize);
		while (cursor.hasNext()) {
			info = cursor.next();
			System.out.println(info.get("_id"));
		}
		cursor.close();
		System.out.println("-----------------");

		while (true) {
			DBObject query = new BasicDBObject();
			DBObject filter = new BasicDBObject();
			filter.put("$gt", info.get("_id"));
			query.put("_id", filter);
			try (DBCursor cur = collection.find(query).sort(order).limit(pageSize)) {
				if (cur.size() == 0) {
					break;
				}
				while (cur.hasNext()) {
					info = cur.next();
					System.out.println(info.get("_id"));
				}
			}
			System.out.println("-----------------");
		}
		client.close();
	}

	public static void main(String[] args) throws Exception {
		test1();
	}
}
