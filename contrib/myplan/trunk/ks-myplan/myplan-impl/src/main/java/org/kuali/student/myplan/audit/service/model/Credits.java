package org.kuali.student.myplan.audit.service.model;

public class Credits {
    public String flag = " ";
    public float required = 100;
    public float inprogress = 10;
    public float earned = 1;
    public float needs = 1;

    public void setFlag( String flag ) {
        this.flag = flag;
    }

    public void setRequired(float required) {
        this.required = required;
    }

    public String getRequired() {
        return smartZero( required );
    }

    public boolean showRequired() {
        return !"R".equals(flag);
    }

    public void setInprogress(float inprogress) {
        this.inprogress = inprogress;
    }

    public String getInprogress() { return smartZero(inprogress); }

    public boolean showInprogress() {
        return inprogress > 0.0001f;
    }

    public void setEarned(float earned) {
        this.earned = earned;
    }

    public String getEarned() {
        return smartZero( earned );
    }

    public boolean showEarned() {
        return !"E".equals(flag);
    }

    public String getNeeds() { return smartZero(needs); }

    public boolean showNeeds() {
        return true;
    }

    public void setNeeds(float needs) {
        this.needs = needs;
    }

    public static String smartZero(float value) {
        String result = Float.toString(value);
        int x = result.length() - 1;
        while ((char) result.codePointAt(x) == '0')
            x--;
        if (result.codePointAt(x) == '.')
            x--;

        return result.substring(0, x + 1);
    }


}
