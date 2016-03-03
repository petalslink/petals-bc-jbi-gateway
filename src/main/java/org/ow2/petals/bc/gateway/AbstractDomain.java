/**
 * Copyright (c) 2016 Linagora
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
package org.ow2.petals.bc.gateway;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.petals.bc.gateway.messages.Transported;
import org.ow2.petals.bc.gateway.messages.TransportedException;
import org.ow2.petals.bc.gateway.messages.TransportedLastMessage;
import org.ow2.petals.bc.gateway.messages.TransportedMessage;
import org.ow2.petals.bc.gateway.messages.TransportedMiddleMessage;
import org.ow2.petals.bc.gateway.messages.TransportedNewMessage;
import org.ow2.petals.bc.gateway.messages.TransportedTimeout;
import org.ow2.petals.component.framework.api.message.Exchange;

import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractDomain {

    protected final Logger logger;

    private final JBISender sender;

    /**
     * Exchange is added
     */
    private final ConcurrentMap<String, Exchange> exchangesInProgress = new ConcurrentHashMap<>();

    public AbstractDomain(final JBISender sender, final Logger logger) {
        this.sender = sender;
        this.logger = logger;
    }

    public void sendFromChannelToNMR(final ChannelHandlerContext ctx, final TransportedMessage m) {
        final String exchangeId = m.exchangeId;

        final Exchange exchange;
        if (m instanceof TransportedNewMessage) {
            exchange = null;
        } else {
            // we remove it now, if it has to come back (normally for InOptOut fault after out) it will be put back
            exchange = exchangesInProgress.remove(exchangeId);
            assert exchange != null;
        }

        this.sender.sendToNMR(getContext(this, ctx, m), exchange);
    }

    private static DomainContext getContext(final AbstractDomain domain, final ChannelHandlerContext ctx,
            final TransportedMessage m) {
        return new DomainContext() {
            @Override
            public void sendToChannel(final Exchange exchange) {
                domain.sendFromNMRToChannel(ctx, m, exchange);
            }

            @Override
            public void sendToChannel(final Exception e) {
                domain.sendFromNMRToChannel(ctx, m, e);
            }

            @Override
            public void sendTimeoutToChannel() {
                domain.sendTimeoutFromNMRToChannel(ctx, m);
            }

            @Override
            public TransportedMessage getMessage() {
                return m;
            }
        };
    }

    private void sendFromNMRToChannel(final ChannelHandlerContext ctx, final TransportedMessage m,
            final Exception e) {
        logger.log(Level.FINE, "Exception caught", e);
        if (m instanceof TransportedLastMessage) {
            ctx.writeAndFlush(new TransportedException(e), ctx.voidPromise());
        } else {
            m.exchange.setError(e);
            ctx.writeAndFlush(new TransportedLastMessage(m, m.exchange), ctx.voidPromise());
        }
    }

    private void sendFromNMRToChannel(final ChannelHandlerContext ctx, final TransportedMessage m,
            final Exchange exchange) {
        assert !(m instanceof TransportedLastMessage);
        if (exchange.isActiveStatus()) {
            // we will be expecting an answer
            sendToChannel(ctx, new TransportedMiddleMessage(m, exchange.getMessageExchange()), exchange);
        } else {
            sendToChannel(ctx, new TransportedLastMessage(m, exchange.getMessageExchange()), exchange);
        }
    }

    private void sendTimeoutFromNMRToChannel(final ChannelHandlerContext ctx, final TransportedMessage m) {
        if (exchangesInProgress.remove(m.exchangeId) == null) {
            this.logger.severe("Tried to remove " + m.exchangeId + " becuse of timeout, but nothing was removed");
        }
        sendToChannel(ctx, new TransportedTimeout(m));
    }

    protected void sendToChannel(final ChannelHandlerContext ctx, final TransportedMessage m, final Exchange exchange) {
        if (!(m instanceof TransportedLastMessage)) {
            if (exchangesInProgress.putIfAbsent(m.exchangeId, exchange) != null) {
                // this can happen with InOptOut from the consumer partner point of view, but the Exchange is always the
                // same normally!
            }
        }
        sendToChannel(ctx, m);
    }

    private void sendToChannel(final ChannelHandlerContext ctx, final Transported m) {
        // TODO couldn't make exceptions be logged by the channel without using this voidPromise...
        ctx.writeAndFlush(m, ctx.voidPromise());
    }

    public void timeoutReceived(final TransportedTimeout m) {
        if (exchangesInProgress.remove(m.exchangeId) == null) {
            // TODO log
        }
    }
}
