package com.example.dmqmanager.model;

public record DmqMessage(
        long rowNumber,
        String messageId,
        String payload,
        String destination,
        String timestamp
) {
}
