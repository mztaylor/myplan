package org.kuali.student.myplan.course.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import org.kuali.student.common.exceptions.*;
import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.core.atp.service.AtpService;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.core.statement.dto.StatementTreeViewInfo;
import org.kuali.student.core.statement.service.StatementService;
import org.kuali.student.core.statement.util.StatementServiceConstants;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.util.CreditsFormatter;

public class CourseDetailsInquiryViewHelperServiceImpl extends KualiInquirableImpl {

    private final Logger logger = Logger.getLogger(CourseDetailsInquiryViewHelperServiceImpl.class);

    private static final String STATEMENT_SERVICE_NAMESPACE = "http://student.kuali.org/wsdl/statement";
    private static final String ENUM_SERVICE_NAMESPACE = "http://student.kuali.org/wsdl/enumerationmanagement";
    //  TODO: This is duplicated in CourseSearchController
    private static final String GEN_EDU_REQUIREMENTS_PREFIX = "genEdRequirement";

    private transient CourseService courseService;

    private transient StatementService statementService;

    private transient AtpService atpService;

    //TODO: These should be changed to a ehCache spring bean
    private Map<String, String> campusLocationCache;
    private Map<String, String> atpCache;

    private transient EnumerationManagementService enumService;

    @Override
    public CourseDetails retrieveDataObject(Map fieldValues) {
        //  Get the courseId from the query parameters.
        String courseId = (String) fieldValues.get("courseId");

        CourseInfo course = null;
        try {
            course = getCourseService().getCourse(courseId);
        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }

        CourseDetails courseDetails = new CourseDetails();

        courseDetails.setCourseId(course.getId());
        courseDetails.setCode(course.getCode());
        courseDetails.setCourseDescription(course.getDescr().getFormatted());
        courseDetails.setCredit(CreditsFormatter.formatCredits(course));
        courseDetails.setCourseTitle(course.getCourseTitle());

        // Terms Offered
        initializeAtpTypesCache();
        List<String> termsOffered = new ArrayList<String>();
        for (String term : course.getTermsOffered()) {
            termsOffered.add(atpCache.get(term));
        }
        courseDetails.setTermsOffered(termsOffered);

        // Campus Locations
        initializeCampusLocations();
        List<String> enumeratedCampus = new ArrayList<String>();
        for (String campus : course.getCampusLocations()) {
            enumeratedCampus.add(this.campusLocationCache.get(campus));
        }

        courseDetails.setCampusLocations(enumeratedCampus);

        //  Lookup course statements and build the requisites list.
        List<StatementTreeViewInfo> statements = null;
        try {
            statements = getCourseService().getCourseStatements(courseId, null, null);
        } catch (DoesNotExistException e) {
            //  TODO: Is this a problem?
        } catch (Exception e) {
            logger.error("Course Statement lookup failed.", e);
        }

        List<String> reqs = new ArrayList<String>();
        for (StatementTreeViewInfo stvi : statements) {
            String statement = null;
            try {
                statement = getStatementService().translateStatementTreeViewToNL(stvi, "KUALI.RULE.PREVIEW", "en");
            } catch (Exception e) {
                logger.error("Translation of Course Statement to natural language failed.", e);
                //  TODO: How should this be handled?
                statement = "";
            }
            reqs.add(statement);
        }
        courseDetails.setRequisites(reqs);

        //  Get general education requirements.
        List<String> genEdReqs = new ArrayList<String>();
        Map<String, String> attributes = course.getAttributes();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            if (entry.getValue().equals("true") && entry.getKey().startsWith(GEN_EDU_REQUIREMENTS_PREFIX)) {
                String r = entry.getKey().replace(GEN_EDU_REQUIREMENTS_PREFIX, "");
                genEdReqs.add(r);
            }
        }
        courseDetails.setGenEdRequirements(genEdReqs);

        return courseDetails;
    }

    protected synchronized CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    protected synchronized StatementService getStatementService() {
        if (this.statementService == null) {
            this.statementService = (StatementService) GlobalResourceLoader
                    .getService(new QName(STATEMENT_SERVICE_NAMESPACE, "StatementService"));
        }
        return this.statementService;
    }


    protected synchronized EnumerationManagementService getEnumerationService() {
        if (this.enumService == null) {
            this.enumService = (EnumerationManagementService) GlobalResourceLoader
                    .getService(new QName(ENUM_SERVICE_NAMESPACE, "EnumerationManagementService"));
        }
        return this.enumService;
    }

    /**
     * Provides an instance of the AtpService client.
     */
    protected AtpService getAtpService() {
        if (atpService == null) {
            // TODO: Namespace should not be hard-coded.
            atpService = (AtpService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/atp", "AtpService"));
        }
        return this.atpService;
    }

    protected synchronized void initializeCampusLocations() {
        if (null == campusLocationCache || campusLocationCache.isEmpty()) {
            try {
                List<EnumeratedValueInfo> campusLocations = getEnumerationService().getEnumeratedValues("kuali.lu.campusLocation", null, null, null);
                if (this.campusLocationCache == null) {
                    this.campusLocationCache = new HashMap<String, String>();
                }
                for (EnumeratedValueInfo campus : campusLocations) {
                    this.campusLocationCache.put(campus.getCode(), campus.getValue());
                }

            } catch (Exception e) {
                logger.error("Could not load campus locations..");
            }
        }
    }

    /**
     * Initializes ATP term cache.
     * AtpSeasonalTypes rarely change, so fetch them all and store them in a Map.
     */
    private synchronized void initializeAtpTypesCache() {

        if (null == atpCache || atpCache.isEmpty()) {
            atpCache = new HashMap<String, String>();
            List<AtpTypeInfo> atpTypeInfos;
            try {
                atpTypeInfos = getAtpService().getAtpTypes();
            } catch (OperationFailedException e) {
                logger.error("ATP types lookup failed.", e);
                return;
            }
            for (AtpTypeInfo ti : atpTypeInfos) {
                atpCache.put(ti.getId(), ti.getName().substring(0,1).toUpperCase() + ti.getName().substring(1));
            }
        }
    }

}