package org.kuali.student.myplan.course.util;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kuali.student.myplan.course.dataobject.MeetingDetails;

public class MeetingDetailsPropertyEditor extends PropertyEditorSupport {
	private final static Logger logger = Logger.getLogger(MeetingDetailsPropertyEditor.class);
	
	@Override
	public void setValue(Object value) {
		if (value == null) {
			logger.error("MeetingDetails was null");
			return;
		}
		
		if ( ! (value instanceof List)) {
            logger.error(String.format("Value was type [%s] instead of MeetingDetails.", value.getClass()));
            return;
        }

        super.setValue(value);
	}
	
	String template = 
		"<div class='meetingdetails'>" +
			"<span class='meetingdays'>%s</span>" +
			"<span class='meetingtime'>%s</span>" +
			"<span class='meetingbuilding'>%s</span>" +
			"<span class='meetingroom'>%s</span>" +
		"</div>";

	
    @Override
    public String getAsText() {
    	List<MeetingDetails> list = (List<MeetingDetails>) super.getValue();
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append( "<div class='meetingdetailslist'>" );
    	
    	for( MeetingDetails m : list ) {
    	
	    	String building = m.getBuilding();
	    	if( !"NOC".equals( building) && !building.startsWith("*") && "seattle".equalsIgnoreCase(m.getCampus()))
	    	{
	    		building = String.format( "<a href='http://uw.edu/maps/?%s' target='_blank' style='margin-right:0.33em;'>%s</a>", building, building );
	    	}
	    	// TODO: campus
	    	
	    	String temp = String.format( template, m.getDays(), m.getTime(), building, m.getRoom() );
	    	sb.append(temp );
    	}
    	sb.append( "</div>" );

    	String result = sb.toString();
    	result = result.replace( '\'', '\"' );
		return result;
    }
	

}
