package com.gbi.commons.net.amqp;

import com.gbi.commons.net.amqp.MsgConsumer;
import com.gbi.commons.net.amqp.MsgProducer;
import com.gbi.commons.net.amqp.MsgWorker;

public class MessageTest {

	private static class MyWorker implements MsgWorker<String> {
		@Override
		public boolean work(String message) throws Exception {
			System.out.println(Thread.currentThread().getName() + ">" + message);
			Thread.sleep(500);
			return true;
		}
	}
	
	private static class MyWorkerFactory implements MsgWorkerFactory<String> {
		@Override
		public MsgWorker<String> newWorker() {
			return new MyWorker();
		}
	}
	
	public static void test1() throws Exception {
		MsgProducer<String> producer = new MsgProducer<>("MessageTest");
		for (int i = 0; i < 100; ++i) {
			producer.send("" + i);
		}
		producer.close();
		new Thread(new MsgConsumer("MessageTest", new MyWorker())).start();
		new Thread(new MsgConsumer("MessageTest", new MyWorker())).start();
		new Thread(new MsgConsumer("MessageTest", new MyWorker())).start();
	}

	public static void test2() throws Exception {
		MsgProducer<String> producer = new MsgProducer<>("MessageTest");
		for (int i = 0; i < 100; ++i) {
			producer.send("" + i);
		}
		producer.close();
		new MsgConsumers("MessageTest", 3, new MyWorkerFactory()).run();;
	}

	public static void main(String[] args) throws Exception {
		test2();
	}
}