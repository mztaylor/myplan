package org.kuali.student.myplan.course.controller;

import javax.transaction.NotSupportedException;
import java.util.Iterator;
import java.util.List;

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
            result = _list.get( a ).value + " " + _list.get( b ).value;
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

}
