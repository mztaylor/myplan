package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.SecondaryActivityOptionsInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: dbmc
 * Date: 4/9/14
 * <p/>
 * This processor traverses an AO List and sorts the AO in the alternativeActivity list.
 * The master AO is also thrown into the sort. When sorting is complete, the top AO is
 * made the master AO.
 * <p/>
 * Sort order is determined by the comparator that is passed in with the constructor.
 * <p/>
 * the return from the apply() is a new AoList with the  alternativeActivity list sorted for each
 * AO and recursively for each ActivityOptions for each secondary list under each AO.
 * <p/>
 * This assumes that the AOList has been run through a coalescor and there are AOLists assigned to
 * MasterAO.AlternateActivities as appropriate. It would be harmless if this is not true, but running
 * apply() would also make no changes.
 *
 */
public class SortAlternativesAoListProcessor extends AbstractAoListProcessor {
    // using the count: not used.

    private Comparator<ActivityOption> sortComparator;

    public SortAlternativesAoListProcessor(Comparator<ActivityOption> sortComparator) {
        this.sortComparator = sortComparator;
        setProcessorDescription("Alternate Activity List Sorter");
        setProcessorCode(44); // later, define a constant somewhere
    }


    @Override
    public List<ActivityOption> apply(List<ActivityOption> aoList) {
        List<ActivityOption> newAOList = new ArrayList<ActivityOption>();
        for (ActivityOption ao : aoList) {
            if (!CollectionUtils.isEmpty(ao.getAlternateActivties())) {
                List<ActivityOption> alternateActivties = ao.getAlternateActivties();
                // avoid making a circular list when we add the master to the altAct list...
                ((ActivityOptionInfo) ao).setAlternateActivities(null);
                alternateActivties.add(ao);
                Collections.sort(alternateActivties, sortComparator);
                // promote first AO to be master AO
                ActivityOption master = alternateActivties.get(0);
                alternateActivties.remove(0);
                ((ActivityOptionInfo) master).setAlternateActivities(alternateActivties);
                newAOList.add(master);
                // at UW, only leaf nodes are eligible to be coalesced,
                // so if an AO has AlternateActivities or if it is in a list of AlternateActivities,
                // it won't have secondaries. Hence, we don't deal with secondaries in this branch.
            } else {
                // process secondaries, if any
                List<SecondaryActivityOptions> secondaryActivities = ao.getSecondaryOptions();
                // secondaryActivities could be lab, quiz, etc; can have 0, 1 or more than 1 diff types.
                // if there are secondaries, recursively process
                if (!CollectionUtils.isEmpty(secondaryActivities)) {
                    for (SecondaryActivityOptions secondaryActivity : secondaryActivities) {
                        List<ActivityOption> newSecondaryAoList = apply(secondaryActivity.getActivityOptions());
                        ((SecondaryActivityOptionsInfo) secondaryActivity).setActivityOptions(newSecondaryAoList);
                    }
                }
                newAOList.add(ao);
            }
        }

        return newAOList;
    }
}
