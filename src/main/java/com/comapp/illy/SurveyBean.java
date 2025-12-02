package com.comapp.illy;

public class SurveyBean {
    private String conversationId;
    private String date;
    private String customerPhone;
    private String type;        // B2B / B2C
    private String scoreQ1;     // Question 1: Service Quality (0-5)
    private String scoreQ2;     // Question 2: Recommendation (0-10)
    private String hasAudio;

    // Constructor
    public SurveyBean(String conversationId, String date, String customerPhone, String type, String scoreQ1, String scoreQ2, String hasAudio) {
        this.conversationId = conversationId;
        this.date = date;
        this.customerPhone = customerPhone;
        this.type = type;
        this.scoreQ1 = scoreQ1;
        this.scoreQ2 = scoreQ2;
        this.hasAudio = hasAudio;
    }

    // Getters
    public String getConversationId() { return conversationId; }
    public String getDate() { return date; }
    public String getCustomerPhone() { return customerPhone; }
    public String getType() { return type; }
    public String getScoreQ1() { return scoreQ1; }
    public String getScoreQ2() { return scoreQ2; }
    public String getHasAudio() { return hasAudio; }
    
    // Backward compatibility - returns scoreQ2 as primary score
    public String getScore() { return scoreQ2; }
}