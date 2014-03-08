package org.kuali.student.myplan.schedulebuilder.dto;

import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildEvent;

import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScheduleBuildEventInfo", propOrder = {"description",
        "allDay", "startDate", "untilDate", "sunday", "monday", "tuesday",
        "wednesday", "thursday", "friday", "saturday", "_futureElements"})
public class ScheduleBuildEventInfo implements ScheduleBuildEvent {

    private static final long serialVersionUID = 804871449240773901L;

    @XmlAttribute
    private String description;

    @XmlAttribute
    private boolean allDay;

    @XmlAttribute
    private String daysAndTimes;

    @XmlAttribute
    private List<String> days;

    @XmlAttribute
    private String times;

    @XmlAttribute
    private Date startDate;

    @XmlAttribute
    private Date endTime;  // doesn't seem to be used anywhere, prob a mistake since untilDate is used for endTime

    @XmlAttribute
    private Date untilDate;

    @XmlAttribute
    private boolean sunday;

    @XmlAttribute
    private boolean monday;

    @XmlAttribute
    private boolean tuesday;

    @XmlAttribute
    private boolean wednesday;

    @XmlAttribute
    private boolean thursday;

    @XmlAttribute
    private boolean friday;

    @XmlAttribute
    private boolean saturday;

    @XmlAnyElement
    private List<?> _futureElements;

    public ScheduleBuildEventInfo() {
    }

    public ScheduleBuildEventInfo(ScheduleBuildEvent copy) {
        this.description = copy.getDescription();
        this.daysAndTimes = copy.getDaysAndTimes();
        this.days = copy.getDays();
        this.times = copy.getTimes();
        this.allDay = copy.isAllDay();
        this.startDate = copy.getStartDate();
        this.untilDate = copy.getUntilDate();
        this.sunday = copy.isSunday();
        this.monday = copy.isMonday();
        this.tuesday = copy.isTuesday();
        this.wednesday = copy.isWednesday();
        this.thursday = copy.isThursday();
        this.friday = copy.isFriday();
        this.saturday = copy.isSaturday();
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    @Override
    public String getDaysAndTimes() {
        return daysAndTimes;
    }

    public void setDaysAndTimes(String daysAndTimes) {
        this.daysAndTimes = daysAndTimes;
    }

    @Override
    public List<String> getDays() {
        return days;
    }

    public void setDays(List<String> days) {
        this.days = days;
    }

    @Override
    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public Date getUntilDate() {
        return untilDate;
    }

    public void setUntilDate(Date untilDate) {
        this.untilDate = untilDate;
    }

    @Override
    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    @Override
    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    @Override
    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    @Override
    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    @Override
    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    @Override
    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    @Override
    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

}
