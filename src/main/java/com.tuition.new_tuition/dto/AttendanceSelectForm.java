package com.tuition.new_tuition.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class AttendanceSelectForm {

    @NotNull
    private Long sessionId;

    @NotNull
    private LocalDate date;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
