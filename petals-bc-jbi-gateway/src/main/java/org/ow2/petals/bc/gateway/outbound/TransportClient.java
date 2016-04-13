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
package org.ow2.petals.bc.gateway.outbound;

import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;
import org.ow2.petals.bc.gateway.messages.Transported.TransportedToConsumer;
import org.ow2.petals.bc.gateway.messages.TransportedForExchange;
import org.ow2.petals.bc.gateway.messages.TransportedPropagatedConsumes;
import org.ow2.petals.commons.log.Level;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * TODO detect when the connection was closed properly ({@link #channelInactive(ChannelHandlerContext)}?) and when there
 * was a problem and we maybe need reconnect ({@link #exceptionCaught(ChannelHandlerContext, Throwable)}?)
 * 
 * @author vnoel
 *
 */
public class TransportClient extends SimpleChannelInboundHandler<TransportedToConsumer> {

    private final ProviderDomain pd;

    private final Logger logger;

    public TransportClient(final Logger logger, final ProviderDomain pd) {
        this.logger = logger;
        this.pd = pd;
    }

    @Override
    public void exceptionCaught(final @Nullable ChannelHandlerContext ctx, final @Nullable Throwable cause)
            throws Exception {
        logger.log(Level.WARNING, "Exception caught", cause);
    }

    @Override
    protected void channelRead0(final @Nullable ChannelHandlerContext ctx, final @Nullable TransportedToConsumer msg)
            throws Exception {
        assert ctx != null;
        assert msg != null;

        if (msg instanceof TransportedForExchange) {
            pd.receiveFromChannel(ctx, (TransportedForExchange) msg);
        } else if (msg instanceof TransportedPropagatedConsumes) {
            pd.updatePropagatedServices((TransportedPropagatedConsumes) msg);
        } else {
            throw new IllegalArgumentException("Impossible case");
        }
    }

    @Override
    public void channelInactive(final @Nullable ChannelHandlerContext ctx) throws Exception {
        pd.close();
    }
}
