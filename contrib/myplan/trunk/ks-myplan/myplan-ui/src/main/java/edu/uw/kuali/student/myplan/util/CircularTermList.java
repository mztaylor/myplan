package edu.uw.kuali.student.myplan.util;

public class CircularTermList {
    private short index = 0;

    //  TODO: This data structure could be improved.
    private String quarters[] = {"Autumn", "Winter", "Spring", "Summer"};
    //  This is the number which will be used to build ATP IDs.
    private String quarterNumber[] = {"4", "1", "2", "3"};
    private int year;

    public CircularTermList(String quarter, int year) {
        index = getIndex(quarter);
        this.year = year;
    }

    /**
     * Gets the index
     * @param quarter
     * @return
     */
    private short getIndex(String quarter) {
        //  Check both name and number.
        for (short i = 0; i < quarters.length; i++) {
            if (quarter.toLowerCase().equals(quarters[i].toLowerCase())) {
                return i;
            }
        }
        for (short i = 0; i < quarterNumber.length; i++) {
            if (quarter.toLowerCase().equals(quarterNumber[i])) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown quarter.");
    }

    public void incrementQuarter() {
        if (index < quarters.length - 1) {
            index++;
            //  Increment the year on Autumn.
            if (index == 1) {
               year++;
            }
        } else {
            index = 0;
        }
    }

    public String getQuarterName() {
       return quarters[index];
    }

    public String getQuarterNumber() {
       return quarterNumber[index];
    }

    public int getYear() {
        return year;
    }
}
