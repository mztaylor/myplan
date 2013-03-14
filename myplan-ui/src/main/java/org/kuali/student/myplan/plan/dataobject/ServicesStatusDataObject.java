package org.kuali.student.myplan.plan.dataobject;


/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 3/13/13
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServicesStatusDataObject {
    private final boolean academicCalendarServiceUp;
    private final boolean academicRecordServiceUp;
    private final boolean courseOfferingServiceUp;
    private final boolean degreeAuditServiceUp;

    public ServicesStatusDataObject(boolean academicCalendarServiceStatus, boolean academicRecordServiceStatus, boolean courseOfferingServiceStatus, boolean degreeAuditServiceStatus) {
        this.academicCalendarServiceUp = academicCalendarServiceStatus;
        this.academicRecordServiceUp = academicRecordServiceStatus;
        this.courseOfferingServiceUp = courseOfferingServiceStatus;
        this.degreeAuditServiceUp = degreeAuditServiceStatus;
    }

    public boolean isAcademicCalendarServiceUp() {
        return academicCalendarServiceUp;
    }

    public boolean isAcademicRecordServiceUp() {
        return academicRecordServiceUp;
    }

    public boolean isCourseOfferingServiceUp() {
        return courseOfferingServiceUp;
    }

    public boolean isDegreeAuditServiceUp() {
        return degreeAuditServiceUp;
    }

}
