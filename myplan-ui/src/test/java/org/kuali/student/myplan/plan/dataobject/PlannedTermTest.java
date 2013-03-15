package org.kuali.student.myplan.plan.dataobject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:myplan-test-context.xml"})
public class PlannedTermTest {

    @Test
    public void testSumCredits() {


        PlannedTerm pt = new PlannedTerm();
        ArrayList<String> list = new ArrayList<String>();
        list.add("1,2,3");
        list.add("1 3");
        list.add("1-3");
        list.add("1/3");
        list.add("1");

        String credits = pt.sumCreditList(list);
        assertEquals(credits, "5-13");

    }
}
