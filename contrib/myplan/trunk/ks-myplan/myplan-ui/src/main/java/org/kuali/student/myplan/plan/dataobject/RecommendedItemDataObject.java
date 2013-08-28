package org.kuali.student.myplan.plan.dataobject;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/27/13
 * Time: 5:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class RecommendedItemDataObject {

    private String recommendedBy;

    private String recommendedOn;
    
    private String recommendedTerm;

    private String recommendationNote;

    private boolean isPlanned;

    public String getRecommendedBy() {
        return recommendedBy;
    }

    public void setRecommendedBy(String recommendedBy) {
        this.recommendedBy = recommendedBy;
    }


    public String getRecommendedOn() {
        return recommendedOn;
    }

    public void setRecommendedOn(String recommendedOn) {
        this.recommendedOn = recommendedOn;
    }

    public String getRecommendationNote() {
        return recommendationNote;
    }

    public void setRecommendationNote(String recommendationNote) {
        this.recommendationNote = recommendationNote;
    }

    public String getRecommendedTerm() {
        return recommendedTerm;
    }

    public void setRecommendedTerm(String recommendedTerm) {
        this.recommendedTerm = recommendedTerm;
    }

    public boolean isPlanned() {
        return isPlanned;
    }

    public void setPlanned(boolean planned) {
        isPlanned = planned;
    }
}
