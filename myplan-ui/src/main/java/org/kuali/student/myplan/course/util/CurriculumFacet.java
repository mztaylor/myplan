package org.kuali.student.myplan.course.util;

import javax.xml.namespace.QName;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.organization.service.OrganizationService;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

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
    public void process(CourseSearchItem course)
    {
        String key = course.getSubject();

        String displayName = UNKNOWN_FACET_DISPLAY_NAME;

        if (key == null || key.equals("")) {
            key = UNKNOWN_FACET_KEY;
        } else {
            // TODO: Use the Org Service to lookup curriculum name based on key.
            // For now just use the serviceArea code as the displayName.
            displayName = getOrganizationName(key);
        }

        key = FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER;

        //  If it's a new facet key then create a new FacetItem.
        if (isNewFacetKey( key )) {
            FacetItem fItem = new FacetItem();
            //  The display name and the key are the same in this case.
            fItem.setKey(key);
            fItem.setDisplayName(displayName);
            facetItems.add(fItem);
        }

        //  Code the item with the facet key.
        course.setCurriculumFacetKey(key);
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
