/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.atom;

import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.construct.SimpleFlowConstruct;
import org.mule.module.atom.routing.EntryLastUpdatedFilter;
import org.mule.module.atom.routing.FeedLastUpdatedFilter;
import org.mule.module.atom.routing.FeedSplitter;
import org.mule.routing.MessageFilter;
import org.mule.tck.FunctionalTestCase;

import java.text.SimpleDateFormat;

public class NamespaceTestCase extends FunctionalTestCase
{
    @Override
    protected String getConfigResources()
    {
        return "namespace-config.xml";
    }

    public void testFlowConfig() throws Exception
    {
        SimpleFlowConstruct flowConstruct = muleContext.getRegistry().lookupObject("flowTest");
        assertNotNull(flowConstruct);
        assertTrue(flowConstruct.getMessageSource() instanceof InboundEndpoint);
        InboundEndpoint ep = ((InboundEndpoint)flowConstruct.getMessageSource());
        assertEquals(2, ep.getMessageProcessors().size());
        MessageProcessor mp = ep.getMessageProcessors().get(0);
        assertTrue(mp instanceof FeedSplitter);
        mp = ep.getMessageProcessors().get(1);
        assertTrue(mp instanceof MessageFilter);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        assertEquals(sdf.parse("2009-10-01"), ((EntryLastUpdatedFilter)((MessageFilter)mp).getFilter()).getLastUpdate());
    }

    public void testGlobalFilterConfig() throws Exception
    {
        FeedLastUpdatedFilter filter = muleContext.getRegistry().lookupObject("feedFilter");
        assertNotNull(filter);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        assertEquals(sdf.parse("2009-10-01 13:00:00"), filter.getLastUpdate());
        assertFalse(filter.isAcceptWithoutUpdateDate());
    }
}