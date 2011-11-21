package edu.uw.myplan.trng.course.util;

import javax.xml.namespace.QName;

import edu.uw.myplan.trng.course.dataobject.CourseSearchItem;
import edu.uw.myplan.trng.course.dataobject.FacetItem;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.organization.dto.OrgInfo;
import org.kuali.student.core.organization.service.OrganizationService;
import org.kuali.student.lum.course.dto.CourseInfo;

import java.util.HashMap;
import java.util.Map;

/**
 *  Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class CurriculumFacet extends AbstractFacet {

    private transient Map<String, String> subjectAreaCache;
    private transient OrganizationService organizationService;

    public CurriculumFacet() {
        super();
        subjectAreaCache = new HashMap<String, String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseInfo course, CourseSearchItem courseSearchItem) {

        String key = course.getSubjectArea();
        boolean isUnknown = false;

        //  If no subject area info was set then setup for an "Unknown" facet item.
        if (key == null || key.equals("")) {
            key = UNKNOWN_FACET_KEY;
        }

        //  If it's a new facet key then create a new FacetItem. Otherwise, just increment the count.
        if (checkIfNewFacetKey(key)) {
            // TODO: Use the Org Service to lookup curriculum name based on key.
            // For now just use the serviceArea code as the displayName.
            String displayName = null;
            if (isUnknown) {
                displayName = UNKNOWN_FACET_DISPLAY_NAME;
            } else {
                displayName = getOrganizationName(key);
            }
            FacetItem item = new FacetItem();
            item.setKey(key);
            item.setDisplayName(displayName);
            item.setCount(1);
            facetItems.add(item);
        }
        //  Code the item with the facet key.
        courseSearchItem.setCurriculumFacetKey(key);
    }

    /**
     * Finds the organization name based on the key.
     * @return
     */
    private String getOrganizationName(String key) {
        //  Prepare to simply return the key if the lookup fails.
        String organizationName = key;

        //  First look in the cache for the name.
        if (subjectAreaCache.containsKey(key)) {
            organizationName = subjectAreaCache.get(key);
        } else {
            //  TODO: FIXME: This doesn't current do the right thing. Waiting for Kamal and Virginia to work through org mapping.
            //  Don't do the lookup because it outputs stack traces.
            //try {
            //    OrgInfo oi = getOrganizationService().getOrganization(key);
           //     organizationName = oi.getShortName();
            //} catch (Exception e) {
            //    //  TODO: Determine the Right thing to do.
                //e.printStackTrace();
            //}
            //  Put this item in the cache.
            subjectAreaCache.put(key, organizationName);
        }
        return organizationName;
    }

    /**
     * Provides an instance of the OrganizationService client.
     */
    protected OrganizationService getOrganizationService() {
        if (organizationService == null) {
            organizationService = (OrganizationService) GlobalResourceLoader
                .getService(new QName("http://student.kuali.org/wsdl/organization", "OrganizationService"));
        }
        return organizationService;
    }
}
