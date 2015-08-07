package com.gbi.commons.net.amqp;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.SerializationUtils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class MsgConsumers {
	
	final class ConConsumer extends MsgBase implements Consumer {

		private MsgWorker<? extends Serializable> _worker = null;
		
		public <T extends Serializable> ConConsumer(String queueName, ExecutorService pool, MsgWorker<T> worker) throws IOException,
				TimeoutException {
			super(queueName, pool);
			_channel.basicQos(1);
			_worker = worker;
		}

		public <T extends Serializable> ConConsumer(String queueName, ExecutorService pool, MsgWorker<T> worker, String host,
				int port, String username, String password, String virtualHost) throws IOException,
				TimeoutException {
			super(queueName, pool, host, port, username, password, virtualHost);
			_channel.basicQos(1);
			_worker = worker;
		}
		
		public void register() {
			try {
				_channel.basicConsume(_queueName, false, this);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void handleConsumeOk(String consumerTag) {
			System.out.println("Consumer:" + consumerTag + ":on>");
		}

		@Override
		public void handleCancelOk(String consumerTag) {
			System.out.println("handleCancelOk");
		}

		@Override
		public void handleCancel(String consumerTag) throws IOException {
			System.out.println("handleCancel");
		}

		@Override
		public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
				byte[] body) throws IOException {
			boolean result;
			try {
				result = _worker.work(SerializationUtils.deserialize(body));
				System.out.println(result);
			} catch (Exception e) {
				e.printStackTrace();
				_channel.basicRecover(true);
				return;
			}
			if (result) {
				_channel.basicAck(envelope.getDeliveryTag(), false);
				System.out.println("return");
				return;
			}
			_channel.basicRecover(true);
		}

		@Override
		public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
			System.out.println("Consumer:" + consumerTag + ":off>");
		}

		@Override
		public void handleRecoverOk(String consumerTag) {
			System.out.println("handleRecoverOk");
		}
	}

	private ExecutorService _threadPool;
	private List<ConConsumer> _consumers = new ArrayList<>();
	private MsgQueueMonitor _monitor;

	public <T extends Serializable> MsgConsumers(String queueName, int size, MsgWorkerFactory<T> factory) {
		if (size <= 0) {
			throw new IllegalArgumentException("size must over 0");
		}
		_threadPool = Executors.newFixedThreadPool(size);
		_monitor = new MsgQueueMonitor("/", queueName);
		try {
			for (int i = 0; i < size; ++i) {
				ConConsumer c = new ConConsumer(queueName, _threadPool, factory.newWorker());
				_consumers.add(c);
				_monitor.addSubject(c);
			}
		} catch (IOException | TimeoutException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void run() {
		// commit the channels >
		for (ConConsumer consumer : _consumers) {
			consumer.register();
		}
		// commit the channels <
		// wait for the tasks finish >
		_monitor.checkFinish();
		// wait for the tasks finish <
		_threadPool.shutdown();
	}
}
