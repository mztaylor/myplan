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
import org.kuali.rice.core.api.config.property.ConfigContext;
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
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.form.*;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.dataobject.ServicesStatusDataObject;
import org.kuali.student.myplan.plan.service.PlannedCoursesLookupableHelperImpl;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;
import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT;
import static org.kuali.student.myplan.course.util.CourseSearchConstants.CONTEXT_INFO;

// http://localhost:8080/student/myplan/audit?methodToCall=audit&viewId=DegreeAudit-FormView

@Controller
@RequestMapping(value = "/audit/**")
public class DegreeAuditController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(DegreeAuditController.class);

    private transient DegreeAuditService degreeAuditService;

    public static OrganizationService organizationService;

    public transient AcademicPlanService academicPlanService;

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
        return new AuditForm();
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

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }

    @RequestMapping(params = "methodToCall=audit")
    public ModelAndView audit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        boolean systemKeyExists = true;
        DegreeAuditForm degreeAuditForm = auditForm.getDegreeAudit();
        PlanAuditForm planAuditForm = auditForm.getPlanAudit();
        try {
            Map<String, String> campusMap = populateCampusMap();
            ServicesStatusDataObject servicesStatusDataObject = (ServicesStatusDataObject) request.getSession().getAttribute(CourseSearchConstants.SWS_SERVICES_STATUS);
            Person user = GlobalVariables.getUserSession().getPerson();
            String regId = UserSessionHelper.getStudentRegId();
            if (StringUtils.hasText(regId)) {
                DegreeAuditService degreeAuditService = getDegreeAuditService();
                String auditId = degreeAuditForm.getAuditId();
                ContextInfo contextInfo = new ContextInfo();
                degreeAuditForm.setCampusParam(campusMap.get("0"));
                planAuditForm.setCampusParam(campusMap.get("0"));
                logger.info("audit regId " + regId);
                String planAuditId = planAuditForm.getAuditId();
                if (!servicesStatusDataObject.isDegreeAuditServiceUp()) {
                    AtpHelper.addServiceError("programParamSeattle");
                } else {
                    Date startDate = new Date();
                    Date endDate = new Date();
                    String degreeProgramParam = null;
                    String degreeProgramId = null;
                    String planProgramParam = null;
                    String planProgramId = null;
                    List<AuditReportInfo> auditReportInfoList = degreeAuditService.getAuditsForStudentInDateRange(regId, startDate, endDate, contextInfo);
                    if (auditId == null && auditReportInfoList.size() > 0) {
                        for (AuditReportInfo auditReportInfo : auditReportInfoList) {
                            if (!auditReportInfo.isWhatIfAudit()) {
                                auditId = auditReportInfo.getAuditId();
                                degreeProgramParam = auditReportInfo.getProgramTitle();
                                break;
                            }
                        }

                    }
                    if (planAuditId == null && auditReportInfoList.size() > 0) {
                        for (AuditReportInfo auditReportInfo : auditReportInfoList) {
                            if (auditReportInfo.isWhatIfAudit()) {
                                planAuditId = auditReportInfo.getAuditId();
                                planProgramParam = auditReportInfo.getProgramTitle();
                                break;
                            }
                        }

                    }
                    // TODO: For now we are getting the auditType from the end user. This needs to be removed before going live and hard coded to audit type key html
                    if (auditId != null) {
                        AuditReportInfo auditReportInfo = degreeAuditService.getAuditReport(auditId, degreeAuditForm.getAuditType(), contextInfo);
                        if (auditReportInfoList != null && auditReportInfoList.size() > 0) {
                            for (AuditReportInfo report : auditReportInfoList) {
                                if (report.getAuditId().equalsIgnoreCase(auditReportInfo.getAuditId())) {
                                    degreeProgramParam = report.getProgramTitle();
                                    degreeProgramId = report.getProgramId();
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
                        degreeAuditForm.setAuditHtml(html);


                        /*Impl to set the default values for campusParam and programParam properties*/
                        if (degreeProgramId != null && degreeProgramParam != null) {
                            int campusPrefix = Integer.parseInt(degreeProgramId.substring(0, 1));
                            degreeProgramId = degreeProgramId.replaceAll(" ", "$");
                            degreeAuditForm.setCampusParam(campusMap.get(String.valueOf(campusPrefix)));
                            switch (campusPrefix) {
                                case 0:
                                    degreeAuditForm.setProgramParamSeattle(degreeProgramId);
                                    break;
                                case 1:
                                    degreeAuditForm.setProgramParamBothell(degreeProgramId);
                                    break;
                                case 2:
                                    degreeAuditForm.setProgramParamTacoma(degreeProgramId);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    /*Plan Audit Report*/
                    if (planAuditId != null) {
                        AuditReportInfo auditReportInfo = degreeAuditService.getAuditReport(planAuditId, planAuditForm.getAuditType(), contextInfo);
                        if (auditReportInfoList != null && auditReportInfoList.size() > 0) {
                            for (AuditReportInfo report : auditReportInfoList) {
                                if (report.getAuditId().equalsIgnoreCase(auditReportInfo.getAuditId())) {
                                    planProgramParam = report.getProgramTitle();
                                    planProgramId = report.getProgramId();
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
                        planAuditForm.setAuditHtml(html);


                        /*Impl to set the default values for campusParam and programParam properties*/
                        if (planProgramId != null && planProgramParam != null) {
                            int campusPrefix = Integer.parseInt(planProgramId.substring(0, 1));
                            planProgramId = planProgramId.replaceAll(" ", "$");
                            planAuditForm.setCampusParam(campusMap.get(String.valueOf(campusPrefix)));
                            switch (campusPrefix) {
                                case 0:
                                    planAuditForm.setProgramParamSeattle(planProgramId);
                                    break;
                                case 1:
                                    planAuditForm.setProgramParamBothell(planProgramId);
                                    break;
                                case 2:
                                    planAuditForm.setProgramParamTacoma(planProgramId);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                }


            }
        } catch (DataRetrievalFailureException e) {
            logger.error("audit failed", e);
            systemKeyExists = false;
            auditForm.setPageId(DegreeAuditConstants.AUDIT_NON_STUDENT_PAGE);
        } catch (Exception e) {
            logger.error("audit failed", e);
            String[] params = {};
            GlobalVariables.getMessageMap().putWarning("planAudit.programParamSeattle", DegreeAuditConstants.TECHNICAL_PROBLEM, params);
        }

        return getUIFModelAndView(auditForm);
    }

    @RequestMapping(params = "methodToCall=runAudit")
    public ModelAndView runAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                 HttpServletRequest request, HttpServletResponse response) {
        DegreeAuditForm form = auditForm.getDegreeAudit();
        String auditID = null;
        try {
            Person user = GlobalVariables.getUserSession().getPerson();
            String regid = UserSessionHelper.getStudentRegId();
            if (StringUtils.hasText(regid)) {
                DegreeAuditService degreeAuditService = getDegreeAuditService();
                String programId = null;
                if ("306".equals(form.getCampusParam())) {
                    programId = form.getProgramParamSeattle();

                } else if ("310".equals(form.getCampusParam())) {
                    programId = form.getProgramParamBothell();

                } else if ("323".equals(form.getCampusParam())) {
                    programId = form.getProgramParamTacoma();

                }
                if (!programId.equalsIgnoreCase(DegreeAuditConstants.DEFAULT_KEY)) {
                    ContextInfo context = new ContextInfo();
                    AuditReportInfo report = degreeAuditService.runAudit(regid, programId, form.getAuditType(), context);
                    auditID = report.getAuditId();
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
                } else {
                    String[] params = {};
                    GlobalVariables.getMessageMap().putError("degreeAudit.programParamSeattle", DegreeAuditConstants.AUDIT_RUN_FAILED, params);
                    form.setAuditHtml(String.format(DegreeAuditConstants.AUDIT_FAILED_HTML, ConfigContext.getCurrentContextConfig().getProperty(DegreeAuditConstants.APPLICATION_URL)));
                }
            }

        } catch (DataRetrievalFailureException e) {
            String[] params = {};
            form.setCampusParam("306");
            GlobalVariables.getMessageMap().putError("degreeAudit.programParamSeattle", DegreeAuditConstants.NO_SYSTEM_KEY, params);

        } catch (Exception e) {
            logger.error("Could not complete audit run");
            String[] params = {};
            GlobalVariables.getMessageMap().putError("degreeAudit.programParamSeattle", DegreeAuditConstants.AUDIT_RUN_FAILED, params);
            Throwable cause = e.getCause();
            if (cause != null) {
                String message = cause.getMessage();
                if (message != null) {
                    String errorMessage = getErrorMessageFromXml(message);
                    String html = String.format(DegreeAuditConstants.AUDIT_FAILED_HTML, ConfigContext.getCurrentContextConfig().getProperty(DegreeAuditConstants.APPLICATION_URL), errorMessage);
                    form.setAuditHtml(html);
                }
            }
        }
        return getUIFModelAndView(auditForm);
    }

    @RequestMapping(params = "methodToCall=runPlanAudit")
    public ModelAndView runPlanAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                     HttpServletRequest request, HttpServletResponse response) {
        PlanAuditForm planAuditform = auditForm.getPlanAudit();
        /*TODO: uncomment this validation once we populate the lastPlannedTerm in start Method
        if (planAuditform.getLastPlannedTerm() == null) {
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putError("plan_audit_report_section", PlanConstants.ERROR_PLAN_AUDIT_QUARTER_EMPTY);
            return getUIFModelAndView(auditForm);
        } else if (AtpHelper.isAtpCompletedTerm(planAuditform.getLastPlannedTerm())) {
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putError("plan_audit_report_section", PlanConstants.ERROR_PLAN_AUDIT_INVALID_QUARTER);
            return getUIFModelAndView(auditForm);
        }*/


        /******Plan Audit Report Process*********/
        try {
            String regid = UserSessionHelper.getStudentRegId();
            if (StringUtils.hasText(regid)) {
                DegreeAuditService degreeAuditService = getDegreeAuditService();
                String programId = null;
                if ("306".equals(planAuditform.getCampusParam())) {
                    programId = planAuditform.getProgramParamSeattle();

                } else if ("310".equals(planAuditform.getCampusParam())) {
                    programId = planAuditform.getProgramParamBothell();

                } else if ("323".equals(planAuditform.getCampusParam())) {
                    programId = planAuditform.getProgramParamTacoma();

                }
                if (!programId.equalsIgnoreCase(DegreeAuditConstants.DEFAULT_KEY)) {
                    Person user = GlobalVariables.getUserSession().getPerson();
                    ContextInfo context = new ContextInfo();
                    context.setPrincipalId(regid);
                    LearningPlanInfo learningPlanInfo = null;
                    List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(regid, LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);
                    for (LearningPlanInfo learningPlan : learningPlanList) {
                        learningPlanInfo = getAcademicPlanService().copyLearningPlan(learningPlan.getId(), AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT, context);
                        break;
                    }
                    AuditReportInfo report = degreeAuditService.runWhatIfAudit(regid, programId, planAuditform.getAuditType(), learningPlanInfo.getId(), context);
                    InputStream in = report.getReport().getDataSource().getInputStream();
                    StringWriter sw = new StringWriter();

                    int c = 0;
                    while ((c = in.read()) != -1) {
                        sw.append((char) c);
                    }

                    String html = sw.toString();
                    String preparedFor = user.getLastName() + ", " + user.getFirstName();
                    html = html.replace("$$PreparedFor$$", preparedFor);
                    planAuditform.setAuditHtml(html);

                } else {
                    String[] params = {};
                    GlobalVariables.getMessageMap().putError("planAudit.programParamSeattle", DegreeAuditConstants.AUDIT_RUN_FAILED, params);
                    planAuditform.setAuditHtml(String.format(DegreeAuditConstants.AUDIT_FAILED_HTML, ConfigContext.getCurrentContextConfig().getProperty(DegreeAuditConstants.APPLICATION_URL)));
                }
            }

        } catch (DataRetrievalFailureException e) {
            String[] params = {};
            planAuditform.setCampusParam("306");
            GlobalVariables.getMessageMap().putError("planAudit.programParamSeattle", DegreeAuditConstants.NO_SYSTEM_KEY, params);

        } catch (Exception e) {
            logger.error("Could not complete audit run");
            String[] params = {};
            GlobalVariables.getMessageMap().putError("planAudit.programParamSeattle", DegreeAuditConstants.AUDIT_RUN_FAILED, params);
            Throwable cause = e.getCause();
            if (cause != null) {
                String message = cause.getMessage();
                if (message != null) {
                    String errorMessage = getErrorMessageFromXml(message);
                    String html = String.format(DegreeAuditConstants.AUDIT_FAILED_HTML, ConfigContext.getCurrentContextConfig().getProperty(DegreeAuditConstants.APPLICATION_URL), errorMessage);
                    planAuditform.setAuditHtml(html);
                }
            }
        }
        return getUIFModelAndView(auditForm);
    }

    @RequestMapping(params = "methodToCall=reviewPlanAudit")
    public ModelAndView reviewPlanAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {

        PlanAuditForm planAuditForm = auditForm.getPlanAudit();
        List<CourseItem> courseItems = new ArrayList<CourseItem>();
        List<MessyItem> messyItems = new ArrayList<MessyItem>();
        PlannedCoursesLookupableHelperImpl plannedCoursesLookupableHelper = new PlannedCoursesLookupableHelperImpl();
        List<PlannedTerm> plannedTermList = plannedCoursesLookupableHelper.getPlannedTerms();
        List<String> scheduledTerms = AtpHelper.getPublishedTerms();
        for (PlannedTerm plannedTerm : plannedTermList) {
            // TS Published : YES
            if (scheduledTerms.contains(plannedTerm.getAtpId())) {
                for (PlannedCourseDataObject plannedCourseDataObject : plannedTerm.getPlannedList()) {
                    List<String> sections = new ArrayList<String>();
                    Set<String> credits = new HashSet<String>();
                    // Planned Sections : YES ; Multiple planned sections : NO
                    if (plannedCourseDataObject.getPlanActivities().size() == 1 && plannedCourseDataObject.getPlanActivities().get(0).isPrimary()) {
                        String section = plannedCourseDataObject.getPlanActivities().get(0).getCode();
                        String credit = plannedCourseDataObject.getPlanActivities().get(0).getCredits();
                        String creditType = getCreditType(credit);
                        // Check Credits if Valid : NO ; Students needs to select Credits
                        if ("MULTIPLE".equalsIgnoreCase(creditType)) {
                            Collections.addAll(credits, credit.replace(" ","").split(","));
                            MessyItem messyItem = new MessyItem();
                            messyItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                            messyItem.setCredits(credits);
                            messyItem.setAtpId(plannedTerm.getAtpId());
                            messyItems.add(messyItem);
                        }
                        // Check Credits if Valid : NO ; Students needs to select Credits 
                        else if ("RANGE".equalsIgnoreCase(creditType)) {
                            credits.addAll(getCreditsForRange(credit));
                            MessyItem messyItem = new MessyItem();
                            messyItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                            messyItem.setCredits(credits);
                            messyItem.setAtpId(plannedTerm.getAtpId());
                            messyItems.add(messyItem);
                        }
                        // Check Credits if Valid : YES
                        else {
                            CourseItem courseItem = new CourseItem();
                            courseItem.setAtpId(plannedTerm.getAtpId());
                            courseItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                            courseItem.setCourseId(plannedCourseDataObject.getCourseDetails().getCourseId());
                            courseItem.setSectionCode(section);
                            courseItems.add(courseItem);

                        }
                    }
                    // Planned Sections : YES ; Multiple planned sections : YES
                    else if (plannedCourseDataObject.getPlanActivities().size() > 1) {
                        boolean allHaveWritingAndHonorFlag = true;
                        boolean allSectionsHaveSameCredit = true;
                        String commonCredit = null;
                        for (ActivityOfferingItem activityOfferingItem : plannedCourseDataObject.getPlanActivities()) {
                            if (activityOfferingItem.isPrimary()) {
                                sections.add(activityOfferingItem.getCode());
                                credits.add(activityOfferingItem.getCredits());
                                if (commonCredit == null) {
                                    commonCredit = activityOfferingItem.getCredits();
                                } else if (allSectionsHaveSameCredit && !commonCredit.equalsIgnoreCase(activityOfferingItem.getCredits())) {
                                    allSectionsHaveSameCredit = false;
                                }
                                if (allHaveWritingAndHonorFlag && !activityOfferingItem.isHonorsSection() && !activityOfferingItem.isWritingSection()) {
                                    allHaveWritingAndHonorFlag = false;
                                }
                            }
                        }
                        // Writing and honor Flag for all the sections : NO
                        if (!allHaveWritingAndHonorFlag) {
                            MessyItem messyItem = new MessyItem();
                            messyItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                            messyItem.setSections(sections);
                            messyItem.setAtpId(plannedTerm.getAtpId());
                            messyItems.add(messyItem);

                        }
                        //  Writing and honor Flag for all the sections : YES
                        else {
                            if (allSectionsHaveSameCredit) {
                                CourseItem courseItem = new CourseItem();
                                courseItem.setAtpId(plannedTerm.getAtpId());
                                courseItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                                courseItem.setCourseId(plannedCourseDataObject.getCourseDetails().getCourseId());
                                courseItem.setSectionCode(null);
                                courseItems.add(courseItem);
                            } else {
                                MessyItem messyItem = new MessyItem();
                                messyItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                                messyItem.setCredits(credits);
                                messyItem.setAtpId(plannedTerm.getAtpId());
                                messyItems.add(messyItem);
                            }
                        }
                    }
                    // Planned Sections : NO 
                    else {
                        CourseDetailsInquiryHelperImpl courseDetailsInquiryHelper = new CourseDetailsInquiryHelperImpl();
                        List<ActivityOfferingItem> sectionsOffered = courseDetailsInquiryHelper.getActivityOfferingItemsById(plannedCourseDataObject.getCourseDetails().getCourseId(), plannedTerm.getAtpId());
                        // Multiple Offered Sections : NO 
                        if (sectionsOffered.size() == 1) {
                            boolean allSectionsHaveSameCredit = true;
                            String commonCredit = null;
                            for (ActivityOfferingItem activityOfferingItem : plannedCourseDataObject.getPlanActivities()) {
                                if (activityOfferingItem.isPrimary()) {
                                    credits.add(activityOfferingItem.getCredits());
                                    if (commonCredit == null) {
                                        commonCredit = activityOfferingItem.getCredits();
                                    } else if (allSectionsHaveSameCredit && !commonCredit.equalsIgnoreCase(activityOfferingItem.getCredits())) {
                                        allSectionsHaveSameCredit = false;
                                    }
                                }
                            }
                            if (allSectionsHaveSameCredit) {
                                CourseItem courseItem = new CourseItem();
                                courseItem.setAtpId(plannedTerm.getAtpId());
                                courseItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                                courseItem.setCourseId(plannedCourseDataObject.getCourseDetails().getCourseId());
                                courseItem.setSectionCode(null);
                                courseItems.add(courseItem);
                            } else {
                                MessyItem messyItem = new MessyItem();
                                messyItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                                messyItem.setCredits(credits);
                                messyItem.setAtpId(plannedTerm.getAtpId());
                                messyItems.add(messyItem);
                            }
                        }
                        // Multiple Offered Sections : YES
                        else if (sectionsOffered.size() > 1) {
                            boolean allHaveWritingAndHonorFlag = true;
                            boolean allSectionsHaveSameCredit = true;
                            String commonCredit = null;
                            for (ActivityOfferingItem activityOfferingItem : sectionsOffered) {
                                if (activityOfferingItem.isPrimary()) {
                                    sections.add(activityOfferingItem.getCode());
                                    credits.add(activityOfferingItem.getCredits());
                                    if (commonCredit == null) {
                                        commonCredit = activityOfferingItem.getCredits();
                                    } else if (allSectionsHaveSameCredit && !commonCredit.equalsIgnoreCase(activityOfferingItem.getCredits())) {
                                        allSectionsHaveSameCredit = false;
                                    }
                                    if (allHaveWritingAndHonorFlag && !activityOfferingItem.isHonorsSection() && !activityOfferingItem.isWritingSection()) {
                                        allHaveWritingAndHonorFlag = false;
                                    }
                                }
                            }
                            // Writing and honor Flag for all the sections : NO
                            if (!allHaveWritingAndHonorFlag) {
                                MessyItem messyItem = new MessyItem();
                                messyItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                                messyItem.setSections(sections);
                                messyItem.setAtpId(plannedTerm.getAtpId());
                                messyItems.add(messyItem);
                            }
                            //  Writing and honor Flag for all the sections : YES
                            else {
                                if (allSectionsHaveSameCredit) {
                                    CourseItem courseItem = new CourseItem();
                                    courseItem.setAtpId(plannedTerm.getAtpId());
                                    courseItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                                    courseItem.setCourseId(plannedCourseDataObject.getCourseDetails().getCourseId());
                                    courseItem.setSectionCode(null);
                                    courseItems.add(courseItem);
                                } else {
                                    MessyItem messyItem = new MessyItem();
                                    messyItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                                    messyItem.setCredits(credits);
                                    messyItem.setAtpId(plannedTerm.getAtpId());
                                    messyItems.add(messyItem);
                                }
                            }
                        }

                    }
                }
            }
            // TS Published : NO
            else {
                Set<String> credits = new HashSet<String>();
                for (PlannedCourseDataObject plannedCourseDataObject : plannedTerm.getPlannedList()) {
                    String credit = plannedCourseDataObject.getCourseDetails().getCredit();
                    String creditType = getCreditType(credit);
                    // Is course Offered in Single value : NO
                    if ("MULTIPLE".equalsIgnoreCase(creditType)) {
                        Collections.addAll(credits, credit.replace(" ","").split(","));
                        MessyItem messyItem = new MessyItem();
                        messyItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                        messyItem.setCredits(credits);
                        messyItem.setAtpId(plannedTerm.getAtpId());
                        messyItems.add(messyItem);
                    }
                    // Is course Offered in Single value : NO
                    else if ("RANGE".equalsIgnoreCase(creditType)) {
                        credits.addAll(getCreditsForRange(credit));
                        MessyItem messyItem = new MessyItem();
                        messyItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                        messyItem.setCredits(credits);
                        messyItem.setAtpId(plannedTerm.getAtpId());
                        messyItems.add(messyItem);
                    }
                    // Is course Offered in Single value : YES
                    else {
                        CourseItem courseItem = new CourseItem();
                        courseItem.setAtpId(plannedTerm.getAtpId());
                        courseItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
                        courseItem.setCourseId(plannedCourseDataObject.getCourseDetails().getCourseId());
                        courseItem.setSectionCode(null);
                        courseItems.add(courseItem);

                    }

                }

            }
        }

        if (courseItems.size() > 0) {
            planAuditForm.setCleanList(courseItems);
        }
        if (messyItems.size() > 0) {
            planAuditForm.setStudentChoiceRequired(true);
            planAuditForm.setMessyItems(messyItems);
        }
        return getUIFModelAndView(auditForm);
    }

    private String getCreditType(String credit) {
        if (credit.matches("^\\d*.\\d*-\\d*.\\d*$")) {
            return "RANGE";
        } else if (credit.matches("^\\d*.\\d*,\\d*.\\d*$")) {
            return "MULTIPLE";
        } else {
            return "NORMAL";
        }
    }

    private List<String> getCreditsForRange(String credit) {
        String[] str = credit.split("-");
        int min = 0;
        int minDecimal = 0;
        int max = 0;
        int maxDecimal = 0;
        List<String> credits = new ArrayList<String>();
        if (str[0].contains(".")) {
            String[] str2 = str[0].split(".");
            min = Integer.valueOf(str2[0].trim());
            minDecimal = Integer.valueOf(str2[1].trim());

        } else {
            min = Integer.valueOf(str[0]);
        }
        if (str[1].contains(".")) {
            String[] str2 = str[1].split(".");
            max = Integer.valueOf(str2[0].trim());
            maxDecimal = Integer.valueOf(str2[1].trim());

        } else {
            max = Integer.parseInt(str[1].trim());
        }
        while (min <= max) {
            credits.add(String.valueOf(min));
            min++;
        }
        if (minDecimal != 0 && credits.size() > 0) {
            String val = credits.get(0);
            credits.add(0, val + String.valueOf(minDecimal));
        }
        if (maxDecimal != 0 && credits.size() > 0) {
            String val = credits.get(credits.size() - 1);
            credits.add(credits.size() - 1, val + String.valueOf(maxDecimal));
        }
        return credits;
    }


    @RequestMapping(value = "/status")
    public void getJsonResponse(HttpServletResponse response, HttpServletRequest request) {
        String programId = request.getParameter("programId").replace("$", " ");
        String auditId = request.getParameter("auditId");
        String regId = UserSessionHelper.getStudentRegId();

        try {
            String status = getDegreeAuditService().getAuditStatus(regId, programId, auditId);
            response.getWriter().printf("{\"status\":\"%s\"}", status);
        } catch (Exception e) {
            logger.warn("cannot retrieve audit status", e);
        }
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

    public String getErrorMessageFromXml(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            if (doc.getElementsByTagName("StatusDescription").getLength() != 0) {
                return doc.getElementsByTagName("StatusDescription").item(0).getTextContent();
            } else {
                return "Technical Problems";
            }
        } catch (Exception e) {
            return "Technical Problems";
        }
    }

    /**
     * Initializes the error page.
     */
    private ModelAndView doErrorPage(AuditForm form, String errorKey, String[] params, String page, String section) {
        GlobalVariables.getMessageMap().putErrorForSectionId(section, errorKey, params);
        return getUIFModelAndView(form, page);
    }

    /**
     * Initializes the warning page.
     */
    private ModelAndView doWarningPage(AuditForm form, String errorKey, String[] params, String page, String section) {
        GlobalVariables.getMessageMap().clearErrorMessages();
        GlobalVariables.getMessageMap().putWarningForSectionId(section, errorKey, params);
        return getUIFModelAndView(form, page);
    }
}


