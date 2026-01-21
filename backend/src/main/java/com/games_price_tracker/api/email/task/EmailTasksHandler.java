package com.games_price_tracker.api.email.task;

import java.util.List;

import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailTasksHandler {
    private final TaskExecutor emailExecutor;
    private final JavaMailSender mailSender;

    public EmailTasksHandler(TaskExecutor emailExecutor, JavaMailSender mailSender){
        this.emailExecutor = emailExecutor;
        this.mailSender = mailSender;
    }

    public void createEmailToMultipleRecipientsTask(List<String> recipients, MimeMessage message){
        emailExecutor.execute(new EmailToMultipleRecipientsTask(recipients, mailSender, message, 1, this));
    }

    public void retryTask(List<String> recipients, MimeMessage message, int attempt){
        emailExecutor.execute(new EmailToMultipleRecipientsTask(recipients, mailSender, message, attempt, this));
    }
}
