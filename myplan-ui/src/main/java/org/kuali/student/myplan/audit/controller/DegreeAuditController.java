/*
 * Copyright 2011 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.student.myplan.audit.controller;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.common.search.dto.SearchResultCell;
import org.kuali.student.common.search.dto.SearchResultRow;
import org.kuali.student.core.organization.service.OrganizationService;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.form.DegreeAuditForm;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

// http://localhost:8080/student/myplan/audit?methodToCall=audit&viewId=DegreeAudit-FormView

@Controller
@RequestMapping(value = "/audit/**")
public class DegreeAuditController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(DegreeAuditController.class);

    private transient DegreeAuditService degreeAuditService;

    public static OrganizationService organizationService;

    public DegreeAuditService getDegreeAuditService() {
        if (degreeAuditService == null) {
            degreeAuditService = (DegreeAuditService)
                    GlobalResourceLoader.getService(new QName(DegreeAuditServiceConstants.NAMESPACE,
                            DegreeAuditServiceConstants.SERVICE_NAME));
        }
        return degreeAuditService;
    }

    public void setDegreeAuditService(DegreeAuditService degreeAuditService) {
        this.degreeAuditService = degreeAuditService;
    }

    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return new DegreeAuditForm();
    }


    public OrganizationService getOrganizationService() {
        if (this.organizationService == null) {
            //   TODO: Use constants for namespace.
            this.organizationService = (OrganizationService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/organization", "orgService"));
        }
        return this.organizationService;
    }

    public void setOrganizationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @RequestMapping(params = "methodToCall=audit")
    public ModelAndView audit(@ModelAttribute("KualiForm") DegreeAuditForm form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> campusMap = populateCampusMap();

            Person user = GlobalVariables.getUserSession().getPerson();
            String studentID = user.getPrincipalId();
            String systemKey = UserSessionHelper.getAuditSystemKey();

            DegreeAuditService degreeAuditService = getDegreeAuditService();
            String auditId = form.getAuditId();
            ContextInfo contextInfo = new ContextInfo();
            Date startDate = new Date();
            Date endDate = new Date();
            String programParam = null;
            form.setCampusParam(campusMap.get("0"));
            List<AuditReportInfo> auditReportInfoList = degreeAuditService.getAuditsForStudentInDateRange(systemKey, startDate, endDate, contextInfo);
            if (auditId == null) {
                auditId = auditReportInfoList.get(0).getAuditId();
                programParam = auditReportInfoList.get(0).getProgramId();
            }

            // TODO: For now we are getting the auditType from the end user. This needs to be removed before going live and hard coded to audit type key html
            AuditReportInfo auditReportInfo = degreeAuditService.getAuditReport(auditId, form.getAuditType(), contextInfo);
            if (auditReportInfoList != null && auditReportInfoList.size() > 0) {
                for (AuditReportInfo report : auditReportInfoList) {
                if(report.getAuditId().equalsIgnoreCase(auditReportInfo.getAuditId())){
                    programParam=report.getProgramId();
                }
                }
            }
            InputStream in = auditReportInfo.getReport().getDataSource().getInputStream();
            StringWriter sw = new StringWriter();

            int c = 0;
            while ((c = in.read()) != -1) {
                sw.append((char) c);
            }

            String html = sw.toString();

            String preparedFor = user.getLastName() + ", " + user.getFirstName();
            html = html.replace("$$PreparedFor$$", preparedFor);
            form.setAuditHtml(html);


            /*Impl to set the default values for campusParam and programParam properties*/
            List<AuditProgramInfo> auditProgramInfoList = new ArrayList<AuditProgramInfo>();
            try {
                auditProgramInfoList = getDegreeAuditService().getAuditPrograms(DegreeAuditConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("could not retrieve AuditPrograms", e);
            }
            for (AuditProgramInfo auditProgramInfo : auditProgramInfoList) {
                if (auditProgramInfo.getProgramTitle().equalsIgnoreCase(programParam)) {
                    int campusPrefix = Integer.parseInt(auditProgramInfo.getProgramId().substring(0,1));
                    form.setCampusParam(campusMap.get(String.valueOf(campusPrefix)));
                    switch( campusPrefix)
                    {
                        case 0:
                            form.setProgramParamSeattle(auditProgramInfo.getProgramId());
                            break;
                        case 1:
                            form.setProgramParamBothell(auditProgramInfo.getProgramId());
                            break;
                        case 2:
                            form.setProgramParamTacoma(auditProgramInfo.getProgramId());
                            break;
                        default:
                            break;
                    }

                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!StringUtils.hasText(form.getAuditHtml())) {
            form.setPageId(DegreeAuditConstants.AUDIT_EMPTY_PAGE);
        }
        return getUIFModelAndView(form);
    }

    @RequestMapping(params = "methodToCall=runAudit")
    public ModelAndView runAudit(@ModelAttribute("KualiForm") DegreeAuditForm form, BindingResult result,
                                 HttpServletRequest request, HttpServletResponse response) {
        if (UserSessionHelper.isAdviser()) {
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putError("audit_report_section", PlanConstants.ERROR_KEY_ADVISER_ACCESS);
            return getUIFModelAndView(form);
        }
        try {
            Person user = GlobalVariables.getUserSession().getPerson();
            String studentId = user.getPrincipalId();
            String systemKey = UserSessionHelper.getAuditSystemKey();
            DegreeAuditService degreeAuditService = getDegreeAuditService();
            String programId = null;
            if( "306".equals( form.getCampusParam()))
            {
                programId = form.getProgramParamSeattle();

            }
            else if( "310".equals( form.getCampusParam()))
            {
                programId = form.getProgramParamBothell();

            }
            else if( "323".equals( form.getCampusParam()))
            {
                programId = form.getProgramParamTacoma();

            }

            ContextInfo context = new ContextInfo();

            AuditReportInfo report = degreeAuditService.runAudit(systemKey, programId, form.getAuditType(), context);
            String auditID = report.getAuditId();
            // TODO: For now we are getting the auditType from the end user. This needs to be remvoed before going live and hard coded to audit type key html
            AuditReportInfo auditReportInfo = degreeAuditService.getAuditReport(auditID, form.getAuditType(), context);
            InputStream in = auditReportInfo.getReport().getDataSource().getInputStream();
            StringWriter sw = new StringWriter();

            int c = 0;
            while ((c = in.read()) != -1) {
                sw.append((char) c);
            }

            String html = sw.toString();
            String preparedFor = user.getLastName() + ", " + user.getFirstName();
            html = html.replace("$$PreparedFor$$", preparedFor);
            form.setAuditHtml(html);

        } catch (Exception e) {
            logger.error("Could not complete audit run");
        }

        return getUIFModelAndView(form);
    }

    public Map<String, String> populateCampusMap() {
        Map<String, String> orgCampusTypes = new HashMap<String, String>();
        SearchRequest searchRequest = new SearchRequest(CourseSearchConstants.ORG_QUERY_SEARCH_BY_TYPE_REQUEST);
        searchRequest.addParam(DegreeAuditConstants.ORG_QUERY_PARAM, DegreeAuditConstants.CAMPUS_LOCATION);
        SearchResult searchResult = new SearchResult();
        try {
            searchResult = getOrganizationService().search(searchRequest);
        } catch (MissingParameterException e) {
            logger.error("Search Failed to get the Organization Data ", e);
        }
        for (SearchResultRow row : searchResult.getRows()) {

            if (getCellValue(row, "org.resultColumn.orgShortName").equalsIgnoreCase("seattle")) {
                orgCampusTypes.put("0", getCellValue(row, "org.resultColumn.orgId"));
            }
            if (getCellValue(row, "org.resultColumn.orgShortName").equalsIgnoreCase("bothell")) {
                orgCampusTypes.put("1", getCellValue(row, "org.resultColumn.orgId"));
            }
            if (getCellValue(row, "org.resultColumn.orgShortName").equalsIgnoreCase("tacoma")) {
                orgCampusTypes.put("2", getCellValue(row, "org.resultColumn.orgId"));
            }

        }
        return orgCampusTypes;
    }


    public String getCellValue(SearchResultRow row, String key) {
        for (SearchResultCell cell : row.getCells()) {
            if (key.equals(cell.getKey())) {
                return cell.getValue();
            }
        }
        throw new RuntimeException("cell result '" + key + "' not found");
    }
}


