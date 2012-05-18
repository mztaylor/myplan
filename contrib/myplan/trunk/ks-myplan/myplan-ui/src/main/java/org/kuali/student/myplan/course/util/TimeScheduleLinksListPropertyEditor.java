package org.kuali.student.myplan.course.util;
/**
 * Copyright 2005-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.myplan.course.dataobject.CourseDetails;

import java.beans.PropertyEditorSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TimeScheduleLinksListPropertyEditor extends PropertyEditorSupport implements Serializable {

    private final static Logger logger = Logger.getLogger(TimeScheduleLinksListPropertyEditor.class);

    private StudentServiceClient studentServiceClient;

    private String baseUrl = "";
    private String label = "See full details about this course in {timeScheduleName} Time Schedule";
    private String title = label;

    private List<String> styleClasses;

    private List<String> emptyListStyleClasses;
    private final static CollectionListPropertyEditorHtmlListType listType = CollectionListPropertyEditorHtmlListType.UL;

    public TimeScheduleLinksListPropertyEditor() {
        styleClasses = new ArrayList<String>();
        emptyListStyleClasses = new ArrayList<String>();
    }

    @Override
    public void setValue(Object value) {
	    if (value == null) {
            logger.error("Object was null.");
            return;
        }

        if ( ! (value instanceof CourseDetails)) {
            logger.error(String.format("Value was thype [%s] instead of CourseDetails.", value.getClass()));
            return;
        }
        super.setValue(value);
    }

    @Override
    public String getAsText() {
        //  Don't alter course details.
        final CourseDetails courseDetails = (CourseDetails) super.getValue();

        if (courseDetails == null) {
            return "";
        }

        /*
         *  If the collection is empty and no empty list message is defined then return an empty string.
         *  Otherwise, add an empty list message to the list.
         */
        String styleClassNames = getEmptyListStyleClassesAsString();

        List<String> scheduledTerms = courseDetails.getScheduledTerms();
        if (scheduledTerms == null) {
            return "";
        }

        StringBuffer formattedText = new StringBuffer();
        formattedText.append("<" + listType.getListElementName() + " class=\"" + styleClassNames + "\">" );

        for (String scheduledTerm : scheduledTerms) {
            String lightboxUrl = makeLightboxTimeScheduleUrl(scheduledTerm, courseDetails.getCode());
            String urlId = makeTimeScheduleUrlId(scheduledTerm, courseDetails.getCode());
            String t = title.replace("{timeScheduleName}", scheduledTerm);
            String l = label.replace("{timeScheduleName}", scheduledTerm);


            //need to change lightbox link url b bus --> bbus for bothell and tacoma



            formattedText.append("<" + listType.getListItemElementName() + ">")

                    /*
                    <input type="hidden" value=" createLightBoxLink('u86', {autoScale:true,centerOnScroll:true,transitionIn:"fade",transitionOut:"fade",speedIn:200,speedOut:200,hideOnOverlayClick:false,type:"iframe",width:"75%",height:"95%"}); " script="first_run">
                     */
                    .append("<input name=\"script\" type=\"hidden\" value=\"createLightBoxLink('" + urlId + "',{autoScale:true,centerOnScroll:true,transitionIn:'fade',transitionOut:'fade',speedIn:200,speedOut:200,hideOnOverlayClick:true,type:'iframe',width:'90%',height:'95%'});\" script=\"first_run\">")
                    .append("<a id=\"" + urlId + "\" href=\"" + lightboxUrl + "\" target=\"_self\">")
                .append(l)
                .append("</a>")
                .append("</" + listType.getListItemElementName() + ">");
        }

        formattedText.append("</" + listType.getListElementName() + ">");
        return formattedText.toString();
    }

    /**
     * Format a UW SWS URL as: SPR2012/chem.html#chem142
     *                         term/curriculmum_abbreviation#curriculmum_abbreviation + course number
     * No space in curriculum code for Bothell/Tacoma
     *
     * @param term
     * @param courseCode
     * @return
     */
    private String makeLightboxTimeScheduleUrl(String term, String courseCode) {

       final CourseDetails courseDetails = (CourseDetails) super.getValue();

       if (courseDetails == null) {
           return "";
       }

       /*
        *  If the collection is empty and no empty list message is defined then return an empty string.
        *  Otherwise, add an empty list message to the list.
        */
       String styleClassNames = getEmptyListStyleClassesAsString();

       List<String> campusLocation = courseDetails.getCampusLocations();

       String campusLocationString = campusLocation.get(0);

        StringBuilder url = new StringBuilder(baseUrl);


        //  Parse out all of the necessary params.
        String year = term.replaceAll("\\D+", "");
        String termName = term.replaceAll("\\d+", "").toLowerCase().trim();
        String courseNumber = courseCode.replaceAll("^\\D+", "");
        String curriculumCode = courseCode.replaceAll("\\d+$", "").toLowerCase().trim();

        //  Convert term to SWS format "SPR2012"
        String swsTerm = termName.substring(0,3).toUpperCase() + year;


        if (campusLocationString.equals("Bothell")) { //Bothell
           url.append("B").append("/");
        } else if (campusLocationString.equals("Tacoma")) { //Tacoma
            url.append("T").append("/");
        }


        // Build the URL: SPR2012/chem.html#chem142
        url.append(swsTerm).append("/");

        //  Query the student web service to convert the curriculum abbreviation to a TimeScheduleLinkAbbreviation.
        String timeScheduleLinkAbbreviation = null;
        try {
            timeScheduleLinkAbbreviation = getStudentServiceClient().getTimeScheduleLinkAbbreviation(year, termName, curriculumCode);
        } catch (ServiceException e) {
            //  If the service call fails just return the base URL.
            logger.error("Call to SWS failed.", e);
            return baseUrl;
        }

        url.append(timeScheduleLinkAbbreviation)
            //.append(".html#")
            .append(".html?external=true&dialogMode=true#");

        if (campusLocationString.equals("Seattle")) { //Seattle
            url.append(curriculumCode.toLowerCase());
        } else if (campusLocationString.equals("Bothell")) { //Bothell
            url.append(curriculumCode.toLowerCase().replaceAll("\\s",""));
        } else if (campusLocationString.equals("Tacoma")) { //Tacoma
            url.append(curriculumCode.toLowerCase().replaceAll("\\s",""));
        }

        url.append(courseNumber);
        return url.toString();
    }



     /**
     * Format a UW SWS URL as: SPR2012/chem.html#chem142
     *                         term/curriculmum_abbreviation#curriculmum_abbreviation + course number
     *
     * @param term
     * @param courseCode
     * @return
     */
    private String makeTimeScheduleUrl(String term, String courseCode) {

       final CourseDetails courseDetails = (CourseDetails) super.getValue();

       if (courseDetails == null) {
           return "";
       }

       /*
        *  If the collection is empty and no empty list message is defined then return an empty string.
        *  Otherwise, add an empty list message to the list.
        */
       String styleClassNames = getEmptyListStyleClassesAsString();

       List<String> campusLocation = courseDetails.getCampusLocations();

       String campusLocationString = campusLocation.get(0);

        StringBuilder url = new StringBuilder(baseUrl);


        //  Parse out all of the necessary params.
        String year = term.replaceAll("\\D+", "");
        String termName = term.replaceAll("\\d+", "").toLowerCase().trim();
        String courseNumber = courseCode.replaceAll("^\\D+", "");
        String curriculumCode = courseCode.replaceAll("\\d+$", "").toLowerCase().trim();

        //  Convert term to SWS format "SPR2012"
        String swsTerm = termName.substring(0,3).toUpperCase() + year;


        if (campusLocationString.equals("Bothell")) { //Bothell
           url.append("B").append("/");
        } else if (campusLocationString.equals("Tacoma")) { //Tacoma
            url.append("T").append("/");
        }


        // Build the URL: SPR2012/chem.html#chem142
        url.append(swsTerm).append("/");

        //  Query the student web service to convert the curriculum abbreviation to a TimeScheduleLinkAbbreviation.
        String timeScheduleLinkAbbreviation = null;
        try {
            timeScheduleLinkAbbreviation = getStudentServiceClient().getTimeScheduleLinkAbbreviation(year, termName, curriculumCode);
        } catch (ServiceException e) {
            //  If the service call fails just return the base URL.
            logger.error("Call to SWS failed.", e);
            return baseUrl;
        }

        url.append(timeScheduleLinkAbbreviation)
            //.append(".html#")
            .append(".html?external=true&dialogMode=true#")
            .append(curriculumCode.toLowerCase())
            .append(courseNumber);
        return url.toString();
    }

    private String makeTimeScheduleUrlId(String term, String courseCode) {

        final CourseDetails courseDetails = (CourseDetails) super.getValue();

        if (courseDetails == null) {
            return "";
        }

       /*
        *  If the collection is empty and no empty list message is defined then return an empty string.
        *  Otherwise, add an empty list message to the list.
        */
       String styleClassNames = getEmptyListStyleClassesAsString();

       String urlId = "";

        //  Parse out all of the necessary params.
        String year = term.replaceAll("\\D+", "");
        String termName = term.replaceAll("\\d+", "").toLowerCase().trim();
        String courseNumber = courseCode.replaceAll("^\\D+", "");
        String curriculumCode = courseCode.replaceAll("\\d+$", "").toLowerCase().trim();


       List<String> campusLocation = courseDetails.getCampusLocations();

       String campusLocationString = campusLocation.get(0);

        if (campusLocationString.equals("Bothell")) { //Bothell
            curriculumCode = curriculumCode.replaceAll("\\s","");
        } else if (campusLocationString.equals("Tacoma")) { //Tacoma
            curriculumCode = curriculumCode.replaceAll("\\s","");
        }


        //  Convert term to SWS format "SPR2012"
        String swsTerm = termName.substring(0,3).toUpperCase() + year;

        urlId = curriculumCode + courseNumber + swsTerm;

        return urlId;
    }



    public List<String> getStyleClasses() {
        return this.styleClasses;
    }

    public void setStyleClasses(List<String> styleClasses) {
        this.styleClasses = styleClasses;
    }

    public String getStyleClassesAsString() {
        if (styleClasses != null) {
            return StringUtils.join(styleClasses, " ");
        }
        return "";
    }

    public List<String> getEmptyListStyleClasses() {
        return this.emptyListStyleClasses;
    }

    public void setEmptyListStyleClasses(List<String> styleClasses) {
        this.emptyListStyleClasses = styleClasses;
    }

    public String getEmptyListStyleClassesAsString() {
        if (emptyListStyleClasses != null) {
            return StringUtils.join(emptyListStyleClasses, " ");
        }
        return "";
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public StudentServiceClient getStudentServiceClient() {
        if (studentServiceClient == null) {
            studentServiceClient = (StudentServiceClient) GlobalResourceLoader.getService(StudentServiceClient.SERVICE_NAME);
        }
        return studentServiceClient;
    }

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }
}
