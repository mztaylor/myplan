package org.kuali.student.myplan.audit.service.model;

public class Credits {
    public String flag = " ";
//    public float required = 100;
    public float inprogress = 10;
    public float earned = 1;
    public float needs = 1;

    public void setFlag( String flag ) {
        this.flag = flag;
    }

//    public void setRequired(float required) {
//        this.required = required;
//    }
//
//    public float getRequired() {
//        return required;
//    }
//
//    public String getRequiredText() {
//        return smartZero(required) + " " + (required == 1 ? "credit" : "credit");
//    }


    public boolean showRequired() {
        return !"R".equals(flag);
    }

    public void setInprogress(float inprogress) {
        this.inprogress = inprogress;
    }

    public float getInprogress() {
        return inprogress;
    }

    public String getInprogressText() {
        return smartZero(inprogress) + " " + (inprogress == 1 ? "credit" : "credits");
    }

    public void setEarned(float earned) {
        this.earned = earned;
    }

    public float getEarned() {
        return earned;
    }


    public String getEarnedText() {
        return smartZero(earned) + " " + (earned == 1 ? "credit" : "credits");
    }


    public boolean showEarned() {
        return !"E".equals(flag);
    }

    public float getNeeds() { return needs; }

    public String getNeedsText() {
        return smartZero(needs) + " " + (needs == 1 ? "credit" : "credits");
    }

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
