package org.kuali.student.myplan.audit.infc;

import org.kuali.student.r2.common.infc.TypeStateEntity;

import javax.activation.DataHandler;
import java.util.Date;


/**
 * Programs for an audit
 *
 * @Author hemanthg
 */
public interface AuditProgram {

    // Weird field our SWS Degree Audit needs
    public String getDegreeLevel();

    // Weird field our SWS Degree Audit needs
    public String getDegreeType();

    // Weird field our SWS Degree Audit needs
    public String getPathway();

    // Weird field our SWS Degree Audit needs
    public String getCampus();

    /**
     *
     * Audit program Id
     */
    public String getProgramId();

    /**
     *
     * Audit program Title
     */
    public String getProgramTitle();


}
