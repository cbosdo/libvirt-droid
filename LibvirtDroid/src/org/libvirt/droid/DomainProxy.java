package org.libvirt.droid;

import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;

/**
 * This class is only about caching some of the Domain data to
 * avoid having to fetch them in the UI thread.
 *
 * @author cbosdo
 *
 */
public class DomainProxy {

    private Domain mDomain;
    private String mName;
    private DomainInfo mInfo;

    public DomainProxy(Domain dom) throws LibvirtException {
        mDomain = dom;
        mName = dom.getName();
        mInfo = dom.getInfo();
    }

    String getName() {
        return mName;
    }

    DomainInfo getInfo() {
        return mInfo;
    }

    /**
     * Get the direct domain object.
     *
     * Pay attention, when using it as it has nothing cached.
     */
    Domain getHandle() {
        return mDomain;
    }
}
