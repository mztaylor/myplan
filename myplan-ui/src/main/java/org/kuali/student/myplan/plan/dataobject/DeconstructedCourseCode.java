package org.kuali.student.myplan.plan.dataobject;

/**
 * kmuthu Don't forget to add comment
 *
 * @Author kmuthu
 * Date: 3/6/13
 */
public class DeconstructedCourseCode {

    private final String subject;
    private final String number;
    private final String section;

    public DeconstructedCourseCode(String subject, String number, String section) {
        this.subject = subject;
        this.number = number;
        this.section = section;
    }

    public String getCourseCode() {
        return String.format("%s %s", subject, number);
    }

    public String getSubject() {
        return subject;
    }

    public String getNumber() {
        return number;
    }

    public String getSection() {
        return section;
    }

}
