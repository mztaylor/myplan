package org.kuali.student.myplan.audit.dto;

import org.kuali.student.myplan.audit.infc.AuditProgram;
import org.kuali.student.myplan.audit.infc.AuditReport;
import org.kuali.student.r2.common.dto.TypeStateEntityInfo;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.List;

/**
 * PlanItem message structure
 *
 * @Author hemanthg
 * Date: 5/17/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditProgramInfo", propOrder = {"programId", "programTitle", "degreeLevel", "degreeType", "pathway", "campus"})
public class AuditProgramInfo  implements AuditProgram, Comparable<AuditProgramInfo> {

    @XmlAttribute
    private String programId;

    @XmlAttribute
    private String programTitle;

    @XmlAttribute
    private String degreeLevel;

    @XmlAttribute
    private String degreeType;

    @XmlAttribute
    private String pathway;

    @XmlAttribute
    private String campus;

    @Override
    public String getDegreeLevel() {
        return degreeLevel;
    }

    public void setDegreeLevel(String degreeLevel) {
        this.degreeLevel = degreeLevel;
    }

    @Override
    public String getDegreeType() {
        return degreeType;
    }

    public void setDegreeType(String degreeType) {
        this.degreeType = degreeType;
    }

    @Override
    public String getPathway() {
        return pathway;
    }

    public void setPathway(String pathway) {
        this.pathway = pathway;
    }

    @Override
    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    @Override
    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    @Override
    public String getProgramTitle() {
        return programTitle;
    }

    public void setProgramTitle(String programTitle) {
        this.programTitle = programTitle;
    }

    public int compareTo(AuditProgramInfo that) {
        return this.getProgramId().compareTo( that.getProgramId() );
    }

    @Override
    public String toString() {
        return "AuditProgramInfo{" +
                "programId='" + programId + '\'' +
                ", programTitle='" + programTitle + '\'' +
                ", degreeLevel='" + degreeLevel + '\'' +
                ", degreeType='" + degreeType + '\'' +
                ", pathway='" + pathway + '\'' +
                ", campus='" + campus + '\'' +
                '}';
    }
}
