package org.kuali.student.myplan.sampleplan.service;

import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.DateFormatHelper;
import org.kuali.student.myplan.sampleplan.dataobject.SamplePlanDataObject;
import org.kuali.student.myplan.sampleplan.util.SamplePlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.util.constants.ProgramServiceConstants;
import org.kuali.student.r2.lum.program.dto.MajorDisciplineInfo;
import org.kuali.student.r2.lum.program.service.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 11/7/13
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplePlanLookupableHelperImpl extends MyPlanLookupableImpl {

    private final Logger logger = Logger.getLogger(SamplePlanLookupableHelperImpl.class);

    private transient AcademicPlanService academicPlanService;

    private transient ProgramService programService;

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Override
    protected List<SamplePlanDataObject> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        List<SamplePlanDataObject> samplePlanDataObjects = new ArrayList<SamplePlanDataObject>();

        List<MajorDisciplineInfo> majorDisciplineInfos = null;
        try {
            majorDisciplineInfos = getProgramService().getMajorDisciplinesByIds(new ArrayList<String>(), SamplePlanConstants.CONTEXT_INFO);
            List<LearningPlanInfo> learningPlanInfos = new ArrayList<LearningPlanInfo>();

            for (MajorDisciplineInfo majorDisciplineInfo : majorDisciplineInfos) {
                List<LearningPlanInfo> infos = getAcademicPlanService().getLearningPlansForPlanProgramByType(majorDisciplineInfo.getId(), PlanConstants.LEARNING_PLAN_TYPE_PLAN_TEMPLATE, PlanConstants.CONTEXT_INFO);
                if (!CollectionUtils.isEmpty(infos)) {
                    learningPlanInfos.addAll(infos);
                }
            }

            if (!CollectionUtils.isEmpty(learningPlanInfos)) {
                for (LearningPlanInfo learningPlanInfo : learningPlanInfos) {
                    SamplePlanDataObject samplePlanDataObject = new SamplePlanDataObject();
                    samplePlanDataObject.setCreatedBy(getUserSessionHelper().getFirstName(learningPlanInfo.getMeta().getCreateId()));
                    String dateAdded = DateFormatHelper.getDateFomatted(learningPlanInfo.getMeta().getCreateTime().toString());
                    String dateUpdated = DateFormatHelper.getDateFomatted(learningPlanInfo.getMeta().getUpdateTime().toString());
                    samplePlanDataObject.setLastCreated(dateAdded);
                    samplePlanDataObject.setLastUpdated(dateUpdated);
                    samplePlanDataObject.setStatus(PlanConstants.LEARNING_PLAN_ITEM_PUBLISHED_STATE_KEY.equals(learningPlanInfo.getStateKey()) ? SamplePlanConstants.PUBLISHED : SamplePlanConstants.DRAFT);
                    MajorDisciplineInfo majorDisciplineInfo = null;
                    try {
                        majorDisciplineInfo = getProgramService().getMajorDiscipline(learningPlanInfo.getPlanProgram(), SamplePlanConstants.CONTEXT_INFO);
                    } catch (Exception e) {
                        logger.error("Could not load degree program for Id:" + learningPlanInfo.getPlanProgram(), e);
                    }
                    samplePlanDataObject.setDegreeProgramTitle(majorDisciplineInfo != null && majorDisciplineInfo.getId() != null ? majorDisciplineInfo.getCode() : null);
                    samplePlanDataObject.setPlanTitle(learningPlanInfo.getName());
                    samplePlanDataObject.setLearningPlanId(learningPlanInfo.getId());
                    samplePlanDataObjects.add(samplePlanDataObject);
                }
            }

        } catch (Exception e) {
            logger.error("Could not load lookup view", e);
        }
        return samplePlanDataObjects;
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

}
