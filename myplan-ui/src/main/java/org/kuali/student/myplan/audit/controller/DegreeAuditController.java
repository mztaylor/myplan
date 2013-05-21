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
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.audit.dataobject.CourseItem;
import org.kuali.student.myplan.audit.dataobject.MessyItem;
import org.kuali.student.myplan.audit.dataobject.MessyItemDataObject;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.form.AuditForm;
import org.kuali.student.myplan.audit.form.DegreeAuditForm;
import org.kuali.student.myplan.audit.form.PlanAuditForm;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.dataobject.ServicesStatusDataObject;
import org.kuali.student.myplan.plan.service.PlanItemLookupableHelperBase;
import org.kuali.student.myplan.plan.service.PlannedTermsHelperBase;
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
import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.*;
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
//        boolean systemKeyExists = true;
        DegreeAuditForm degreeAuditForm = auditForm.getDegreeAudit();
        PlanAuditForm planAuditForm = auditForm.getPlanAudit();
        try {
            Map<String, String> campusMap = populateCampusMap();
            ServicesStatusDataObject servicesStatusDataObject = (ServicesStatusDataObject) request.getSession().getAttribute(CourseSearchConstants.SWS_SERVICES_STATUS);
//            Person user = GlobalVariables.getUserSession().getPerson();
            String regId = UserSessionHelper.getStudentRegId();
            if (StringUtils.hasText(regId)) {
                DegreeAuditService degreeAuditService = getDegreeAuditService();
                String degreeAuditId = degreeAuditForm.getAuditId();
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
                    for (AuditReportInfo auditReportInfo : auditReportInfoList) {
                        if (!auditReportInfo.isWhatIfAudit()) {
                            degreeAuditId = auditReportInfo.getAuditId();
                            degreeProgramParam = auditReportInfo.getProgramTitle();
                            break;
                        }
                    }

                    if (planAuditId == null) {
                        for (AuditReportInfo auditReportInfo : auditReportInfoList) {
                            if (auditReportInfo.isWhatIfAudit()) {
                                planAuditId = auditReportInfo.getAuditId();
                                planProgramParam = auditReportInfo.getProgramTitle();
                                break;
                            }
                        }

                    }
                    // TODO: For now we are getting the auditType from the end user. This needs to be removed before going live and hard coded to audit type key html
                    if (degreeAuditId != null) {
                        AuditReportInfo auditReportInfo = degreeAuditService.getAuditReport(degreeAuditId, degreeAuditForm.getAuditType(), contextInfo);
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
            //Check to see if the stddent has any planItems from current to future atp
            PlanItemLookupableHelperBase planHelper = new PlanItemLookupableHelperBase();
            String startAtp = null;
            for (String term : AtpHelper.getPublishedTerms()) {
                if (AtpHelper.isAtpSetToPlanning(term)) {
                    startAtp = term;
                    break;
                }
            }
            List<PlannedCourseDataObject> planItems = planHelper.getPlannedCoursesFromAtp(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, UserSessionHelper.getStudentRegId(), startAtp);
            if (planItems != null && !planItems.isEmpty()) {
                auditForm.setPlanExists(true);
            }
        } catch (DataRetrievalFailureException e) {
            logger.error("audit failed", e);
//            systemKeyExists = false;
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
//            Person user = GlobalVariables.getUserSession().getPerson();
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
//                    String preparedFor = user.getLastName() + ", " + user.getFirstName();
//                    html = html.replace("$$PreparedFor$$", preparedFor);
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

    /**
     * @param
     * @return
     */
//    private List<CourseItem> getCleanCourseItems(PlanAuditForm planAuditForm, String learningPlanId) {
//        List<CourseItem> courseItems = planAuditForm.getCleanList();
//        if (learningPlanId != null) {
//            Map<String, PlanItemInfo> planItemInfoMap = getPlanItemsMapByRefObjId(learningPlanId);
//            for (MessyItemDataObject messyItemDataObject : planAuditForm.getMessyItems()) {
//                for (MessyItem messyItem : messyItemDataObject.getMessyItemList()) {
//                    if (!DegreeAuditConstants.DEFAULT_KEY.equalsIgnoreCase(messyItem.getSelectedCredit())) {
//                        String[] str = messyItem.getSelectedCredit().split(":");
//                        CourseItem courseItem = new CourseItem();
//                        courseItem.setAtpId(messyItemDataObject.getAtpId());
//                        courseItem.setCourseCode(messyItem.getCourseCode());
//                        courseItem.setCredit(str[1]);
//                        courseItem.setSectionCode(str[0].isEmpty() ? null : str[0]);
//                        courseItem.setCourseId(messyItem.getCourseId());
//                        courseItems.add(courseItem);
//                        PlanItemInfo planItemInfo = planItemInfoMap.get(messyItem.getVersionIndependentId());
//                        if (planItemInfo != null) {
//                            List<AttributeInfo> attributeInfos = new ArrayList<AttributeInfo>();
//                            attributeInfos.add(new AttributeInfo(CREDIT, str[1]));
//                            attributeInfos.add(new AttributeInfo(HONORS_CREDIT, String.valueOf(str[2].contains(HONORS_CREDIT))));
//                            attributeInfos.add(new AttributeInfo(WRITING_CREDIT, String.valueOf(str[2].contains(WRITING_CREDIT))));
//                            attributeInfos.add(new AttributeInfo(SECTION_SELECTED, str[0].isEmpty() ? null : str[0]));
//                            planItemInfo.setAttributes(attributeInfos);
//                            try {
//                                getAcademicPlanService().updatePlanItem(planItemInfo.getId(), planItemInfo, CONTEXT_INFO);
//                            } catch (Exception e) {
//                                logger.error("Could not update the PlanitemInfo for PlanId:" + planItemInfo.getId() + e);
//                            }
//                        }
//                    }
//
//
//                }
//
//            }
//        }
//        return courseItems;
//    }
    @RequestMapping(params = "methodToCall=runPlanAudit")
    public ModelAndView runPlanAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                     HttpServletRequest request, HttpServletResponse response) {
        PlanAuditForm form = auditForm.getPlanAudit();
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
        /*TODO: uncomment once the hand-off screen is completed*/


        /******Plan Audit Report Process*********/
        try {
            String regid = UserSessionHelper.getStudentRegId();
            if (StringUtils.hasText(regid)) {
                String programId = null;
                String campusParam = form.getCampusParam();
                if ("306".equals(campusParam)) {
                    programId = form.getProgramParamSeattle();

                } else if ("310".equals(campusParam)) {
                    programId = form.getProgramParamBothell();

                } else if ("323".equals(campusParam)) {
                    programId = form.getProgramParamTacoma();

                }
                if (!programId.equalsIgnoreCase(DegreeAuditConstants.DEFAULT_KEY)) {
                    Person user = GlobalVariables.getUserSession().getPerson();
                    ContextInfo context = new ContextInfo();
                    context.setPrincipalId(regid);
                    String learningPlanInfoId = null;
                    List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(regid, LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);
                    for (LearningPlanInfo learningPlan : learningPlanList) {
                        LearningPlanInfo learningPlanInfo = getAcademicPlanService().copyLearningPlan(learningPlan.getId(), AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT, context);
                        learningPlanInfoId = learningPlanInfo.getId();
                        break;
                    }

                    DegreeAuditService degreeAuditService = getDegreeAuditService();

                    AuditReportInfo report = degreeAuditService.runWhatIfAudit(regid, programId, form.getAuditType(), learningPlanInfoId, context);
                    InputStream in = report.getReport().getDataSource().getInputStream();
                    StringWriter sw = new StringWriter();

                    int c = 0;
                    while ((c = in.read()) != -1) {
                        sw.append((char) c);
                    }

                    String html = sw.toString();
                    form.setAuditHtml(html);

                } else {
                    String[] params = {};
                    GlobalVariables.getMessageMap().putError("planAudit.programParamSeattle", DegreeAuditConstants.AUDIT_RUN_FAILED, params);
                    form.setAuditHtml(String.format(DegreeAuditConstants.AUDIT_FAILED_HTML, ConfigContext.getCurrentContextConfig().getProperty(DegreeAuditConstants.APPLICATION_URL)));
                }
            }

        } catch (DataRetrievalFailureException e) {
            String[] params = {};
            form.setCampusParam("306");
            GlobalVariables.getMessageMap().putError("planAudit.programParamSeattle", DegreeAuditConstants.NO_SYSTEM_KEY, params);

        } catch (Exception e) {
            logger.error("Could not complete audit run", e);
            String[] params = {};
            GlobalVariables.getMessageMap().putError("planAudit.programParamSeattle", DegreeAuditConstants.AUDIT_RUN_FAILED, params);
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

    @RequestMapping(params = "methodToCall=reviewPlanAudit")
    public ModelAndView reviewPlanAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {

        PlanAuditForm form = auditForm.getPlanAudit();
        List<CourseItem> courseItems = new ArrayList<CourseItem>();
        PlannedTermsHelperBase plannedTermsHelperBase = new PlannedTermsHelperBase();
        Map<String, String> planItemSnapShots = getPlanItemSnapShots();
        List<PlannedTerm> plannedTermList = plannedTermsHelperBase.getPlannedTermsFromStartAtp();
        List<String> scheduledTerms = AtpHelper.getPublishedTerms();
        Map<String, List<MessyItem>> messyItemMap = new HashMap<String, List<MessyItem>>();
        for (PlannedTerm plannedTerm : plannedTermList) {
            //TS PUBLISHED: YES
            String atpId = plannedTerm.getAtpId();
            if (!AtpHelper.isAtpCompletedTerm(atpId) && scheduledTerms.contains(atpId)) {
                for (PlannedCourseDataObject plannedCourse : plannedTerm.getPlannedList()) {
                    Set<String> credits = new HashSet<String>();
                    // Planned Sections : YES ; Multiple planned sections : NO
                    if (plannedCourse.getPlanActivities().size() == 1 && plannedCourse.getPlanActivities().get(0).isPrimary()) {
                        String section = plannedCourse.getPlanActivities().get(0).getCode();
                        String credit = plannedCourse.getPlanActivities().get(0).getCredits();
                        String creditType = getCreditType(credit);
                        // Is section Offered in Single value : NO
                        if ("MULTIPLE".equalsIgnoreCase(creditType)) {
                            String[] crs = credit.replace(" ", "").split(",");
                            for (String cr : crs) {
                                credits.add(String.format("%s:%s:%s", section, cr, cr));
                            }
                            buildMessyItemAndAddToMap(plannedCourse, credits, messyItemMap, planItemSnapShots);
                        }
                        // Is section Offered in Single value : NO
                        else if ("RANGE".equalsIgnoreCase(creditType)) {
                            List<String> crs = getCreditsForRange(credit);
                            for (String cr : crs) {
                                credits.add(String.format("%s:%s:%s", section, cr, cr));
                            }
                            buildMessyItemAndAddToMap(plannedCourse, credits, messyItemMap, planItemSnapShots);
                        }
                        // Is course Offered in Single value : YES
                        else {
                            buildCourseItemAndAddToList(plannedCourse, credit, section, courseItems);
                        }

                    }
                    // Planned Sections : YES ; Multiple planned sections : YES
                    else if (plannedCourse.getPlanActivities().size() > 1) {
                        Set<String> choicesList = processSectionProfile(plannedCourse.getPlanActivities());
                        if (choicesList.size() > 1) {
                            buildMessyItemAndAddToMap(plannedCourse, choicesList, messyItemMap, planItemSnapShots);
                        } else if (choicesList.size() == 1) {
                            buildCourseItemAndAddToList(plannedCourse, (String) choicesList.toArray()[0], null, courseItems);
                        }

                    }
                    // Planned Sections : NO
                    else {
                        CourseDetailsInquiryHelperImpl courseDetailsInquiryHelper = new CourseDetailsInquiryHelperImpl();
                        List<ActivityOfferingItem> activityOfferingItems = courseDetailsInquiryHelper.getActivityOfferingItemsById(plannedCourse.getCourseDetails().getCourseId(), plannedCourse.getPlanItemDataObject().getAtp());
                        Set<String> choicesList = processSectionProfile(activityOfferingItems);
                        if (choicesList.size() > 1) {
                            buildMessyItemAndAddToMap(plannedCourse, choicesList, messyItemMap, planItemSnapShots);
                        } else if (choicesList.size() == 1) {
                            buildCourseItemAndAddToList(plannedCourse, (String) choicesList.toArray()[0], null, courseItems);
                        } else {
                            String credit = plannedCourse.getCourseDetails().getCredit();
                            String creditType = getCreditType(credit);
                            // Is course Offered in Single value : NO
                            if ("MULTIPLE".equalsIgnoreCase(creditType)) {
                                String[] crs = credit.replace(" ", "").split(",");
                                for (String cr : crs) {
                                    credits.add(String.format("%s:%s:%s", "", cr, cr));
                                }
                                buildMessyItemAndAddToMap(plannedCourse, credits, messyItemMap, planItemSnapShots);
                            }
                            // Is course Offered in Single value : NO
                            else if ("RANGE".equalsIgnoreCase(creditType)) {
                                List<String> crs = getCreditsForRange(credit);
                                for (String cr : crs) {
                                    credits.add(String.format("%s:%s:%s", "", cr, cr));
                                }
                                buildMessyItemAndAddToMap(plannedCourse, credits, messyItemMap, planItemSnapShots);
                            }
                            // Is course Offered in Single value : YES
                            else {
                                buildCourseItemAndAddToList(plannedCourse, String.format("%s:%s:%s", "", credit, credit), null, courseItems);

                            }
                        }
                    }
                }

            }
            //TS PUBLISHED: NO
            else if (!AtpHelper.isAtpCompletedTerm(atpId)) {
                Set<String> credits = new HashSet<String>();
                for (PlannedCourseDataObject plannedCourseDataObject : plannedTerm.getPlannedList()) {
                    String credit = plannedCourseDataObject.getCourseDetails().getCredit();
                    String creditType = getCreditType(credit);
                    // Is course Offered in Single value : NO
                    if ("MULTIPLE".equalsIgnoreCase(creditType)) {
                        String[] crs = credit.replace(" ", "").split(",");
                        for (String cr : crs) {
                            credits.add(String.format("%s:%s:%s", "", cr, cr));
                        }
                        buildMessyItemAndAddToMap(plannedCourseDataObject, credits, messyItemMap, planItemSnapShots);
                    }
                    // Is course Offered in Single value : NO
                    else if ("RANGE".equalsIgnoreCase(creditType)) {
                        List<String> crs = getCreditsForRange(credit);
                        for (String cr : crs) {
                            credits.add(String.format("%s:%s:%s", "", cr, cr));
                        }
                        buildMessyItemAndAddToMap(plannedCourseDataObject, credits, messyItemMap, planItemSnapShots);
                    }
                    // Is course Offered in Single value : YES
                    else {
                        buildCourseItemAndAddToList(plannedCourseDataObject, credit, null, courseItems);

                    }

                }


            }
        }
        if (courseItems.size() > 0) {
            form.setCleanList(courseItems);
        }
        if (messyItemMap.size() > 0) {
            List<MessyItemDataObject> messyItemDataObjects = new ArrayList<MessyItemDataObject>();
            for (Map.Entry<String, List<MessyItem>> entry : messyItemMap.entrySet()) {
                MessyItemDataObject messyItemDataObject = new MessyItemDataObject();
                messyItemDataObject.setAtpId(entry.getKey());
                messyItemDataObject.setMessyItemList(entry.getValue());
                messyItemDataObjects.add(messyItemDataObject);
            }
            Collections.sort(messyItemDataObjects, new Comparator<MessyItemDataObject>() {
                @Override
                public int compare(MessyItemDataObject val1, MessyItemDataObject val2) {
                    return val1.getAtpId().compareTo(val2.getAtpId());
                }
            });
            form.setStudentChoiceRequired(true);
            form.setMessyItems(messyItemDataObjects);
        }
        return getUIFModelAndView(auditForm);
    }

    /**
     * A map of Credit snapshots that student chose previously are returned back with course version independentId as key
     *
     * @return
     */
    private Map<String, String> getPlanItemSnapShots() {
        Map<String, String> planItemSnapShots = new HashMap<String, String>();
        PlannedTermsHelperBase plannedTermsHelperBase = new PlannedTermsHelperBase();
        List<PlanItemInfo> planItemInfos = plannedTermsHelperBase.getLatestSnapShotPlanItemsByRefObjType(PlanConstants.COURSE_TYPE, UserSessionHelper.getStudentRegId());
        for (PlanItemInfo planItemInfo : planItemInfos) {
            String snapShotCredit = null;
            boolean writing = false;
            boolean honors = false;
            String section = "";
            for (AttributeInfo attributeInfo : planItemInfo.getAttributes()) {
                String value = attributeInfo.getValue();
                if (value == null) continue;

                String key = attributeInfo.getKey();
                if (CREDIT.equals(key)) {
                    snapShotCredit = value;
                } else if (WRITING_CREDIT.equals(key)) {
                    writing = Boolean.valueOf(value);
                } else if (HONORS_CREDIT.equals(key)) {
                    honors = Boolean.valueOf(value);
                } else if (SECTION_SELECTED.equalsIgnoreCase(key)) {
                    section = value;
                }
            }
            String refObjectId = planItemInfo.getRefObjectId();
            if (!planItemSnapShots.containsKey(refObjectId) && snapShotCredit != null) {
                String format = String.format("%s:%s:%s", section, snapShotCredit, snapShotCredit + (writing ? " -- " + WRITING_CREDIT : "") + (honors ? " -- " + HONORS_CREDIT : ""));
                planItemSnapShots.put(refObjectId, format);
            }
        }
        return planItemSnapShots;
    }

    /**
     * @return
     */
    private Map<String, PlanItemInfo> getPlanItemsMapByRefObjId(String learningPlanId) {
        Map<String, PlanItemInfo> planItemsMap = new HashMap<String, PlanItemInfo>();
        try {
            List<PlanItemInfo> planItemInfos = getAcademicPlanService().getPlanItemsInPlan(learningPlanId, CONTEXT_INFO);
            for (PlanItemInfo planItemInfo : planItemInfos) {
                planItemsMap.put(planItemInfo.getRefObjectId(), planItemInfo);
            }
        } catch (Exception e) {
            logger.error("Couldnot retrieve planitems for learningPlanId:" + learningPlanId, e);
        }
        return planItemsMap;
    }

    /**
     * processes the section profiling logic
     *
     * @param activityOfferingItems
     * @return
     */
    private Set<String> processSectionProfile(List<ActivityOfferingItem> activityOfferingItems) {
        Set<String> stringSet = new HashSet<String>();
        Set<String> choicesList = new HashSet<String>();
        for (ActivityOfferingItem activityOfferingItem : activityOfferingItems) {
            if (activityOfferingItem.isPrimary()) {

                String section = activityOfferingItem.getCode();
                boolean isHonorSection = activityOfferingItem.isHonorsSection();
                boolean isWritingSection = activityOfferingItem.isWritingSection();
                String credit = activityOfferingItem.getCredits();
                String creditType = getCreditType(credit);
                // Is section Offered in Single value : NO
                if ("MULTIPLE".equalsIgnoreCase(creditType)) {
                    String[] crs = credit.replace(" ", "").split(",");
                    for (String cr : crs) {
                        String display = cr + (isWritingSection ? " -- " + WRITING_CREDIT : "") + (isHonorSection ? " -- " + HONORS_CREDIT : "");
                        if (!stringSet.contains(display)) {
                            stringSet.add(display);
                            choicesList.add(String.format("%s:%s:%s", section, cr, display));
                        }
                    }

                }
                // Is section Offered in Single value : NO
                else if ("RANGE".equalsIgnoreCase(creditType)) {
                    List<String> crs = getCreditsForRange(credit);
                    for (String cr : crs) {
                        String display = cr + (isWritingSection ? " -- " + WRITING_CREDIT : "") + (isHonorSection ? " -- " + HONORS_CREDIT : "");
                        if (!stringSet.contains(display)) {
                            stringSet.add(display);
                            choicesList.add(String.format("%s:%s:%s", section, cr, display));
                        }
                    }
                } else {
                    String display = credit + (isWritingSection ? " -- " + WRITING_CREDIT : "") + (isHonorSection ? " -- " + HONORS_CREDIT : "");
                    if (!stringSet.contains(display)) {
                        stringSet.add(display);
                        choicesList.add(String.format("%s:%s:%s", section, credit, display));
                    }
                }

            }
        }
        return choicesList;
    }

    /**
     * Builds the course item and adds to the list
     *
     * @param plannedCourseDataObject
     * @param credit
     * @param section
     * @param courseItems
     */

    private void buildCourseItemAndAddToList(PlannedCourseDataObject plannedCourseDataObject, String credit, String section, List<CourseItem> courseItems) {
        CourseItem courseItem = new CourseItem();
        courseItem.setAtpId(plannedCourseDataObject.getPlanItemDataObject().getAtp());
        courseItem.setCourseCode(plannedCourseDataObject.getCourseDetails().getCode());
        courseItem.setCourseId(plannedCourseDataObject.getCourseDetails().getCourseId());
        courseItem.setCredit(credit);
        courseItem.setSectionCode(section);
        courseItems.add(courseItem);
    }

    /**
     * Builds the messy item and adds to the Map
     *
     * @param plannedCourseDataObject
     * @param credits
     * @param messyItemMap
     */
    private void buildMessyItemAndAddToMap(PlannedCourseDataObject plannedCourseDataObject, Set<String> credits, Map<String, List<MessyItem>> messyItemMap, Map<String, String> planItemSnapShots) {
        CourseSummaryDetails courseDetails = plannedCourseDataObject.getCourseDetails();
        String versionIndependentId = courseDetails.getVersionIndependentId();

        MessyItem messyItem = new MessyItem();
        messyItem.setCourseCode(courseDetails.getCode());
        messyItem.setCourseTitle(courseDetails.getCourseTitle());
        messyItem.setCourseId(courseDetails.getCourseId());
        messyItem.setVersionIndependentId(versionIndependentId);
        messyItem.setCredits(credits);

        if (planItemSnapShots != null && planItemSnapShots.containsKey(versionIndependentId) && StringUtils.hasText(planItemSnapShots.get(versionIndependentId))) {
            messyItem.setSelectedCredit(planItemSnapShots.get(versionIndependentId));
        }
        String atp = plannedCourseDataObject.getPlanItemDataObject().getAtp();
        messyItem.setAtpId(atp);

        if (messyItemMap.containsKey(atp)) {
            messyItemMap.get(atp).add(messyItem);
        } else {
            List<MessyItem> items = new ArrayList<MessyItem>();
            items.add(messyItem);
            messyItemMap.put(atp, items);
        }
    }

    /**
     * returns type of credit
     * eg: credit 1-4 RANGE
     * credit 1,4 MULTIPLE
     * credit 1 NORMAL
     *
     * @param credit
     * @return
     */
    private String getCreditType(String credit) {
        if (credit.matches("^\\d*.\\d*-\\d*.\\d*$")) {
            return "RANGE";
        } else if (credit.matches("^\\d*.\\d*,\\d*.\\d*$")) {
            return "MULTIPLE";
        } else {
            return "NORMAL";
        }
    }

    /**
     * returns credits for given range (eg:for credit='1-4' gives 1,2,3,4)
     *
     * @param credit
     * @return
     */
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
        try {
            SearchRequest searchRequest = new SearchRequest(CourseSearchConstants.ORG_QUERY_SEARCH_BY_TYPE_REQUEST);
            searchRequest.addParam(ORG_QUERY_PARAM, CAMPUS_LOCATION);
            SearchResult searchResult = new SearchResult();
            searchResult = getOrganizationService().search(searchRequest);
            for (SearchResultRow row : searchResult.getRows()) {

                String name = getCellValue(row, "org.resultColumn.orgShortName");
                // Default campus is Seattle = 0
                String code = "0";
                if ("bothell".equalsIgnoreCase(name)) {
                    code = "1";
                } else if ("tacoma".equalsIgnoreCase(name)) {
                    code = "2";
                }
                String cellValue = getCellValue(row, "org.resultColumn.orgId");
                orgCampusTypes.put(code, cellValue);
            }
        } catch (MissingParameterException e) {
            logger.error("Search Failed to get the Organization Data ", e);
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


