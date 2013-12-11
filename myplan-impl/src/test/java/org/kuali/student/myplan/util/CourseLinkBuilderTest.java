package org.kuali.student.myplan.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.student.r2.lum.clu.service.CluService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:myplan-test-context.xml"})
public class CourseLinkBuilderTest {

    @Autowired
    private CourseLinkBuilder courseLinkBuilder;

    @Autowired
    private CluService luServiceImpl;

    public CluService getLuServiceImpl() {
        return luServiceImpl;
    }

    public void setLuServiceImpl(CluService luServiceImpl) {
        this.luServiceImpl = luServiceImpl;
    }

    public CourseLinkBuilder getCourseLinkBuilder() {
        return courseLinkBuilder;
    }

    public void setCourseLinkBuilder(CourseLinkBuilder courseLinkBuilder) {
        this.courseLinkBuilder = courseLinkBuilder;
    }

    @Test
    public void testLinkBuilder() {

        try {
            /*coursePattern Test
            * ABC 123
            * */
            getLuServiceImpl().createClu("GEOG 371:WORLD HUNGER AND AGRICULTURAL DEVELOPMENT", null, null);
            assertTrue("<a onclick=\"openCourse('GEOG 371','GEOG 371', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">GEOG 371</a>".equals(getCourseLinkBuilder().makeLinks("GEOG 371")));

            /*coursePattern + non linked numPattern Test
            * ABC 100, 101
            * */
            assertTrue("<a onclick=\"openCourse('GEOG 371','GEOG 371', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">GEOG 371</a>, 401".equals(getCourseLinkBuilder().makeLinks("GEOG 371, 401")));

            /*coursePattern + linked numPattern Test*/
            getLuServiceImpl().createClu("GEOG 401:WORLD HUNGER AND AGRICULTURAL DEVELOPMENT", null, null);
            assertTrue("<a onclick=\"openCourse('GEOG 371','GEOG 371', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">GEOG 371</a>, <a onclick=\"openCourse('GEOG 401','GEOG 401', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">401</a>".equals(getCourseLinkBuilder().makeLinks("GEOG 371, 401")));
            assertTrue("<a onclick=\"openCourse('GEOG 371','GEOG 371', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">GEOG 371</a> or <a onclick=\"openCourse('GEOG 401','GEOG 401', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">401</a>".equals(getCourseLinkBuilder().makeLinks("GEOG 371 or 401")));
            assertTrue("<a onclick=\"openCourse('GEOG 371','GEOG 371', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">GEOG 371</a> - <a onclick=\"openCourse('GEOG 401','GEOG 401', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">401</a>".equals(getCourseLinkBuilder().makeLinks("GEOG 371 - 401")));
            assertTrue("<a onclick=\"openCourse('GEOG 371','GEOG 371', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">GEOG 371</a>, <a onclick=\"openCourse('GEOG 401','GEOG 401', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">401</a>, 136 and 324 or <a onclick=\"openCourse('GEOG 371','GEOG 371', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">371</a>".equals(getCourseLinkBuilder().makeLinks("GEOG 371, 401, 136 and 324 or 371")));


            /*jointPattern Test
            * ABC 100, 101
            * */
            assertTrue("<a onclick=\"openCourse('GEOG 371','GEOG 371', event);\" href=\"#\" title=\"WORLD HUNGER AND AGRICULTURAL DEVELOPMENT\">COM/GEOG 371</a>".equals(getCourseLinkBuilder().makeLinks("COM/GEOG 371")));


            /*courseNoSpacePattern Test
            * ABC101
            * */
            getLuServiceImpl().createClu("JSIS B 216:SCIENCE AND SOCIETY", null, null);
            assertTrue("<a onclick=\"openCourse('JSIS B 216', event);\" href=\"#\" title=\"SCIENCE AND SOCIETY\">JSIS B216</a>".equals(getCourseLinkBuilder().makeLinks("JSIS B216")));


            /*jointRangePattern Test
            * ABC/DEF 100-200
            * */
            getLuServiceImpl().createClu("JSIS 100:SCIENCE AND SOCIETY", null, null);
            getLuServiceImpl().createClu("JSIS 200:SCIENCE AND SOCIETY", null, null);
            assertTrue("GEOG/JSIS 100-200".equals(getCourseLinkBuilder().makeLinks("GEOG/JSIS 100-200")));


            /*jointRangePattern Test
            * ABC 100 level
            * */
            assertTrue("JSIS 100 level".equals(getCourseLinkBuilder().makeLinks("JSIS 100 level")));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
