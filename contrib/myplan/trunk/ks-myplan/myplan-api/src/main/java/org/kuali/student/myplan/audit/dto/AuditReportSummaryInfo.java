package org.kuali.student.myplan.audit.dto;

import org.kuali.student.myplan.audit.infc.AuditReport;
import org.kuali.student.myplan.audit.infc.AuditReportSummary;
import org.kuali.student.r2.common.dto.TypeStateEntityInfo;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * PlanItem message structure
 *
 * @Author kmuthu
 * Date: 2/13/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuditReportSummaryInfo", propOrder = {"auditId", "typeKey", "stateKey", "meta", "attributes", "_futureElements"})
public class AuditReportSummaryInfo extends TypeStateEntityInfo implements AuditReportSummary {

    @XmlAttribute
    private String auditId;

    @XmlAnyElement
    private List<Element> _futureElements;


    public AuditReportSummaryInfo() {
        this.auditId = null;
        this._futureElements = null;
    }

    public AuditReportSummaryInfo(AuditReport item) {
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
}
