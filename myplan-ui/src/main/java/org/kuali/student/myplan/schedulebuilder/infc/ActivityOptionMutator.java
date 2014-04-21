package org.kuali.student.myplan.schedulebuilder.infc;

/**
 * Created by IntelliJ IDEA.
 * User: dbmc
 * Date: 4/18/14
 * Time: 3:42 PM
 *
 * simply defines an interface for some method that takes an ActivityOption and does something to it.
 * Most commonly, set a field to a value, but possibly do any type mutation to the contents of an AO,
 * perhaps even conditionally.
 *
 * For purposes of keeping a count, the exec method should return true if it does its thing to the AO.
 *
 */
public interface ActivityOptionMutator {
     /**
      *
      *
      * @return true if the mutator operated on the AO. Used to count AO in AoList that get mutated.
      * @param ao the ActivityOption representing a course section to be mutated
      */
    boolean mutator(ActivityOption ao);

    String getMutatorName();

    int getMutatorId();
}
