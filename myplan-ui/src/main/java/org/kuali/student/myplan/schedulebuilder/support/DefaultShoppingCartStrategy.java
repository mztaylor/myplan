package org.kuali.student.myplan.schedulebuilder.support;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.CourseOptionInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ShoppingCartRequest;
import org.kuali.student.myplan.schedulebuilder.util.ShoppingCartStrategy;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.infc.Attribute;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class DefaultShoppingCartStrategy implements ShoppingCartStrategy,
		Serializable {

	private static final long serialVersionUID = -5919246688332396604L;

    private transient ScheduleBuildStrategy scheduleBuildStrategy;

    private transient AcademicPlanService academicPlanService;

    @Autowired
    private transient CourseHelper courseHelper;

	@Override
	public boolean isCartAvailable(String termId, String campusCode) {
		return false;
	}

	@Override
	public List<CourseOption> getCourseOptionsForPlanItem(Term term,
			PlanItem planItem) {
		ScheduleBuildStrategy scheduleBuildStrategy = getScheduleBuildStrategy();
		assert PlanConstants.COURSE_TYPE.equals(planItem.getRefObjectType());
		assert planItem.getPlanPeriods().contains(term.getId());

		List<CourseOption> courseOptions = scheduleBuildStrategy
				.getCourseOptions(
						Collections.singletonList(planItem.getRefObjectId()),
						term.getId());

		List<String> acodes = Collections.emptyList();
		for (Attribute attr : planItem.getAttributes())
			if (attr.getKey().equals("activityCode"))
				acodes = Arrays.asList(attr.getValue().split(","));

		for (CourseOption courseOption : courseOptions) {
			for (ActivityOption primaryActivityOption : courseOption
					.getActivityOptions()) {
				boolean selected = acodes.contains(primaryActivityOption
						.getRegistrationCode());
				if (selected) {
					((ActivityOptionInfo) primaryActivityOption)
							.setSelected(true);
					for (SecondaryActivityOptions secondaryOptions : primaryActivityOption
							.getSecondaryOptions())
						for (ActivityOption secondaryActivityOption : secondaryOptions
								.getActivityOptions())
							if (primaryActivityOption.isEnrollmentGroup())
								((ActivityOptionInfo) secondaryActivityOption)
										.setLockedIn(primaryActivityOption
												.isEnrollmentGroup());
							else if (acodes.contains(secondaryActivityOption
									.getRegistrationCode()))
								((ActivityOptionInfo) secondaryActivityOption)
										.setSelected(true);
				}
			}

			// The selected flag on the course option dictates whether we are
			// adding or removing from the shopping cart when passed to
			// createRequests() --
			// When item category is cart, the request will be to remove from the
			// cart. For other items types, the request will be to add.
			((CourseOptionInfo) courseOption).setSelected(!planItem
					.getCategory().equals(
							AcademicPlanServiceConstants.ItemCategory.CART));
		}
		return courseOptions;
	}

	@Override
	public List<ShoppingCartRequest> createRequests(Term term,
			List<CourseOption> courseOptions) {
		CourseHelper courseHelper = getCourseHelper();
		List<ShoppingCartRequest> requests = new ArrayList<ShoppingCartRequest>(
				courseOptions.size());
		for (CourseOption courseOption : courseOptions) {
			ShoppingCartRequestInfo cartRequest = new ShoppingCartRequestInfo();
			cartRequest.setTerm(new TermInfo(term));
			cartRequest.setCourse(courseHelper.getCourseInfo(courseOption
					.getCourseId()));
			cartRequest.setAddToCart(courseOption.isSelected());

			ActivityOption primary = null;
			int secondaryLength = 0;
			for (ActivityOption primaryActivityOption : courseOption
					.getActivityOptions()) {
				if (primary == null && primaryActivityOption.isSelected())
					primary = primaryActivityOption;
				List<SecondaryActivityOptions> secondaryOptions = primaryActivityOption
						.getSecondaryOptions();
				if (secondaryOptions != null)
					secondaryLength = Math.max(secondaryLength,
							secondaryOptions.size());
			}

			List<String> secondaryRegCodes = new ArrayList<String>(
					secondaryLength);
			if (primary != null) {
				cartRequest.setPrimaryRegistrationCode(primary
						.getRegistrationCode());

				for (SecondaryActivityOptions secondaryOption : primary
						.getSecondaryOptions()) {
					ActivityOption secondary = null;
					for (ActivityOption secondaryActivityOption : secondaryOption
							.getActivityOptions())
						if (secondary == null
								&& secondaryActivityOption.isSelected())
							secondary = secondaryActivityOption;
					if (secondary != null)
						secondaryRegCodes.add(secondary.getRegistrationCode());
				}
				cartRequest.setSecondaryRegistrationCodes(secondaryRegCodes);
			}

			requests.add(cartRequest);
		}
		return requests;
	}

	private void buildExistingCartOptions(String learningPlanId,
			TermInfo termInfo, Map<String, ShoppingCartRequestInfo> rmap) {
		AcademicPlanService academicPlanService = getAcademicPlanService();

		List<PlanItemInfo> planItems;
		try {
			planItems = academicPlanService.getPlanItemsInPlanByCategory(learningPlanId, AcademicPlanServiceConstants.ItemCategory.CART,
					PlanConstants.CONTEXT_INFO);
		} catch (DoesNotExistException e) {
			throw new IllegalArgumentException("CO lookup failure", e);
		} catch (InvalidParameterException e) {
			throw new IllegalArgumentException("CO lookup failure", e);
		} catch (MissingParameterException e) {
			throw new IllegalArgumentException("CO lookup failure", e);
		} catch (OperationFailedException e) {
			throw new IllegalArgumentException("CO lookup failure", e);
		}

		for (PlanItemInfo planItem : planItems) {
			if (!PlanConstants.COURSE_TYPE.equals(planItem.getRefObjectType()))
				continue;

			List<String> periods = planItem.getPlanPeriods();
			if (periods == null || !periods.contains(termInfo.getId()))
				continue;

			String acodeattr = planItem.getAttributeValue("activityCode");
			List<String> acodes;
			if (acodeattr == null || acodeattr.isEmpty())
				continue;
			else
				acodes = Arrays.asList(acodeattr.split(","));

			String courseId = planItem.getRefObjectId();
			ShoppingCartRequestInfo cartRequest = new ShoppingCartRequestInfo();
			cartRequest.setAddToCart(false);
			cartRequest.setCourse(getCourseHelper().getCourseInfo(courseId));
			cartRequest.setPrimaryRegistrationCode(acodes.get(0));
			if (acodes.size() > 1)
				cartRequest.setSecondaryRegistrationCodes(acodes.subList(1,
						acodes.size()));
			else
				cartRequest.setSecondaryRegistrationCodes(Collections
						.<String> emptyList());
			cartRequest.setTerm(termInfo);
			
			rmap.put(courseId, cartRequest);
		}

	}

	@Override
	public List<ShoppingCartRequest> createRequests(String learningPlanId,
			Term term, PossibleScheduleOption schedule) {
		TermInfo termInfo = new TermInfo(term);
		Map<String, ShoppingCartRequestInfo> rmap = new LinkedHashMap<String, ShoppingCartRequestInfo>();
		buildExistingCartOptions(learningPlanId, termInfo, rmap);

		Map<String, List<ActivityOption>> aomap = new LinkedHashMap<String, List<ActivityOption>>();
		for (ActivityOption ao : schedule.getActivityOptions()) {
			if (ao.isCourseLockedIn())
				continue;
			List<ActivityOption> aol = aomap.get(ao.getCourseId());
			if (aol == null)
				aomap.put(ao.getCourseId(),
						aol = new ArrayList<ActivityOption>());
			aol.add(ao);
		}

		List<ShoppingCartRequest> rv = new ArrayList<ShoppingCartRequest>(
				aomap.size() + rmap.size());
		for (Entry<String, List<ActivityOption>> aoe : aomap.entrySet()) {
			String courseId = aoe.getKey();

			ShoppingCartRequestInfo cartRequest = rmap.remove(courseId);
			if (cartRequest != null) {
				Set<String> acodes = new HashSet<String>();
				acodes.add(cartRequest.getPrimaryRegistrationCode());
				for (String regcode : cartRequest
						.getSecondaryRegistrationCodes())
					acodes.add(regcode);
				if (acodes.size() != aoe.getValue().size()) {
					rv.add(cartRequest);
					cartRequest = null;
				} else
					aoloop: for (ActivityOption ao : aoe.getValue()) {
						if (!acodes.contains(ao.getRegistrationCode())) {
							rv.add(cartRequest);
							cartRequest = null;
							break aoloop;
						} else
							acodes.remove(ao.getRegistrationCode());
					}
			}

			if (cartRequest == null) {
				cartRequest = new ShoppingCartRequestInfo();
				cartRequest.setAddToCart(true);
				cartRequest.setCourse(getCourseHelper().getCourseInfo(courseId));
				List<ActivityOption> aol = aoe.getValue();
				cartRequest.setPrimaryRegistrationCode(aol.get(0)
						.getRegistrationCode());
				if (aol.size() > 1) {
					List<String> scodes = new ArrayList<String>(aol.size() - 1);
					for (ActivityOption ao : aol.subList(1, aol.size()))
						scodes.add(ao.getRegistrationCode());
					cartRequest.setSecondaryRegistrationCodes(scodes);
				} else
					cartRequest.setSecondaryRegistrationCodes(Collections
							.<String> emptyList());
				cartRequest.setTerm(termInfo);
				rv.add(cartRequest);
			}
		}
		
		rv.addAll(rmap.values());

		return rv;
	}

	@Override
	public List<ShoppingCartRequest> processRequests(
			List<ShoppingCartRequest> requests) {
		throw new UnsupportedOperationException(
				"Not implemented in KS - override at the institution level");
	}

    public ScheduleBuildStrategy getScheduleBuildStrategy() {
        if (scheduleBuildStrategy == null) {
            scheduleBuildStrategy = new DefaultScheduleBuildStrategy();
        }
        return scheduleBuildStrategy;
    }

    public void setScheduleBuildStrategy(ScheduleBuildStrategy scheduleBuildStrategy) {
        this.scheduleBuildStrategy = scheduleBuildStrategy;
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

}
