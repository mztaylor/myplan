package org.kuali.student.myplan.audit.service.darsws;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.tempuri package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.tempuri
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MPRequestAuditResponse }
     * 
     */
    public MPRequestAuditResponse createMPRequestAuditResponse() {
        return new MPRequestAuditResponse();
    }

    /**
     * Create an instance of {@link MPAuditResponse }
     * 
     */
    public MPAuditResponse createMPAuditResponse() {
        return new MPAuditResponse();
    }

    /**
     * Create an instance of {@link MPRequestAudit }
     * 
     */
    public MPRequestAudit createMPRequestAudit() {
        return new MPRequestAudit();
    }

}
