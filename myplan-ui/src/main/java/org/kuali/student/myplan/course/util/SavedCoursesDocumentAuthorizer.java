package org.kuali.student.myplan.course.util;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.document.MaintenanceDocument;
import org.kuali.rice.krad.document.authorization.MaintenanceDocumentAuthorizer;

import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: sudduth
 * Date: 1/20/12
 * Time: 4:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class SavedCoursesDocumentAuthorizer implements MaintenanceDocumentAuthorizer {
    @Override
    public boolean canCreate(Class boClass, Person user) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canMaintain(Object dataObject, Person user) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canCreateOrMaintain(MaintenanceDocument maintenanceDocument, Person user) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> getSecurePotentiallyReadOnlySectionIds() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> getDocumentActions(Document document, Person user, Set<String> documentActions) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canInitiate(String documentTypeName, Person user) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canOpen(Document document, Person user) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canReceiveAdHoc(Document document, Person user, String actionRequestCode) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canAddNoteAttachment(Document document, String attachmentTypeCode, Person user) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canDeleteNoteAttachment(Document document, String attachmentTypeCode, String createdBySelfOnly, Person user) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canViewNoteAttachment(Document document, String attachmentTypeCode, Person user) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean canSendAdHocRequests(Document document, String actionRequestCd, Person user) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<String> getSecurePotentiallyHiddenSectionIds() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAuthorized(BusinessObject businessObject, String namespaceCode, String permissionName, String principalId) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAuthorizedByTemplate(BusinessObject businessObject, String namespaceCode, String permissionTemplateName, String principalId) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAuthorized(BusinessObject businessObject, String namespaceCode, String permissionName, String principalId, Map<String, String> additionalPermissionDetails, Map<String, String> additionalRoleQualifiers) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isAuthorizedByTemplate(Object dataObject, String namespaceCode, String permissionTemplateName, String principalId, Map<String, String> additionalPermissionDetails, Map<String, String> additionalRoleQualifiers) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> getCollectionItemRoleQualifications(BusinessObject collectionItemBusinessObject) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> getCollectionItemPermissionDetails(BusinessObject collectionItemBusinessObject) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
