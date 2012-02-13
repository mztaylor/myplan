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
import java.util.List;

/**
 * PlanItem message structure
 *
 * @Author kmuthu
 * Date: 2/13/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditReportInfo", propOrder = {"auditId", "reportContentTypeKey", "report" , "typeKey", "stateKey", "meta", "attributes", "_futureElements"})
public class AuditReportInfo extends TypeStateEntityInfo implements AuditReport {

    @XmlAttribute
    private String auditId;

    @XmlElement
    private String reportContentTypeKey;

    @XmlMimeType("application/octet-stream")
    private DataHandler report;

    @XmlAnyElement
    private List<Element> _futureElements;


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
}
