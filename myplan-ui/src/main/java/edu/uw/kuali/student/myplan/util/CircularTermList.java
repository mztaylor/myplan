package edu.uw.kuali.student.myplan.util;

public class CircularTermList {
    private short index = 0;

    private String quarters[] = {"Autumn", "Winter", "Spring", "Summer"};
    private int year;

    public CircularTermList(String quarter, int year) {
        index = getIndex(quarter);
        this.year = year;
    }

    /**
     * Gets the index of the
     * @param quarter
     * @return
     */
    private short getIndex(String quarter) {
        for (short i = 0; i < quarters.length; i++) {
            if (quarter.toLowerCase().equals(quarters[i].toLowerCase())) {
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

    public String getQuarter() {
       return quarters[index];
    }

    public int getYear() {
        return year;
    }
}
