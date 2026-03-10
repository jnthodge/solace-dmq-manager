package com.example.dmqmanager.model;

import jakarta.validation.constraints.NotNull;

public record ReplayRequest(@NotNull Boolean deleteFromDmqAfterCopy) {
}
