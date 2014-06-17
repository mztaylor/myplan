package org.kuali.student.myplan.plan.util;

import org.apache.cxf.common.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.myplan.service.mock.AcademicCalendarServiceMockImpl;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;

import javax.jws.WebParam;
import java.util.List;

import static org.junit.Assert.fail;

@RunWith(JUnit4ClassRunner.class)
public class AtpHelperTest {

    @Test
    public void testFormatCurrentQuarterLabelForFirstPlanTerm() throws Exception {
        AtpHelper atpHelper = new AtpHelper();
        atpHelper.setAcademicCalendarService(new MiniMockAcalService());

        try {
            String formattedQuarterLabel = atpHelper.formatCurrentQuarterLabelForFirstPlanTerm();
            // check for everything we know will be true, regardless of quarter (until the 22nd century)
            Assert.assertFalse(StringUtils.isEmpty(formattedQuarterLabel));
            Assert.assertTrue(formattedQuarterLabel.contains("Quarter"));
            Assert.assertEquals("20", formattedQuarterLabel.substring(formattedQuarterLabel.length() - 4, formattedQuarterLabel.length() - 2));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private class MiniMockAcalService extends AcademicCalendarServiceMockImpl {
        @Override
        public List<TermInfo> searchForTerms(@WebParam(name = "criteria") QueryByCriteria queryByCriteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
            return null;
        }
    }
}