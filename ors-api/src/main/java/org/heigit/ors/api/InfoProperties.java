package org.heigit.ors.api;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "info")
public class InfoProperties {

    private String baseUrl;
    private String swaggerDocumentationUrl;
    private String supportMail;
    private String authorTag;
    private String contentLicence;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setSwaggerDocumentationUrl(String swaggerDocumentationUrl) {
        this.swaggerDocumentationUrl = swaggerDocumentationUrl;
    }

    public void setSupportMail(String supportMail) {
        this.supportMail = supportMail;
    }

    public void setAuthorTag(String authorTag) {
        this.authorTag = authorTag;
    }

    public void setContentLicence(String contentLicence) {
        this.contentLicence = contentLicence;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSwaggerDocumentationUrl() {
        return swaggerDocumentationUrl;
    }

    public String getSupportMail() {
        return supportMail;
    }

    public String getAuthorTag() {
        return authorTag;
    }

    public String getContentLicence() {
        return contentLicence;
    }
}
