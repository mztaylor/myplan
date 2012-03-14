package org.kuali.student.myplan.audit.service.darsws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.5.2
 * 2012-03-09T14:11:25.969-08:00
 * Generated source version: 2.5.2
 * 
 */
@WebService(targetNamespace = "http://tempuri.org/", name = "AuditRequestSvcSoap")
@XmlSeeAlso({ObjectFactory.class})
public interface AuditRequestSvcSoap {

    @WebResult(name = "MPRequestAuditResult", targetNamespace = "http://tempuri.org/")
    @RequestWrapper(localName = "MPRequestAudit", targetNamespace = "http://tempuri.org/", className = "org.tempuri.MPRequestAudit")
    @WebMethod(operationName = "MPRequestAudit", action = "http://tempuri.org/MPRequestAudit")
    @ResponseWrapper(localName = "MPRequestAuditResponse", targetNamespace = "http://tempuri.org/", className = "org.kuali.student.myplan.audit.service.darsws.MPRequestAuditResponse")
    public MPAuditResponse mpRequestAudit(
        @WebParam(name = "studentNo", targetNamespace = "http://tempuri.org/")
        int studentNo,
        @WebParam(name = "major", targetNamespace = "http://tempuri.org/")
        java.lang.String major,
        @WebParam(name = "lineNo", targetNamespace = "http://tempuri.org/")
        int lineNo,
        @WebParam(name = "systemKey", targetNamespace = "http://tempuri.org/")
        int systemKey,
        @WebParam(name = "origin", targetNamespace = "http://tempuri.org/")
        java.lang.String origin
    );
}
