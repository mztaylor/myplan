package org.kuali.student.myplan.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

@RunWith(value = Parameterized.class)
public class CourseLinkBuilderTest {

    private String rawText, cookedText;

    public CourseLinkBuilderTest(String rawText, String cookedText) {
        this.rawText = rawText;
        this.cookedText = cookedText;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
                { "20 credits, including ECON 200 and ECON 201:",
                        "20 credits, including [p1::Title Text::ECON 200] and [p1::Title Text::ECON 201]:" },
                { "19 - 20 credits, including MATH 112 or 124.",
                        "19 - 20 credits, including [p1::Title Text::MATH 112] or [p1::Title Text::124]."},
                { "You must complete MUHST 211, 212, & 210:",
                        "You must complete [p1::Title Text::MUHST 211], [p1::Title Text::212], & [p1::Title Text::210]:"},
                {" +  4) One from BIOL 220, PHYS 115 & 118, 122,",
                        " +  4) One from [p1::Title Text::BIOL 220], [p1::Title Text::PHYS 115] & [p1::Title Text::118], [p1::Title Text::122],"},
                {" 18 credits in MUSAP 320 and/or 420",
                        " 18 credits in [p1::Title Text::MUSAP 320] and/or [p1::Title Text::420]"},
                {"From: C LIT 240, ENGL  110, 111, 121, 131, 182, 197, 198, 199, 281, 9",
                        "From: [p1::Title Text::C LIT 240], [p1::Title Text::ENGL  110], [p1::Title Text::111], [p1::Title Text::121], [p1::Title Text::131], [p1::Title Text::182], [p1::Title Text::197], [p1::Title Text::198], [p1::Title Text::199], [p1::Title Text::281], 9"},
                { "1) You must complete ECON 200:",
                        "1) You must complete [p1::Title Text::ECON 200]:" },
                { "Civil Engineering Requirement: ECON 200, 201",
                        "Civil Engineering Requirement: [p1::Title Text::ECON 200], [p1::Title Text::201]"}  ,
                {"-  1) Part A Writing: One course from B CMU 301, 302, 410, ENGL 281, 381.",
                        "-  1) Part A Writing: One course from [p1::Title Text::B CMU 301], [p1::Title Text::302], [p1::Title Text::410], [p1::Title Text::ENGL 281], [p1::Title Text::381]."},
                {" +  4) One from BIOL 220, PHYS 115 & 118, 122,",
                        " +  4) One from [p1::Title Text::BIOL 220], [p1::Title Text::PHYS 115] & [p1::Title Text::118], [p1::Title Text::122],"},
                {"MATH 307 And 320 or AMATH 351 plus MATH 308 or AMATH 352",
                        "[p1::Title Text::MATH 307] And [p1::Title Text::320] or [p1::Title Text::AMATH 351] plus [p1::Title Text::MATH 308] or [p1::Title Text::AMATH 352]"},
               // {"From: MATH  124 OR 134, 125 OR 135, 126 OR 136",
               //     "From: [p1::Title Text::MATH  124] OR [p1::Title Text::134], [p1::Title Text::125] OR [p1::Title Text::135], [p1::Title Text::126] OR [p1::Title Text::136]"},
                {"  -  Required Courses: E E 271, 331, 332, 361, 433 plus 473 or 482",
                    "  -  Required Courses: [p1::Title Text::E E 271], [p1::Title Text::331], [p1::Title Text::332], [p1::Title Text::361], [p1::Title Text::433] plus [p1::Title Text::473] or [p1::Title Text::482]"},
                {"Suggested Electives: E E 341, 486",
                    "Suggested Electives: [p1::Title Text::E E 341], [p1::Title Text::486]"},
                //{"-  1) Calculus with Analytical Geometry: MATH 124-126",
                //    "-  1) Calculus with Analytical Geometry: MATH 124-126"},
                {"MATH 307 or AMATH 351 plus MATH 308 or AMATH 352",
                    "[p1::Title Text::MATH 307] or [p1::Title Text::AMATH 351] plus [p1::Title Text::MATH 308] or [p1::Title Text::AMATH 352]"},
                {"-  3) Advanced Multivariable Calculus: MATH 324",
                    "-  3) Advanced Multivariable Calculus: [p1::Title Text::MATH 324]"},
                {"-  1) Part A Writing: One course from B CMU 301, 302, 410, ENGL 281, 381.",
                    "-  1) Part A Writing: One course from [p1::Title Text::B CMU 301], [p1::Title Text::302], [p1::Title Text::410], [p1::Title Text::ENGL 281], [p1::Title Text::381]."},
                {"-  1) Introductory Accounting I (ACCTG 215)   <5>",
                    "-  1) Introductory Accounting I ([p1::Title Text::ACCTG 215])   <5>"},
                {"(excluding GEN ST 350), which may include up to 14 credits",
                    "(excluding [p1::Title Text::GEN ST 350]), which may include up to 14 credits"},
                {"1) Construction Engineering: CEE 404, 421, 425 or, with adviser approval, 498",
                    "1) Construction Engineering: [p1::Title Text::CEE 404], [p1::Title Text::421], [p1::Title Text::425] or, with adviser approval, [p1::Title Text::498]"},
                {"You must complete MUSIC 113 and 119 (taken         concurrently) and 120, or a placement test:",
                    "You must complete [p1::Title Text::MUSIC 113] and [p1::Title Text::119] (taken         concurrently) and [p1::Title Text::120], or a placement test:"},
                {"-  2) MATH 125 and 126 along with either MATH 308 (preferred), 205, 307, or AMATH 352.",
                    "-  2) [p1::Title Text::MATH 125] and [p1::Title Text::126] along with either [p1::Title Text::MATH 308] (preferred), [p1::Title Text::205], [p1::Title Text::307], or [p1::Title Text::AMATH 352]."},
                //{"-  4) 12 credits in ESS courses numbered 401-488.",
                //    "-  4) 12 credits in ESS courses numbered 401-488."},
                //{"The course from ESS 311-314 which you did not",
                //    "The course from ESS 311-314 which you did not"},
                {"2) A A 210 with a 2.0 or better",
                    "2) [p1::Title Text::A A 210] with a 2.0 or better"},
                //{"-  1) Earth Systems Literacy: One course from ATM S 211, ESS 201, ESS/OCEAN 230, GEOG 205 or OCEAN 200",
                //    "-  1) Earth Systems Literacy: One course from ATM S 211, ESS 201, ESS/OCEAN 230, GEOG 205 or OCEAN 200"},
                {"-  1) Biocultural Anthropology: BIO A 201",
                    "-  1) Biocultural Anthropology: [p1::Title Text::BIO A 201]"},
                //{"-  3) Statistics: Either CS&SS/SOC/STAT 221, STAT 220, STAT 311 or Q SCI 381",
                //    "-  3) Statistics: Either CS&SS/SOC/STAT 221, STAT 220, STAT 311 or Q SCI 381"},
                {"From: C LIT 240, ENGL  110, 111, 121, 131, 182, 197, 198,  199, 281, 9",
                    "From: [p1::Title Text::C LIT 240], [p1::Title Text::ENGL  110], [p1::Title Text::111], [p1::Title Text::121], [p1::Title Text::131], [p1::Title Text::182], [p1::Title Text::197], [p1::Title Text::198],  [p1::Title Text::199], [p1::Title Text::281], 9"},
                {"From: BIOL  100, 118, 161, PSYCH 202, 357, GENOME261, 351",
                    "From: [p1::Title Text::BIOL  100], [p1::Title Text::118], [p1::Title Text::161], [p1::Title Text::PSYCH 202], [p1::Title Text::357], [p1::Title Text::GENOME261], [p1::Title Text::351]"},
                {"From: FRENCH204 to 206, 208 to 226, 228 to 233, 235, 236, 238 to 296, 298, 300, 306, 307(SP07  OR AFTER), 308 to 336, 338 to 399, 400 to 499",
                    "From: [p1::Title Text::FRENCH204] to [p1::Title Text::206], [p1::Title Text::208] to [p1::Title Text::226], [p1::Title Text::228] to [p1::Title Text::233], [p1::Title Text::235], [p1::Title Text::236], [p1::Title Text::238] to [p1::Title Text::296], [p1::Title Text::298], [p1::Title Text::300], [p1::Title Text::306], [p1::Title Text::307](SP07  OR AFTER), [p1::Title Text::308] to [p1::Title Text::336], [p1::Title Text::338] to [p1::Title Text::399], [p1::Title Text::400] to [p1::Title Text::499]"},
                {"Civil Engineering Requirement: ECON 200, 201 or IND E 250",
                    "Civil Engineering Requirement: [p1::Title Text::ECON 200], [p1::Title Text::201] or [p1::Title Text::IND E 250]"},
                {"ECON 200 and 201 can also apply toward the I&S requirement, below.   -   Needs:  1 course",
                    "[p1::Title Text::ECON 200] and [p1::Title Text::201] can also apply toward the I&S requirement, below.   -   Needs:  1 course"},
                {"SP09 SPAN  202  5.0 3.6   INTERMEDIATE",
                    "SP09 [p1::Title Text::SPAN  202]  5.0 3.6   INTERMEDIATE"},
                //{ "AU11 BIOL    180  5.0   2.7    INTRO BIOLOGY",
                //        "AU11 [p1::Title Text::BIOL    180]  5.0   2.7    INTRO BIOLOGY"},
        };
        return Arrays.asList(data);
    }

    @Test
    public void testLinkBuilder() {
          assertEquals(cookedText, CourseLinkBuilder.makeLinks(rawText, CourseLinkBuilder.LINK_TEMPLATE.TEST));
    }
}
