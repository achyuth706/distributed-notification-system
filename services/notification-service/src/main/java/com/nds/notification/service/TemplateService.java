package com.nds.notification.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TemplateService {

    private static final Map<String, String> TEMPLATES = new HashMap<>();

    static {
        TEMPLATES.put("WELCOME",
            "Subject: Welcome to our platform, {{firstName}}!\n" +
            "Body: Hello {{firstName}}, welcome aboard! Your account has been created successfully.");

        TEMPLATES.put("ALERT",
            "Subject: Important Alert - Action Required\n" +
            "Body: Dear {{firstName}}, we detected unusual activity on your account. " +
            "Please review your recent transactions. Alert code: {{alertCode}}");

        TEMPLATES.put("TRANSACTION",
            "Subject: Transaction Confirmation - {{amount}}\n" +
            "Body: Hi {{firstName}}, your transaction of {{amount}} has been processed successfully. " +
            "Transaction ID: {{transactionId}}. Date: {{date}}");
    }

    public String resolveTemplate(String templateId, Map<String, String> variables) {
        if (templateId == null) return null;
        String template = TEMPLATES.getOrDefault(templateId.toUpperCase(), templateId);
        return substitute(template, variables);
    }

    public String substitute(String text, Map<String, String> variables) {
        if (text == null || variables == null) return text;
        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
