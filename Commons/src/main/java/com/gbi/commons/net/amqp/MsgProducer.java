package com.gbi.commons.net.amqp;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.SerializationUtils;

import com.rabbitmq.client.MessageProperties;

public final class MsgProducer<T extends Serializable> extends MsgBase {

	public MsgProducer(String queueName) throws IOException, TimeoutException {
		super(queueName);
	}

	public MsgProducer(String queueName, String host, int port, String username, String password,
			String virtualHost) throws IOException, TimeoutException {
		super(queueName, null, host, port, username, password, virtualHost);
	}

	public void send(T object) {
		try {
			_channel.basicPublish("", _queueName, MessageProperties.PERSISTENT_TEXT_PLAIN,
					SerializationUtils.serialize(object));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
