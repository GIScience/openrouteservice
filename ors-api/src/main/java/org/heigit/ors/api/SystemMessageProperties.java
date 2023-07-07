package org.heigit.ors.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties
public class SystemMessageProperties {
    private List<MessageObject> messages;
    public List<MessageObject> getMessages() {
        return messages;
    }
    public void setMessages(List<MessageObject> messages) {
        this.messages = messages;
    }

    public static class MessageObject {
        private boolean active;
        private String text;
        private List<Map<String, String>> condition;

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<Map<String, String>> getCondition() {
            return condition;
        }

        public void setCondition(List<Map<String, String>> condition) {
            this.condition = condition;
        }
    }
}
