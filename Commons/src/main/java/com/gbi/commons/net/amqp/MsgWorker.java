package com.gbi.commons.net.amqp;

import java.io.Serializable;

@FunctionalInterface
public interface MsgWorker<T extends Serializable> {
	/**
	 * @return true if the work is done correctly
	 */
	public boolean work(T message) throws Exception;
}