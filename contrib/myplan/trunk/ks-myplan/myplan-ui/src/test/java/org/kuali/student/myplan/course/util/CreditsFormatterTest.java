package org.kuali.student.myplan.course.util;

import org.junit.Test;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.r2.lum.lrc.dto.ResultValuesGroupInfo;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class CreditsFormatterTest {
    @Test
    public void formatCreditsNullEmptyAndFixed() {
        CourseInfo courseInfo = new CourseInfo();

        //  Null credit options list.
        assertEquals("", CreditsFormatter.formatCredits(courseInfo));

        //  Empty credit options list.
        List<ResultValuesGroupInfo> creditOptions = new ArrayList<ResultValuesGroupInfo>();
        courseInfo.setCreditOptions(creditOptions);

        assertEquals("", CreditsFormatter.formatCredits(courseInfo));

        //  Credit options: fixed.
        String creditsText = "3";
        ResultValuesGroupInfo rci = new ResultValuesGroupInfo();
        rci.setType(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        List<AttributeInfo> courseOptionAttributes = new ArrayList<AttributeInfo>();
        courseOptionAttributes.add(new AttributeInfo(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_FIXED_CREDIT_VALUE, creditsText));
        rci.setAttributes(courseOptionAttributes);
        creditOptions.add(rci);

        assertEquals(creditsText, CreditsFormatter.formatCredits(courseInfo));
    }

    @Test
    public void formatCreditsMultiple() {

        CourseInfo courseInfo = new CourseInfo();
        List<ResultValuesGroupInfo> creditOptions = new ArrayList<ResultValuesGroupInfo>();

        //  Credit options: list.
        String creditsText = "1, 2, 5, 25";
        ResultValuesGroupInfo rci = new ResultValuesGroupInfo();
        rci.setType(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE);

        List<String> resultValues = new ArrayList<String>();
        resultValues.add("1");
        resultValues.add("2");
        resultValues.add("5");
        resultValues.add("25");

        rci.setResultValueKeys(resultValues);

        creditOptions.add(rci);

        courseInfo.setCreditOptions(creditOptions);

        assertEquals(creditsText, CreditsFormatter.formatCredits(courseInfo));
    }

    @Test
    public void formatCreditsRange() {
        CourseInfo courseInfo = new CourseInfo();
        List<ResultValuesGroupInfo> creditOptions = new ArrayList<ResultValuesGroupInfo>();

        //  Credit options: list.
        String creditsText = "1.5-25";
        ResultValuesGroupInfo rci = new ResultValuesGroupInfo();
        rci.setType(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

        List<AttributeInfo> courseOptionAttributes = new ArrayList<AttributeInfo>();
        courseOptionAttributes.add(new AttributeInfo(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_MIN_CREDIT_VALUE, "1.5"));
        courseOptionAttributes.add(new AttributeInfo(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_MAX_CREDIT_VALUE, "25"));

        rci.setAttributes(courseOptionAttributes);

        creditOptions.add(rci);

        courseInfo.setCreditOptions(creditOptions);

        assertEquals(creditsText, CreditsFormatter.formatCredits(courseInfo));
    }

    @Test
    public void multipleCreditOptionsFirstIsUsed() {
        CourseInfo courseInfo = new CourseInfo();

        List<ResultValuesGroupInfo> creditOptions = new ArrayList<ResultValuesGroupInfo>();
        courseInfo.setCreditOptions(creditOptions);

        //  Credit options: fixed.
        String creditsText = "3";

        ResultValuesGroupInfo rci1 = new ResultValuesGroupInfo();
        rci1.setType(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

        List<AttributeInfo> courseOptionAttributes = new ArrayList<AttributeInfo>();
        courseOptionAttributes.add(new AttributeInfo(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_FIXED_CREDIT_VALUE, creditsText));
        rci1.setAttributes(courseOptionAttributes);
        creditOptions.add(rci1);

        ResultValuesGroupInfo rci2 = new ResultValuesGroupInfo();
        rci2.setType(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE);
        creditOptions.add(rci2);

        assertEquals(creditsText, CreditsFormatter.formatCredits(courseInfo));
    }

    @Test
    public void unknownCreditType() {
        CourseInfo courseInfo = new CourseInfo();

        List<ResultValuesGroupInfo> creditOptions = new ArrayList<ResultValuesGroupInfo>();
        courseInfo.setCreditOptions(creditOptions);

        //  Credit options: fixed.
        String creditsText = "";

        ResultValuesGroupInfo rci = new ResultValuesGroupInfo();
        rci.setType("Unknown");

        List<AttributeInfo> courseOptionAttributes = new ArrayList<AttributeInfo>();
        courseOptionAttributes.add(new AttributeInfo(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_FIXED_CREDIT_VALUE, "25"));
        rci.setAttributes(courseOptionAttributes);
        creditOptions.add(rci);

        assertEquals(creditsText, CreditsFormatter.formatCredits(courseInfo));
    }

    @Test
    public void trim() {
        assertEquals("1", CreditsFormatter.trimCredits("1"));
        assertEquals("1", CreditsFormatter.trimCredits("1.0"));
        assertEquals("1.5", CreditsFormatter.trimCredits("1.5"));
        assertEquals("1.5", CreditsFormatter.trimCredits("1.5 "));
        assertEquals("1.5", CreditsFormatter.trimCredits(" 1.5 "));
        assertEquals("", CreditsFormatter.trimCredits(null));
        assertEquals("", CreditsFormatter.trimCredits(" "));
    }
}
