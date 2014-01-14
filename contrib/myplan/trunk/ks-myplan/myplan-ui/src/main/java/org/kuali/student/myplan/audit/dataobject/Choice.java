package org.kuali.student.myplan.audit.dataobject;

import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.springframework.util.StringUtils;

import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.CR_NO_CR_GRADING_OPTION;
import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.HONORS_CREDIT;
import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.WRITING_CREDIT;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 1/13/14
 * Time: 1:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Choice {

    public String credit = "";
    public String section = "";
    public String secondaryActivity = "";
    public boolean crNcGradingOption = false;
    public boolean writing = false;
    public boolean honors = false;

    public int hashCode() {
        return credit.hashCode();
    }

    public boolean equals(Object that) {
        if (that == null) return false;
        if (this == that) return true;
        if (!(that instanceof Choice)) return false;
        if (!(writing == ((Choice) that).writing)) return false;
        if (!(honors == ((Choice) that).honors)) return false;
        if (!(crNcGradingOption == ((Choice) that).crNcGradingOption)) return false;
        return credit.equals(((Choice) that).credit);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // section:credits:display, eg "A:5:5 -- Writing"
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(section);
        sb.append(':');
        sb.append(secondaryActivity);
        sb.append(':');
        sb.append(credit);
        sb.append(':');
        sb.append(credit);
        if (writing) {
            sb.append(" -- ");
            sb.append(WRITING_CREDIT);
        }
        if (honors) {
            sb.append(" -- ");
            sb.append(HONORS_CREDIT);
        }
        if (crNcGradingOption) {
            sb.append(" -- ");
            sb.append(DegreeAuditConstants.CR_NO_CR_GRADING_OPTION);
        }
        return sb.toString();
    }

    /**
     * Format for the key is same as value the toString method returns
     *
     * @param key
     * @return
     */
    public Choice build(String key) {
        Choice choice = new Choice();
        String[] str = key.split(":");
        choice.section = StringUtils.hasText(str[0]) ? str[0] : "";
        choice.secondaryActivity = StringUtils.hasText(str[1]) ? str[1] : "";
        choice.credit = StringUtils.hasText(str[2]) ? str[2] : "";
        choice.writing = str[3].contains(WRITING_CREDIT);
        choice.honors = str[3].contains(HONORS_CREDIT);
        choice.crNcGradingOption = str[3].contains(CR_NO_CR_GRADING_OPTION);
        return choice;
    }
}
