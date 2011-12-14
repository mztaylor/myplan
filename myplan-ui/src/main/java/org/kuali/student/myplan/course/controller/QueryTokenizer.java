package org.kuali.student.myplan.course.controller;

import java.util.*;
import java.util.regex.*;

public class QueryTokenizer
{

    Pattern LEVEL = Pattern.compile( "[0-9][Xx][Xx]" );
    Pattern NUMBER = Pattern.compile( "[0-9]+" );

	enum Rule
	{
        // Order significant. See matcher's foreach loop below.
		WORD( "[A-Za-z0-9&]+" ),
		QUOTED( "\"[^\"]*\"" );
		
		public Pattern pattern;
		
		Rule( String regex )
		{
			pattern = Pattern.compile( regex );
		}
	}
	
	public static class Token
	{
		Rule rule;
		String value;
		public Token( Rule rule, String value )
		{
			this.rule = rule;
			this.value = value;
		}
	}
	
	public List<Token> tokenize( String source )
	{
		ArrayList<Token> tokens = new ArrayList<Token>();
		int pos = 0;
		final int len = source.length();
		
		Matcher m = Pattern.compile( "dummy" ).matcher( source );
		m.useTransparentBounds( true );
		m.useAnchoringBounds( false );

		loop:
		while( pos < len )
		{
			m.region( pos, len );
			for( Rule rule : Rule.values() )
			{
				if( m.usePattern( rule.pattern ).lookingAt() )
				{
					String value = source.substring( m.start(), m.end() );
					Token token = new Token( rule, value );
					tokens.add( token );	
					pos = m.end();
					continue loop;
				}
			}
			
			pos++;
		}
		
		return tokens;
	}

    public List<String> extractCourseLevels(String source)
    {
        ArrayList<String> tokens = new ArrayList<String>();
        int pos = 0;
        final int len = source.length();

        Matcher m = Pattern.compile( "dummy" ).matcher( source );
        m.useTransparentBounds( true );
        m.useAnchoringBounds( false );

        while( pos < len )
        {
            m.region( pos, len );

            if( m.usePattern( LEVEL ).lookingAt() )
            {
                String value = source.substring( m.start(), m.end() );
                tokens.add( value );
                pos = m.end();
            }

            pos++;
        }

        return tokens;
    }

    public List<String> extractCourseCodes(String source)
    {
        ArrayList<String> tokens = new ArrayList<String>();
        int pos = 0;
        final int len = source.length();

        Matcher m = Pattern.compile( "dummy" ).matcher( source );
        m.useTransparentBounds( true );

        while( pos < len )
        {
            m.region( pos, len );

            if( m.usePattern( NUMBER ).lookingAt() )
            {
                if( m.end() - m.start() == 3 )
                {
                    String value = source.substring( m.start(), m.end() );
                    tokens.add( value );
                }
                pos = m.end();
            }

            pos++;
        }

        return tokens;
    }


	public static void main( String[] args ) 
		throws Exception
	{


         /*
        {
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.add( "apple" );
        set.add( "banana" );
        set.add( "cherry" );
        set.remove( "apple" );
        set.add( "apple" );


        for( String item : set )
        {
            System.out.println( item );
        }
        }
        System.out.println( "**" );
        {
        LinkedHashMap<String,String> set = new LinkedHashMap<String,String>();
         set.put( "apple", "apple" );
         set.put( "banana", "banana" );
         set.put( "cherry", "cherry" );
         set.put( "apple", "apple" );

         for( String item : set.keySet() )
         {
             System.out.println( item );
         }
        }
        */
//		String str = "abc123 xyzzy XYZZY 4xx 4XX \"quoted text\" 0 00 000 0000";
        /*
        {
            String str = "a pol123";
            QueryTokenizer toho = new QueryTokenizer();
            List<Token> result = toho.tokenize( str );
            for( Token t : result )
            {
                System.out.println(t.value);
            }
        }
        */

        /*
        {
            String str = "apple banana 3xx 4xx";
            QueryTokenizer toho = new QueryTokenizer();
            for( String level : toho.extractCourseLevels(str))
            {
                str = str.replace( level, "" );
                System.out.println( level + ", " + str );
            }
        }

        {
            String str = "econ253";
            QueryTokenizer toho = new QueryTokenizer();
            for( String level : toho.extractCourseCodes(str))
            {
                str = str.replace( level, "" );
                System.out.println( level + ", " + str );
            }
        }
        */

        System.out.println( "gah" );
        {
            String str = "A A xyzzy XYZZY edc&i \"quoted text\"";
            QueryTokenizer toho = new QueryTokenizer();
            List<Token> result = toho.tokenize( str );
            for( Token t : result )
            {
                System.out.println(t.rule + " " + t.value);
            }


        }
	}

}