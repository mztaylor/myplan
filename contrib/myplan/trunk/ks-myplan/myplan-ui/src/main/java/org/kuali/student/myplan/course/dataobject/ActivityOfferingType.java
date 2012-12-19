package org.kuali.student.myplan.course.dataobject;

/**
 * Created with IntelliJ IDEA.
 * User: jasonosgood
 * Date: 12/5/12
 * Time: 11:13 AM
 * To change this template use File | Settings | File Templates.
 *
 * https://wiki.cac.washington.edu/display/MyPlan/MyPlan+Course+Section+Details+Data+Needs
 */
public enum ActivityOfferingType {

    unknown( "??" ),
    lecture( "LC" ),
    laboratory( "LB" ),
    quiz( "QZ" ),
    independentStudy( "IS" ),
    studio( "ST" ),
    clinic( "CL" ),
    conference( "CO" ),
    seminar( "SM" ),
    clerkship( "CK" ),
    practicum( "PR" );


    private String code;
    ActivityOfferingType( String code )
    {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

}
