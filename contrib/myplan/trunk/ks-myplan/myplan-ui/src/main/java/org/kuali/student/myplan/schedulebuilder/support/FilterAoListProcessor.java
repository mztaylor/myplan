package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.SecondaryActivityOptionsInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOptionFilter;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dbmc
 * Date: 3/26/14
 * Time: 11:13 AM
 * <p/>
 * This processor traverses an AO List and deletes any AO that fails the filter test.
 * Does not actually delete the AO from the AO List, it makes a new list
 * and copies the keepers to it. This list is returned from the apply().
 * <p/>
 * The filter is a ActivityOptionFilter and is passed in with the constuctor.
 * <p/>
 * This processor recursively processes the alternateActivities and SecondaryActivities lists too.
 * <p/>
 * Normal logic is delete the AO if the filter returns true; invertLogic flag changes that to false.
 * <p/>
 * The FilterAoListProcessor can be called before or after coalescing to the
 * AO.AlternateActivities, in other words, it supports AlternateActivities by traversing through them
 * and applying the filter to AOs in that list.
 *
 * If an AO should be deleted but there is an AO in the alternateActivities, that AO (the first one found,
 * to be exact) will be promoted to be the master AO.
 *
 */
public class FilterAoListProcessor extends AbstractAoListProcessor {

    // using the count:
    // meaning: counts the number of AO that are deleted.
    // usage: caller should call resetCount() before calling apply() and finally call getCount().

    private ActivityOptionFilter filter;
    private boolean invertLogic;
    private boolean skipAlternates;


    public FilterAoListProcessor(ActivityOptionFilter filter) {
        this(filter, false);
    }

    public FilterAoListProcessor(ActivityOptionFilter filter, boolean invertLogic) {
        this(filter, invertLogic, false);
    }

     public FilterAoListProcessor(ActivityOptionFilter filter, boolean invertLogic, boolean skipAlternates) {
        this.filter = filter;
        this.invertLogic = invertLogic;
        this.skipAlternates = skipAlternates;
    }

    @Override
    public List<ActivityOption> apply(List<ActivityOption> aoList) {
        List<ActivityOption> newAOList = new ArrayList<ActivityOption>();
        for (ActivityOption ao : aoList) {
            // if we are using normal logic (not inverting it) and if filter returns true, delete the AO
            // or, if we are inverting logic and filter returns false, the delete the ao
            if ((!invertLogic && filter.evaluateAo(ao)) || (invertLogic && !filter.evaluateAo(ao))) {
                // delete it implicitly, by not copying it to the new list
                incCount();
                incCount(countSecondaries(ao));
                // look for an AO in the AlternateActivities that passes the filter, promote it
                if (!CollectionUtils.isEmpty(ao.getAlternateActivties())) {
                    int saveCount = getCount();
                    resetCount();
                    List<ActivityOption> newAlternateList = apply(ao.getAlternateActivties());
                    saveCount += getCount();
                    setCount(saveCount);
                    if (!CollectionUtils.isEmpty(newAlternateList)) {
                        // currenlty, skipAlternates is only used by user-planned-sections filter. It is
                        // run after sorting the alternates list, which would move any planned sections
                        // to the master link. If we're running the user-planned-sections filter
                        // and we're here, then none of the sections are planned, so we don't need to worry
                        // about skipAlternates in this logic. Future uses of skipAlternates might change that.
                        ActivityOptionInfo newMasterAo = (ActivityOptionInfo) newAlternateList.get(0);
                        newAlternateList.remove(0);
                        newMasterAo.setAlternateActivities(newAlternateList.size() > 0 ? newAlternateList :
                                new ArrayList<ActivityOption>());
                        newAOList.add(newMasterAo);
                    }
                }
            } else {
                // it's a keeper
                // recurse to filter alt activities
                if (!CollectionUtils.isEmpty(ao.getAlternateActivties()) && !skipAlternates) {
                    int saveCount = getCount();
                    resetCount();
                    ((ActivityOptionInfo)ao).setAlternateActivities(apply(ao.getAlternateActivties()));
                    saveCount += getCount();
                    setCount(saveCount);
                }
                // recurse to filter secondaries
                boolean noSecondaries = false;
                List<SecondaryActivityOptions> secondaryActivities = ao.getSecondaryOptions();
                if (!CollectionUtils.isEmpty(secondaryActivities)) {
                    for (SecondaryActivityOptions secondaryActivity : secondaryActivities) {
                        List<ActivityOption> saList = secondaryActivity.getActivityOptions();
                        if (!CollectionUtils.isEmpty(saList)) {
                            int saveCount = getCount();
                            resetCount();
                            List<ActivityOption> newSecondaryList = apply(saList);
                            if (CollectionUtils.isEmpty(newSecondaryList)) {
                                noSecondaries = true;
                            } else {
                                ((SecondaryActivityOptionsInfo) secondaryActivity).setActivityOptions(newSecondaryList);
                            }
                            saveCount += getCount();
                            setCount(saveCount);
                        }
                    }
                }
                // delete a primary if all the secondaries are filtered out
                //      (could make this a separate processor to run after all filters
                //          so other institutions can choose to not do it this way)
                if (noSecondaries) {
                    // could be slightly misleading to say this primary was filtered out due to this filter,
                    // it could be due to this filters and other filters which eliminated other secondaries.
                    // oh well, count it.
                    incCount();
                } else {
                    newAOList.add(ao);
                }
            }
        }

        return newAOList;
    }

    private int countSecondaries(ActivityOption ao) {
        int count = 0;
        List<SecondaryActivityOptions> secondaryActivities = ao.getSecondaryOptions();
        if (!CollectionUtils.isEmpty(secondaryActivities)) {
            for (SecondaryActivityOptions secondaryActivity : secondaryActivities) {
                List<ActivityOption> saList = secondaryActivity.getActivityOptions();
                if (!CollectionUtils.isEmpty(saList)) {
                    count += saList.size();
                    // recurse to get the secondaries, if any, for each secondary
                    for (ActivityOption sao : saList) {
                        count += countSecondaries(sao);
                    }
                }
            }
        }

        return count;
    }

    @Override
    public String getProcessorDescription() {
        return filter.getFilterName();
    }

    @Override
    public int getProcessorCode() {
        return filter.getFilterId();
    }
}