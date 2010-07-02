/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.endpoint.inbound;

import org.mule.RequestContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.security.EndpointSecurityFilter;
import org.mule.api.security.SecurityException;
import org.mule.api.transport.MessageReceiver;
import org.mule.context.notification.SecurityNotification;
import org.mule.processor.AbstractInterceptingMessageProcessor;
import org.mule.transport.AbstractConnector;

/**
 * Filters the inbound flow using the {@link EndpointSecurityFilter} configured on
 * the endpoint. If unauthorised the inbound flow is stopped and response message has
 * securiy exception message as it's payload. A {@link SecurityNotification} is also
 * published.
 */
public class InboundSecurityFilterMessageProcessor extends AbstractInterceptingMessageProcessor
{
    
    protected InboundEndpoint endpoint;

    public InboundSecurityFilterMessageProcessor(InboundEndpoint endpoint)
    {
        this.endpoint = endpoint;
    }
    
    public MuleEvent process(MuleEvent event) throws MuleException
    {
        AbstractConnector connector = (AbstractConnector) endpoint.getConnector();
        MessageReceiver receiver = ((AbstractConnector) connector).getReceiver(event.getFlowConstruct(), endpoint);
        MuleEvent resultEvent = null;

        // Apply Security filter if one is set
        boolean authorised = false;
        if (endpoint.getSecurityFilter() != null)
        {
            try
            {
                endpoint.getSecurityFilter().authenticate(event);
                authorised = true;
            }
            catch (SecurityException e)
            {
                logger.warn("Request was made but was not authenticated: " + e.getMessage(), e);
                ((AbstractConnector) endpoint.getConnector()).fireNotification(new SecurityNotification(e,
                    SecurityNotification.SECURITY_AUTHENTICATION_FAILED));
                connector.handleException(e, receiver);
                resultEvent = RequestContext.getEvent();
                resultEvent.getMessage().setPayload(e.getLocalizedMessage());
            }
        }
        else
        {
            authorised = true;
        }

        if (authorised)
        {
            resultEvent = processNext(event);
        }
        return resultEvent;
    }
}
