package com.tuition.new_tuition.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceMarkForm {

    private Long sessionId;
    private LocalDate date;
    private List<AttendanceMarkRow> rows = new ArrayList<>();

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

    public List<AttendanceMarkRow> getRows() {
        return rows;
    }

    public void setRows(List<AttendanceMarkRow> rows) {
        this.rows = rows;
    }
}
