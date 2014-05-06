package org.kuali.student.myplan.schedulebuilder.infc;

/**
 * Created by IntelliJ IDEA.
 * User: dbmc
 * Date: 2/20/14
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ActivityOptionFilter {
     /**
      * Determine if the ActivityOption satisfies the filter.
      *
      * @return true true if it has the attribute implied by the
      *     class name, e.g. for AoFilterSectionOpen, return true if the Ao is open.
      * @param ao the ActivityOption representing a course section to be filtered
      */
    boolean evaluateAo(ActivityOption ao);

    String getFilterName();

    int getFilterId();
}
