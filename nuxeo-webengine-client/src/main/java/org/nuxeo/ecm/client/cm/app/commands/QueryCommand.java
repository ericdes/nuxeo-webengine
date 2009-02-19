/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     matic
 */
package org.nuxeo.ecm.client.cm.app.commands;

import org.nuxeo.ecm.client.cm.DocumentEntry;

/**
 * @author matic
 * 
 */
public class QueryCommand extends AbstractCommand<DocumentEntry[]> {

    public final String repositoryId;

    public QueryCommand(String repositoryId) {
        super("discovery", "query");
        this.repositoryId = repositoryId;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public String formatURL(String baseURL) {
        return baseURL + "/" + repositoryId;
    }
}
