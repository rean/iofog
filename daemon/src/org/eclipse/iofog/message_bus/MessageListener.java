/*******************************************************************************
 * Copyright (c) 2016, 2017 Iotracks, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Saeid Baghbidi
 * Kilton Hopkins
 *  Ashita Nagar
 *******************************************************************************/
package org.eclipse.iofog.message_bus;

import org.eclipse.iofog.local_api.MessageCallback;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.MessageHandler;

/**
 * listener for real-time receiving
 * 
 * @author saeid
 *
 */
public class MessageListener implements MessageHandler{
	private final MessageCallback callback;
	
	public MessageListener(MessageCallback callback) {
		this.callback = callback;
	}
	
	@Override
	public void onMessage(ClientMessage msg) {
		try {
			msg.acknowledge();
		} catch (Exception e) {}
		
		Message message = new Message(msg.getBytesProperty("message"));
		callback.sendRealtimeMessage(message);
	}

}
