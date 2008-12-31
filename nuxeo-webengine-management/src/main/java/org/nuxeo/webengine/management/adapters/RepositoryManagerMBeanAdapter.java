package org.nuxeo.webengine.management.adapters;

/*
 * (C) Copyright 2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     Stephane Lacoin (Nuxeo EP Software Engineer)
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.model.Repository;
import org.nuxeo.ecm.core.repository.RepositoryManager;
import org.nuxeo.ecm.core.repository.RepositoryService;
import org.nuxeo.runtime.api.Framework;

/**
 * An MBean to manage repositories.
 * 
 * @author Stephane Lacoin (Nuxeo EP Software Engineer)
 */
public class RepositoryManagerMBeanAdapter implements RepositoryMBean {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(RepositoryManagerMBeanAdapter.class);

    protected RepositoryManager guardedRepositoryManager() {
        RepositoryService service = null;
        try {
            service = (RepositoryService) Framework.getRuntime().getComponent(
                    RepositoryService.NAME);
        } catch (Exception cause) {
            throw new ClientRuntimeException("Cannot get repository service",
                    cause);
        }
        if (service == null) {
            throw new ClientRuntimeException("Cannot get repository service");
        }
        try {
            return service.getRepositoryManager();
        } catch (Exception cause) {
            throw new ClientRuntimeException("Cannot get repository manager",
                    cause);
        }
    }

    protected Repository guardedRepository(RepositoryManager manager,
            String name) {
        try {
            return manager.getRepository(name);
        } catch (Exception cause) {
            throw new ClientRuntimeException("Cannot get repository manager",
                    cause);
        }
    }

    protected Collection<Repository> guardedRepositories() {
        Set<Repository> repositories = new HashSet<Repository>();
        RepositoryManager manager = guardedRepositoryManager();
        for (String name : manager.getRepositoryNames()) {
            repositories.add(guardedRepository(manager, name));
        }
        return repositories;
    }

    protected interface CounterExtractor {
        Integer getCounter(Repository repository);
    }

    protected Integer doSummarizeCounters(CounterExtractor extractor) {
        Integer counter = 0;
        for (Repository repository : guardedRepositories()) {
            counter += extractor.getCounter(repository);
        }
        return counter;
    }

    public Integer getActiveSessionsCount() {
        return doSummarizeCounters(new CounterExtractor() {
            public Integer getCounter(Repository repository) {
                return repository.getActiveSessionsCount();
            }
        });
    }

    public Integer getClosedSessionsCount() {
        return doSummarizeCounters(new CounterExtractor() {
            public Integer getCounter(Repository repository) {
                return repository.getClosedSessionsCount();
            }
        });
    }

    public Integer getStartedSessionsCount() {
        return doSummarizeCounters(new CounterExtractor() {
            public Integer getCounter(Repository repository) {
                return repository.getStartedSessionsCount();
            }
        });
    }

}
