package org.kuali.student.myplan.sampleplan.controller;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import edu.uw.kuali.student.myplan.util.PlanHelperImpl;
import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.datadictionary.exception.DuplicateEntryException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.comment.CommentConstants;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.util.EnumerationHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.sampleplan.dataobject.SamplePlanItem;
import org.kuali.student.myplan.sampleplan.dataobject.SamplePlanTerm;
import org.kuali.student.myplan.sampleplan.dataobject.SamplePlanYear;
import org.kuali.student.myplan.sampleplan.form.SamplePlanForm;
import org.kuali.student.myplan.sampleplan.util.SamplePlanConstants;
import org.kuali.student.myplan.util.CourseLinkBuilder;
import org.kuali.student.myplan.utils.UrlLinkBuilder;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r1.core.statement.dto.ReqCompFieldInfo;
import org.kuali.student.r1.core.statement.dto.ReqComponentInfo;
import org.kuali.student.r1.core.statement.dto.StatementInfo;
import org.kuali.student.r1.core.statement.dto.StatementOperatorTypeKey;
import org.kuali.student.r1.core.statement.service.StatementService;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.MetaInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.util.constants.ProgramServiceConstants;
import org.kuali.student.r2.core.comment.dto.CommentInfo;
import org.kuali.student.r2.core.comment.service.CommentService;
import org.kuali.student.r2.core.constants.StatementServiceConstants;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.infc.SearchResultCell;
import org.kuali.student.r2.core.search.infc.SearchResultRow;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.program.dto.MajorDisciplineInfo;
import org.kuali.student.r2.lum.program.service.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 11/7/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping(value = "/samplePlan/**")
public class SamplePlanController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(SamplePlanController.class);

    private transient AcademicPlanService academicPlanService;

    private transient PermissionService permissionService;

    private transient ProgramService programService;

    private transient StatementService statementService;

    private CommentService commentService;


    private transient List<String> advisePermNames;

    private transient String ADVISE_NM_CODE;

    @Autowired
    private transient CourseHelper courseHelper;


    @Autowired
    private UserSessionHelper userSessionHelper;

    @Autowired
    private PlanHelper planHelper;


    @Override
    protected SamplePlanForm createInitialForm(HttpServletRequest request) {
        return new SamplePlanForm();
    }


    /**
     * Initial Plan page loading
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=startNew")
    public ModelAndView startNew(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                 HttpServletRequest request, HttpServletResponse response) {
        /*TODO: Add this check
        if (!isAuthorizedUser()) {
            return new ModelAndView("redirect:/myplan/unauthorized");
        }*/
        SamplePlanForm samplePlanForm = (SamplePlanForm) form;
        if (StringUtils.hasText(samplePlanForm.getLearningPlanId())) {
            try {
                LearningPlanInfo learningPlanInfo = getAcademicPlanService().getLearningPlan(samplePlanForm.getLearningPlanId(), PlanConstants.CONTEXT_INFO);
                MajorDisciplineInfo majorDisciplineInfo = getProgramService().getMajorDiscipline(learningPlanInfo.getPlanProgram(), SamplePlanConstants.CONTEXT_INFO);
                samplePlanForm.setDegreeProgramTitle(majorDisciplineInfo != null && majorDisciplineInfo.getId() != null ? majorDisciplineInfo.getCode() : null);
                samplePlanForm.setDescription(samplePlanForm.isPreview() ? learningPlanInfo.getDescr().getFormatted() : learningPlanInfo.getDescr().getPlain());
                samplePlanForm.setPlanTitle(learningPlanInfo.getName());

                List<CommentInfo> commentInfos = getCommentService().getCommentsByReferenceAndType(learningPlanInfo.getId(), CommentConstants.PLAN_REF_TYPE, SamplePlanConstants.CONTEXT_INFO);

                /*Assuming that for each learningPlan there will be only one general Notes saved in Comment table*/
                String generalNotes = !CollectionUtils.isEmpty(commentInfos) && commentInfos.size() > 0 ? (samplePlanForm.isPreview() ? commentInfos.get(0).getCommentText().getFormatted() : commentInfos.get(0).getCommentText().getPlain()) : null;

                samplePlanForm.setGeneralNotes(generalNotes);
                List<PlanItemInfo> planItemInfos = getAcademicPlanService().getPlanItemsInPlan(learningPlanInfo.getId(), PlanConstants.CONTEXT_INFO);
                List<SamplePlanYear> samplePlanYears = getDefaultSamplePlanTable();
                for (PlanItemInfo planItemInfo : planItemInfos) {

                    if (!CollectionUtils.isEmpty(samplePlanYears) && !StringUtils.isEmpty(planItemInfo.getPlanPeriods())) {
                        String atpId = planItemInfo.getPlanPeriods().get(0);
                        String[] str = atpId.split("Year");
                        String term = str[0].trim();
                        int samplePlanYearIndex = Integer.parseInt(str[1].trim()) - 1;
                        int samplePlanTermIndex = SamplePlanConstants.TERM_LABELS_LIST.indexOf(term);
                        SamplePlanYear samplePlanYear = samplePlanYears.get(samplePlanYearIndex);
                        SamplePlanTerm samplePlanTerm = samplePlanYear.getSamplePlanTerms().get(samplePlanTermIndex);
                        for (SamplePlanItem samplePlanItem : samplePlanTerm.getSamplePlanItems()) {
                            if (StringUtils.isEmpty(samplePlanItem.getCode())) {
                                if (PlanConstants.COURSE_TYPE.equals(planItemInfo.getRefObjectType())) {
                                    String crossListedCourse = getPlanHelper().getCrossListedCourse(planItemInfo.getAttributes());
                                    CourseInfo courseInfo = getCourseHelper().getCourseInfoByIdAndCd(planItemInfo.getRefObjectId(), crossListedCourse);
                                    samplePlanItem.setCode(courseInfo.getCode());
                                    samplePlanItem.setPlanItemId(planItemInfo.getId());
                                    samplePlanItem.setCredit(CreditsFormatter.formatCredits(courseInfo));
                                    samplePlanItem.setNote(planItemInfo.getDescr().getPlain());
                                    break;
                                } else if (PlanConstants.PLACE_HOLDER_TYPE_GEN_ED.equals(planItemInfo.getRefObjectType()) ||
                                        PlanConstants.PLACE_HOLDER_TYPE.equals(planItemInfo.getRefObjectType())) {
                                    String placeHolderCode = EnumerationHelper.getEnumAbbrValForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.PLACE_HOLDER_ENUM_KEY);
                                    //String placeHolderValue = EnumerationHelper.getEnumValueForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.PLACE_HOLDER_ENUM_KEY);
                                    if (placeHolderCode == null) {
                                        placeHolderCode = EnumerationHelper.getEnumAbbrValForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.GEN_EDU_ENUM_KEY);
                                        //placeHolderValue = EnumerationHelper.getEnumValueForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.GEN_EDU_ENUM_KEY);
                                    }
                                    samplePlanItem.setCode(samplePlanForm.isPreview() ? placeHolderCode : String.format("%s|%s", planItemInfo.getRefObjectId(), planItemInfo.getRefObjectType()));
                                    samplePlanItem.setPlanItemId(planItemInfo.getId());
                                    samplePlanItem.setCredit(planItemInfo.getCredit().toString());
                                    samplePlanItem.setNote(planItemInfo.getDescr().getPlain());
                                    break;
                                } else if (PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(planItemInfo.getRefObjectType())) {
                                    samplePlanItem.setCode(planItemInfo.getRefObjectId());
                                    samplePlanItem.setPlanItemId(planItemInfo.getId());
                                    samplePlanItem.setCredit(planItemInfo.getCredit().toString());
                                    samplePlanItem.setNote(planItemInfo.getDescr().getPlain());
                                    break;
                                }
                            }
                        }
                    }
                }
                samplePlanForm.setSamplePlanYears(samplePlanYears);


            } catch (Exception e) {
                logger.error("Error getting learning plan for id : " + samplePlanForm.getLearningPlanId(), e);
            }
        }

        return getUIFModelAndView(samplePlanForm);
    }

    /**
     * Initial Plan page loading
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=saveSamplePlan")
    public ModelAndView saveSamplePlan(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        /*TODO: Add this check
        if (!isAuthorizedUser()) {
            return new ModelAndView("redirect:/myplan/unauthorized");
        }*/
        SamplePlanForm samplePlanForm = (SamplePlanForm) form;
        if (isValidSamplePlan(samplePlanForm)) {
            try {

                /*Learning Plan created when no learning plan exists (OR) Updated when a learning plan exists and description in that is updated*/
                LearningPlanInfo learningPlan = null;
                if (StringUtils.hasText(samplePlanForm.getLearningPlanId())) {
                    learningPlan = getAcademicPlanService().getLearningPlan(samplePlanForm.getLearningPlanId(), SamplePlanConstants.CONTEXT_INFO);
                }
                if (learningPlan == null || (learningPlan != null && samplePlanForm.getDescription() != null && !samplePlanForm.getDescription().equals(learningPlan.getDescr().getPlain()))) {
                    learningPlan = createUpdateLearningPlanTemplate(samplePlanForm, PlanConstants.LEARNING_PLAN_ITEM_DRAFT_STATE_KEY, samplePlanForm.getLearningPlanId());
                    samplePlanForm.setLearningPlanId(learningPlan.getId());
                }

                /*General comments newly added or updated are saved*/
                List<CommentInfo> commentInfos = getCommentService().getCommentsByReferenceAndType(learningPlan.getId(), CommentConstants.PLAN_REF_TYPE, SamplePlanConstants.CONTEXT_INFO);
                if (CollectionUtils.isEmpty(commentInfos) || (!CollectionUtils.isEmpty(commentInfos) && samplePlanForm.getGeneralNotes() != null && !samplePlanForm.getGeneralNotes().equals(commentInfos.get(0).getCommentText().getPlain()))) {
                    saveUpdateGeneralNotes(learningPlan.getId(), samplePlanForm.getGeneralNotes(), !CollectionUtils.isEmpty(commentInfos) ? commentInfos.get(0) : null);
                }
                for (SamplePlanYear samplePlanYear : samplePlanForm.getSamplePlanYears()) {
                    int year = samplePlanYear.getYear();
                    for (SamplePlanTerm samplePlanTerm : samplePlanYear.getSamplePlanTerms()) {
                        String atpId = String.format(SamplePlanConstants.SAMPLE_PLAN_ATP_FORMAT, samplePlanTerm.getTermName(), year);
                        for (SamplePlanItem samplePlanItem : samplePlanTerm.getSamplePlanItems()) {
                            /*If No "OR" courses are specified*/
                            if (StringUtils.isEmpty(samplePlanItem.getOrCode()) && StringUtils.hasText(samplePlanItem.getCode())) {
                                if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {
                                    PlanItemInfo planItemInfo = addCourse(learningPlan, samplePlanItem, atpId);
                                    if (planItemInfo != null) {
                                        samplePlanItem.setPlanItemId(planItemInfo.getId());
                                    }
                                } else if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_PLACE_HOLDER_REGEX)) {
                                    DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(samplePlanItem.getCode());
                                    String refObjId = String.format("%s %s", courseCode.getSubject(), courseCode.getNumber());
                                    String refObjType = PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL;

                                    /*Newly added courses PlaceHolders are added to planItem (OR) Already existing planItems which are updated with notes or credit then planItem is updated*/
                                    PlanItemInfo planItemInfo = getExistingPlanItem(refObjId, refObjType, learningPlan.getId(), atpId);
                                    if ((planItemInfo == null) || (planItemInfo.getDescr().getPlain() != null && !planItemInfo.getDescr().getPlain().equals(samplePlanItem.getNote())) || (!planItemInfo.getCredit().equals(samplePlanItem.getCredit()))) {
                                        planItemInfo = addUpdatePlanItem(learningPlan, refObjId, refObjType, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), samplePlanItem.getCredit(), null, planItemInfo != null ? planItemInfo.getId() : null);
                                    }
                                    samplePlanItem.setPlanItemId(planItemInfo.getId());

                                } else {
                                    String[] placeHolder = samplePlanItem.getCode().split(PlanConstants.CODE_KEY_SEPARATOR);
                                    String placeHolderId = placeHolder[0];
                                    String placeHolderType = placeHolder[1];

                                    /*Newly added placeHolders are added as a new planItem (OR) Already existing planItems which are updated with notes or credit then planItem is updated*/
                                    PlanItemInfo planItemInfo = getExistingPlanItem(placeHolderId, placeHolderType, learningPlan.getId(), atpId);
                                    if ((planItemInfo == null) || (planItemInfo.getDescr().getPlain() != null && !planItemInfo.getDescr().getPlain().equals(samplePlanItem.getNote())) || (!planItemInfo.getCredit().equals(samplePlanItem.getCredit()))) {
                                        planItemInfo = addUpdatePlanItem(learningPlan, placeHolderId, placeHolderType, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), samplePlanItem.getCredit(), null, planItemInfo != null ? planItemInfo.getId() : null);
                                    }
                                    samplePlanItem.setPlanItemId(planItemInfo.getId());
                                }
                            }

                            /*If "OR" courses are specified*/
                            else if (StringUtils.hasText(samplePlanItem.getOrCode()) && StringUtils.hasText(samplePlanItem.getCode())) {

                            /*TODO: Implement Logic for adding OR conditional Items*/

                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Could not save sample plan", e);
            }
        }
        return getUIFModelAndView(samplePlanForm);
    }


    /**
     * Verifies if any planItem exists for given params
     *
     * @param refObjId
     * @param refObjType
     * @param learningPlanId
     * @param atpId
     * @return
     */
    private PlanItemInfo getExistingPlanItem(String refObjId, String refObjType, String learningPlanId, String atpId) {
        List<PlanItemInfo> planItemInfos = null;
        try {
            planItemInfos = getAcademicPlanService().getPlanItemsInPlanByRefObjectIdByRefObjectType(learningPlanId, refObjId, refObjType, SamplePlanConstants.CONTEXT_INFO);

        } catch (Exception e) {
            logger.error("Could not get PlanItems for refObjId: " + refObjId, e);
        }
        if (CollectionUtils.isEmpty(planItemInfos)) {
            return null;
        } else {
            for (PlanItemInfo planItemInfo : planItemInfos) {
                if (!CollectionUtils.isEmpty(planItemInfo.getPlanPeriods())) {
                    if (atpId.equals(planItemInfo.getPlanPeriods().get(0))) {
                        return planItemInfo;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Verifies if any LearningPlan exists for given params
     *
     * @param planProgram
     * @param name
     * @return
     */
    private LearningPlanInfo getExistingLearningPlan(String planProgram, String name) {
        try {
            SearchRequestInfo req = new SearchRequestInfo("learningPlan.id.by.programAndName");
            req.addParam("planProgram", planProgram);
            req.addParam("planType", PlanConstants.LEARNING_PLAN_TYPE_PLAN_TEMPLATE);
            req.addParam("name", name);
            SearchResultInfo result = getAcademicPlanService().search(req, SamplePlanConstants.CONTEXT_INFO);
            for (SearchResultRow row : result.getRows()) {
                for (SearchResultCell cell : row.getCells()) {
                    if ("learningPlan.id".equals(cell.getKey())) {
                        return getAcademicPlanService().getLearningPlan(cell.getValue(), SamplePlanConstants.CONTEXT_INFO);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Could not get LearningPlan for planProgram: " + planProgram, e);
        }
        return null;
    }


    /**
     * Validates all sample Plan Items and adds Error messages to the propertyNames.
     *
     * @param samplePlanForm
     * @return
     */
    private boolean isValidSamplePlan(SamplePlanForm samplePlanForm) {
        boolean isValidSamplePlan = true;
        int yearIndex = 0;
        for (SamplePlanYear samplePlanYear : samplePlanForm.getSamplePlanYears()) {
            int termIndex = 0;
            for (SamplePlanTerm samplePlanTerm : samplePlanYear.getSamplePlanTerms()) {
                int itemIndex = 0;
                for (SamplePlanItem samplePlanItem : samplePlanTerm.getSamplePlanItems()) {
                    if (StringUtils.isEmpty(samplePlanItem.getOrCode()) && StringUtils.hasText(samplePlanItem.getCode())) {
                        if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {
                            /*Validate Course*/
                            DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(samplePlanItem.getCode());
                            String courseId = getCourseHelper().getCourseId(courseCode.getSubject(), courseCode.getNumber());
                            if (StringUtils.isEmpty(courseId)) {
                                String[] params = {samplePlanItem.getCode()};
                                GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, yearIndex, termIndex, itemIndex), PlanConstants.COURSE_NOT_FOUND, params);
                                isValidSamplePlan = false;
                            }
                        } else if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_PLACE_HOLDER_REGEX)) {
                            DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(samplePlanItem.getCode());
                            /*Validate the subject in course placeholder*/
                            HashMap<String, String> divisionMap = getCourseHelper().fetchCourseDivisions();
                            ArrayList<String> divisions = new ArrayList<String>();
                            getCourseHelper().extractDivisions(divisionMap, courseCode.getSubject(), divisions, false);
                            if (CollectionUtils.isEmpty(divisions)) {
                                String[] params = {courseCode.getSubject()};
                                GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, yearIndex, termIndex, itemIndex), PlanConstants.CURRIC_NOT_FOUND, params);
                                isValidSamplePlan = false;
                            }
                        } else if (samplePlanItem.getCode().contains(PlanConstants.PLACEHOLDER_KEY_SEPARATOR)) {
                            /*Validate the placeHolder*/
                            String[] placeHolder = samplePlanItem.getCode().split(PlanConstants.CODE_KEY_SEPARATOR);
                            if (StringUtils.isEmpty(placeHolder) || placeHolder.length != 2) {
                                String[] params = {};
                                GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, yearIndex, termIndex, itemIndex), PlanConstants.ERROR_KEY_UNKNOWN_COURSE, params);
                                isValidSamplePlan = false;
                            } else {
                                String placeHolderId = placeHolder[0];
                                String placeHolderType = placeHolder[1];
                                String placeHolderCd = EnumerationHelper.getEnumAbbrValForCodeByType(placeHolderId, placeHolderType);
                                if (StringUtils.isEmpty(placeHolderCd)) {
                                    String[] params = {};
                                    GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, yearIndex, termIndex, itemIndex), PlanConstants.ERROR_KEY_UNKNOWN_COURSE, params);
                                    isValidSamplePlan = false;
                                }
                                if (PlanConstants.PLACE_HOLDER_OTHER_CODE.equals(placeHolderId) && StringUtils.isEmpty(samplePlanItem.getNote())) {
                                    String[] params = {};
                                    GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, yearIndex, termIndex, itemIndex), PlanConstants.NOTE_REQUIRED, params);
                                    isValidSamplePlan = false;
                                }
                            }
                        } else {
                            String[] params = {};
                            GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, yearIndex, termIndex, itemIndex), PlanConstants.ERROR_KEY_UNKNOWN_COURSE, params);
                            isValidSamplePlan = false;
                        }
                    } else if (StringUtils.hasText(samplePlanItem.getOrCode()) && StringUtils.hasText(samplePlanItem.getCode())) {
                                  /*TODO: Implement code for validating the OR conditional statements*/
                    }
                    itemIndex++;
                }
                termIndex++;
            }
            yearIndex++;
        }

        LearningPlanInfo learningPlanInfo = getExistingLearningPlan(samplePlanForm.getDegreeProgramTitle(), samplePlanForm.getPlanTitle());
        if (learningPlanInfo != null && !learningPlanInfo.getId().equals(samplePlanForm.getLearningPlanId())) {
            String[] params = {samplePlanForm.getDegreeProgramTitle(), samplePlanForm.getPlanTitle()};
            GlobalVariables.getMessageMap().putError("planTitle", SamplePlanConstants.DUPLICATE_ERROR, params);
            isValidSamplePlan = false;
        }


        return isValidSamplePlan;
    }


    /**
     * Adds a course to Plan Item or ReqComponent based on boolean toReqComponent
     *
     * @param learningPlan
     * @param samplePlanItem
     * @param atpId
     */
    private PlanItemInfo addCourse(LearningPlan learningPlan, SamplePlanItem samplePlanItem, String atpId) {
        PlanItemInfo actualPlanItem = null;
        DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(samplePlanItem.getCode());
        String courseId = getCourseHelper().getCourseId(courseCode.getSubject(), courseCode.getNumber());
        if (StringUtils.hasText(courseId)) {
            boolean isCrossListedCourse = false;
            CourseInfo courseInfo = getCourseHelper().getCourseInfo(courseId);
            try {
                isCrossListedCourse = getCourseHelper().isCrossListedCourse(courseInfo, samplePlanItem.getCode());
            } catch (DoesNotExistException e) {
                logger.error("Course not found" + samplePlanItem.getCode(), e);
            }

            if (courseInfo != null) {

                if (StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                    try {
                        actualPlanItem = getAcademicPlanService().getPlanItem(samplePlanItem.getPlanItemId(), SamplePlanConstants.CONTEXT_INFO);
                    } catch (Exception e) {
                        logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                    }
                }

                /*Newly added courses are added as a new planItem (OR) Already existing planItems which are updated with notes then planItem is updated*/
                PlanItemInfo planItemInfo = getExistingPlanItem(courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, learningPlan.getId(), atpId);
                if ((planItemInfo == null) || (planItemInfo != null && planItemInfo.getDescr().getPlain() != null && !planItemInfo.getDescr().getPlain().equals(samplePlanItem.getNote())) || (planItemInfo != null && actualPlanItem != null && planItemInfo.getRefObjectId().equals(actualPlanItem.getRefObjectId()))) {
                    actualPlanItem = addUpdatePlanItem(learningPlan, courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), null, isCrossListedCourse ? samplePlanItem.getCode() : null, planItemInfo != null ? planItemInfo.getId() : null);
                }
            }

        }
        return actualPlanItem;
    }

    /**
     * Saved the general text as comment with reference Id as learningPlanId
     *
     * @param learningPlanId
     * @param generalNotes
     */
    private void saveUpdateGeneralNotes(String learningPlanId, String generalNotes, CommentInfo info) {
        if (info != null) {
            RichTextInfo rtiBody = new RichTextInfo();
            rtiBody.setPlain(StringUtils.hasText(generalNotes) ? generalNotes : "");
            CourseLinkBuilder courseLinkBuilder = new CourseLinkBuilder();
            rtiBody.setFormatted(StringUtils.hasText(generalNotes) ? courseLinkBuilder.makeLinks(UrlLinkBuilder.buildLinksForText(generalNotes)) : "");
            info.setCommentText(rtiBody);
            try {
                getCommentService().updateComment(info.getId(), info, SamplePlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Could not update generalText for learningPlanId: " + learningPlanId, e);
            }
        } else if (StringUtils.hasText(learningPlanId) && StringUtils.hasText(generalNotes)) {
            CommentInfo commentInfo = new CommentInfo();
            commentInfo.setType(CommentConstants.MESSAGE_TYPE);
            commentInfo.setState("ACTIVE");
            RichTextInfo rtiBody = new RichTextInfo();
            rtiBody.setPlain(StringUtils.hasText(generalNotes) ? generalNotes : "");
            CourseLinkBuilder courseLinkBuilder = new CourseLinkBuilder();
            rtiBody.setFormatted(StringUtils.hasText(generalNotes) ? courseLinkBuilder.makeLinks(UrlLinkBuilder.buildLinksForText(generalNotes)) : "");
            commentInfo.setCommentText(rtiBody);
            try {
                getCommentService().createComment(learningPlanId, CommentConstants.PLAN_REF_TYPE, CommentConstants.MESSAGE_TYPE, commentInfo, SamplePlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Could not save generalText for learningPlanId: " + learningPlanId, e);
            }
        }
    }


    /**
     * Builds a default template view for sample plans
     *
     * @return
     */
    private List<SamplePlanYear> getDefaultSamplePlanTable() {
        List<SamplePlanYear> samplePlanYears = new ArrayList<SamplePlanYear>();
        for (int i = 1; i <= SamplePlanConstants.SAMPLE_PLAN_YEAR_COUNT; i++) {
            SamplePlanYear samplePlanYear = new SamplePlanYear();
            samplePlanYear.setYearName(String.format(SamplePlanConstants.SAMPLE_PLAN_YEAR, i));
            samplePlanYear.setYear(i);
            List<SamplePlanTerm> samplePlanTerms = samplePlanYear.getSamplePlanTerms();
            for (String term : SamplePlanConstants.TERM_LABELS_LIST) {
                SamplePlanTerm samplePlanTerm = new SamplePlanTerm();
                samplePlanTerm.setTermName(term);
                samplePlanTerm.setYear(i);
                List<SamplePlanItem> planItems = samplePlanTerm.getSamplePlanItems();
                for (int j = 1; j <= SamplePlanConstants.SAMPLE_PLAN_ITEMS_COUNT; j++) {
                    planItems.add(new SamplePlanItem());
                }
                samplePlanTerms.add(samplePlanTerm);
            }
            samplePlanYear.setSamplePlanTerms(samplePlanTerms);
            samplePlanYears.add(samplePlanYear);
        }
        return samplePlanYears;
    }


    /**
     * Creates or updates a learning plan of type 'kuali.academicplan.type.plan.template' for sample plans
     *
     * @param samplePlanForm
     * @return The plan.
     */
    private LearningPlanInfo createUpdateLearningPlanTemplate(SamplePlanForm samplePlanForm, String state, String learningPlanId) throws
            InvalidParameterException, DataValidationErrorException,
            MissingParameterException, AlreadyExistsException, PermissionDeniedException, OperationFailedException, DoesNotExistException {

        LearningPlanInfo plan = new LearningPlanInfo();
        plan.setTypeKey(PlanConstants.LEARNING_PLAN_TYPE_PLAN_TEMPLATE);
        RichTextInfo rti = new RichTextInfo();
        CourseLinkBuilder courseLinkBuilder = new CourseLinkBuilder();
        rti.setFormatted(StringUtils.hasText(samplePlanForm.getDescription()) ? courseLinkBuilder.makeLinks(UrlLinkBuilder.buildLinksForText(samplePlanForm.getDescription())) : "");
        rti.setPlain(StringUtils.hasText(samplePlanForm.getDescription()) ? samplePlanForm.getDescription() : "");
        plan.setShared(true);
        plan.setDescr(rti);
        plan.setStudentId(samplePlanForm.getDegreeProgramTitle());
        plan.setStateKey(state);
        plan.setMeta(new MetaInfo());
        plan.setName(samplePlanForm.getPlanTitle());
        plan.setPlanProgram(samplePlanForm.getDegreeProgramTitle());

        ContextInfo contextInfo = new ContextInfo();
        contextInfo.setPrincipalId(getUserSessionHelper().getCurrentUserId());
        if (StringUtils.hasText(learningPlanId)) {
            return getAcademicPlanService().updateLearningPlan(learningPlanId, plan, contextInfo);
        } else {
            return getAcademicPlanService().createLearningPlan(plan, contextInfo);
        }

    }


    /**
     * Advisers are the only authorized users.
     *
     * @return
     */
    private boolean isAuthorizedUser() {
        UserSession session = GlobalVariables.getUserSession();

        //Initialize the permission service and name space codes
        getPermissionService();
        boolean authorized = false;
        for (String adviseNm : advisePermNames) {
            if (getPermissionService().hasPermission(session.getPrincipalId(), ADVISE_NM_CODE, adviseNm.trim())) {
                authorized = true;
                break;
            }

            logger.info("Adviser authz failed for " + session.getPrincipalName() + " Data|" + session.getPrincipalId() + "|" + ADVISE_NM_CODE + "|" + adviseNm);
        }

        return authorized;
    }

    /**
     * Adds or Updates Req Component for given value and type
     *
     * @param value
     * @param type
     * @param reqComponentInfo
     * @return
     */
    /*protected ReqComponentInfo addUpdateReqComponent(String value, String type, ReqComponentInfo reqComponentInfo) {
        if (reqComponentInfo != null) {

        } else {
            ReqComponentInfo componentInfo = new ReqComponentInfo();
            componentInfo.setType(type);
            componentInfo.setState("ACTIVE");
            List<ReqCompFieldInfo> reqCompFieldInfos = new ArrayList<ReqCompFieldInfo>();
            ReqCompFieldInfo fieldInfo = new ReqCompFieldInfo();
            fieldInfo.setValue(value);
            fieldInfo.setType(type);
            reqCompFieldInfos.add(fieldInfo);
            componentInfo.setReqCompFields(reqCompFieldInfos);
            try {
                componentInfo = getStatementService().createReqComponent(type, componentInfo);
            } catch (Exception e) {
                logger.error("Could not create reqComponentField item.", e);
            }
            return componentInfo;
        }
        return null;
    }*/


    /**
     * Adds or Updates statement Info using reqComponentId's
     *
     * @param reqComponentIds
     * @param statementInfo
     * @return
     */
    /*protected StatementInfo addUpdateStatement(List<String> reqComponentIds, StatementInfo statementInfo) {
        if (statementInfo != null) {

        } else {
            StatementInfo stInfo = new StatementInfo();
            stInfo.setType(SamplePlanConstants.STATEMENT_TYPE_RECOMMENDED);
            stInfo.setOperator(StatementOperatorTypeKey.OR);
            stInfo.setReqComponentIds(reqComponentIds);
            try {
                stInfo = getStatementService().createStatement(SamplePlanConstants.STATEMENT_TYPE_RECOMMENDED, stInfo);
            } catch (Exception e) {
                logger.error("Could not create Statement item.", e);
            }
            return stInfo;
        }
        return null;
    }*/


    /**
     * Adds or Updates plan item for the given course id and ATPs.
     *
     * @param plan         The learning plan to add the item to.
     * @param refObjId     The id of the course.
     * @param atpId        A list of ATP/term ids if the plan item is a planned course.
     * @param planItemType Saved course or planned course.
     * @return The newly created plan item or the existing plan item where a plan item already exists for the given course.
     * @throws RuntimeException on errors.
     */
    protected PlanItemInfo addUpdatePlanItem(LearningPlan plan, String refObjId, String refObjType, String atpId, String planItemType, String note, String credit, String crossListedCourse, String planItemId)
            throws DuplicateEntryException {

        if (org.apache.commons.lang.StringUtils.isEmpty(refObjId)) {
            throw new RuntimeException("Empty Course ID");
        }

        PlanItemInfo newPlanItem = null;

        PlanItemInfo pii = new PlanItemInfo();
        pii.setLearningPlanId(plan.getId());
        pii.setTypeKey(planItemType);
        pii.setRefObjectType(refObjType);
        pii.setRefObjectId(refObjId);

        pii.setStateKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItemType) ? PlanConstants.LEARNING_PLAN_ITEM_PROPOSED_STATE_KEY : PlanConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        RichTextInfo rti = new RichTextInfo();
        rti.setFormatted(hasText(note) ? note : "");
        rti.setPlain(hasText(note) ? note : "");
        pii.setDescr(rti);

        if (null != atpId) {
            pii.setPlanPeriods(Arrays.asList(atpId));
        }

        if (isPlaceHolderType(refObjType) && hasText(credit)) {
            pii.setCredit(new BigDecimal(credit));
        }

        if (!org.apache.commons.lang.StringUtils.isEmpty(crossListedCourse)) {
            List<AttributeInfo> attributeInfos = new ArrayList<AttributeInfo>();
            AttributeInfo attributeInfo = new AttributeInfo(PlanConstants.CROSS_LISTED_COURSE_ATTR_KEY, crossListedCourse);
            attributeInfos.add(attributeInfo);
            pii.setAttributes(attributeInfos);
        }

        try {
            if (StringUtils.hasText(planItemId)) {
                newPlanItem = getAcademicPlanService().updatePlanItem(planItemId, pii, getUserSessionHelper().makeContextInfoInstance());
            } else {
                newPlanItem = getAcademicPlanService().createPlanItem(pii, getUserSessionHelper().makeContextInfoInstance());
            }
        } catch (AlreadyExistsException e) {
            logger.error("Could not create plan item.", e);
            throw new DuplicateEntryException("plan Item already exists", e);
        } catch (Exception e) {
            logger.error("Could not create plan item.", e);
            throw new RuntimeException("Could not create plan item.", e);
        }

        return newPlanItem;
    }

    /**
     * returns true if the reofbjtype is a placeholder
     *
     * @param refObjType
     * @return
     */
    private boolean isPlaceHolderType(String refObjType) {
        return PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(refObjType) || PlanConstants.PLACE_HOLDER_TYPE_GEN_ED.equals(refObjType) || PlanConstants.PLACE_HOLDER_TYPE.equals(refObjType);
    }

    public StatementService getStatementService() {
        if (statementService == null) {
            statementService = (StatementService)
                    GlobalResourceLoader.getService(new QName(StatementServiceConstants.NAMESPACE, "statement"));
        }
        return statementService;
    }

    public void setStatementService(StatementService statementService) {
        this.statementService = statementService;
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

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }

    public synchronized PermissionService getPermissionService() {
        if (permissionService == null) {

            ADVISE_NM_CODE = ConfigContext.getCurrentContextConfig().getProperty("myplan.advise.namespacecode");
            advisePermNames = Arrays.asList(ConfigContext.getCurrentContextConfig().getProperty("myplan.advise.permissionname").split(","));

            permissionService = KimApiServiceLocator.getPermissionService();
        }

        return this.permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
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

    public PlanHelper getPlanHelper() {
        if (planHelper == null) {
            planHelper = new PlanHelperImpl();
        }
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }

    public ProgramService getProgramService() {
        if (programService == null) {
            programService = (ProgramService)
                    GlobalResourceLoader.getService(new QName(ProgramServiceConstants.PROGRAM_NAMESPACE, "ProgramService"));
        }
        return programService;
    }

    public void setProgramService(ProgramService programService) {
        this.programService = programService;
    }

    public CommentService getCommentService() {
        if (commentService == null) {
            commentService = (CommentService)
                    GlobalResourceLoader.getService(new QName(CommentConstants.NAMESPACE, CommentConstants.SERVICE_NAME));
        }
        return commentService;
    }

    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }
}
