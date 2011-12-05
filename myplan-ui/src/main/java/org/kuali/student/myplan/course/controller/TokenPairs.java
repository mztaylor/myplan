package org.kuali.student.myplan.course.controller;

import javax.transaction.NotSupportedException;
import java.util.*;

/**
 * User: jasonosgood
 * Date: 12/2/11
 * Time: 11:13 AM
 */
public class TokenPairs implements Iterable<String>, Iterator<String>
{
    List<QueryTokenizer.Token> _list = null;

    public TokenPairs( List<QueryTokenizer.Token> list )
    {
        _list = list;
    }

    int a = 0;
    int b = 0;

    @Override
    public boolean hasNext() {
        return a < _list.size();
    }

    @Override
    public String next() {
        String result = null;
        if( a == b )
        {
            result = _list.get( a ).value;
            b++;
        }
        else if( a != b )
        {
            result = _list.get( a ).value + _list.get( b ).value;
            a++;
        }

        if( b == _list.size() )
        {
            a = b;
        }

        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

    public static class LongestFirst implements Comparator<String>
    {
       public int compare( String left, String right )
       {
         return right.length() - left.length();
//           return left.length() - right.length();
       }
    }

    public List<String> sortedLongestFirst()
    {
        ArrayList<String> sorted = new ArrayList<String>();
        for( String pair : this )
        {
            sorted.add( pair );
        }
        Collections.sort(sorted, new LongestFirst());
        return sorted;
    }

    public static void main( String[] args )
    {
        List<String> sorted = Arrays.asList( new String[] { "A", "BBBB", "AA", "BB", "BBB" } );
        Collections.sort( sorted, new LongestFirst()  );
        for( String ugh : sorted )
        {
            System.out.println( ugh );
        }
    }
}
