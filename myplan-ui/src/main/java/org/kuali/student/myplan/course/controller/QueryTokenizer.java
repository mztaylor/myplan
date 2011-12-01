package org.kuali.student.myplan.course.controller;

import java.util.*;
import java.util.regex.*;

public class QueryTokenizer
{
	enum Rule
	{
        // Order significant. See matcher's foreach loop below.
		LEVEL( "[0-9][Xx][Xx]" ),
		NUMBER( "[0-9]+" ),
		WORD( "[A-Za-z]+" ),
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


	public static void main( String[] args ) 
		throws Exception
	{
		String str = "abc123 xyzzy XYZZY 4xx 4XX \"quoted text\" 0 00 000 0000";
		QueryTokenizer toho = new QueryTokenizer();
		List<Token> result = toho.tokenize( str );
		for( Token t : result )
		{
			System.out.println(t.value);
		}

        String argh = "\"abcdef\"";
        argh = argh.substring( 1, argh.length() - 1 );
        System.out.println( argh );
	}
}