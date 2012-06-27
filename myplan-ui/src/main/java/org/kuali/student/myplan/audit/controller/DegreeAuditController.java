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
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.form.DegreeAuditForm;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// http://localhost:8080/student/myplan/audit?methodToCall=audit&viewId=DegreeAudit-FormView

@Controller
@RequestMapping(value = "/audit/**")
public class DegreeAuditController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(DegreeAuditController.class);

    private transient DegreeAuditService degreeAuditService;

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

    @RequestMapping(params = "methodToCall=audit")
    public ModelAndView audit(@ModelAttribute("KualiForm") DegreeAuditForm form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        try {
            Person user = GlobalVariables.getUserSession().getPerson();
            String studentID = user.getPrincipalId();

            DegreeAuditService degreeAuditService = getDegreeAuditService();
            String auditId = form.getAuditId();
            ContextInfo contextInfo = new ContextInfo();
            Date startDate = new Date();
            Date endDate = new Date();
            String programParam=null;
            form.setCampusParam("0");
            if (auditId == null) {
                List<AuditReportInfo> auditReportInfos = degreeAuditService.getAuditsForStudentInDateRange(studentID, startDate, endDate, contextInfo);
                auditId = auditReportInfos.get(0).getAuditId();
                programParam=auditReportInfos.get(0).getProgramId();
            }

            // TODO: For now we are getting the auditType from the end user. This needs to be removed before going live and hard coded to audit type key html
            AuditReportInfo auditReportInfo = degreeAuditService.getAuditReport(auditId, form.getAuditType(), contextInfo);
            InputStream in = auditReportInfo.getReport().getDataSource().getInputStream();
            StringWriter sw = new StringWriter();

            int c = 0;
            while ((c = in.read()) != -1) {
                sw.append((char) c);
            }

            String html = sw.toString();

            String preparedFor = user.getLastName() + ", " + user.getFirstName();
            html = html.replace( "$$PreparedFor$$", preparedFor );
            form.setAuditHtml(html);


            /*Impl to set the default values for campusParam and programParam properties*/
            List<AuditProgramInfo> auditProgramInfoList = new ArrayList<AuditProgramInfo>();
            try {
                auditProgramInfoList = getDegreeAuditService().getAuditPrograms(DegreeAuditConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("could not retrieve AuditPrograms", e);
            }
            for(AuditProgramInfo auditProgramInfo:auditProgramInfoList){
                if(auditProgramInfo.getProgramTitle().equalsIgnoreCase(programParam)){
                  form.setProgramParam(auditProgramInfo.getProgramId());
                  form.setCampusParam(auditProgramInfo.getProgramId().substring(0,1));
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return getUIFModelAndView(form);
    }

    @RequestMapping(params = "methodToCall=runAudit")
    public ModelAndView runAudit(@ModelAttribute("KualiForm") DegreeAuditForm form, BindingResult result,
                                 HttpServletRequest request, HttpServletResponse response) {
        if(UserSessionHelper.isAdviser()){
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putError("audit_report_section", PlanConstants.ERROR_KEY_ADVISER_ACCESS);
            return getUIFModelAndView(form);
        }
        try {
            Person user = GlobalVariables.getUserSession().getPerson();
            String studentId = user.getPrincipalId();
            DegreeAuditService degreeAuditService = getDegreeAuditService();
            String programId = form.getProgramParam();
            ContextInfo context = new ContextInfo();

            AuditReportInfo report = degreeAuditService.runAudit(studentId, programId, form.getAuditType(), context);
            String auditID = report.getAuditId();
            // TODO: For now we are getting the auditType from the end user. This needs to be remvoed before going live and hard coded to audit type key html
            AuditReportInfo auditReportInfo = degreeAuditService.getAuditReport(auditID,form.getAuditType(), context);
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
}


