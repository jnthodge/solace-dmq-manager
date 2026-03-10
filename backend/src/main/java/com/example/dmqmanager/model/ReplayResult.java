package com.example.dmqmanager.model;

public record ReplayResult(String queueName, String originQueueName, long copiedMessages, boolean deletedFromDmq) {
}
