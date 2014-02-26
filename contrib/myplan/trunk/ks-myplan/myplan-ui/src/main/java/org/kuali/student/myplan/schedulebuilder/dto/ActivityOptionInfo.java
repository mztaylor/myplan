package org.kuali.student.myplan.schedulebuilder.dto;

import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.ClassMeetingTime;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ActivityOptionInfo", propOrder = {"parentUniqueId",
        "courseIndex", "parentIndex", "courseId", "courseCd", "courseTitle", "activityOfferingId",
        "activityTypeDescription", "courseOfferingCode", "registrationCode",
        "academicSessionDescr", "activityName", "courseLockedIn", "enrollmentGroup", "enrollmentRestriction",
        "closed", "openSeats", "totalSeats", "requiresPermission", "primary",
        "minCredits", "maxCredits", "notes", "secondaryOptions", "alternateActivties", "classMeetingTimes", "termId"})
public class ActivityOptionInfo extends ScheduleBuildOptionInfo implements
        ActivityOption {

    private static final long serialVersionUID = 2200048680553005339L;

    @XmlAttribute
    private String parentUniqueId;

    @XmlAttribute
    private int courseIndex;

    @XmlAttribute
    private int parentIndex;

    @XmlAttribute
    private String courseId;

    @XmlAttribute
    private String courseCd;

    @XmlAttribute
    private String courseTitle;

    @XmlAttribute
    private String activityOfferingId;

    @XmlAttribute
    private String activityTypeDescription;

    @XmlAttribute
    private String courseOfferingCode;

    @XmlAttribute
    private String registrationCode;

    @XmlAttribute
    private String academicSessionDescr;

    @XmlAttribute
    private String activityName;

    @XmlAttribute
    private boolean courseLockedIn;

    @XmlAttribute
    private boolean enrollmentGroup;

    @XmlAttribute
    private boolean enrollmentRestriction;

    @XmlAttribute
    private boolean closed;

    @XmlAttribute
    private int openSeats;

    @XmlAttribute
    private int totalSeats;

    @XmlAttribute
    private boolean requiresPermission;

    @XmlAttribute
    private boolean primary;

    @XmlAttribute
    private BigDecimal minCredits;

    @XmlAttribute
    private BigDecimal maxCredits;

    @XmlElement
    private List<String> notes;

    @XmlElement
    public List<SecondaryActivityOptionsInfo> secondaryOptions;

    @XmlElement
    public List<ActivityOptionInfo> alternateActivties;

    @XmlElement
    private List<ClassMeetingTimeInfo> classMeetingTimes;

    @XmlAttribute
    private String termId;


    public ActivityOptionInfo() {
    }

    public ActivityOptionInfo(ActivityOption copy) {
        super(copy);
        parentUniqueId = copy.getParentUniqueId();
        courseIndex = copy.getCourseIndex();
        parentIndex = copy.getParentIndex();
        courseId = copy.getCourseId();
        courseCd = copy.getCourseCd();
        courseTitle = copy.getCourseTitle();
        termId = copy.getTermId();
        activityOfferingId = copy.getActivityOfferingId();
        activityTypeDescription = copy.getActivityTypeDescription();
        activityName = copy.getActivityName();
        courseOfferingCode = copy.getCourseOfferingCode();
        registrationCode = copy.getRegistrationCode();
        academicSessionDescr = copy.getAcademicSessionDescr();
        courseLockedIn = copy.isCourseLockedIn();
        enrollmentGroup = copy.isEnrollmentGroup();
        enrollmentRestriction = copy.isEnrollmentRestriction();
        closed = copy.isClosed();
        openSeats = copy.getOpenSeats();
        totalSeats = copy.getTotalSeats();
        requiresPermission = copy.isRequiresPermission();
        primary = copy.isPrimary();
        minCredits = copy.getMinCredits();
        maxCredits = copy.getMaxCredits();
        List<String> copyNotes = copy.getNotes();
        notes = copyNotes == null ? null : new ArrayList<String>(copyNotes);
        setSecondaryOptions(copy.getSecondaryOptions());
        setAlternateActivities(copy.getAlternateActivties());
        setClassMeetingTimes(copy.getClassMeetingTimes());
    }

    @Override
    public int getCourseIndex() {
        return courseIndex;
    }

    public void setCourseIndex(int courseIndex) {
        this.courseIndex = courseIndex;
    }

    @Override
    public int getParentIndex() {
        return parentIndex;
    }

    public void setParentIndex(int parentIndex) {
        this.parentIndex = parentIndex;
    }

    @Override
    public String getParentUniqueId() {
        return parentUniqueId;
    }

    public void setParentUniqueId(String parentUniqueId) {
        this.parentUniqueId = parentUniqueId;
    }

    @Override
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    @Override
    public String getCourseCd() {
        return courseCd;
    }

    public void setCourseCd(String courseCd) {
        this.courseCd = courseCd;
    }

    @Override
    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    @Override
    public String getActivityOfferingId() {
        return activityOfferingId;
    }

    public void setActivityOfferingId(String activityOfferingId) {
        this.activityOfferingId = activityOfferingId;
    }

    @Override
    public boolean isCourseLockedIn() {
        return courseLockedIn;
    }

    public void setCourseLockedIn(boolean courseLockedIn) {
        this.courseLockedIn = courseLockedIn;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public String getActivityTypeDescription() {
        return activityTypeDescription;
    }

    public void setActivityTypeDescription(String activityTypeDescription) {
        this.activityTypeDescription = activityTypeDescription;
    }

    @Override
    public String getCourseOfferingCode() {
        return courseOfferingCode;
    }

    public void setCourseOfferingCode(String courseOfferingCode) {
        this.courseOfferingCode = courseOfferingCode;
    }

    @Override
    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    @Override
    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    @Override
    public String getAcademicSessionDescr() {
        return academicSessionDescr;
    }

    public void setAcademicSessionDescr(String academicSessionDescr) {
        this.academicSessionDescr = academicSessionDescr;
    }

    @Override
    public int getOpenSeats() {
        return openSeats;
    }

    public void setOpenSeats(int openSeats) {
        this.openSeats = openSeats;
    }

    @Override
    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    @Override
    public boolean isRequiresPermission() {
        return requiresPermission;
    }

    public void setRequiresPermission(boolean requiresPermission) {
        this.requiresPermission = requiresPermission;
    }

    @Override
    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    @Override
    public BigDecimal getMinCredits() {
        return minCredits;
    }

    public void setMinCredits(BigDecimal minCredits) {
        this.minCredits = minCredits;
    }

    @Override
    public BigDecimal getMaxCredits() {
        return maxCredits;
    }

    public void setMaxCredits(BigDecimal maxCredits) {
        this.maxCredits = maxCredits;
    }

    @Override
    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    @Override
    public List<ClassMeetingTime> getClassMeetingTimes() {
        return classMeetingTimes == null ? Collections
                .<ClassMeetingTime>emptyList() : Collections
                .<ClassMeetingTime>unmodifiableList(classMeetingTimes);
    }

    public void setClassMeetingTimes(List<ClassMeetingTime> classMeetingTimes) {
        if (classMeetingTimes != null) {
            List<ClassMeetingTimeInfo> meetingTimes = new ArrayList<ClassMeetingTimeInfo>(
                    classMeetingTimes.size());
            for (ClassMeetingTime classMeetingTime : classMeetingTimes) {
                meetingTimes.add(new ClassMeetingTimeInfo(classMeetingTime));
            }
            this.classMeetingTimes = meetingTimes;
        } else {
            this.classMeetingTimes = null;
        }
    }

    @Override
    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    @Override
    public boolean isEnrollmentGroup() {
        return enrollmentGroup;
    }

    public void setEnrollmentGroup(boolean enrollmentGroup) {
        this.enrollmentGroup = enrollmentGroup;
    }

    @Override
    public boolean isEnrollmentRestriction() {
        return enrollmentRestriction;
    }

    public void setEnrollmentRestriction(boolean enrollmentRestriction) {
        this.enrollmentRestriction = enrollmentRestriction;
    }

    @Override
    public List<SecondaryActivityOptions> getSecondaryOptions() {
        return secondaryOptions == null ? Collections
                .<SecondaryActivityOptions>emptyList() : Collections
                .<SecondaryActivityOptions>unmodifiableList(secondaryOptions);
    }

    @Override
    public ActivityOption getActivity() {
        return this;
    }

    public void setSecondaryOptions(
            List<SecondaryActivityOptions> secondaryOptions) {
        if (secondaryOptions != null) {
            List<SecondaryActivityOptionsInfo> secondaryOpts = new ArrayList<SecondaryActivityOptionsInfo>(
                    secondaryOptions.size());
            for (SecondaryActivityOptions secondaryOption : secondaryOptions) {
                secondaryOpts.add(new SecondaryActivityOptionsInfo(
                        secondaryOption));
            }
            this.secondaryOptions = secondaryOpts;
        } else {
            this.secondaryOptions = null;
        }
    }



    public List<SecondaryActivityOptions> getSelectedSecondaryOptions() {
        if (secondaryOptions == null) {
            return Collections.<SecondaryActivityOptions>emptyList();
        }
        List<SecondaryActivityOptions> copySecondaryOptions = new ArrayList<SecondaryActivityOptions>();
        for (SecondaryActivityOptions secondaryOption : secondaryOptions) {
            SecondaryActivityOptions copySecondary = new SecondaryActivityOptionsInfo(secondaryOption);
            // copy the selected sections from activityOptions
            SecondaryActivityOptionsInfo copySecondarySAOI = (SecondaryActivityOptionsInfo) copySecondary;
            SecondaryActivityOptionsInfo secondaryOptionSAOI = (SecondaryActivityOptionsInfo) secondaryOption;
            copySecondarySAOI.setActivityOptions(secondaryOptionSAOI.getSelectedSecondaryActivityOptions());
            copySecondaryOptions.add(copySecondary);
        }
        return copySecondaryOptions;
    }

    public List<ActivityOption> getAlternateActivties() {
        if (alternateActivties == null) {
            return Collections.<ActivityOption>emptyList();
        }

        List<ActivityOption> copyAlternates = new ArrayList<ActivityOption>();
        for (ActivityOption activity : alternateActivties) {
            ActivityOption alternate = new ActivityOptionInfo(activity);
            // copy the selected sections from activityOptions
            copyAlternates.add(alternate);
        }
        return copyAlternates;
    }


    public void setAlternateActivities(
            List<ActivityOption> activityOptions) {
        if (activityOptions != null) {
            List<ActivityOptionInfo> alternateOpts = new ArrayList<ActivityOptionInfo>(
                    activityOptions.size());
            for (ActivityOption activity : activityOptions) {
                alternateOpts.add(new ActivityOptionInfo(
                        activity));
            }
            this.alternateActivties = alternateOpts;
        } else {
            this.alternateActivties = null;
        }
    }



    @Override
    public String toString() {
        return "ActivityOptionInfo [activityOfferingId=" + activityOfferingId
                + ", activityTypeDescription=" + activityTypeDescription
                + ", closed=" + closed + ", openSeats=" + openSeats
                + ", totalSeats=" + totalSeats + ", requiresPermission="
                + requiresPermission + ", primary=" + primary
                + ", secondaryOptions=" + secondaryOptions
                + ", classMeetingTimes=" + classMeetingTimes
                + ", getUniqueId()=" + getUniqueId() + ", isSelected()="
                + isSelected() + "]";
    }

    @Override
    public int compareTo(ActivityOption o) {
        if (o == null)
            return -1;
        boolean oc = o.isClosed();
        if (oc != closed)
            return closed ? 1 : -1;

        boolean arranged = false;
        if (classMeetingTimes != null)
            for (ClassMeetingTime classMeetingTime : classMeetingTimes)
                arranged = arranged || classMeetingTime.isArranged();
        boolean oarr = false;
        if (o.getClassMeetingTimes() != null)
            for (ClassMeetingTime classMeetingTime : o.getClassMeetingTimes())
                oarr = oarr || classMeetingTime.isArranged();
        if (oarr != arranged)
            return arranged ? -1 : 1;

        int os = o.getOpenSeats();
        if (os != openSeats)
            return openSeats < os ? 1 : -1;
        os = o.getTotalSeats();
        if (os != totalSeats)
            return totalSeats < os ? 1 : -1;

        BigDecimal omc = o.getMinCredits();
        if (omc != null && minCredits != null) {
            int mcc = minCredits.compareTo(omc);
            if (mcc != 0)
                return -mcc;
        }

        String orc = o.getRegistrationCode();
        if (registrationCode == null && orc == null)
            return 0;
        if (registrationCode == null)
            return 1;
        if (orc == null)
            return -1;
        return registrationCode.compareTo(orc);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime
                * result
                + ((activityOfferingId == null) ? 0 : activityOfferingId
                .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ActivityOptionInfo other = (ActivityOptionInfo) obj;
        if (activityOfferingId == null) {
            if (other.activityOfferingId != null)
                return false;
        } else if (!activityOfferingId.equals(other.activityOfferingId))
            return false;
        return true;
    }

}
