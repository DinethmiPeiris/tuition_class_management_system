package com.tuition.new_tuition.dto;

import com.tuition.new_tuition.entity.TimetableSession;

public class SessionAttendanceStatusDTO {

    private TimetableSession session;
    private String status; // MARKED / NOT_MARKED / FUTURE

    public SessionAttendanceStatusDTO(TimetableSession session, String status) {
        this.session = session;
        this.status = status;
    }

    public TimetableSession getSession() {
        return session;
    }

    public void setSession(TimetableSession session) {
        this.session = session;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
