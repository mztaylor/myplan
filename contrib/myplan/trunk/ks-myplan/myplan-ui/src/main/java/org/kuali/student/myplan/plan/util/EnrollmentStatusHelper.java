package org.kuali.student.myplan.plan.util;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import org.dom4j.DocumentException;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;

import java.util.LinkedHashMap;

public interface EnrollmentStatusHelper {


    public LinkedHashMap<String, LinkedHashMap<String, Object>> getAllSectionStatus(LinkedHashMap<String, LinkedHashMap<String, Object>> mapmap, String year, String quarter,
                                                                                    String curric, String num) throws ServiceException, DocumentException;

    public void populateEnrollmentFields(ActivityOfferingItem activity, String year, String quarter, String curric, String num, String sectionID) throws Exception;

}
