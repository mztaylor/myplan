package org.kuali.student.myplan.audit.dto;

import org.kuali.student.core.atp.dto.AtpInfo;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.audit.infc.AuditReport;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.dto.TypeStateEntityInfo;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * PlanItem message structure
 *
 * @Author kmuthu
 * Date: 2/13/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditReportInfo", propOrder = {"auditId", "reportType", "reportContentTypeKey", "report" , "typeKey", "stateKey", "meta", "attributes", "_futureElements", "requirementsSatisfied", "studentID", "programID", "runDate"})
public class AuditReportInfo extends TypeStateEntityInfo implements AuditReport {

    @XmlAttribute
    private String auditId;

    @XmlAttribute
    private String reportType;

    @XmlElement
    private String reportContentTypeKey;

    @XmlMimeType("application/octet-stream")
    private DataHandler report;

    @XmlAnyElement
    private List<Element> _futureElements;

    @XmlElement
    private String requirementsSatisfied;

    @XmlElement
    private String studentID;

    @XmlElement
    private String programID;

    @XmlElement
    private Date runDate;


    public AuditReportInfo() {
        this.auditId = null;
        this._futureElements = null;
    }

    public AuditReportInfo(AuditReport item) {
        super(item);

        if(null != item) {
            this.auditId = item.getAuditId();
        }
    }


    @Override
    public String getAuditId() {
        return this.auditId;
    }

    public void setAuditId(String auditId) {
        this.auditId = auditId;
    }

    @Override
    public String getReportContentTypeKey() {
        return reportContentTypeKey;
    }

    public void setReportContentTypeKey(String reportContentTypeKey) {
        this.reportContentTypeKey = reportContentTypeKey;
    }

    @Override
    public DataHandler getReport() {
        return report;
    }

    public void setReport(DataHandler report) {
        this.report = report;
    }

    @Override
    public String getRequirementsSatisfied() {
        return requirementsSatisfied;
    }

    public void setRequirementsSatisfied( String requirementsSatisfied ) {
        this.requirementsSatisfied = requirementsSatisfied;
    }

    @Override
    public String getStudentID() {
        return studentID;
    }

    public void setStudentID( String studentID ) {
        this.studentID = studentID;
    }

    @Override
    public String getProgramID() {
        return programID;
    }

    public void setProgramID(String programID) {
        this.programID = programID;
    }

    @Override
    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    @Override
    public String getReportType() {
        return reportType;
    }

    public void setReportType( String reportType ) {
        this.reportType = reportType;
    }
}
