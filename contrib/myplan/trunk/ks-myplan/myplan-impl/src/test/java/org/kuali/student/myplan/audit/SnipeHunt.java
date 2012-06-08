package org.kuali.student.myplan.audit;

import org.kuali.student.myplan.util.CourseLinkBuilder;

import java.io.BufferedReader;
import java.io.FileReader;

public class SnipeHunt {
    public static void main( String[] args )
        throws Exception
    {
        FileReader file = new FileReader("/Users/jasonosgood/Desktop/dprog POL job_queue_out.csv");
        BufferedReader buffy = new BufferedReader(file);

        // skip header row
        String lame = buffy.readLine();

        while ((lame = buffy.readLine()) != null) {

            String[] weak = lame.split( "\\t" );
            try
            {
                char lasera = weak[9].charAt( 0 );
                String darout = weak[11];
                switch ( lasera )
                {
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '7':
                    case '8':
                    case 'a':
                    case 'g':
                    case 'j':
                    case 'C':
                    case 'D':
                    case 'F':
                        // ignore
                        break;
                    default:
                        String victim = CourseLinkBuilder.makeLinks( darout, CourseLinkBuilder.LINK_TEMPLATE.TEST );
                        System.out.printf("\n[%s]  %s", lasera, victim);
//                        System.out.println( darout );
                        break;
                }
            }
            catch( Exception e )
            {
                // ignore
            }
        }

    }


}


