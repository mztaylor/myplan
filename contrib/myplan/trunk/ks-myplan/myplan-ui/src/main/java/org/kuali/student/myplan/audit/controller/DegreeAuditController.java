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
import org.kuali.student.myplan.plan.service.PlanItemLookupableHelperBase;
import org.kuali.student.myplan.plan.service.PlannedTermsHelperBase;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;
import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT;
import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.*;
import static org.kuali.student.myplan.course.util.CourseSearchConstants.CONTEXT_INFO;

// http://localhost:8080/student/myplan/audit?methodToCall=audit&viewId=DegreeAudit-FormView

@Controller
@RequestMapping(value = "/audit/**")
public class DegreeAuditController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(DegreeAuditController.class);

    private transient DegreeAuditService degreeAuditService;

    private static OrganizationService organizationService;

    private transient AcademicPlanService academicPlanService;

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
        DegreeAuditForm degreeAuditForm = auditForm.getDegreeAudit();
        PlanAuditForm planAuditForm = auditForm.getPlanAudit();
        try {
            String regId = UserSessionHelper.getStudentRegId();
            if (StringUtils.hasText(regId)) {
                logger.info("audit regId " + regId);

                DegreeAuditService degreeAuditService = getDegreeAuditService();

                Map<Character, String> campusMap = populateCampusMap();
                String campus = campusMap.get('0');
                degreeAuditForm.setCampusParam(campus);
                planAuditForm.setCampusParam(campus);


                Date startDate = new Date();
                Date endDate = new Date();
                ContextInfo context = new ContextInfo();

                List<AuditReportInfo> reportList = degreeAuditService.getAuditsForStudentInDateRange(regId, startDate, endDate, context);

                // Grab first degree audit
                for (AuditReportInfo report : reportList) {
                    if (!report.isWhatIfAudit()) {
                        String auditId = report.getAuditId();
                        // TODO: For now we are getting the auditType from the end user. This needs to be removed before going live and hard coded to audit type key html
                        AuditReportInfo degreeReport = degreeAuditService.getAuditReport(auditId, degreeAuditForm.getAuditType(), context);
                        degreeAuditForm.setAuditId(auditId);
                        copyCampusToForm(degreeReport, campusMap, degreeAuditForm);
                        copyReportToForm(degreeReport, degreeAuditForm);
                        break;
                    }
                }

                // Grab first plan audit Id
                String planAuditId = getRecentPlanAudit(regId);
                if (StringUtils.hasText(planAuditId)) {
                    AuditReportInfo planReport = degreeAuditService.getAuditReport(planAuditId, planAuditForm.getAuditType(), context);
                    planAuditForm.setAuditId(planAuditId);
                    copyCampusToForm(planReport, campusMap, planAuditForm);
                    copyReportToForm(planReport, planAuditForm);
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
            auditForm.setPageId(DegreeAuditConstants.AUDIT_NON_STUDENT_PAGE);
        } catch (Exception e) {
            logger.error("audit failed", e);
        }


        return getUIFModelAndView(auditForm);
    }

    private void copyCampusToForm(AuditReportInfo report, Map<Character, String> campusMap, DegreeAuditForm form) {
        String programId = report.getProgramId();
        char prefix = programId.charAt(0);
        programId = programId.replace(' ', '$');

        // Impl to set the default values for campusParam and programParam properties
        String campus = campusMap.get(prefix);
        form.setCampusParam(campus);
        switch (prefix) {
            case '0':
                form.setProgramParamSeattle(programId);
                break;
            case '1':
                form.setProgramParamBothell(programId);
                break;
            case '2':
                form.setProgramParamTacoma(programId);
                break;
            default:
                break;
        }
    }

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

    @RequestMapping(params = "methodToCall=runAudit")
    public ModelAndView runAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                 HttpServletRequest request, HttpServletResponse response) {
        DegreeAuditForm form = auditForm.getDegreeAudit();
        try {
            String regid = UserSessionHelper.getStudentRegId();
            if (StringUtils.hasText(regid)) {
                String programId = getFormProgramID(form);
                if (!programId.equalsIgnoreCase(DegreeAuditConstants.DEFAULT_KEY)) {
                    DegreeAuditService degreeAuditService = getDegreeAuditService();
                    ContextInfo context = new ContextInfo();
                    String auditType = form.getAuditType();
                    AuditReportInfo info = degreeAuditService.runAudit(regid, programId, auditType, context);
                    String auditID = info.getAuditId();
                    // TODO: For now we are getting the auditType from the end user. This needs to be remvoed before going live and hard coded to audit type key html
                    AuditReportInfo report = degreeAuditService.getAuditReport(auditID, auditType, context);
                    copyReportToForm(report, form);
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

    private String getFormProgramID(DegreeAuditForm form) {
        String programId = DegreeAuditConstants.DEFAULT_KEY;
        String campusParam = form.getCampusParam();
        if ("306".equals(campusParam)) {
            programId = form.getProgramParamSeattle();

        } else if ("310".equals(campusParam)) {
            programId = form.getProgramParamBothell();

        } else if ("323".equals(campusParam)) {
            programId = form.getProgramParamTacoma();

        }

        return programId;
    }

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


        // Plan Audit Report Process
        try {
            String regid = UserSessionHelper.getStudentRegId();
            if (StringUtils.hasText(regid)) {
                String programId = getFormProgramID(form);
                if (!programId.equals(DegreeAuditConstants.DEFAULT_KEY)) {
                    ContextInfo context = new ContextInfo();
                    context.setPrincipalId(regid);
                    DegreeAuditService degreeAuditService = getDegreeAuditService();
                    List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(regid, LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);
                    for (LearningPlanInfo learningPlan : learningPlanList) {
                        LearningPlanInfo learningPlanInfo = getAcademicPlanService().copyLearningPlan(learningPlan.getId(), AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT, context);
                        String learningPlanInfoId = learningPlanInfo.getId();

                        saveMessySelectionsToLearningPlan(form, learningPlanInfoId);
                        String auditId = degreeAuditService.runWhatIfAuditAsync(regid, programId, form.getAuditType(), learningPlanInfoId, context);
                        AuditReportInfo report = degreeAuditService.getAuditReport(auditId, form.getAuditType(), context);


                        copyReportToForm(report, form);
                        break;
                    }

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

    private void saveMessySelectionsToLearningPlan(PlanAuditForm form, String learningPlanId) {
        try {
            List<PlanItemInfo> planItemInfos = getAcademicPlanService().getPlanItemsInPlan(learningPlanId, CONTEXT_INFO);

            Map<String, PlanItemInfo> planItemInfoMap = new HashMap<String, PlanItemInfo>();

            for (PlanItemInfo item : planItemInfos) {
                boolean isCourse = PlanConstants.COURSE_TYPE.equalsIgnoreCase(item.getRefObjectType());
                boolean isPlanned = AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equalsIgnoreCase(item.getTypeKey());
                if (isCourse && isPlanned) {
                    // Version independent id + term = key
                    String key = item.getRefObjectId() + item.getPlanPeriods().get(0);
                    planItemInfoMap.put(key, item);
                }
            }

            for (MessyItemDataObject messyTerm : form.getMessyItems()) {
                for (MessyItem item : messyTerm.getMessyItemList()) {
                    String key = item.getVersionIndependentId() + item.getAtpId();
                    PlanItemInfo planItem = planItemInfoMap.get(key);
                    if (planItem != null) {
                        String choice = item.getSelectedCredit();
                        String[] str = choice.split(":");
                        List<AttributeInfo> list = planItem.getAttributes();

                        list.add(new AttributeInfo(BUCKET, BUCKET_MESSY));
                        list.add(new AttributeInfo(CREDIT, str[1]));
                        list.add(new AttributeInfo(CHOICE, choice));
                        list.add(new AttributeInfo(SECTION, str[0].isEmpty() ? null : str[0]));

                        getAcademicPlanService().updatePlanItem(planItem.getId(), planItem, CONTEXT_INFO);
                    }
                }
            }

            for (CourseItem item : form.getCleanList()) {
                PlanItemInfo planItem = planItemInfoMap.get(item.getCourseId());
                if (planItem != null) {
                    List<AttributeInfo> list = planItem.getAttributes();

                    list.add(new AttributeInfo(BUCKET, BUCKET_CLEAN));
                    list.add(new AttributeInfo(CREDIT, item.getCredit()));
                    list.add(new AttributeInfo(SECTION, item.getSectionCode()));

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

        String regid = UserSessionHelper.getStudentRegId();

        PlannedTermsHelperBase plannedTermsHelperBase = new PlannedTermsHelperBase();
        List<PlanItemInfo> itemList = plannedTermsHelperBase.getLatestSnapShotPlanItemsByRefObjType(PlanConstants.COURSE_TYPE, regid);

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
                String key = item.getRefObjectId() + item.getPlanPeriods().get( 0 );
                map.put(key, choice);

            }
        }
        return map;
    }

    public static class Choice {
        String credit = "";
        String section = null;
        boolean writing = false;
        boolean honors = false;

        public int hashCode() {
            return credit.hashCode();
        }

        public boolean equals(Object that) {
            if (that == null) return false;
            if (this == that) return true;
            if (!(that instanceof Choice)) return false;
            return credit.equals(((Choice) that).credit);
        }

        // section:credits:display, eg "A:5:5 -- Writing"
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(section);
            sb.append(':');
            sb.append(credit);
            sb.append(':');
            sb.append(credit);
            if (writing) {
                sb.append(" -- ");
                sb.append(WRITING_CREDIT);
            }
            if (honors) {
                sb.append(" -- ");
                sb.append(HONORS_CREDIT);
            }
            return sb.toString();
        }
    }

    @RequestMapping(params = "methodToCall=reviewPlanAudit")
    public ModelAndView reviewPlanAudit(@ModelAttribute("KualiForm") AuditForm auditForm, BindingResult result,
                                        HttpServletRequest request, HttpServletResponse response) {

        PlanAuditForm form = auditForm.getPlanAudit();

        List<CourseItem> courseItems = form.getCleanList();
        List<CourseItem> ignoreItems = form.getIgnoreList();

        CourseDetailsInquiryHelperImpl courseDetailsInquiryHelper = new CourseDetailsInquiryHelperImpl();

        PlannedTermsHelperBase plannedTermsHelperBase = new PlannedTermsHelperBase();
        List<PlannedTerm> termList = plannedTermsHelperBase.getPlannedTermsFromStartAtp();
        List<String> scheduledTerms = AtpHelper.getPublishedTerms();


        for (PlannedTerm term : termList) {
            String atpId = term.getAtpId();

            // Skip past terms
            if (AtpHelper.isAtpCompletedTerm(atpId)) continue;

            MessyItemDataObject messyTerm = new MessyItemDataObject();
            messyTerm.setAtpId(atpId);


            for (PlannedCourseDataObject course : term.getPlannedList()) {

                // No scheduled terms means IGNORE this one
                if (course.getCourseDetails().getScheduledTerms().isEmpty()) {
                    CourseItem item = new CourseItem();
                    item.setAtpId(atpId);
                    CourseSummaryDetails details = course.getCourseDetails();
                    item.setCourseCode(details.getCode());
                    item.setCourseId(details.getVersionIndependentId());
                    item.setCredit(course.getCredit());
                    item.setSectionCode("");
                    ignoreItems.add(item);
                    continue;
                }

                Set<Choice> choices = new HashSet<Choice>();

                // If time schedule is published, divine credit choices from course's sections (primary activities)
                if (scheduledTerms.contains(atpId)) {
                    List<ActivityOfferingItem> activities = course.getPlanActivities();

                    // If plan item activity list is empty, default to all sections
                    if (activities.isEmpty()) {
                        String courseId = course.getCourseDetails().getCourseId();
                        activities = courseDetailsInquiryHelper.getActivityOfferingItemsById(courseId, atpId);
                    }


                    for (ActivityOfferingItem activity : activities) {
                        if (activity.isPrimary()) {

                            String credits = activity.getCredits();
                            String section = activity.getCode();
                            boolean honors = activity.isHonorsSection();
                            boolean writing = activity.isWritingSection();

                            String[] list = creditToList(credits);
                            for (String temp : list) {
                                Choice choice = new Choice();
                                choice.credit = temp;
                                choice.section = section;
                                choice.honors = honors;
                                choice.writing = writing;
                                choices.add(choice);
                            }
                        }
                    }
                }

                // Otherwise just use course's default credit choices
                if (choices.isEmpty()) {
                    String credits = course.getCourseDetails().getCredit();
                    String section = "";
                    String[] list = creditToList(credits);
                    for (String temp : list) {
                        Choice choice = new Choice();
                        choice.credit = temp;
                        choice.section = section;
                        choices.add(choice);
                    }
                }

                if (choices.size() == 1) {
                    for (Choice choice : choices) {
                        String credits = choice.credit;
                        String section = choice.section;
                        CourseItem item = new CourseItem();
                        item.setAtpId(atpId);
                        CourseSummaryDetails details = course.getCourseDetails();
                        item.setCourseCode(details.getCode());
                        item.setCourseId(details.getVersionIndependentId());
                        item.setCredit(credits);
                        item.setSectionCode(section);

                        courseItems.add(item);
                        break;
                    }
                } else {
                    Set<String> credits = new HashSet<String>();
                    for (Choice choice : choices) {
                        String formatted = choice.toString();
                        credits.add(formatted);
                    }

                    CourseSummaryDetails details = course.getCourseDetails();
                    String versionIndependentId = details.getVersionIndependentId();

                    MessyItem item = new MessyItem();
                    item.setCourseCode(details.getCode());
                    item.setCourseTitle(details.getCourseTitle());
                    item.setCourseId(details.getCourseId());
                    item.setVersionIndependentId(versionIndependentId);
                    item.setCredits(credits);
                    item.setAtpId(atpId);

                    messyTerm.getMessyItemList().add(item);
                }
            }
            if (!messyTerm.getMessyItemList().isEmpty()) {
                form.getMessyItems().add(messyTerm);
            }

        }

        if (!form.getMessyItems().isEmpty()) {
            Map<String, String> prevChoices = getPlanItemSnapShots();

            for (MessyItemDataObject messyTerm : form.getMessyItems()) {

                for (MessyItem messy : messyTerm.getMessyItemList()) {
                    String key = messy.getVersionIndependentId() + messy.getAtpId();
                    if (prevChoices.containsKey(key)) {
                        String choice = prevChoices.get(key);
                        messy.setSelectedCredit(choice);
                    }
                }
            }

            form.setStudentChoiceRequired(true);
        }
        return getUIFModelAndView(auditForm);
    }

    private String[] creditToList(String credit) {
        credit = credit.replace(" ", "");
        String[] result = null;
        if (credit.contains("-")) {
            String[] temp = credit.split("-");
            int min = Integer.valueOf(temp[0]);
            int max = Integer.valueOf(temp[1]);
            int len = max - min + 1;
            result = new String[len];
            for (int x = 0; x < len; x++) {
                result[x] = Integer.toString(min + x);
            }
        } else if (credit.contains(",")) {
            result = credit.split(",");
        } else {
            result = new String[]{credit};
        }
        return result;
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


    private Map<Character, String> populateCampusMap() {
        Map<Character, String> orgCampusTypes = new HashMap<Character, String>();
        try {
            SearchRequest request = new SearchRequest(CourseSearchConstants.ORG_QUERY_SEARCH_BY_TYPE_REQUEST);
            request.addParam(ORG_QUERY_PARAM, CAMPUS_LOCATION);
            SearchResult result = getOrganizationService().search(request);
            for (SearchResultRow row : result.getRows()) {

                String name = getCellValue(row, "org.resultColumn.orgShortName");
                // Default campus is Seattle = 0
                Character code = '0';
                if ("bothell".equalsIgnoreCase(name)) {
                    code = '1';
                } else if ("tacoma".equalsIgnoreCase(name)) {
                    code = '2';
                }
                String cellValue = getCellValue(row, "org.resultColumn.orgId");
                orgCampusTypes.put(code, cellValue);
            }
        } catch (MissingParameterException e) {
            logger.error("Search Failed to get the Organization Data ", e);
        }
        return orgCampusTypes;
    }


    private String getCellValue(SearchResultRow row, String key) {
        for (SearchResultCell cell : row.getCells()) {
            if (key.equals(cell.getKey())) {
                return cell.getValue();
            }
        }
        throw new RuntimeException("cell result '" + key + "' not found");
    }

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
     * @return
     */
    private String getRecentPlanAudit(String studentId) {
        String recentPlanAuditId = null;
        try {
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(studentId, LEARNING_PLAN_TYPE_PLAN_AUDIT, PlanConstants.CONTEXT_INFO);
            for (LearningPlanInfo learningPlanInfo : learningPlanList) {
                for (AttributeInfo attributeInfo : learningPlanInfo.getAttributes()) {
                    String key = attributeInfo.getKey();
                    String value = attributeInfo.getValue();
                    if ("auditId".equalsIgnoreCase(key)) {
                        /*TODO: cache this getAuditStatus method*/
                        StatusInfo statusInfo = degreeAuditService.getAuditRunStatus(value, PlanConstants.CONTEXT_INFO);
                        if (statusInfo.getIsSuccess()) {
                            recentPlanAuditId = value;
                        }
                    }
                }
                if (recentPlanAuditId != null) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not retrieve the latest Plan Audit Id", e);
        }
        return recentPlanAuditId;
    }

}


