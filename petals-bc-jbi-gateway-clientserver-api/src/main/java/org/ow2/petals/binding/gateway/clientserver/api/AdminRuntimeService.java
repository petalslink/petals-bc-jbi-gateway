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
package org.ow2.petals.binding.gateway.clientserver.api;

import org.ow2.petals.basisapi.exception.PetalsException;

/**
 * Client/Server interface of the service 'Runtime Administration' part dedicated to the BC Gateway
 * 
 * @author Christophe DENEUX - Linagora
 * 
 */
public interface AdminRuntimeService extends AdminService {

    /**
     * Repropagate endpoints to client domains.
     * 
     * @throws PetalsException
     *             An error occurs during refresh
     */
    public void refreshPropagations() throws PetalsException;

}