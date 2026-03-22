package com.games_price_tracker.api.email;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BrevoPostBody {
    private String subject;
    private Map<String, String> sender;
    private List<To> messageVersions = new LinkedList<>();
    private String htmlContent;

    public BrevoPostBody(String subject, String senderEmail, List<String> recipients, String htmlContent){
        this.subject = subject;
        this.sender = Map.of("email", senderEmail);
        this.messageVersions = recipients.stream().map(recipient -> new To(recipient)).toList();
        this.htmlContent = htmlContent;
    }

    class To{
        List<Map<String, String>> to;

        To(String recipient){
            this.to = List.of(Map.of("email", recipient));
        }

        public List<Map<String, String>> getTo() {
            return to;
        }
    }

    public String getSubject() {
        return subject;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public Map<String, String> getSender() {
        return sender;
    }

    public List<To> getMessageVersions() {
        return messageVersions;
    }
}
