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
 *     bstefanescu
 *
 * $Id$
 */

package org.nuxeo.ecm.webengine.config;



/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public abstract class ExtensibleContribution extends Contribution {

    protected ExtensibleContribution baseContribution;
    protected String baseContributionId;


    /**
     * Copy this contribution data over the given one.
     * <p>
     * Warn that the copy must be done deeply - you should clone every element in any collection you have
     * This is to avoid merging data you copy into the base contribution and breaking subsequent merging operations
     * <p>
     * The baseContributionId and contributionId fields should not be copied since their are copied
     * by the base classes implementation
     *
     */
    protected abstract void copyOver(ExtensibleContribution contrib);


    /**
     * @return the baseContributionId.
     */
    public String getBaseContributionId() {
        return baseContributionId;
    }

    /**
     * @param baseContribution the baseContribution to set.
     */
    public void setBaseContribution(ExtensibleContribution baseContribution) {
        this.baseContribution = baseContribution;
    }

    /**
     * @param baseContributionId the baseContributionId to set.
     */
    public void setBaseContributionId(String baseContributionId) {
        this.baseContributionId = baseContributionId;
    }

    @Override
    public void resolve(ContributionManager mgr) {
        if (baseContributionId != null) {
            baseContribution = (ExtensibleContribution)mgr.getResolved(baseContributionId);
        }
    }

    @Override
    public void unresolve(ContributionManager mgr) {
        baseContribution = null;
    }

    /**
     * @return the base.
     */
    public ExtensibleContribution getBaseContribution() {
        return baseContribution;
    }

    public ExtensibleContribution getRootContribution() {
        return baseContribution == null ? this : baseContribution.getRootContribution();
    }

    public boolean isRootContribution() {
        return baseContribution == null;
    }

    protected ExtensibleContribution getMergedContribution() throws Exception {
        if (baseContribution == null) {
            return this.clone();
        }
        ExtensibleContribution mc = baseContribution.getMergedContribution();
        copyOver(mc);
        mc.contributionId = contributionId;
        mc.baseContributionId = baseContributionId;
        return mc;
    }

    @Override
    public void install(ManagedComponent comp) throws Exception {
        install(comp, getMergedContribution());
    }

    @Override
    public void uninstall(ManagedComponent comp) throws Exception {
        uninstall(comp, getMergedContribution());
    }


    /**
     * perform a deep clone to void sharing collection elements between clones
     */
    @Override
    public ExtensibleContribution clone() throws CloneNotSupportedException {
        try {
            ExtensibleContribution clone = this.getClass().newInstance();
            copyOver(clone);
            clone.contributionId = contributionId;
            clone.baseContributionId = baseContributionId;
            return clone;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CloneNotSupportedException("Failed to instantiate the contribution class. Contribution classes must have a trivial constructor");
        }
    }

}
