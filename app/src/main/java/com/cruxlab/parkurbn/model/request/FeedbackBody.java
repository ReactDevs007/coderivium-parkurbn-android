package com.cruxlab.parkurbn.model.request;

/**
 * Created by alla on 6/20/17.
 */

public class FeedbackBody {

    private String neighborhood;
    private String city;
    private String state;
    private Integer rating;
    private String feedback;

    public FeedbackBody() {
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
