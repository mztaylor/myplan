package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOptionMutator;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dbmc
 * Date: 4/18/14
 * Time: 3:34 PM
 * <p/>
 * <p/>
 * This processor traverses an AO List and executes some method against each AO in the list,
 * including recursively processing the alternateActivities and SecondaryActivities lists too.
 * <p/>
 * Does not make a new list like the other processors to date but returns from the apply()
 * the AoList that was passed in.
 * <p/>
 * The mutatorMethod is a ActivityOptionMutator and is passed in with the constuctor.
 * <p/>
 * This can be called before or after coalescing to the
 * AO.AlternateActivities, in other words, it supports AlternateActivities by traversing through them
 * and applying the filter to AOs in that list.
 */

public class MutatorAoListProcessor extends AbstractAoListProcessor {

    // using the count:
    //      meaning: counts the number of AO that are mutated.
    //      usage: caller should call startCount() before calling apply() and finally call getCount().

    private ActivityOptionMutator mutatorMethod;

    public MutatorAoListProcessor(ActivityOptionMutator mutatorMethod) {
        this.mutatorMethod = mutatorMethod;
    }

    @Override
    public List<ActivityOption> apply(List<ActivityOption> aoList) {
        for (ActivityOption ao : aoList) {
            if (mutatorMethod.mutator(ao))
                incCount();    // caller needs to call startCount()
            // recurse to mutate alt activities
            if (!CollectionUtils.isEmpty(ao.getAlternateActivties())) {
                int saveCount = getCount();
                startCount();
                // getAlternateActivties returns a copy
                List<ActivityOption> alternates = apply(ao.getAlternateActivties());
                ((ActivityOptionInfo) ao).setAlternateActivities(alternates);
                saveCount += getCount();
                setCount(saveCount);
            }
            // recurse to filter secondaries
            List<SecondaryActivityOptions> secondaryActivities = ao.getSecondaryOptions();
            if (!CollectionUtils.isEmpty(secondaryActivities)) {
                for (SecondaryActivityOptions secondaryActivity : secondaryActivities) {
                    List<ActivityOption> saList = secondaryActivity.getActivityOptions();
                    if (!CollectionUtils.isEmpty(saList)) {
                        int saveCount = getCount();
                        startCount();
                        apply(saList);
                        saveCount += getCount();
                        setCount(saveCount);
                    }
                }
            }
        }

        return aoList;
    }

    @Override
    public String getProcessorDescription() {
        return mutatorMethod.getMutatorName();
    }

    @Override
    public int getProcessorCode() {
        return mutatorMethod.getMutatorId();
    }
}