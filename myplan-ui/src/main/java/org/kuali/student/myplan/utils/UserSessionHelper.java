package org.kuali.student.myplan.utils;

import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.Map;

/**
 * Provides an initialized Context which can be used for service requests.
 */
@WebService(name = "UserSessionHelper", serviceName = "UserSessionHelper", portName = "UserSessionHelper", targetNamespace = "http://student.kuali.org/wsdl/userSession")
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface UserSessionHelper {

    public ContextInfo makeContextInfoInstance();

    public boolean isAdviser();

    public boolean isStudent();

    public boolean isUserSession();

    public String getStudentId();

    public String getCurrentUserId();

    public String getStudentName();

    public String getName(String principleId);

    public String getFirstName(String principleId);

    public String getLastName(String principleId);

    public String getCapitalizedName(String principleId);

    public String getMailAddress(String principleId);

    public String getStudentNumber();

    public String getStudentExternalIdentifier();

    public String getExternalIdentifier(String regId);

    public boolean authorizedByTemplate(String userId, Map<String, String> roleQualifiers, String permissionTemplateName);



}
