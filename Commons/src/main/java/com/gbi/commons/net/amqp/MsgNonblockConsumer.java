package com.gbi.commons.net.amqp;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.SerializationUtils;

import com.rabbitmq.client.GetResponse;

public class MsgNonblockConsumer extends MsgBase implements Runnable {

	private MsgWorker<? extends Serializable> _worker = null;

	public <T extends Serializable> MsgNonblockConsumer(String queueName, MsgWorker<T> worker) throws IOException,
			TimeoutException {
		super(queueName);
		_channel.basicQos(1);
		_worker = worker;
	}

	public <T extends Serializable> MsgNonblockConsumer(String queueName, MsgWorker<T> worker, String host,
			int port, String username, String password, String virtualHost) throws IOException,
			TimeoutException {
		super(queueName, null, host, port, username, password, virtualHost);
		_channel.basicQos(1);
		_worker = worker;
	}

	@Override
	public void run() {
		while (true) {
			try {
				GetResponse response = _channel.basicGet(_queueName, false);
				if (response == null) {
					close();
					break;
				} else if (response.getBody() != null) {
					boolean reslut = _worker.work(SerializationUtils.deserialize(response.getBody()));
					System.out.println(reslut);
					_channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
				} else {
					System.out.println("null");
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
	}

}
