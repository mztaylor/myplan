package org.kuali.student.myplan.course.service;

import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.student.myplan.course.dataobject.SavedCourses;
import org.kuali.student.myplan.course.dataobject.SavedCoursesItem;

import java.util.Map;

public class SavedCoursesInquiryView extends KualiInquirableImpl {
    @Override
    public SavedCourses retrieveDataObject(Map fieldValues) {
        SavedCourses saved = new SavedCourses();
        saved.setUserID( "123" );

        SavedCoursesItem item = new SavedCoursesItem();
        item.setCourseID( "abc" );
        item.setTitle( "English" );
        saved.addSavedCourses( item );

        return saved;
    }
}
