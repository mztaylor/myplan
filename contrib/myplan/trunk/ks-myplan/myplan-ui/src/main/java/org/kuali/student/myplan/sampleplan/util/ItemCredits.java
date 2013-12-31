package org.kuali.student.myplan.sampleplan.util;

/**
 * Created by IntelliJ IDEA.
 * User: dbmc
 * Date: 12/30/13
 * Time: 11:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class ItemCredits {
    private int min;
    private int max;
    private boolean rangeType;
    private boolean multipleType;
    private boolean fixedType;


    public ItemCredits(int min, int max, boolean rangeType, boolean multipleType, boolean fixedType) {
        this.min = min;
        this.max = max;
        this.rangeType = rangeType;
        this.multipleType = multipleType;
        this.fixedType = fixedType;
    }

    public ItemCredits(String creditString) {
        // parse out any of the following: "5", "5,15", "5-15"
        int parsedMin, parsedMax;

        if (creditString.matches("\\d+")) {
            // regex just a single number so this is a fixed credit
            parsedMin = parsedMax = Integer.parseInt(creditString);
            this.min = parsedMin;
            this.max = parsedMax;
            this.rangeType = false;
            this.multipleType = false;
            this.fixedType = true;
        } else if (creditString.matches("\\d+, *\\d+")) {    // allow 0 or more spaces after the comma
            // multiple credit
            String[] splitStr = creditString.split(",");
            parsedMin = Integer.parseInt(splitStr[0].trim());
            parsedMax = Integer.parseInt(splitStr[1].trim());
            this.min = parsedMin;
            this.max = parsedMax;
            this.rangeType = false;
            this.multipleType = true;
            this.fixedType = false;
        } else if (creditString.matches("\\d+-\\d+")) {
            // range credit
            String[] splitStr = creditString.split("-");
            parsedMin = Integer.parseInt(splitStr[0]);
            parsedMax = Integer.parseInt(splitStr[1]);
            this.min = parsedMin;
            this.max = parsedMax;
            this.rangeType = true;
            this.multipleType = false;
            this.fixedType = false;
        }  else  throw new RuntimeException("Invalid credit string in ItemCredits ctor:'" + creditString + "'");
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isRangeType() {
        return rangeType;
    }

    public void setRangeType(boolean rangeType) {
        this.rangeType = rangeType;
    }

    public boolean isMultipleType() {
        return multipleType;
    }

    public void setMultipleType(boolean multipleType) {
        this.multipleType = multipleType;
    }

    public boolean isFixedType() {
        return fixedType;
    }

    public void setFixedType(boolean fixedType) {
        this.fixedType = fixedType;
    }
}