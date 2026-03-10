package com.example.dmqmanager.model;

public record DmqSummary(String queueName, String originQueueName, long messageCount) {
}
