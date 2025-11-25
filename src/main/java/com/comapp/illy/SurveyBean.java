package com.comapp.illy;

public class SurveyBean {
    private String conversationId;
    private String date;
    private String customerPhone;
    private String type;   // BtoB / BtoC
    private String score;  // 0-10
    private String hasAudio;

    // Constructor
    public SurveyBean(String conversationId, String date, String customerPhone, String type, String score, String hasAudio) {
        this.conversationId = conversationId;
        this.date = date;
        this.customerPhone = customerPhone;
        this.type = type;
        this.score = score;
        this.hasAudio = hasAudio;
    }

    // Getters
    public String getConversationId() { return conversationId; }
    public String getDate() { return date; }
    public String getCustomerPhone() { return customerPhone; }
    public String getType() { return type; }
    public String getScore() { return score; }
    public String getHasAudio() { return hasAudio; }
}