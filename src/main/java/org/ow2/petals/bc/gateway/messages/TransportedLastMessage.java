/**
 * Copyright (c) 2015-2016 Linagora
 * 
 * This program/library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This program/library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program/library; If not, see <http://www.gnu.org/licenses/>
 * for the GNU Lesser General Public License version 2.1.
 */
package org.ow2.petals.bc.gateway.messages;

import javax.jbi.messaging.MessageExchange;

public class TransportedLastMessage extends TransportedMessage {

    private static final long serialVersionUID = 8953552087309338043L;

    public final MessageExchange receiverExchange;

    public TransportedLastMessage(final TransportedMiddleMessage m) {
        this(m.service, m.senderExchange);
    }

    public TransportedLastMessage(final TransportedNewMessage m) {
        this(m.service, m.senderExchange);
    }

    public TransportedLastMessage(final ServiceKey service, final MessageExchange receiverExchange) {
        super(service);
        this.receiverExchange = receiverExchange;
    }
}