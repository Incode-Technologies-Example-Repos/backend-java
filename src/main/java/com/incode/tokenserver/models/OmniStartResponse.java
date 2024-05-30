package com.incode.tokenserver.models;

public record OmniStartResponse(
    String interviewId, 
    String token, 
    String interviewCode, 
    String flowType, 
    int idCaptureTimeout, 
    int selfieCaptureTimeout, 
    int idCaptureRetries, 
    int selfieCaptureRetries, 
    int curpValidationRetries, 
    String clientId, 
    String env, 
    boolean existingSession, 
    String endScreenTitle, 
    String endScreenText, 
    boolean optinEnabled, 
    String optinCompanyName) { }

