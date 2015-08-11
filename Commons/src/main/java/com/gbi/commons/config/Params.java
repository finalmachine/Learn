package com.gbi.commons.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Params {

	public static Logger log = LoggerFactory.getLogger("");

	public enum MongoDB {
		NAVISUS("127.0.0.1", 27017, "NAVISUS"),
		PROXIES("127.0.0.1", 27017, "PROXIES"),
		TEST("127.0.0.1", 27017, "TEST");
		public String host;
		public int port;
		public String database;
		
		MongoDB(String host, int port, String database) {
			this.host = host;
			this.port = port;
			this.database = database;
		}
	}

	public enum StableProxy {
		ZHITAO("192.168.0.116", 1080);
		
		public String host;
		public int port;
		
		StableProxy(String host, int port) {
			this.host = host;
			this.port = port;
		}
	}
	
	public enum MQ {
		Company("192.168.0.242", 15672),
		LOCAL("127.0.0.1", 5672);
		
		public String host;
		public int port;
		
		private MQ(String host, int port) {
			this.host = host;
			this.port = port;
		}
	}


}
