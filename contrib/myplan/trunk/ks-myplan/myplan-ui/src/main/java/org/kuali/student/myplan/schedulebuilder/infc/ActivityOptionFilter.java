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
      * Determine if the ActivityOption satisfies the filter. Note the meaning of true and false
      * below may require reversing the sense of the field being tested if it starts as a negative.
      * eg., a flag sectionClosed: if you want to consider open sections only, the filter  would need to
      * return true if the the flag is false
      *
      * @return true if the ActivityOption passes the filter and should be further considered
      *         false if the ActivityOption fails the filter test and should be eliminated
      * from further consideration
      * @param ao the ActivityOption representing a course section to be filtered in or out
      */
    boolean test(ActivityOption ao);
}
