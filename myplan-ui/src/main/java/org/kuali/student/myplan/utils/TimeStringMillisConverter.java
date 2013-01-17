package org.kuali.student.myplan.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TimeStringMillisConverter 
{

	public static final long MILLIS_PER_SECOND = 1000L;
	public static final long MILLIS_PER_MINUTE = 60L * MILLIS_PER_SECOND;
	public static final long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;
	public static final long MILLIS_PER_DAY = 60L * MILLIS_PER_HOUR;
	
	public final static Pattern timeRegex = Pattern.compile( "([0-9][0-9]):([0-9][0-9])" );
	
	public static long militaryTimeToMillis( String example )
	{
		long timeofday = 0L;
		
		Matcher m = timeRegex.matcher(example);
		if( m.find() ) 
		{
			long hours = Long.parseLong( m.group( 1 ));
			long minutes = Long.parseLong (m.group( 2 ));
			if( hours > 23 || minutes > 59)
			{
				throw new NumberFormatException( example );
			}
			
			hours = hours * MILLIS_PER_HOUR;
			minutes = minutes * MILLIS_PER_MINUTE;
			timeofday = hours + minutes;
		}
		return timeofday;
	}
	
	public static String millisToMilitaryTime( long millis )
	{
		// Limit to 24 hours (one day) max
		millis = millis % MILLIS_PER_DAY;
		long minutes = millis % MILLIS_PER_HOUR;
		long hours = millis - minutes;
		minutes = minutes / MILLIS_PER_MINUTE;
		hours = hours / MILLIS_PER_HOUR;
		String time = String.format( "%02d:%02d", hours, minutes );
		return time;
	}
	
	public static String millisToStandardTime( long millis )
	{
		// Limit to 24 hours (one day) max
		millis = millis % MILLIS_PER_DAY;
		boolean am = true;
		long minutes = millis % MILLIS_PER_HOUR;
		long hours = millis - minutes;
		minutes = minutes / MILLIS_PER_MINUTE;
		hours = hours / MILLIS_PER_HOUR;
		if( hours > 12 )
		{
			am = false;
			hours -= 12;
		}
		String time = String.format( "%d:%02d %s", hours, minutes, am ? "AM" : "PM" );
		return time;
	}
	

	
	public static void main( String[] args ) 
		throws Exception 
	{
			
		testMilitaryRoundTrip( "08:30" );
		testMilitaryRoundTrip( "00:00" );
		testMilitaryRoundTrip( "00:01" );
		testMilitaryRoundTrip( "01:00" );
		testMilitaryRoundTrip( "23:59" );
		testMilitaryRoundTrip( "12:00" );
		testMilitaryRoundTrip( "24:00" );
		testMilitaryRoundTrip( "00:60" );
		
		testStandardToMilitary( "08:30", "8:30 AM" );
		testStandardToMilitary( "13:30", "1:30 PM" );
	}

	private static void testMilitaryRoundTrip( String example ) 
	{
		try
		{
			long timeofday = militaryTimeToMillis( example );
			String time = millisToMilitaryTime( timeofday );
			System.out.printf( "\n%s == %s -> %b", example, time, example.equals( time ));
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private static void testStandardToMilitary( String example, String expected ) 
	{
		try
		{
			long timeofday = militaryTimeToMillis( example );
			String time = millisToStandardTime( timeofday );
			System.out.printf( "\n%s == %s -> %b", time, expected, expected.equals( time ));
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
}
