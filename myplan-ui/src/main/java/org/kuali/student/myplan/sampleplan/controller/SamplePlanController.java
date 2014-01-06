package org.kuali.student.myplan.sampleplan.controller;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import edu.uw.kuali.student.myplan.util.PlanHelperImpl;
import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.commons.collections.ListUtils;
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
        if (!getUserSessionHelper().isAdviserForManagePlan()) {
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
                        if (StringUtils.hasText(atpId)) {
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
                                        String placeHolderCode = EnumerationHelper.getEnumAbbrValForCodeByType(planItemInfo.getRefObjectId(), planItemInfo.getRefObjectType());
                                        //String placeHolderValue = EnumerationHelper.getEnumValueForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.PLACE_HOLDER_ENUM_KEY);
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
                                    } else if (PlanConstants.STATEMENT_TYPE.equals(planItemInfo.getRefObjectType())) {
                                        StatementTreeViewInfo statementTreeViewInfo = null;
                                        try {
                                            statementTreeViewInfo = getStatementService().getStatementTreeView(planItemInfo.getRefObjectId());
                                        } catch (Exception e) {
                                            logger.error("Could not load statement tree view for statementId: " + planItemInfo.getRefObjectId(), e);
                                        }
                                        populateSamplePlanItemForStatement(samplePlanForm, samplePlanItem, statementTreeViewInfo, planItemInfo.getId(), planItemInfo.getDescr().getPlain());
                                        break;
                                    }
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
        if (!getUserSessionHelper().isAdviserForManagePlan()) {
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
                Map<SamplePlanItem, List<ReqComponentInfo>> addOrUpdateReqComponents = new HashMap<SamplePlanItem, List<ReqComponentInfo>>();
                Map<String, PlanItemInfo> statementTypePlanItems = new HashMap<String, PlanItemInfo>();


                Set<SamplePlanItem> deletePlanItems = new HashSet<SamplePlanItem>();
                for (SamplePlanYear samplePlanYear : samplePlanForm.getSamplePlanYears()) {
                    for (SamplePlanTerm samplePlanTerm : samplePlanYear.getSamplePlanTerms()) {
                        Map<String, Integer> refObjIdsAdded = new HashMap<String, Integer>();
                        Map<String, Integer> courseCodesAdded = new HashMap<String, Integer>();

                        Map<SamplePlanItem, PlanItemInfo> additionalCheckPlanItems = new HashMap<SamplePlanItem, PlanItemInfo>();
                        Map<SamplePlanItem, Map<String, Boolean>> additionalCheckReqComps = new HashMap<SamplePlanItem, Map<String, Boolean>>();

                        for (SamplePlanItem samplePlanItem : samplePlanTerm.getSamplePlanItems()) {

                            /*If "Alternate" course is not specified and regular course is specified*/
                            if (StringUtils.isEmpty(samplePlanItem.getAlternateCode()) && StringUtils.hasText(samplePlanItem.getCode())) {
                                if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {

                                    addCourseItem(learningPlan, samplePlanItem, refObjIdsAdded, courseCodesAdded, additionalCheckPlanItems, addOrUpdatePlanItems, deletePlanItems);

                                } else if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_PLACE_HOLDER_REGEX)) {

                                    addCoursePlaceHolderItem(learningPlan, samplePlanItem, addOrUpdatePlanItems);

                                } else if (samplePlanItem.getCode().contains(PlanConstants.PLACEHOLDER_KEY_SEPARATOR)) {

                                    addPlaceHolderItem(learningPlan, samplePlanItem, addOrUpdatePlanItems);

                                } else {
                                    /*Invalid Item*/
                                    String[] params = {};
                                    GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_UNKNOWN_COURSE, params);
                                }
                            }

                            /*If "Alternate" course is specified and regular course is also specified*/
                            else if (StringUtils.hasText(samplePlanItem.getAlternateCode()) && StringUtils.hasText(samplePlanItem.getCode())) {

                                /*Creating ReqComponentInfo for regular item*/
                                if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {

                                    addCourseReqComponent(learningPlan, samplePlanItem, refObjIdsAdded, courseCodesAdded, addOrUpdateReqComponents, statementTypePlanItems, additionalCheckReqComps, deletePlanItems, false);


                                } else if (samplePlanItem.getCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_PLACE_HOLDER_REGEX)) {

                                    addReqComponentCoursePlaceHolderItem(learningPlan, samplePlanItem, addOrUpdateReqComponents, statementTypePlanItems, false);

                                } else if (samplePlanItem.getCode().contains(PlanConstants.PLACEHOLDER_KEY_SEPARATOR)) {

                                    addReqComponentPlaceHolderItem(learningPlan, samplePlanItem, addOrUpdateReqComponents, statementTypePlanItems, false);

                                } else {
                                    /*Invalid Item*/
                                    String[] params = {};
                                    GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_UNKNOWN_COURSE, params);
                                }




                                /*Creating ReqComponentInfo for alternative item*/
                                if (samplePlanItem.getAlternateCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {

                                    addCourseReqComponent(learningPlan, samplePlanItem, refObjIdsAdded, courseCodesAdded, addOrUpdateReqComponents, statementTypePlanItems, additionalCheckReqComps, deletePlanItems, true);

                                } else if (samplePlanItem.getAlternateCode().matches(CourseSearchConstants.UNFORMATTED_COURSE_PLACE_HOLDER_REGEX)) {

                                    addReqComponentCoursePlaceHolderItem(learningPlan, samplePlanItem, addOrUpdateReqComponents, statementTypePlanItems, true);

                                } else if (samplePlanItem.getAlternateCode().contains(PlanConstants.PLACEHOLDER_KEY_SEPARATOR)) {

                                    addReqComponentPlaceHolderItem(learningPlan, samplePlanItem, addOrUpdateReqComponents, statementTypePlanItems, true);

                                } else {
                                    /*Invalid Item*/
                                    String[] params = {};
                                    GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.ALT_CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_UNKNOWN_COURSE, params);
                                }

                            }

                            /*If "Alternate" course is specified and regular course is Not specified*/
                            else if (StringUtils.hasText(samplePlanItem.getAlternateCode()) && StringUtils.isEmpty(samplePlanItem.getCode())) {
                               /*Invalid Item*/
                                String[] params = {};
                                GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), SamplePlanConstants.REG_COURSE_MISSING, params);
                            }

                            /*Deleting Alternate PlanItems which are removed from UI*/
                            if ((StringUtils.isEmpty(samplePlanItem.getCode()) && StringUtils.hasText(samplePlanItem.getReqComponentId())) && (StringUtils.isEmpty(samplePlanItem.getAlternateCode()) && StringUtils.hasText(samplePlanItem.getAlternateReqComponentId())) && StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                                deletePlanItems.add(samplePlanItem);
                            }

                            /*Deleting PlanItems which are removed from UI*/
                            if (StringUtils.isEmpty(samplePlanItem.getCode()) && StringUtils.isEmpty(samplePlanItem.getReqComponentId()) && StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                                deletePlanItems.add(samplePlanItem);
                            }

                        }

                        /*Duplicate course planItems re-validated as already planned items may have been updated now*/
                        for (SamplePlanItem samplePlanItem : additionalCheckPlanItems.keySet()) {
                            PlanItemInfo actualPlanItem = additionalCheckPlanItems.get(samplePlanItem);
                            if (refObjIdsAdded.containsKey(actualPlanItem.getRefObjectId()) && refObjIdsAdded.get(actualPlanItem.getRefObjectId()) > 1) {
                                /*PlanItem with same refObjId for given term already exists*/
                                String[] params = {getCourseHelper().getCourseInfo(actualPlanItem.getRefObjectId()).getCode(), samplePlanItem.getAtpId()};
                                GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);

                            } else {
                                addOrUpdatePlanItems.put(samplePlanItem, actualPlanItem);
                            }
                        }

                        /*Duplicate course reqComponentFields re-validated as already planned items may have been updated now*/
                        for (SamplePlanItem samplePlanItem : additionalCheckReqComps.keySet()) {
                            Map<String, Boolean> courseCodes = additionalCheckReqComps.get(samplePlanItem);
                            List<ReqComponentInfo> reqComponentInfos = new ArrayList<ReqComponentInfo>();

                            boolean validNow = true;
                            for (String course : courseCodes.keySet()) {
                                String errorCodeValidationFormat = courseCodes.get(course) ? SamplePlanConstants.ALT_CODE_VALIDATION_ERROR_FORMAT : SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT;
                                if (courseCodesAdded.containsKey(course) && courseCodesAdded.get(course) > 1) {
                                    /*PlanItem with same refObjId for given term already exists*/
                                    validNow = false;
                                    String[] params = {course, samplePlanItem.getAtpId()};
                                    GlobalVariables.getMessageMap().putError(String.format(errorCodeValidationFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
                                } else {
                                    reqComponentInfos.add(buildReqComponent(SamplePlanConstants.REQ_COMP_TYPE_COURSE, SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE, course, ""));
                                }
                            }

                            if (validNow && !CollectionUtils.isEmpty(reqComponentInfos)) {
                                if (addOrUpdateReqComponents.get(samplePlanItem) != null) {
                                    /*In this case there will be only one reqComponent will be available*/
                                    addOrUpdateReqComponents.get(samplePlanItem).add(reqComponentInfos.get(0));
                                } else {
                                    addOrUpdateReqComponents.put(samplePlanItem, reqComponentInfos);
                                }

                                String identicalCourseCode = hasIdenticalReqCompFields(addOrUpdateReqComponents.get(samplePlanItem));
                                if (identicalCourseCode != null) {
                                    String[] params = {identicalCourseCode, samplePlanItem.getAtpId()};
                                    GlobalVariables.getMessageMap().putError(String.format(SamplePlanConstants.ALT_CODE_VALIDATION_ERROR_FORMAT, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
                                }

                            }

                        }
                    }
                }

                if (CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages())) {

                    /*Deleting planItems*/
                    for (SamplePlanItem samplePlanItem : deletePlanItems) {
                        try {

                            PlanItemInfo planItemInfo = getAcademicPlanService().getPlanItem(samplePlanItem.getPlanItemId(), SamplePlanConstants.CONTEXT_INFO);

                            if (PlanConstants.STATEMENT_TYPE.equals(planItemInfo.getRefObjectType())) {
                                /*deleteing statementTreeViewInfo*/
                                getStatementService().deleteStatementTreeView(planItemInfo.getRefObjectId());
                            }

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

                    /*Create Statements & PlanItems*/
                    for (SamplePlanItem samplePlanItem : addOrUpdateReqComponents.keySet()) {
                        List<ReqComponentInfo> reqComponentInfos = addOrUpdateReqComponents.get(samplePlanItem);

                        if (!CollectionUtils.isEmpty(reqComponentInfos)) {
                            if (StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                                /*Updating*/
                                PlanItemInfo planItemInfo = statementTypePlanItems.get(samplePlanItem.getPlanItemId());
                                addUpdateStatementTreeViewInfo(reqComponentInfos, planItemInfo.getRefObjectId());
                            } else {
                                /*Creating*/
                                StatementTreeViewInfo statementTreeViewInfo = addUpdateStatementTreeViewInfo(reqComponentInfos, null);
                                try {
                                    PlanItemInfo planItemInfo = buildPlanItem(learningPlan, statementTreeViewInfo.getId(), PlanConstants.STATEMENT_TYPE, samplePlanItem.getAtpId(), PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), null, null, samplePlanItem.getPlanItemId());
                                    planItemInfo = getAcademicPlanService().createPlanItem(planItemInfo, getUserSessionHelper().makeContextInfoInstance());
                                    samplePlanItem.setCode(null);
                                    samplePlanItem.setAlternateCode(null);
                                    populateSamplePlanItemForStatement(samplePlanForm, samplePlanItem, statementTreeViewInfo, planItemInfo.getId(), planItemInfo.getDescr().getPlain());

                                } catch (Exception e) {
                                    logger.error("Could not add Plan Item", e);
                                }
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
     * Populates the samplePlanItem with associated code, credit and reqComponentId associated with statementTreeView components given
     *
     * @param samplePlanForm
     * @param samplePlanItem
     * @param statementTreeViewInfo
     * @param planItemId
     * @param note
     */
    private void populateSamplePlanItemForStatement(SamplePlanForm samplePlanForm, SamplePlanItem samplePlanItem, StatementTreeViewInfo statementTreeViewInfo, String planItemId, String note) {
        SamplePlanYear samplePlanYear = samplePlanForm.getSamplePlanYears().get(samplePlanItem.getYearIndex());
        SamplePlanTerm samplePlanTerm = samplePlanForm.getSamplePlanYears().get(samplePlanItem.getYearIndex()).getSamplePlanTerms().get(samplePlanItem.getTermIndex());

        if (statementTreeViewInfo != null) {
            for (ReqComponentInfo reqComponentInfo : statementTreeViewInfo.getReqComponents()) {
                if (SamplePlanConstants.REQ_COMP_TYPE_COURSE.equals(reqComponentInfo.getType())) {
                    for (ReqCompFieldInfo reqCompFieldInfo : reqComponentInfo.getReqCompFields()) {
                        if (SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE.equals(reqCompFieldInfo.getType())) {
                            String courseId = getCourseHelper().getCourseIdForCode(reqCompFieldInfo.getValue());
                            CourseInfo courseInfo = getCourseHelper().getCourseInfoByIdAndCd(courseId, reqCompFieldInfo.getValue());
                            samplePlanItem.setPlanItemId(planItemId);

                            if (samplePlanItem.getCode() != null) {
                                samplePlanItem.setAlternateCode(courseInfo.getCode());
                                samplePlanItem.setPlanItemId(planItemId);
                                samplePlanItem.setAlternateCredit(CreditsFormatter.formatCredits(courseInfo));
                                samplePlanItem.setAlternateReqComponentId(reqComponentInfo.getId());
                                if (samplePlanForm.isPreview()) {
                                    samplePlanTerm.addCredit(samplePlanItem.getAlternateCredit());
                                    samplePlanYear.addCredit(samplePlanItem.getAlternateCredit());
                                    samplePlanForm.addCredit(samplePlanItem.getAlternateCredit());
                                }
                            }

                            if (samplePlanItem.getCode() == null) {
                                samplePlanItem.setCode(courseInfo.getCode());
                                samplePlanItem.setPlanItemId(planItemId);
                                samplePlanItem.setCredit(CreditsFormatter.formatCredits(courseInfo));
                                samplePlanItem.setReqComponentId(reqComponentInfo.getId());
                                if (samplePlanForm.isPreview()) {
                                    samplePlanTerm.addCredit(samplePlanItem.getCredit());
                                    samplePlanYear.addCredit(samplePlanItem.getCredit());
                                    samplePlanForm.addCredit(samplePlanItem.getCredit());
                                }
                            }


                        }
                    }
                } else if (SamplePlanConstants.REQ_COMP_TYPE_PLACEHOLDER.equals(reqComponentInfo.getType())) {

                    String credit = null;
                    /*Extracting credit value*/
                    for (ReqCompFieldInfo reqCompFieldInfo : reqComponentInfo.getReqCompFields()) {
                        if (SamplePlanConstants.REQ_COMP_FIELD_TYPE_CREDIT.equals(reqCompFieldInfo.getType())) {
                            credit = reqCompFieldInfo.getValue();
                            break;
                        }
                    }

                    for (ReqCompFieldInfo reqCompFieldInfo : reqComponentInfo.getReqCompFields()) {
                        if (SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE_PLACEHOLDER.equals(reqCompFieldInfo.getType())) {
                            if (samplePlanItem.getCode() != null) {
                                samplePlanItem.setAlternateCode(reqCompFieldInfo.getValue());
                                samplePlanItem.setAlternateCredit(credit);
                                samplePlanItem.setPlanItemId(planItemId);
                                samplePlanItem.setAlternateReqComponentId(reqComponentInfo.getId());
                                if (samplePlanForm.isPreview()) {
                                    samplePlanTerm.addCredit(samplePlanItem.getAlternateCredit());
                                    samplePlanYear.addCredit(samplePlanItem.getAlternateCredit());
                                    samplePlanForm.addCredit(samplePlanItem.getAlternateCredit());
                                }
                            }

                            if (samplePlanItem.getCode() == null) {
                                samplePlanItem.setCode(reqCompFieldInfo.getValue());
                                samplePlanItem.setPlanItemId(planItemId);
                                samplePlanItem.setCredit(credit);
                                samplePlanItem.setReqComponentId(reqComponentInfo.getId());
                                if (samplePlanForm.isPreview()) {
                                    samplePlanTerm.addCredit(samplePlanItem.getCredit());
                                    samplePlanYear.addCredit(samplePlanItem.getCredit());
                                    samplePlanForm.addCredit(samplePlanItem.getCredit());
                                }
                            }
                        } else if (SamplePlanConstants.REQ_COMP_FIELD_TYPE_PLACEHOLDER.equals(reqCompFieldInfo.getType())) {
                            String placeHolderCode = reqCompFieldInfo.getValue();
                            if (samplePlanForm.isPreview()) {
                                String[] placeHolder = reqCompFieldInfo.getValue().split(PlanConstants.CODE_KEY_SEPARATOR);
                                String placeHolderId = placeHolder[0];
                                String placeHolderType = placeHolder[1];
                                placeHolderCode = EnumerationHelper.getEnumAbbrValForCodeByType(placeHolderId, placeHolderType);
                            }
                            //String placeHolderValue = EnumerationHelper.getEnumValueForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.PLACE_HOLDER_ENUM_KEY);
                            if (samplePlanItem.getCode() != null) {
                                samplePlanItem.setAlternateCode(placeHolderCode);
                                samplePlanItem.setAlternateCredit(credit);
                                samplePlanItem.setPlanItemId(planItemId);
                                samplePlanItem.setAlternateReqComponentId(reqComponentInfo.getId());
                                if (samplePlanForm.isPreview()) {
                                    samplePlanTerm.addCredit(samplePlanItem.getAlternateCredit());
                                    samplePlanYear.addCredit(samplePlanItem.getAlternateCredit());
                                    samplePlanForm.addCredit(samplePlanItem.getAlternateCredit());
                                }
                            }

                            if (samplePlanItem.getCode() == null) {
                                samplePlanItem.setCode(placeHolderCode);
                                samplePlanItem.setPlanItemId(planItemId);
                                samplePlanItem.setCredit(credit);
                                samplePlanItem.setReqComponentId(reqComponentInfo.getId());
                                if (samplePlanForm.isPreview()) {
                                    samplePlanTerm.addCredit(samplePlanItem.getCredit());
                                    samplePlanYear.addCredit(samplePlanItem.getCredit());
                                    samplePlanForm.addCredit(samplePlanItem.getCredit());
                                }
                            }
                        }
                    }
                }
            }
        }
        samplePlanItem.setNote(note);
    }


    /**
     * Validate Course and Builds PlanItem
     *
     * @param learningPlan
     * @param samplePlanItem
     * @param refObjIdsAdded
     * @param additionalCheckPlanItems
     * @param addOrUpdatePlanItems
     */
    private void addCourseItem(LearningPlanInfo learningPlan, SamplePlanItem samplePlanItem, Map<String, Integer> refObjIdsAdded, Map<String, Integer> courseCodesAdded, Map<SamplePlanItem, PlanItemInfo> additionalCheckPlanItems, Map<SamplePlanItem, PlanItemInfo> addOrUpdatePlanItems, Set<SamplePlanItem> deletePlanItems) {
        PlanItemInfo actualPlanItem = null;
        boolean addOrUpdate = false;
        String code = samplePlanItem.getCode();
        String atpId = samplePlanItem.getAtpId();
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
                            /*Case where a alternate course statement is changed to a regular course planitem*/
                                /*add samplePlanItem to delete list*/
                            deletePlanItems.add(samplePlanItem);

                                /*Create a new PlanItemInfo*/
                            actualPlanItem = buildPlanItem(learningPlan, courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, samplePlanItem.getNote(), null, isCrossListedCourse ? code : null, null);
                            addOrUpdate = true;

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
                                if (planItemInfo != null || hasDuplicateReqCompField(learningPlan.getId(), atpId, courseInfo.getCode(), null)) {
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
                    if (planItemInfo != null || hasDuplicateReqCompField(learningPlan.getId(), atpId, courseInfo.getCode(), null)) {
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
                    courseCodesAdded.put(courseInfo.getCode(), courseCodesAdded.get(courseInfo.getCode()) + 1);
                } else {
                    refObjIdsAdded.put(courseInfo.getVersion().getVersionIndId(), 1);
                    courseCodesAdded.put(courseInfo.getCode(), 1);
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
     * Validate Course and Builds PlanItem
     *
     * @param samplePlanItem
     * @param refObjIdsAdded
     */
    private void addCourseReqComponent(LearningPlan learningPlan, SamplePlanItem samplePlanItem, Map<String, Integer> refObjIdsAdded, Map<String, Integer> courseCodesAdded, Map<SamplePlanItem, List<ReqComponentInfo>> addOrUpdateReqComponents, Map<String, PlanItemInfo> statementTypePlanItems, Map<SamplePlanItem, Map<String, Boolean>> additionalCheckReqComps, Set<SamplePlanItem> deletePlanItems, boolean alternateCourse) {
        ReqComponentInfo reqComponentInfo = null;
        String code = alternateCourse ? samplePlanItem.getAlternateCode() : samplePlanItem.getCode();
        String courseId = getCourseHelper().getCourseIdForCode(code);
        String codeValidationErrorFormat = alternateCourse ? SamplePlanConstants.ALT_CODE_VALIDATION_ERROR_FORMAT : SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT;
        String reqComponentId = alternateCourse ? samplePlanItem.getAlternateReqComponentId() : samplePlanItem.getReqComponentId();
        if (StringUtils.hasText(courseId)) {
            CourseInfo courseInfo = getCourseHelper().getCourseInfoByIdAndCd(courseId, code);
            if (courseInfo != null) {
                code = courseInfo.getCode();
                PlanItemInfo existingPlanItem = getExistingPlanItem(courseInfo.getVersion().getVersionIndId(), PlanConstants.COURSE_TYPE, learningPlan.getId(), samplePlanItem.getAtpId());
                if (existingPlanItem == null && !hasDuplicateReqCompField(learningPlan.getId(), samplePlanItem.getAtpId(), courseInfo.getCode(), reqComponentId)) {
                    if (StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                        PlanItemInfo planItemInfo = null;
                        try {
                            planItemInfo = getAcademicPlanService().getPlanItem(samplePlanItem.getPlanItemId(), SamplePlanConstants.CONTEXT_INFO);
                        } catch (Exception e) {
                            logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                        }


                        if (planItemInfo != null) {

                            if (PlanConstants.STATEMENT_TYPE.equals(planItemInfo.getRefObjectType())) {
                                try {
                                    reqComponentInfo = getStatementService().getReqComponent(reqComponentId);
                                } catch (Exception e) {
                                    logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                                }

                                if (reqComponentInfo != null) {
                                    if (!SamplePlanConstants.REQ_COMP_TYPE_COURSE.equals(reqComponentInfo.getType())) {
                                        reqComponentInfo.setType(SamplePlanConstants.REQ_COMP_TYPE_COURSE);
                                    }
                                    ReqCompFieldInfo courseField = null;
                                    ReqCompFieldInfo creditField = null;
                                    for (ReqCompFieldInfo reqCompFieldInfo : reqComponentInfo.getReqCompFields()) {

                                        if (SamplePlanConstants.REQ_COMP_FIELD_TYPE_CREDIT.equals(reqCompFieldInfo.getType())) {

                                            creditField = reqCompFieldInfo;
                                        } else {
                                            courseField = reqCompFieldInfo;
                                        }

                                        if (creditField != null && courseField != null) {
                                            break;
                                        }
                                    }
                                    if (!SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE.equals(courseField.getType()) || !courseInfo.getCode().equals(courseField.getValue())) {
                                        courseField.setType(SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE);
                                        courseField.setValue(courseInfo.getCode());
                                    }
                                    if (StringUtils.hasText(creditField.getValue())) {
                                        creditField.setValue("");
                                    }

                                } else {
                                /*out of sync*/
                                    String[] params = {};
                                    GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED, params);
                                }
                                statementTypePlanItems.put(planItemInfo.getId(), planItemInfo);

                            } else {
                                /*Case where a regular course is changed to a alternate course*/
                                /*add samplePlanItem to delete list*/
                                deletePlanItems.add(samplePlanItem);

                                /*Create a new reqComponent*/
                                reqComponentInfo = buildReqComponent(SamplePlanConstants.REQ_COMP_TYPE_COURSE, SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE, courseInfo.getCode(), "");
                            }


                        } else {
                        /*out of sync*/
                            String[] params = {};
                            GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED, params);
                        }


                    }
                /*Creating a reqComponent for first time*/
                    else {
                        reqComponentInfo = buildReqComponent(SamplePlanConstants.REQ_COMP_TYPE_COURSE, SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE, courseInfo.getCode(), "");
                    }
                } else if (refObjIdsAdded.containsKey(courseInfo.getVersion().getVersionIndId())) {
                        /*PlanItem with same refObjId for given term already exists*/
                    String[] params = {courseInfo.getCode(), samplePlanItem.getAtpId()};
                    GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
                } else {
                    if (additionalCheckReqComps.containsKey(samplePlanItem)) {
                        Map<String, Boolean> course = additionalCheckReqComps.get(samplePlanItem);
                        course.put(courseInfo.getCode(), alternateCourse);
                    } else {
                        Map<String, Boolean> course = new HashMap<String, Boolean>();
                        course.put(courseInfo.getCode(), alternateCourse);
                        additionalCheckReqComps.put(samplePlanItem, course);
                    }
                }

                if (refObjIdsAdded.containsKey(courseInfo.getVersion().getVersionIndId())) {
                    refObjIdsAdded.put(courseInfo.getVersion().getVersionIndId(), refObjIdsAdded.get(courseInfo.getVersion().getVersionIndId()) + 1);
                    courseCodesAdded.put(courseInfo.getCode(), courseCodesAdded.get(courseInfo.getCode()) + 1);
                } else {
                    refObjIdsAdded.put(courseInfo.getVersion().getVersionIndId(), 1);
                    courseCodesAdded.put(courseInfo.getCode(), 1);
                }

            }

        } else {
            /*Invalid Course*/
            String[] params = {code};
            GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.COURSE_NOT_FOUND, params);
        }

        if (reqComponentInfo != null) {
            if (addOrUpdateReqComponents.containsKey(samplePlanItem)) {
                addOrUpdateReqComponents.get(samplePlanItem).add(reqComponentInfo);
                if (hasIdenticalReqCompFields(addOrUpdateReqComponents.get(samplePlanItem)) != null) {
                    String[] params = {code, samplePlanItem.getAtpId()};
                    GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
                }
            } else {
                List<ReqComponentInfo> reqComponentInfos = new ArrayList<ReqComponentInfo>();
                reqComponentInfos.add(reqComponentInfo);
                addOrUpdateReqComponents.put(samplePlanItem, reqComponentInfos);
            }
        }


    }

    /**
     * Validate Course PlaceHolder and Builds ReqComponent
     *
     * @param learningPlan
     * @param samplePlanItem
     * @param addOrUpdateReqComponents
     */
    private void addReqComponentCoursePlaceHolderItem(LearningPlanInfo learningPlan, SamplePlanItem samplePlanItem, Map<SamplePlanItem, List<ReqComponentInfo>> addOrUpdateReqComponents, Map<String, PlanItemInfo> statementTypePlanItems, boolean alternateCourse) {
        ReqComponentInfo reqComponentInfo = null;
        String code = alternateCourse ? samplePlanItem.getAlternateCode() : samplePlanItem.getCode();
        String codeValidationErrorFormat = alternateCourse ? SamplePlanConstants.ALT_CODE_VALIDATION_ERROR_FORMAT : SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT;
        String credit = alternateCourse ? samplePlanItem.getAlternateCredit() : samplePlanItem.getCredit();
        DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(code);

        /*Validate Course PlaceHolder and Build PlanItem*/
        ValidationResultInfo validationResultInfo = getCourseHelper().isValidCoursePlaceHolder(code);
        if (validationResultInfo == null) {
            String coursePlaceholder = getCourseHelper().getKeyForCourse(courseCode.getSubject(), courseCode.getNumber());
            if (StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                PlanItemInfo planItemInfo = null;
                try {
                    planItemInfo = getAcademicPlanService().getPlanItem(samplePlanItem.getPlanItemId(), SamplePlanConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                }


                if (planItemInfo != null) {
                    try {
                        reqComponentInfo = getStatementService().getReqComponent(alternateCourse ? samplePlanItem.getAlternateReqComponentId() : samplePlanItem.getReqComponentId());
                    } catch (Exception e) {
                        logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                    }

                    if (reqComponentInfo != null) {
                        if (!SamplePlanConstants.REQ_COMP_TYPE_PLACEHOLDER.equals(reqComponentInfo.getType())) {
                            reqComponentInfo.setType(SamplePlanConstants.REQ_COMP_TYPE_PLACEHOLDER);
                        }
                        ReqCompFieldInfo coursePlaceHolderField = null;
                        ReqCompFieldInfo creditField = null;
                        for (ReqCompFieldInfo reqCompFieldInfo : reqComponentInfo.getReqCompFields()) {

                            if (SamplePlanConstants.REQ_COMP_FIELD_TYPE_CREDIT.equals(reqCompFieldInfo.getType())) {

                                creditField = reqCompFieldInfo;
                            } else {
                                coursePlaceHolderField = reqCompFieldInfo;
                            }

                            if (creditField != null && coursePlaceHolderField != null) {
                                break;
                            }
                        }
                        if (!SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE_PLACEHOLDER.equals(coursePlaceHolderField.getType()) || !coursePlaceholder.equals(coursePlaceHolderField.getValue())) {
                            coursePlaceHolderField.setType(SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE_PLACEHOLDER);
                            coursePlaceHolderField.setValue(coursePlaceholder);
                        }

                        creditField.setValue(credit);


                    } else {
                        /*out of sync*/
                        String[] params = {};
                        GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED, params);
                    }
                    statementTypePlanItems.put(planItemInfo.getId(), planItemInfo);

                } else {
                    /*out of sync*/
                    String[] params = {};
                    GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED, params);
                }
            } else {


                reqComponentInfo = buildReqComponent(SamplePlanConstants.REQ_COMP_TYPE_PLACEHOLDER, SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE_PLACEHOLDER, coursePlaceholder, credit);

            }
            if (reqComponentInfo != null) {
                if (addOrUpdateReqComponents.containsKey(samplePlanItem)) {
                    addOrUpdateReqComponents.get(samplePlanItem).add(reqComponentInfo);
                } else {
                    List<ReqComponentInfo> reqComponentInfos = new ArrayList<ReqComponentInfo>();
                    reqComponentInfos.add(reqComponentInfo);
                    addOrUpdateReqComponents.put(samplePlanItem, reqComponentInfos);
                }
            }
        } else {
            /*Invalid Item*/
            String[] params = {courseCode.getSubject()};
            GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), validationResultInfo.getMessage(), params);
        }


    }


    /**
     * Validate PlaceHolder and Builds ReqComponent
     *
     * @param learningPlan
     * @param samplePlanItem
     * @param addOrUpdateReqComponents
     */
    private void addReqComponentPlaceHolderItem(LearningPlanInfo learningPlan, SamplePlanItem samplePlanItem, Map<SamplePlanItem, List<ReqComponentInfo>> addOrUpdateReqComponents, Map<String, PlanItemInfo> statementTypePlanItems, boolean alternateCourse) {
        ReqComponentInfo reqComponentInfo = null;
        String placeholderKey = alternateCourse ? samplePlanItem.getAlternateCode() : samplePlanItem.getCode();
        String codeValidationErrorFormat = alternateCourse ? SamplePlanConstants.ALT_CODE_VALIDATION_ERROR_FORMAT : SamplePlanConstants.CODE_VALIDATION_ERROR_FORMAT;
        String credit = alternateCourse ? samplePlanItem.getAlternateCredit() : samplePlanItem.getCredit();

        /*Validate Course PlaceHolder and Build PlanItem*/
        ValidationResultInfo validationResultInfo = getCourseHelper().isValidPlaceHolder(placeholderKey, samplePlanItem.getNote());

        if (validationResultInfo == null) {
            if (StringUtils.hasText(samplePlanItem.getPlanItemId())) {
                PlanItemInfo planItemInfo = null;
                try {
                    planItemInfo = getAcademicPlanService().getPlanItem(samplePlanItem.getPlanItemId(), SamplePlanConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                }
                if (planItemInfo != null) {
                    try {
                        reqComponentInfo = getStatementService().getReqComponent(alternateCourse ? samplePlanItem.getAlternateReqComponentId() : samplePlanItem.getReqComponentId());
                    } catch (Exception e) {
                        logger.error("Could Not load planItem for planId: " + samplePlanItem.getPlanItemId(), e);
                    }

                    if (reqComponentInfo != null) {
                        if (!SamplePlanConstants.REQ_COMP_TYPE_PLACEHOLDER.equals(reqComponentInfo.getType())) {
                            reqComponentInfo.setType(SamplePlanConstants.REQ_COMP_TYPE_PLACEHOLDER);
                        }
                        ReqCompFieldInfo coursePlaceHolderField = null;
                        ReqCompFieldInfo creditField = null;
                        for (ReqCompFieldInfo reqCompFieldInfo : reqComponentInfo.getReqCompFields()) {

                            if (SamplePlanConstants.REQ_COMP_FIELD_TYPE_CREDIT.equals(reqCompFieldInfo.getType())) {

                                creditField = reqCompFieldInfo;
                            } else {
                                coursePlaceHolderField = reqCompFieldInfo;
                            }

                            if (creditField != null && coursePlaceHolderField != null) {
                                break;
                            }
                        }
                        if (!SamplePlanConstants.REQ_COMP_FIELD_TYPE_PLACEHOLDER.equals(coursePlaceHolderField.getType()) || !placeholderKey.equals(coursePlaceHolderField.getValue())) {
                            coursePlaceHolderField.setType(SamplePlanConstants.REQ_COMP_FIELD_TYPE_PLACEHOLDER);
                            coursePlaceHolderField.setValue(placeholderKey);
                        }

                        creditField.setValue(credit);


                    } else {
                        /*out of sync*/
                        String[] params = {};
                        GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED, params);
                    }
                    statementTypePlanItems.put(planItemInfo.getId(), planItemInfo);
                } else {
                    /*out of sync*/
                    String[] params = {};
                    GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED, params);
                }
            } else {
                reqComponentInfo = buildReqComponent(SamplePlanConstants.REQ_COMP_TYPE_PLACEHOLDER, SamplePlanConstants.REQ_COMP_FIELD_TYPE_PLACEHOLDER, placeholderKey, credit);
            }
            if (reqComponentInfo != null) {
                if (addOrUpdateReqComponents.containsKey(samplePlanItem)) {
                    addOrUpdateReqComponents.get(samplePlanItem).add(reqComponentInfo);
                } else {
                    List<ReqComponentInfo> reqComponentInfos = new ArrayList<ReqComponentInfo>();
                    reqComponentInfos.add(reqComponentInfo);
                    addOrUpdateReqComponents.put(samplePlanItem, reqComponentInfos);
                }
            }
        } else {
                                        /*Invalid Item*/
            String[] params = {};
            GlobalVariables.getMessageMap().putError(String.format(codeValidationErrorFormat, samplePlanItem.getYearIndex(), samplePlanItem.getTermIndex(), samplePlanItem.getItemIndex()), validationResultInfo.getMessage(), params);
        }

    }


    /**
     * Validate Course PlaceHolder and Builds PlanItem
     *
     * @param learningPlan
     * @param samplePlanItem
     * @param addOrUpdatePlanItems
     */
    private void addCoursePlaceHolderItem(LearningPlanInfo learningPlan, SamplePlanItem samplePlanItem, Map<SamplePlanItem, PlanItemInfo> addOrUpdatePlanItems) {
        String atpId = samplePlanItem.getAtpId();
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
     */
    private void addPlaceHolderItem(LearningPlanInfo learningPlan, SamplePlanItem samplePlanItem, Map<SamplePlanItem, PlanItemInfo> addOrUpdatePlanItems) {
        /*validating placeHolder*/
        String atpId = samplePlanItem.getAtpId();
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
     * validates if any of the ReqComponentFields have identical values and returns the identical courseCode
     *
     * @param reqComponentInfos
     * @return
     */
    private String hasIdenticalReqCompFields(List<ReqComponentInfo> reqComponentInfos) {
        List<String> courses = new ArrayList<String>();
        for (ReqComponentInfo reqComponentInfo : reqComponentInfos) {
            if (SamplePlanConstants.REQ_COMP_TYPE_COURSE.equals(reqComponentInfo.getType())) {
                for (ReqCompFieldInfo reqCompFieldInfo : reqComponentInfo.getReqCompFields()) {
                    if (SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE.equals(reqCompFieldInfo)) {
                        if (courses.contains(reqCompFieldInfo.getValue())) {
                            return reqCompFieldInfo.getValue();
                        } else {
                            courses.add(reqCompFieldInfo.getValue());
                        }
                    }
                }
            }
        }

        return null;

    }


    /**
     * validates if a course is present in the reqComponentField for given atpId learningPlanId
     *
     * @param learningPlanId
     * @param atpId
     * @param courseCd
     * @param statementId
     * @return
     */
    private boolean hasDuplicateReqCompField(String learningPlanId, String atpId, String courseCd, String statementId) {
        List<PlanItemInfo> planItemInfos = null;
        try {
            planItemInfos = getAcademicPlanService().getPlanItemsInPlanByAtpAndRefObjType(learningPlanId, atpId, PlanConstants.STATEMENT_TYPE, SamplePlanConstants.CONTEXT_INFO);

        } catch (Exception e) {
            logger.error("Could not get PlanItems for learningPlan: " + learningPlanId, e);
        }

        if (CollectionUtils.isEmpty(planItemInfos)) {
            return false;
        }

        for (PlanItemInfo planItemInfo : planItemInfos) {

            try {
                StatementTreeViewInfo statementTreeViewInfo = getStatementService().getStatementTreeView(planItemInfo.getRefObjectId());
                for (ReqComponentInfo reqComponentInfo : statementTreeViewInfo.getReqComponents()) {
                    if (!reqComponentInfo.getId().equals(statementId)) {
                        if (SamplePlanConstants.REQ_COMP_TYPE_COURSE.equals(reqComponentInfo.getType())) {
                            for (ReqCompFieldInfo reqCompFieldInfo : reqComponentInfo.getReqCompFields()) {
                                if (SamplePlanConstants.REQ_COMP_FIELD_TYPE_COURSE.equals(reqCompFieldInfo.getType()) && courseCd.equals(reqCompFieldInfo.getValue())) {
                                    return true;
                                }
                            }
                        }
                    }
                }


            } catch (Exception e) {
                logger.error("Could not get Statement tree for statementId: " + planItemInfo.getRefObjectId(), e);
            }

        }
        return false;

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
                    samplePlanItem.setAtpId(String.format(SamplePlanConstants.SAMPLE_PLAN_ATP_FORMAT, term, i));
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
     * @param reqComponentInfos
     * @return
     */
    private StatementTreeViewInfo addUpdateStatementTreeViewInfo(List<ReqComponentInfo> reqComponentInfos, String statementId) {
        StatementTreeViewInfo statementTreeViewInfo = null;

        if (StringUtils.hasText(statementId)) {

            try {
                /*Fetching*/
                statementTreeViewInfo = getStatementService().getStatementTreeView(statementId);

                /*modifying*/
                statementTreeViewInfo.setReqComponents(reqComponentInfos);

                    /*updating*/
                statementTreeViewInfo = getStatementService().updateStatementTreeView(statementId, statementTreeViewInfo);


            } catch (Exception e) {
                logger.error("Could not complete statement tree View ", e);
            }

        } else {

            statementTreeViewInfo = new StatementTreeViewInfo();
            statementTreeViewInfo.setType(SamplePlanConstants.STATEMENT_TYPE_RECOMMENDED);
            statementTreeViewInfo.setOperator(StatementOperatorTypeKey.OR);

            statementTreeViewInfo.setReqComponents(reqComponentInfos);

            /*Creating*/
            try {
                statementTreeViewInfo = getStatementService().createStatementTreeView(statementTreeViewInfo);
            } catch (Exception e) {
                logger.error("Could not create statement tree View ", e);
            }
        }
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
        pii.setId(StringUtils.hasText(planItemId) ? planItemId : null);
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
                    GlobalResourceLoader.getService(new QName(StatementServiceConstants.NAMESPACE, "StatementService"));
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
