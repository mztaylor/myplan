package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.SecondaryActivityOptionsInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilderConstants;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dbmc
 * Date: 4/2/14
 * <p/>
 * This processor traverses an AO List and coalesces sections.
 * Determining if sections are equivalent for this purpose and can be coalesced is
 * determined by the comparator that is passed it with the constructor.
 * <p/>
 * the return from the apply() is a new AoList with sections coalesced.
 * <p/>
 * SecondaryActivityOptions.ActivityOptions gets set to a new AoList with sections coalesced.
 * This is unavoidable since SecondaryActivityOptionsInfo.getActivityOptions() copies the
 * existing to a new list and returns it to us.
 *
 * For primaries, apply() is given an AOList parameter (and not the object that holds the AoList),
 * so we don't need to get the AOList so we never get a copy of
 * the original list and we always work on the original list. Hence, the external caller to apply() does not need
 * to assign the returned AOList to their object pointer. We could change this if we wanted a bit more
 * consistency.
 */

public class CoalescorAoListProcessor extends AbstractAoListProcessor {
    // using the count:
    //      meaning: counts the number of AO that are coalesced, ie., moved into an alternates list.
    //      usage: caller should call resetCount() before calling apply() and finally call getCount().

    private Comparator<ActivityOption> coalesceComparator;

    public CoalescorAoListProcessor(Comparator<ActivityOption> coalesceComparator) {
        this.coalesceComparator = coalesceComparator;
        setProcessorDescription(ScheduleBuilderConstants.SECTION_COALESCOR_PROCESSOR_DESC);
        setProcessorCode(ScheduleBuilderConstants.SECTION_COALESCOR_PROCESSOR_ID);
    }

    @Override
    public List<ActivityOption> apply(List<ActivityOption> aoList) {
        // get a list of indexes in the aoList for AO that are eligible to be coalesced
        List<Integer> coalesceList = findCoalescablesIndexes(aoList);
        if (coalesceList.size() > 1) {
            coalesceSections(aoList, coalesceList);
        }
        return aoList;
    }

    // UW definition of eligible coalescables is leaf sections at the same level.
    //  In a list of AO, some may have different leaf levels,
    //      so don't necessarily coalesce all sections in the list.
    // loop through aoList looking for eligible coalescable where
    //      'eligible' means the AO has no secondaries.
    //
    // others might have different notions, so this method might eventually have to be
    // made configurable.

    public List<Integer> findCoalescablesIndexes(List<ActivityOption> aoList) {
        List<Integer> coalesceList = new ArrayList<Integer>();
        int ndx = 0;
        for (ActivityOption section : aoList) {
            List<SecondaryActivityOptions> secondaryActivities = section.getSecondaryOptions();
            // secondaryActivities could be lab, quiz, etc; can have 0, 1 or more than 1 diff types.
            // if there are secondaries, coalesce() them;  this parent section should not be coalesced
            if (!CollectionUtils.isEmpty(secondaryActivities)) {
                for (SecondaryActivityOptions secondaryActivity : secondaryActivities) {
                    List<ActivityOption> newAoList = apply(secondaryActivity.getActivityOptions());
                    ((SecondaryActivityOptionsInfo) secondaryActivity).setActivityOptions(newAoList);
                }
            } else {
                // we have reached the leaf level, add index to the list of sections eligible for coalescing
                coalesceList.add(ndx);
            }
            ndx++;
        }
        return coalesceList;
    }


    /**
     * Rather than deleting alternates as we traverse the aoList and changing the list dynamically,
     * keep track of sections that have been coalesced and delete them all from aoList at the end to keep
     * the list from changing as we traverse it. Use a list of indexes to keep track.
     *
     * @param aoList       list of ActivityOption to be coalesced
     * @param coalesceList list of ints for the indexes (in the aoList) of eligible sections
     */
    public void coalesceSections(List<ActivityOption> aoList, List<Integer> coalesceList) {
        List<Integer> deletesAoIndexList = new ArrayList<Integer>();
        int nextSectionCoalesceListNdx = 0;
        for (Integer aoListNdx : coalesceList) {
            nextSectionCoalesceListNdx++;  // used as starting point for traversing the rest of the coalesceList

            // if section is already an alternate from earlier action, we can skip it. check delete list.
            // This is a slight optimization, especially useful when there are 200 sections.
            if (deletesAoIndexList.contains(aoListNdx)) {
                continue;
            }

            // master section is the one that will have all the others assigned to its AlternateActivities
            ActivityOption masterSection = aoList.get(aoListNdx);
            List<Integer> alternatesAoIndexList = new ArrayList<Integer>();

            // go through the rest of the coalesceList looking for matching sections to coalesce
            for (int i = nextSectionCoalesceListNdx; i < coalesceList.size(); i++) {
                int sectionAoListIndex = coalesceList.get(i);
                ActivityOption nextSection = aoList.get(sectionAoListIndex);
                // if section is already an alternate from earlier traverse through the list, we can skip it. check delete list.
                if (deletesAoIndexList.contains(sectionAoListIndex)) {
                    continue;
                }
                if (coalesceComparator.compare(masterSection, nextSection) == 0) {
                    alternatesAoIndexList.add(sectionAoListIndex);
                }
            }
            if (alternatesAoIndexList.size() > 0) {  // we had matches, coalesce them
                List<ActivityOption> alternates = new ArrayList<ActivityOption>();
                for (Integer sectionAoListIndex : alternatesAoIndexList) {
                    alternates.add(aoList.get(sectionAoListIndex));
                    deletesAoIndexList.add(sectionAoListIndex);
                }
                ((ActivityOptionInfo) masterSection).setAlternateActivities(alternates);
            }
        }

        // delete the alternate sections from the aoList from the back to the front
        // so we don't saw off the tree limb we're sitting on.
        Collections.sort(deletesAoIndexList, java.util.Collections.reverseOrder());
        for (int aoListNdx : deletesAoIndexList) {
            aoList.remove(aoListNdx);
            incCount();
        }
    }
}
