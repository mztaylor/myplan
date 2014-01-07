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

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import edu.uw.kuali.student.myplan.util.DegreeAuditHelperImpl;
import edu.uw.kuali.student.myplan.util.DegreeAuditHelperImpl.Choice;
import edu.uw.kuali.student.myplan.util.PlanHelperImpl;
import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.audit.dataobject.CourseItem;
import org.kuali.student.myplan.audit.dataobject.MessyItem;
import org.kuali.student.myplan.audit.dataobject.MessyTermDataObject;
import org.kuali.student.myplan.audit.dataobject.PlanAuditItem;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.form.AuditForm;
import org.kuali.student.myplan.audit.form.DegreeAuditForm;
import org.kuali.student.myplan.audit.form.PlanAuditForm;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.audit.util.DegreeAuditHelper;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.service.PlannedTermsHelperBase;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.core.organization.service.OrganizationService;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;
import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT;
import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.*;
import static org.kuali.student.myplan.course.util.CourseSearchConstants.CONTEXT_INFO;

@Controller
@RequestMapping(value = "/audit/**")
public class DegreeAuditController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(DegreeAuditController.class);

    //this is the Id of seattle campus
    private final String DEFAULT_CAMPUS_ID = "306";

    private DegreeAuditService degreeAuditService;

    private OrganizationService organizationService;

    private AcademicPlanService academicPlanService;

    private CourseOfferingService courseOfferingService;

    @Autowired
    private CourseHelper courseHelper;

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Autowired
    private PlanHelper planHelper;

    private DegreeAuditHelper degreeAuditHelper;


    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return new AuditForm();
    }


    /**
     * Method used to load the degree audit page
     * url: http://localhost:8080/student/myplan/audit?methodToCall=audit&viewId=DegreeAudit-FormView
     *
     * @param auditForm
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=audit")
    public ModelAndView audit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        String degreeAuditId = null;
        String planAuditId = null;
        DegreeAuditForm degreeAuditForm = auditForm.getDegreeAudit();
        PlanAuditForm planAuditForm = auditForm.getPlanAudit();
        if (degreeAuditForm != null) {
            degreeAuditId = degreeAuditForm.getAuditId();
        }
        if (planAuditForm != null) {
            planAuditId = planAuditForm.getAuditId();
        }

        try {
            String regId = getUserSessionHelper().getStudentId();
            if (StringUtils.hasText(regId)) {
                logger.info("audit regId " + regId);

                DegreeAuditService degreeAuditService = getDegreeAuditService();

                /*TODO:Default value should be set in UI, Remove adding the default value in code once fix for
                 KULRICE-9846 is in*/
                degreeAuditForm.setCampusParam(DEFAULT_CAMPUS_ID);
                planAuditForm.setCampusParam(DEFAULT_CAMPUS_ID);

                if (DegreeAuditConstants.DEGREE_AUDIT_VIEW_ID.equals(auditForm.getViewId())) {
                    Date startDate = new Date();
                    Date endDate = new Date();


                    List<AuditReportInfo> reportList = degreeAuditService.getAuditsForStudentInDateRange(regId, startDate,
                            endDate, DegreeAuditConstants.CONTEXT_INFO);

                    Map<String, PlanAuditItem> auditsInLearningPlan = getDegreeAuditHelper().getPlanItemSnapShots(regId);

                    if (degreeAuditId == null) {
                        // Grab first degree audit
                        for (AuditReportInfo report : reportList) {
                            if (!report.isWhatIfAudit() && !auditsInLearningPlan.containsKey(report.getAuditId())) {
                                degreeAuditId = report.getAuditId();
                                break;
                            }
                        }
                    }
                    // TODO: For now we are getting the auditType from the end user. This needs to be removed before going
                    // live and hard coded to audit type key html
                    if (StringUtils.hasText(degreeAuditId)) {
                        AuditReportInfo degreeReport = degreeAuditService.getAuditReport(degreeAuditId,
                                degreeAuditForm.getAuditType(), DegreeAuditConstants.CONTEXT_INFO);
                        degreeAuditForm.setAuditId(degreeAuditId);
                        getDegreeAuditHelper().copyCampusToForm(degreeReport, degreeAuditForm);
                        copyReportToForm(degreeReport, degreeAuditForm);
                    }
                }
                if (DegreeAuditConstants.PLAN_AUDIT_VIEW_ID.equals(auditForm.getViewId())) {
                    if (planAuditId == null) {
                        // Grab first plan audit Id
                        planAuditId = getRecentPlanAudit(regId);
                    }
                    if (StringUtils.hasText(planAuditId)) {
                        AuditReportInfo planReport = degreeAuditService.getAuditReport(planAuditId, planAuditForm.getAuditType(), DegreeAuditConstants.CONTEXT_INFO);
                        planAuditForm.setAuditId(planAuditId);
                        getDegreeAuditHelper().copyCampusToForm(planReport, planAuditForm);
                        copyReportToForm(planReport, planAuditForm);
                    }
                }

            }

            auditForm.setPlanExists(doesPlannedCourseExist());

        } catch (DataRetrievalFailureException e) {
            logger.error("audit failed", e);
            auditForm.setPageId(DegreeAuditConstants.AUDIT_NON_STUDENT_PAGE);
        } catch (Exception e) {
            logger.error("audit failed", e);
        }


        return getUIFModelAndView(auditForm);
    }


    /**
     * Method is used to check if at least one planned course exists in the student's Academic plan from the current
     * plannable term
     *
     * @return
     */
    private boolean doesPlannedCourseExist() {
        try {
            List<LearningPlanInfo> learningPlanList =
                    getAcademicPlanService().getLearningPlansForStudentByType(getUserSessionHelper().getStudentId(),
                            LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);

            for (LearningPlanInfo learningPlanInfo : learningPlanList) {
                String learningPlanID = learningPlanInfo.getId();
                List<PlanItemInfo> planItemInfoList =
                        getAcademicPlanService().getPlanItemsInPlanByType(learningPlanID,
                                PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, CONTEXT_INFO);
                if (!CollectionUtils.isEmpty(planItemInfoList)) {
                    AtpHelper.YearTerm firstOpenForPlanningTerm = AtpHelper.atpToYearTerm(AtpHelper.getFirstPlanTerm());
                    for (PlanItemInfo planItemInfo : planItemInfoList) {
                        if (!CollectionUtils.isEmpty(planItemInfo.getPlanPeriods()) &&
                                PlanConstants.COURSE_TYPE.equals(planItemInfo.getRefObjectType()) &&
                                StringUtils.hasText(getCourseHelper().getVerifiedCourseId(planItemInfo.getRefObjectId()))) {
                            AtpHelper.YearTerm planItemYearTerm =
                                    AtpHelper.atpToYearTerm(planItemInfo.getPlanPeriods().get(0));
                            if (planItemYearTerm.compareTo(firstOpenForPlanningTerm) >= 0) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e)

        {
            logger.error("Error loading Plan items ", e);
        }

        return false;
    }

    /**
     * Method to copy the audit html report on to the form
     *
     * @param report
     * @param form
     * @throws IOException
     */
    private void copyReportToForm(AuditReportInfo report, DegreeAuditForm form) throws IOException {

        InputStream in = report.getReport().getDataSource().getInputStream();
        StringWriter sw = new StringWriter();

        int c = 0;
        while ((c = in.read()) != -1) {
            sw.append((char) c);
        }

        String html = sw.toString();
        form.setAuditHtml(html);
    }


    /**
     * Method to run a regular degree audit
     *
     * @param auditForm
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=runAudit")
    public ModelAndView runAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                 HttpServletRequest request, HttpServletResponse response) {
        DegreeAuditForm form = auditForm.getDegreeAudit();
        try {
            String regid = getUserSessionHelper().getStudentId();
            if (StringUtils.hasText(regid)) {
                String programId = getDegreeAuditHelper().getFormProgramID(form);
                if (!programId.equalsIgnoreCase(DegreeAuditConstants.DEFAULT_KEY)) {
                    DegreeAuditService degreeAuditService = getDegreeAuditService();
                    ContextInfo context = new ContextInfo();
                    String auditType = form.getAuditType();
                    AuditReportInfo info = degreeAuditService.runAudit(regid, programId, auditType, context);
                    String auditID = info.getAuditId();
                    // TODO: For now we are getting the auditType from the end user. This needs to be remvoed before
                    // going live and hard coded to audit type key html
                    AuditReportInfo report = degreeAuditService.getAuditReport(auditID, auditType, context);
                    copyReportToForm(report, form);
                } else {
                    String[] params = {};
                    GlobalVariables.getMessageMap().putError(DEGREE_AUDIT_PROGRAM_PARAM_SEATTLE,
                            DegreeAuditConstants.AUDIT_RUN_FAILED, params);
                    form.setAuditHtml(String.format(DegreeAuditConstants.AUDIT_FAILED_HTML, DegreeAuditConstants.AUDIT_STATUS_ERROR_MSG, DegreeAuditConstants.AUDIT_TYPE_DEGREE));
                }
            }

        } catch (DataRetrievalFailureException e) {
            String[] params = {};
            GlobalVariables.getMessageMap().putError(DEGREE_AUDIT_PROGRAM_PARAM_SEATTLE,
                    DegreeAuditConstants.NO_SYSTEM_KEY, params);

        } catch (Exception e) {
            logger.error("Could not complete audit run");
            String[] params = {};
            GlobalVariables.getMessageMap().putError(DEGREE_AUDIT_PROGRAM_PARAM_SEATTLE,
                    DegreeAuditConstants.AUDIT_RUN_FAILED, params);
            Throwable cause = e.getCause();
            if (cause != null) {
                String message = cause.getMessage();
                if (message != null) {
                    String errorMessage = getErrorMessageFromXml(message);
                    String html = String.format(DegreeAuditConstants.AUDIT_FAILED_HTML, errorMessage, DegreeAuditConstants.AUDIT_TYPE_DEGREE);
                    form.setAuditHtml(html);
                }
            }
        }
        return getUIFModelAndView(auditForm);
    }

    /**
     * Method to run a plan audit
     *
     * @param auditForm
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=runPlanAudit")
    public ModelAndView runPlanAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                     HttpServletRequest request, HttpServletResponse response) {
        PlanAuditForm form = auditForm.getPlanAudit();
        // Plan Audit Report Process
        try {
            String regid = getUserSessionHelper().getStudentId();
            if (StringUtils.hasText(regid)) {
                String programId = getDegreeAuditHelper().getFormProgramID(form);
                if (!programId.equals(DegreeAuditConstants.DEFAULT_KEY)) {
                    ContextInfo context = new ContextInfo();
                    context.setPrincipalId(regid);
                    DegreeAuditService degreeAuditService = getDegreeAuditService();
                    List<LearningPlanInfo> learningPlanList =
                            getAcademicPlanService().getLearningPlansForStudentByType(regid, LEARNING_PLAN_TYPE_PLAN,
                                    CONTEXT_INFO);
                    for (LearningPlanInfo learningPlan : learningPlanList) {
                        /*LearningPlanInfo learningPlanInfo = getAcademicPlanService().copyLearningPlan(learningPlan.getId(),
                                AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT, context);*/
                        LearningPlanInfo learningPlanInfo = getAcademicPlanService().copyLearningPlan(learningPlan.getId(),
                                context);
                        String learningPlanInfoId = learningPlanInfo.getId();

                        savePlanItemSnapshots(form, learningPlanInfoId);
                        String auditId = degreeAuditService.runWhatIfAuditAsync(regid, programId, form.getAuditType(),
                                learningPlanInfoId, context);
                        AuditReportInfo report = degreeAuditService.getAuditReport(auditId, form.getAuditType(), context);


                        copyReportToForm(report, form);
                        break;
                    }

                } else {
                    String[] params = {};
                    GlobalVariables.getMessageMap().putError(PLAN_AUDIT_PROGRAM_PARAM_SEATTLE,
                            DegreeAuditConstants.AUDIT_RUN_FAILED, params);
                    form.setAuditHtml(String.format(DegreeAuditConstants.AUDIT_FAILED_HTML, DegreeAuditConstants.AUDIT_STATUS_ERROR_MSG, DegreeAuditConstants.AUDIT_TYPE_PLAN));
                }
            }

        } catch (DataRetrievalFailureException e) {
            String[] params = {};
            form.setCampusParam("306");
            GlobalVariables.getMessageMap().putError(PLAN_AUDIT_PROGRAM_PARAM_SEATTLE,
                    DegreeAuditConstants.NO_SYSTEM_KEY, params);

        } catch (Exception e) {
            logger.error("Could not complete audit run", e);
            String[] params = {};
            GlobalVariables.getMessageMap().putError(PLAN_AUDIT_PROGRAM_PARAM_SEATTLE,
                    DegreeAuditConstants.AUDIT_RUN_FAILED, params);
            Throwable cause = e.getCause();
            if (cause != null) {
                String message = cause.getMessage();
                if (message != null) {
                    String errorMessage = getErrorMessageFromXml(message);
                    String html = String.format(DegreeAuditConstants.AUDIT_FAILED_HTML, errorMessage, DegreeAuditConstants.AUDIT_TYPE_PLAN);
                    form.setAuditHtml(html);
                }
            }
        }
        return getUIFModelAndView(auditForm);
    }

    /**
     * creates the planItem snapshots with the course items info to be used for running plan audit
     *
     * @param form
     * @param learningPlanId
     */
    private void savePlanItemSnapshots(PlanAuditForm form, String learningPlanId) {
        try {
            List<PlanItemInfo> planItemInfos = getAcademicPlanService().getPlanItemsInPlan(learningPlanId, CONTEXT_INFO);

            Map<String, PlanItemInfo> planItemInfoMap = new HashMap<String, PlanItemInfo>();

            for (PlanItemInfo item : planItemInfos) {
                boolean isCourse = PlanConstants.COURSE_TYPE.equalsIgnoreCase(item.getRefObjectType());
                boolean isPlanned =
                        AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equalsIgnoreCase(item.getTypeKey());
                if (isCourse && isPlanned) {
                    String crossListedCourse = getPlanHelper().getCrossListedCourse(item.getAttributes());
                    CourseInfo courseInfo = getCourseHelper().getCourseInfoByIdAndCd(item.getRefObjectId(), crossListedCourse);
                    if (courseInfo != null) {
                        // Version independent id + term = key
                        String key = item.getRefObjectId() + item.getPlanPeriods().get(0) + courseInfo.getSubjectArea().trim() + courseInfo.getCourseNumberSuffix().trim();
                        planItemInfoMap.put(key, item);
                    }
                }
            }

            for (MessyTermDataObject messyTerm : form.getMessyItems()) {
                for (MessyItem item : messyTerm.getMessyItemList()) {
                    String key = item.getVersionIndependentId() + item.getAtpId() + item.getSubject() + item.getNumber();
                    PlanItemInfo planItem = planItemInfoMap.get(key);
                    if (planItem != null) {
                        String choiceKey = item.getSelectedCredit();
                        Choice choice = new Choice();
                        choice = choice.build(choiceKey);
                        List<AttributeInfo> list = planItem.getAttributes();
                        list.add(new AttributeInfo(BUCKET, BUCKET_MESSY));
                        list.add(new AttributeInfo(CHOICE, choiceKey));
                        list.add(new AttributeInfo(SECTION, choice.section));
                        list.add(new AttributeInfo(SECONDARY_ACTIVITY, choice.secondaryActivity));
                        if (StringUtils.hasText(choice.credit)) {
                            planItem.setCredit(new BigDecimal(choice.credit));
                        }

                        getAcademicPlanService().updatePlanItem(planItem.getId(), planItem, CONTEXT_INFO);
                    }
                }
            }

            for (CourseItem item : form.getCleanList()) {
                PlanItemInfo planItem = planItemInfoMap.get(item.getCourseId() + item.getAtpId() + item.getSubject() + item.getNumber());
                if (planItem != null) {
                    List<AttributeInfo> list = planItem.getAttributes();

                    list.add(new AttributeInfo(BUCKET, BUCKET_CLEAN));
                    list.add(new AttributeInfo(SECTION, item.getSectionCode()));
                    list.add(new AttributeInfo(SECONDARY_ACTIVITY, item.getSecondaryActivityCode()));
                    if (StringUtils.hasText(item.getCredit())) {
                        planItem.setCredit(new BigDecimal(item.getCredit()));
                    }

                    getAcademicPlanService().updatePlanItem(planItem.getId(), planItem, CONTEXT_INFO);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * A map of Credit snapshots that student chose previously are returned back with course version independentId as key
     *
     * @return
     */
    private Map<String, String> getPlanItemSnapShots() {
        Map<String, String> map = new HashMap<String, String>();

        String regid = getUserSessionHelper().getStudentId();

        PlannedTermsHelperBase plannedTermsHelperBase = new PlannedTermsHelperBase();
        List<PlanItemInfo> itemList =
                plannedTermsHelperBase.getLatestSnapShotPlanItemsByRefObjType(PlanConstants.COURSE_TYPE, regid);

        for (PlanItemInfo item : itemList) {
            String bucketType = BUCKET_IGNORE;

            String choice = null;
            for (AttributeInfo attrib : item.getAttributes()) {
                String key = attrib.getKey();
                String value = attrib.getValue();
                if (BUCKET.equals(key)) {
                    bucketType = value;
                } else if (CHOICE.equals(key)) {
                    choice = value;
                }
            }
            if (BUCKET_MESSY.equals(bucketType) && choice != null) {
                String key = item.getRefObjectId() + item.getPlanPeriods().get(0);
                map.put(key, choice);

            }
        }
        return map;
    }


    /**
     * Method to review the plan audit request.
     *
     * @param auditForm
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=reviewPlanAudit")
    public ModelAndView reviewPlanAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {
        logger.info("Started the hand off screen at" + System.currentTimeMillis());

        //Processing the Handoff logic and adding the messy and clean courses for plan audit
        PlanAuditForm form = getDegreeAuditHelper().processHandOff(auditForm.getPlanAudit(),
                getUserSessionHelper().getStudentId());

        int selectionsNeeded = 0;

        if (!form.getMessyItems().isEmpty()) {
            Map<String, String> prevChoices = getPlanItemSnapShots();

            for (MessyTermDataObject messyTerm : form.getMessyItems()) {

                for (MessyItem messy : messyTerm.getMessyItemList()) {
                    String key = messy.getVersionIndependentId() + messy.getAtpId();
                    if (prevChoices.containsKey(key)) {
                        String choice = prevChoices.get(key);
                        messy.setSelectedCredit(choice);
                    } else {
                        selectionsNeeded++;
                    }
                }
            }

        }

        boolean showHandOffScreen = !(form.getMessyItems().isEmpty() && form.getIgnoreList().isEmpty());
        form.setShowHandOffScreen(showHandOffScreen);
        logger.info("Ended the hand off screen at" + System.currentTimeMillis());

        /*Log used to know how many selection a adviser needed from student and also to know
            at what rate are the advisers seeing this screen for selections from students*/
        if (getUserSessionHelper().isAdviser() && selectionsNeeded > 0) {
            logger.info(String.format("Adviser needs %s selections from student to run the Plan Audit", selectionsNeeded));
        }
        return getUIFModelAndView(auditForm);

    }

    /**
     * Used to get the status of the audit that is currently in execution
     *
     * @param response
     * @param request
     */
    @RequestMapping(value = "/status")
    public void getJsonResponse(HttpServletResponse response, HttpServletRequest request) {
        String programId = request.getParameter("programId").replace("$", " ");
        String auditId = request.getParameter("auditId");
        String regId = getUserSessionHelper().getStudentId();

        try {
            String status = getDegreeAuditService().getAuditStatus(regId, programId, auditId);
            response.getWriter().printf("{\"status\":\"%s\"}", status);
        } catch (Exception e) {
            logger.warn("cannot retrieve audit status", e);
        }
    }


    /**
     * Strips out the error message from the xml
     *
     * @param xmlString
     * @return
     */
    private String getErrorMessageFromXml(String xmlString) {
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
     * returns recently ran planAudit ID from Snapshots
     *
     * @param studentId
     * @return
     */
    private String getRecentPlanAudit(String studentId) {
        String recentPlanAuditId = null;
        try {
            List<LearningPlanInfo> learningPlanList =
                    getAcademicPlanService().getLearningPlansForStudentByType(studentId, LEARNING_PLAN_TYPE_PLAN_AUDIT,
                            PlanConstants.CONTEXT_INFO);
            Collections.reverse(learningPlanList);
            for (LearningPlanInfo learningPlanInfo : learningPlanList) {
                if (recentPlanAuditId != null) {
                    break;
                }
                for (AttributeInfo attributeInfo : learningPlanInfo.getAttributes()) {
                    String key = attributeInfo.getKey();
                    String value = attributeInfo.getValue();
                    if ("auditId".equalsIgnoreCase(key) && StringUtils.hasText(value)) {
                        /*TODO: cache this getAuditStatus method*/
                        StatusInfo statusInfo = degreeAuditService.getAuditRunStatus(value, PlanConstants.CONTEXT_INFO);
                        if (statusInfo.getIsSuccess() != null) {
                            recentPlanAuditId = value;
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not retrieve the latest Plan Audit Id", e);
        }
        return recentPlanAuditId;
    }


    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(
                    new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    public PlanHelper getPlanHelper() {
        if (planHelper == null) {
            planHelper = new PlanHelperImpl();
        }
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

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

    public OrganizationService getOrganizationService() {
        if (this.organizationService == null) {
            //   TODO: Use constants for namespace.
            this.organizationService = (OrganizationService) GlobalResourceLoader.getService(
                    new QName("http://student.kuali.org/wsdl/organization", "orgService"));
        }
        return this.organizationService;
    }


    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setOrganizationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    public DegreeAuditHelper getDegreeAuditHelper() {
        if (degreeAuditHelper == null) {
            degreeAuditHelper = new DegreeAuditHelperImpl();
        }
        return degreeAuditHelper;
    }

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}


