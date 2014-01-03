package org.kuali.student.myplan.sampleplan.controller;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import edu.uw.kuali.student.myplan.util.PlanHelperImpl;
import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
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
import org.kuali.student.myplan.plan.util.AtpHelper;
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
import org.kuali.student.r1.core.statement.dto.StatementOperatorTypeKey;
import org.kuali.student.r1.core.statement.dto.StatementTreeViewInfo;
import org.kuali.student.r1.core.statement.service.StatementService;
import org.kuali.student.r2.common.dto.*;
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
import java.util.*;

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

        /*Authorization check*/
        if (GlobalVariables.getUserSession().retrieveObject(PlanConstants.SESSION_KEY_IS_ADVISER_MANAGE_PLAN) == null) {
            return new ModelAndView("redirect:/samplePlan/unauthorized");
        }

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

                if (samplePlanForm.isCopyPlan()) {
                    return getUIFModelAndView(samplePlanForm);
                }

                List<PlanItemInfo> planItemInfos = getAcademicPlanService().getPlanItemsInPlan(learningPlanInfo.getId(), PlanConstants.CONTEXT_INFO);
                List<SamplePlanYear> samplePlanYears = getDefaultSamplePlanTable();
                List<String> availableTerms = AtpHelper.getTerms();
                for (PlanItemInfo planItemInfo : planItemInfos) {

                    if (!CollectionUtils.isEmpty(samplePlanYears) && !StringUtils.isEmpty(planItemInfo.getPlanPeriods())) {
                        String atpId = planItemInfo.getPlanPeriods().get(0);
                        String[] str = atpId.split("Year");
                        String term = str[0].trim();
                        int samplePlanYearIndex = Integer.parseInt(str[1].trim()) - 1;
                        int samplePlanTermIndex = availableTerms.indexOf(term);
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
                                    if (samplePlanForm.isPreview()) {
                                        samplePlanTerm.addCredit(samplePlanItem.getCredit());
                                        samplePlanYear.addCredit(samplePlanItem.getCredit());
                                        samplePlanForm.addCredit(samplePlanItem.getCredit());
                                    }
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
                                    samplePlanItem.setCredit(planItemInfo.getCredit() != null ? planItemInfo.getCredit().toString() : null);
                                    if (samplePlanForm.isPreview()) {
                                        samplePlanTerm.addCredit(samplePlanItem.getCredit());
                                        samplePlanYear.addCredit(samplePlanItem.getCredit());
                                        samplePlanForm.addCredit(samplePlanItem.getCredit());
                                    }
                                    samplePlanItem.setNote(planItemInfo.getDescr().getPlain());
                                    break;
                                } else if (PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(planItemInfo.getRefObjectType())) {
                                    samplePlanItem.setCode(planItemInfo.getRefObjectId());
                                    samplePlanItem.setPlanItemId(planItemInfo.getId());
                                    samplePlanItem.setCredit(planItemInfo.getCredit() != null ? planItemInfo.getCredit().toString() : null);
                                    if (samplePlanForm.isPreview()) {
                                        samplePlanTerm.addCredit(samplePlanItem.getCredit());
                                        samplePlanYear.addCredit(samplePlanItem.getCredit());
                                        samplePlanForm.addCredit(samplePlanItem.getCredit());
                                    }
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
         /*Authorization check*/
        if (GlobalVariables.getUserSession().retrieveObject(PlanConstants.SESSION_KEY_IS_ADVISER_MANAGE_PLAN) == null) {
            return new ModelAndView("redirect:/samplePlan/unauthorized");
        }
        SamplePlanForm samplePlanForm = (SamplePlanForm) form;
        if (isValidSamplePlan(samplePlanForm)) {
            try {

                if (samplePlanForm.isCopyPlan()) {
                    // TBD
                    return getUIFModelAndView(samplePlanForm);
                }

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
                if (CollectionUtils.isEmpty(commentInfos) || (!CollectionUtils.isEmpty(commentInfos) && StringUtils.hasText(samplePlanForm.getGeneralNotes()) && !samplePlanForm.getGeneralNotes().equals(commentInfos.get(0).getCommentText().getPlain()))) {
                    saveUpdateGeneralNotes(learningPlan.getId(), samplePlanForm.getGeneralNotes(), !CollectionUtils.isEmpty(commentInfos) ? commentInfos.get(0) : null);
                }
                Map<SamplePlanItem, PlanItemInfo> addOrUpdatePlanItems = new HashMap<SamplePlanItem, PlanItemInfo>();


                List<SamplePlanItem> deletePlanItems = new ArrayList<SamplePlanItem>();
                for (SamplePlanYear samplePlanYear : samplePlanForm.getSamplePlanYears()) {
                    int year = samplePlanYear.getYear();
                    for (SamplePlanTerm samplePlanTerm : samplePlanYear.getSamplePlanTerms()) {
                        String atpId = String.format(SamplePlanConstants.SAMPLE_PLAN_ATP_FORMAT, samplePlanTerm.getTermName(), year);
                        Map<String, Integer> refObjIdsAdded = new HashMap<String, Integer>();

                        Map<SamplePlanItem, PlanItemInfo> additionalCheckPlanItems = new HashMap<SamplePlanItem, PlanItemInfo>();

                        for (SamplePlanItem samplePlanItem : samplePlanTerm.getSamplePlanItems()) {

                            /*If "Alternate" course is not specified and regular course is specified*/
                            if (StringUtils.isEmpty(samplePlanItem.getAlternateCode()) && StringUtils.hasText(samplePlanItem.getCode())) {
                                if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {

                                    addCourseItem(learningPlan, samplePlanItem, refObjIdsAdded, additionalCheckPlanItems, addOrUpdatePlanItems, atpId);
                                } else if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_PLACE_HOLDER_REGEX)) {

                                    addCoursePlaceHolderItem(learningPlan, samplePlanItem, addOrUpdatePlanItems, atpId);

                                } else if (samplePlanItem.getCode().contains(PlanConstants.PLACEHOLDER_KEY_SEPARATOR)) {

                                    addPlaceHolderItem(learningPlan, samplePlanItem, addOrUpdatePlanItems, atpId);

                                } else {
                                    /*Invalid Item*/
                                    String[] params = {};
                                    GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_UNKNOWN_COURSE, params);
                                }
                            }

                            /*If "Alternate" course is specified and regular course is specified*/
                            else if (StringUtils.hasText(samplePlanItem.getAlternateCode()) && StringUtils.hasText(samplePlanItem.getCode())) {

                            /*TODO: Implement Logic for adding OR conditional Items*/


                            }

                            /*If "Alternate" course is specified and regular course is Not specified*/
                            else if (StringUtils.hasText(samplePlanItem.getAlternateCode()) && StringUtils.isEmpty(samplePlanItem.getCode())) {
                               /*Invalid Item*/
                                String[] params = {};
                                GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), SamplePlanConstants.REG_COURSE_MISSING, params);
                            }

                            /*Deleting "OR" PlanItems which are removed from UI*/
                            if (StringUtils.isEmpty(samplePlanItem.getAlternateCode()) && StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                              /*TODO: Implement Logic for deleting OR conditional Items*/
                            }

                            /*Deleting PlanItems which are removed from UI*/
                            if (StringUtils.isEmpty(samplePlanItem.getCode()) && StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                                deletePlanItems.add(samplePlanItem);
                            }

                        }

                        /*Duplicate course planItems re-validated as already planned items may have been updated now*/
                        for (SamplePlanItem samplePlanItem : additionalCheckPlanItems.keySet()) {
                            PlanItemInfo actualPlanItem = additionalCheckPlanItems.get(samplePlanItem);
                            if (refObjIdsAdded.containsKey(actualPlanItem.getRefObjectId()) && refObjIdsAdded.get(actualPlanItem.getRefObjectId()) > 1) {
                                /*PlanItem with same refObjId for given term already exists*/
                                String[] params = {getCourseHelper().getCourseInfo(actualPlanItem.getRefObjectId()).getCode(), atpId};
                                GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);

                            } else {
                                addOrUpdatePlanItems.put(samplePlanItem, actualPlanItem);
                            }
                        }
                    }
                }

                if (CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages())) {

                    /*Deleting planItems*/
                    for (SamplePlanItem samplePlanItem : deletePlanItems) {
                        try {
                            getAcademicPlanService().deletePlanItem(samplePlanItem.getPlanItemId(), SamplePlanConstants.CONTEXT_INFO);
                        } catch (Exception e) {
                            logger.error("Could not delete plaItem with Id: " + samplePlanItem.getPlanItemId(), e);
                        }
                    }


                    /*Update PlanItems*/
                    for (SamplePlanItem samplePlanItem : addOrUpdatePlanItems.keySet()) {
                        PlanItemInfo planItemInfo = addOrUpdatePlanItems.get(samplePlanItem);
                        try {
                            if (planItemInfo.getId() != null) {
                                planItemInfo = getAcademicPlanService().updatePlanItem(planItemInfo.getId(), planItemInfo, getUserSessionHelper().makeContextInfoInstance());
                                samplePlanItem.setPlanItemId(planItemInfo.getId());
                            }
                        } catch (Exception e) {
                            logger.error("Could not update Plan Item", e);
                        }
                    }

                    /*Create PlanItems*/
                    for (SamplePlanItem samplePlanItem : addOrUpdatePlanItems.keySet()) {
                        PlanItemInfo planItemInfo = addOrUpdatePlanItems.get(samplePlanItem);
                        try {
                            if (planItemInfo.getId() == null) {
                                planItemInfo = getAcademicPlanService().createPlanItem(planItemInfo, getUserSessionHelper().makeContextInfoInstance());
                                samplePlanItem.setPlanItemId(planItemInfo.getId());
                            }
                        } catch (Exception e) {
                            logger.error("Could not add Plan Item", e);
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
     * Validate Course and Builds PlanItem
     *
     * @param learningPlan
     * @param samplePlanItem
     * @param refObjIdsAdded
     * @param additionalCheckPlanItems
     * @param addOrUpdatePlanItems
     * @param atpId
     */
    private void addCourseItem(LearningPlanInfo learningPlan, SamplePlanItem samplePlanItem, Map<String, Integer> refObjIdsAdded, Map<SamplePlanItem, PlanItemInfo> additionalCheckPlanItems, Map<SamplePlanItem, PlanItemInfo> addOrUpdatePlanItems, String atpId) {
        PlanItemInfo actualPlanItem = null;
        boolean addOrUpdate = false;
        String code = samplePlanItem.getCode();
        String courseId = getCourseHelper().getCourseIdForCode(code);
        if (StringUtils.hasText(courseId)) {
            boolean isCrossListedCourse = false;
            CourseInfo courseInfo = getCourseHelper().getCourseInfo(courseId);
            try {
                isCrossListedCourse = getCourseHelper().isCrossListedCourse(courseInfo, code);
            } catch (DoesNotExistException e) {
                logger.error("Course not found" + code, e);
            }
            if (courseInfo != null) {
                if (StringUtils.hasText(samplePlanItem.getPlanItemId())) {

                    try {
                        actualPlanItem = getAcademicPlanService().getPlanItem(samplePlanItem.getPlanItemId(), SamplePlanConstants.CONTEXT_INFO);
                    } catch (Exception e) {
                        logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                    }


                    if (actualPlanItem != null) {
                        if (PlanConstants.STATEMENT_TYPE.equals(actualPlanItem.getRefObjectType())) {
                            /*TODO: Implement logic for deleting  statementTreeViewInfo, planItem and creating planItem for course*/

                        } else {
                            String versionIndId = actualPlanItem.getRefObjectId();
                            /*PlanItem exists and has same refObjId and only the note is modified*/
                            if (versionIndId.equals(courseInfo.getVersion().getVersionIndId()) && (actualPlanItem.getDescr() != null && actualPlanItem.getDescr().getPlain() != null && !actualPlanItem.getDescr().getPlain().equals(samplePlanItem.getNote()))) {
                                if (refObjIdsAdded.containsKey(courseInfo.getVersion().getVersionIndId())) {
                                    /*PlanItem with same refObjId for given term already exists*/
                                    String[] params = {courseInfo.getCode(), atpId};
                                    GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
                                } else {
                                    actualPlanItem = buildPlanItem(learningPlan, courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), null, isCrossListedCourse ? code : null, actualPlanItem.getId());
                                    addOrUpdate = true;
                                }
                            }
                                                /*PlanItem exists and has different refObjId*/
                            else if (actualPlanItem != null && (!versionIndId.equals(courseInfo.getVersion().getVersionIndId()) || !PlanConstants.COURSE_TYPE.equals(actualPlanItem.getRefObjectType()))) {
                                PlanItemInfo planItemInfo = getExistingPlanItem(courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, learningPlan.getId(), atpId);
                                if (planItemInfo != null) {
                                    actualPlanItem = buildPlanItem(learningPlan, courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), null, isCrossListedCourse ? code : null, null);
                                    additionalCheckPlanItems.put(samplePlanItem, actualPlanItem);
                                } else if (refObjIdsAdded.containsKey(courseInfo.getVersion().getVersionIndId())) {
                                    /*PlanItem with same refObjId for given term already exists*/
                                    String[] params = {courseInfo.getCode(), atpId};
                                    GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
                                } else {
                                    actualPlanItem = buildPlanItem(learningPlan, courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), null, isCrossListedCourse ? code : null, actualPlanItem.getId());
                                    addOrUpdate = true;
                                }
                            }
                        }


                    } else {
                        /*out of sync*/
                        String[] params = {};
                        GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED, params);
                    }


                }
                                            /*Creating a planItem for first time*/
                else {
                    PlanItemInfo planItemInfo = getExistingPlanItem(courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, learningPlan.getId(), atpId);
                    if (planItemInfo != null) {
                        actualPlanItem = buildPlanItem(learningPlan, courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), null, isCrossListedCourse ? code : null, null);
                        additionalCheckPlanItems.put(samplePlanItem, actualPlanItem);
                    } else if (refObjIdsAdded.containsKey(courseInfo.getVersion().getVersionIndId())) {
                        /*PlanItem with same refObjId for given term already exists*/
                        String[] params = {courseInfo.getCode(), atpId};
                        GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
                    } else {
                        actualPlanItem = buildPlanItem(learningPlan, courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), null, isCrossListedCourse ? code : null, null);
                        addOrUpdate = true;
                    }
                }

                if (refObjIdsAdded.containsKey(courseInfo.getVersion().getVersionIndId())) {
                    refObjIdsAdded.put(courseInfo.getVersion().getVersionIndId(), refObjIdsAdded.get(courseInfo.getVersion().getVersionIndId()) + 1);
                } else {
                    refObjIdsAdded.put(courseInfo.getVersion().getVersionIndId(), 1);
                }

            }
            /*Invalid Course*/
            else {
                String[] params = {courseInfo.getCode()};
                GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.COURSE_NOT_FOUND, params);
            }

        }
                                    /*Invalid Course*/
        else {
            String[] params = {code};
            GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.COURSE_NOT_FOUND, params);
        }

        if (actualPlanItem != null && addOrUpdate) {
            addOrUpdatePlanItems.put(samplePlanItem, actualPlanItem);
        }

    }

    /**
     * Validate Course PlaceHolder and Builds PlanItem
     *
     * @param learningPlan
     * @param samplePlanItem
     * @param addOrUpdatePlanItems
     * @param atpId
     */
    private void addCoursePlaceHolderItem(LearningPlanInfo learningPlan, SamplePlanItem samplePlanItem, Map<SamplePlanItem, PlanItemInfo> addOrUpdatePlanItems, String atpId) {

        PlanItemInfo actualPlanItem = null;
        boolean addOrUpdate = false;
        DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(samplePlanItem.getCode());


                                    /*Validate Course PlaceHolder and Build PlanItem*/
        ValidationResultInfo validationResultInfo = getCourseHelper().isValidCoursePlaceHolder(samplePlanItem.getCode());
        if (validationResultInfo == null) {
            String refObjId = getCourseHelper().getKeyForCourse(courseCode.getSubject(), courseCode.getNumber());
            String refObjType = PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL;

            if (StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                try {
                    actualPlanItem = getAcademicPlanService().getPlanItem(samplePlanItem.getPlanItemId(), SamplePlanConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                }
                if (actualPlanItem != null &&
                        ((actualPlanItem.getCredit() != null &&
                                !actualPlanItem.getCredit().equals(samplePlanItem.getCredit()))
                                || (actualPlanItem.getRefObjectId() != null &&
                                !actualPlanItem.getRefObjectId().equals(refObjId))
                                || (actualPlanItem.getRefObjectType() != null &&
                                !actualPlanItem.getRefObjectType().equals(refObjType))
                                || (actualPlanItem.getDescr() != null &&
                                actualPlanItem.getDescr().getPlain() != null &&
                                !actualPlanItem.getDescr().getPlain().equals(samplePlanItem.getNote())))) {
                    actualPlanItem = buildPlanItem(learningPlan, refObjId, refObjType, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), samplePlanItem.getCredit(), null, actualPlanItem.getId());
                    addOrUpdate = true;
                }

            } else {
                actualPlanItem = buildPlanItem(learningPlan, refObjId, refObjType, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), samplePlanItem.getCredit(), null, null);
                addOrUpdate = true;
            }
            if (actualPlanItem != null && addOrUpdate) {
                addOrUpdatePlanItems.put(samplePlanItem, actualPlanItem);
            }
        } else {
            /*Invalid Item*/
            String[] params = {courseCode.getSubject()};
            GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), validationResultInfo.getMessage(), params);
        }


    }

    /**
     * Validate PlaceHolder and Builds PlanItem
     *
     * @param learningPlan
     * @param samplePlanItem
     * @param addOrUpdatePlanItems
     * @param atpId
     */
    private void addPlaceHolderItem(LearningPlanInfo learningPlan, SamplePlanItem samplePlanItem, Map<SamplePlanItem, PlanItemInfo> addOrUpdatePlanItems, String atpId) {
        /*validating placeHolder*/
        PlanItemInfo actualPlanItem = null;
        boolean addOrUpdate = false;
        ValidationResultInfo validationResultInfo = getCourseHelper().isValidPlaceHolder(samplePlanItem.getCode(), samplePlanItem.getNote());
        if (validationResultInfo == null) {
            String[] placeHolder = samplePlanItem.getCode().split(PlanConstants.CODE_KEY_SEPARATOR);
            String placeHolderId = placeHolder[0];
            String placeHolderType = placeHolder[1];

            if (StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                try {
                    actualPlanItem = getAcademicPlanService().getPlanItem(samplePlanItem.getPlanItemId(), SamplePlanConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                }
                if (actualPlanItem != null && ((actualPlanItem.getCredit() != null && !actualPlanItem.getCredit().equals(samplePlanItem.getCredit())) || !actualPlanItem.getRefObjectId().equals(placeHolderId) || !actualPlanItem.getRefObjectType().equals(placeHolderType) || (actualPlanItem.getDescr() != null && !actualPlanItem.getDescr().getPlain().equals(samplePlanItem.getNote())))) {
                    actualPlanItem = buildPlanItem(learningPlan, placeHolderId, placeHolderType, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), samplePlanItem.getCredit(), null, actualPlanItem.getId());
                    addOrUpdate = true;
                }

            } else {
                actualPlanItem = buildPlanItem(learningPlan, placeHolderId, placeHolderType, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), samplePlanItem.getCredit(), null, null);
                addOrUpdate = true;
            }
            if (actualPlanItem != null && addOrUpdate) {
                addOrUpdatePlanItems.put(samplePlanItem, actualPlanItem);
            }
        } else {
                                        /*Invalid Item*/
            String[] params = {};
            GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), validationResultInfo.getMessage(), params);
        }
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
     * Validates if sample plan program and title combination is unique for all new ones
     *
     * @param samplePlanForm
     * @return
     */
    private boolean isValidSamplePlan(SamplePlanForm samplePlanForm) {
        boolean isValidSamplePlan = true;
        LearningPlanInfo learningPlanInfo = getExistingLearningPlan(samplePlanForm.getDegreeProgramTitle(), samplePlanForm.getPlanTitle());
        if (learningPlanInfo != null && learningPlanInfo.getId().equals(samplePlanForm.getLearningPlanId())) {
            String[] params = {samplePlanForm.getDegreeProgramTitle(), samplePlanForm.getPlanTitle()};
            GlobalVariables.getMessageMap().putError("planTitle", SamplePlanConstants.DUPLICATE_ERROR, params);
            isValidSamplePlan = false;
        }
        return isValidSamplePlan;
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
            int termIndex = 0;
            for (String term : AtpHelper.getTerms()) {
                SamplePlanTerm samplePlanTerm = new SamplePlanTerm();
                samplePlanTerm.setTermName(term);
                samplePlanTerm.setYear(i);
                List<SamplePlanItem> planItems = samplePlanTerm.getSamplePlanItems();
                for (int j = 1; j <= SamplePlanConstants.SAMPLE_PLAN_ITEMS_COUNT; j++) {
                    SamplePlanItem samplePlanItem = new SamplePlanItem();
                    samplePlanItem.setYearIndex(i - 1);
                    samplePlanItem.setTermIndex(termIndex);
                    samplePlanItem.setItemIndex(j - 1);
                    planItems.add(samplePlanItem);
                }
                samplePlanTerms.add(samplePlanTerm);
                termIndex++;
            }
            samplePlanYear.setSamplePlanTerms(samplePlanTerms);
            samplePlanYears.add(samplePlanYear);
        }
        return samplePlanYears;
    }


    /**
     * Builds Statement TreeViewInfo for given reqComponents
     *
     * @param reqComponentInfo1
     * @param reqComponentInfo2
     * @return
     */
    private StatementTreeViewInfo buildStatementTreeViewInfo(ReqComponentInfo reqComponentInfo1, ReqComponentInfo reqComponentInfo2) {
        StatementTreeViewInfo statementTreeViewInfo = new StatementTreeViewInfo();

        statementTreeViewInfo.setType(SamplePlanConstants.STATEMENT_TYPE_RECOMMENDED);
        statementTreeViewInfo.setOperator(StatementOperatorTypeKey.OR);

        statementTreeViewInfo.setReqComponents(Arrays.asList(reqComponentInfo1, reqComponentInfo2));

        return statementTreeViewInfo;

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
     * Adds or Updates Req Component for given value and type
     *
     * @param value
     * @param credit
     * @return
     */
    protected ReqComponentInfo buildReqComponent(String reqComponentType, String reqCompFieldType, String value, String credit) {

        ReqComponentInfo componentInfo = new ReqComponentInfo();
        componentInfo.setType(reqComponentType);
        componentInfo.setState("ACTIVE");

        List<ReqCompFieldInfo> reqCompFieldInfos = new ArrayList<ReqCompFieldInfo>();

            /*Creating a course/placeholder field*/
        ReqCompFieldInfo fieldInfo = new ReqCompFieldInfo();
        fieldInfo.setValue(value);
        fieldInfo.setType(reqCompFieldType);
        reqCompFieldInfos.add(fieldInfo);
        componentInfo.setReqCompFields(reqCompFieldInfos);

            /*Creating a credit field*/
        ReqCompFieldInfo creditField = new ReqCompFieldInfo();
        creditField.setValue(credit);
        creditField.setType(SamplePlanConstants.REQ_COMP_FIELD_TYPE_CREDIT);
        reqCompFieldInfos.add(creditField);
        componentInfo.setReqCompFields(reqCompFieldInfos);

        return componentInfo;

    }


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
    protected PlanItemInfo buildPlanItem(LearningPlan plan, String refObjId, String refObjType, String atpId, String planItemType, String note, String credit, String crossListedCourse, String planItemId)
            throws DuplicateEntryException {

        if (org.apache.commons.lang.StringUtils.isEmpty(refObjId)) {
            throw new RuntimeException("Empty Course ID");
        }

        PlanItemInfo pii = new PlanItemInfo();
        pii.setId(planItemId);
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

        return pii;
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
