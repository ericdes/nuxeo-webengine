/*
 * Copyright 2009 Nuxeo SA <http://nuxeo.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors:
 *     Florent Guillaume
 */
package org.nuxeo.ecm.webengine.cmis.adapters;

import java.util.Date;
import java.util.List;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Content;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Person;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.chemistry.ObjectEntry;
import org.apache.chemistry.SPI;
import org.apache.chemistry.repository.Repository;

/**
 * CMIS Collection for the children of a Folder. 
 * 
 * @author Bogdan Stefanescu 
 * 
 */
public class ObjectDescendantsCollection extends ObjectRootedCollection {

    public ObjectDescendantsCollection(String name, 
            Repository repository) {
        super(name, "Descendants", repository);
    }

    @Override
    protected void createFeedLinks(Feed feed, RequestContext request) throws ResponseContextException {
        super.createFeedLinks(feed, request);
        String objectId = getObjectId(request);
        feed.addLink(getDescendantsLink(objectId, request), "self");        
    }


    /** Feed operations **/ 
    
    @Override
    public Iterable<ObjectEntry> getEntries(RequestContext request)
            throws ResponseContextException {
        SPI spi = getConnection(request).getSPI();
        List<ObjectEntry> children = spi.getDescendants(getObjectId(request), null, -1, null, false,
                false, null);
        return children;
    }
    
    @Override
    public ObjectEntry postEntry(String title, IRI id, String summary,
            Date updated, List<Person> authors, Content content,
            RequestContext request) throws ResponseContextException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putEntry(ObjectEntry object, String title, Date updated,
            List<Person> authors, String summary, Content content,
            RequestContext request) throws ResponseContextException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteEntry(String resourceName, RequestContext request)
            throws ResponseContextException {
        throw new UnsupportedOperationException();
    }


}