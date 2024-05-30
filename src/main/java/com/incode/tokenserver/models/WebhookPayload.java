package com.incode.tokenserver.models;

public record WebhookPayload(String interviewId, String onboardingStatus, String clientId, String flowId) {

}
