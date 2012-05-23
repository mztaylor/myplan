package org.kuali.student.myplan.audit.service.model;

public class Credits {
    public float minimum = -1;
    public float maximum = -99;
    public float required = 100;
    public float inprogress = 10;
    public float earned = 1;
    public float needs = 1;

    public static String smartZero(float value) {
        String result = Float.toString(value);
        int x = result.length() - 1;
        while ((char) result.codePointAt(x) == '0')
            x--;
        if (result.codePointAt(x) == '.')
            x--;

        return result.substring(0, x + 1);
    }

    public float getMinimum() { return minimum; }
    public String getRequired() {
        return smartZero( required );
    }

    public String getInprogress() { return smartZero(inprogress); }

    public boolean hasInprogress() {
        return inprogress > 0.0001f;
    }

    public String getEarned() {
        return smartZero( earned );
    }

    public String getNeeds() { return smartZero(needs); }

    public boolean hasNeeds() { return needs > 0.0001f; }

    public void setRequired(float required) {
        this.required = required;
    }

    public void setEarned(float earned) {
        this.earned = earned;
    }

    public void setInprogress(float inprogress) {
        this.inprogress = inprogress;
    }

    public void setNeeds(float needs) {
        this.needs = needs;
    }
}
