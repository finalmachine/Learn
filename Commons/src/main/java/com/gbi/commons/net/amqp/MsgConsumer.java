package com.gbi.commons.net.amqp;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONObject;

import com.gbi.commons.net.http.BasicHttpClient;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public final class MsgConsumer extends MsgBase implements Runnable, Consumer {

	private MsgWorker<? extends Serializable> _worker = null;

	public <T extends Serializable> MsgConsumer(String queueName, MsgWorker<T> worker) throws IOException,
			TimeoutException {
		super(queueName);
		_channel.basicQos(1);
		_worker = worker;
	}

	public <T extends Serializable> MsgConsumer(String queueName, MsgWorker<T> worker, String host,
			int port, String username, String password, String virtualHost) throws IOException,
			TimeoutException {
		super(queueName, null, host, port, username, password, virtualHost);
		_channel.basicQos(1);
		_worker = worker;
	}

	@Override
	public void run() {
		BasicHttpClient client = new BasicHttpClient();
		Map<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders.put("Authorization", "Basic Z3Vlc3Q6Z3Vlc3Q=");
		
		try {
			String url = "http://localhost:15672/api/queues/" + URLEncoder.encode("/", "UTF-8") + "/"
					+ URLEncoder.encode(_queueName, "UTF-8");
			_channel.basicConsume(_queueName, false, this);
			while (true) {
				JSONObject json = new JSONObject(new String(client.get(url, extraHeaders).getContent()));
				if (json.getInt("messages") == 0) {
					close();
					break;
				}
				Thread.sleep(30000);
			}
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			client.close();
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
		} catch (Exception e) {
			e.printStackTrace();
			_channel.basicRecover(true);
			return;
		}
		if (result) {
			_channel.basicAck(envelope.getDeliveryTag(), false);
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
