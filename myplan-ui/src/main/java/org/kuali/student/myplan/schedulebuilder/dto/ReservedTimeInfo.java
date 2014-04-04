package org.kuali.student.myplan.schedulebuilder.dto;

import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReservedTimeInfo", propOrder = {"id", "uniqueId", "selected"})
public class ReservedTimeInfo extends ScheduleBuildEventInfo implements
        ReservedTime, Serializable {

    private static final long serialVersionUID = 2036743000466751688L;

    @XmlAttribute
    private String id;

    @XmlAttribute
    private String uniqueId;

    @XmlAttribute
    private boolean selected = true;

    @XmlAttribute
    private String event;

    @XmlAttribute
    private String termId;

    public ReservedTimeInfo() {
    }

    public ReservedTimeInfo(ReservedTime copy) {
        super(copy);
        id = copy.getId();
        uniqueId = copy.getUniqueId();
        termId = copy.getTermId();
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }
}
