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

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.servicedesc.ServiceEndpoint;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ow2.easywsdl.wsdl.api.abstractItf.AbsItfOperation.MEPPatternConstants;
import org.ow2.petals.bc.gateway.utils.JbiGatewayJBIHelper;
import org.ow2.petals.component.framework.junit.helpers.MessageChecks;
import org.ow2.petals.component.framework.junit.helpers.ServiceProviderImplementation;

import com.jayway.awaitility.core.ConditionTimeoutException;

public class JBIGatewaySSLTest extends AbstractComponentTest {

    private static final String CLIENT_CRT = "/ssl/test.crt";

    private static final String SERVER_CRT = "/ssl/test2.crt";

    private static final String CLIENT_KEY = "/ssl/test_unencrypted.pem";

    private static final String SERVER_KEY = "/ssl/test2_unencrypted.pem";

    @BeforeClass
    public static void checkFiles() {
        assertTrue(JbiGatewayJBIHelper.getFile(CLIENT_CRT).exists());
        assertTrue(JbiGatewayJBIHelper.getFile(CLIENT_KEY).exists());
        assertTrue(JbiGatewayJBIHelper.getFile(SERVER_CRT).exists());
        assertTrue(JbiGatewayJBIHelper.getFile(SERVER_KEY).exists());
    }

    @Test
    public void testOk() throws Exception {
        test(CLIENT_CRT, CLIENT_KEY, SERVER_CRT, SERVER_CRT, SERVER_KEY, CLIENT_CRT);
    }

    @Test
    public void testOkNoClientCert() throws Exception {
        test(null, null, SERVER_CRT, SERVER_CRT, SERVER_KEY, null);
    }

    @Test(expected = ConditionTimeoutException.class)
    public void testNOkNoClientCert() throws Exception {
        test(null, null, SERVER_CRT, SERVER_CRT, SERVER_KEY, CLIENT_CRT);
    }

    @Test(expected = ConditionTimeoutException.class)
    public void testNOk() throws Exception {
        test(null, null, null, SERVER_CRT, SERVER_KEY, null);
    }

    public void test(final @Nullable String clientCertificate, final @Nullable String clientKey,
            final @Nullable String clientRemoteCertificate, final @Nullable String serverCertificate,
            final @Nullable String serverKey, final @Nullable String serverRemoteCertificate) throws Exception {

        final ServiceEndpoint endpoint = deployTwoDomains(true, true, clientCertificate, clientKey,
                clientRemoteCertificate, serverCertificate, serverKey, serverRemoteCertificate);

        COMPONENT.sendAndCheckResponseAndSendStatus(helloRequest(endpoint, MEPPatternConstants.IN_OUT.value()),
                ServiceProviderImplementation.outMessage(OUT),
                MessageChecks.hasOut().andThen(MessageChecks.hasXmlContent(OUT)), ExchangeStatus.DONE);
    }

}
