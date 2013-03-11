package org.kuali.student.myplan.course.dataobject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jasonosgood
 * Date: 12/5/12
 * Time: 10:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class CourseOfferingInstitution implements Comparable<CourseOfferingInstitution> {
    private int code;
    private String name;

    private List<CourseOfferingTerm> courseOfferingTermList;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CourseOfferingTerm> getCourseOfferingTermList() {
        if (courseOfferingTermList == null) {
            courseOfferingTermList = new ArrayList<CourseOfferingTerm>();
        }
        return courseOfferingTermList;
    }

    public void setCourseOfferingTermList(List<CourseOfferingTerm> courseOfferingTermList) {
        this.courseOfferingTermList = courseOfferingTermList;
    }

    @Override
    public int compareTo(CourseOfferingInstitution that) {
        // assume integer values won't overflow (eg > Integer.MAX )
        // pretty good writeup http://stackoverflow.com/questions/9150446/compareto-with-primitives-integer-int
        return this.code - that.code;
//        final int BEFORE = -1;
//        final int EQUAL = 0;
//        final int AFTER = 1;
    }

}
