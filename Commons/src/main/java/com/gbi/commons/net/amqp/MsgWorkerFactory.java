package com.gbi.commons.net.amqp;

import java.io.Serializable;

@FunctionalInterface
public interface MsgWorkerFactory<T extends Serializable> {
	public MsgWorker<T> newWorker();
}
