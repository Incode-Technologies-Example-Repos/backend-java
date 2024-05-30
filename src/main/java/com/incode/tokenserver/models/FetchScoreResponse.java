package com.incode.tokenserver.models;
public record FetchScoreResponse (Overall overall) {
    public record Overall (String value, String status){}
}
