package com.pipemasters.server.service;

public interface ImotioService {
    void processImotioFileUpload(Long mediaFileId);
    void handleImotioWebhook(String callId);
    void setImotioIntegrationEnabled(boolean enabled);
    boolean isImotioIntegrationEnabled();
}
