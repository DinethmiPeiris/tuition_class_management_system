package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.TimetableSession;
import com.tuition.new_tuition.exception.TimetableConflictException;
import com.tuition.new_tuition.repository.TimetableSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class TimetableService {

    private final TimetableSessionRepository timetableSessionRepository;

    public TimetableService(TimetableSessionRepository timetableSessionRepository) {
        this.timetableSessionRepository = timetableSessionRepository;
    }

    public List<TimetableSession> getAllSessions() {
        return timetableSessionRepository.findAll();
    }

    public List<TimetableSession> getAllSessionsOldestFirst() {
        List<TimetableSession> sessions = timetableSessionRepository.findAll();
        sessions.sort(Comparator.comparing(
                TimetableSession::getDate,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return sessions;
    }

    public List<TimetableSession> listForTeacher(Long teacherId) {
        return timetableSessionRepository.findByTeacherId(teacherId);
    }

    public List<TimetableSession> getSessionsByDate(LocalDate date) {
        return timetableSessionRepository.findByDate(date);
    }

    public TimetableSession getById(Long id) {
        return timetableSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timetable session not found with id: " + id));
    }

    public TimetableSession save(TimetableSession session) {
        validateConflict(session);
        return timetableSessionRepository.save(session);
    }

    public void deleteById(Long id) {
        timetableSessionRepository.deleteById(id);
    }

    private void validateConflict(TimetableSession newSession) {
        if (newSession.getTeacherId() == null ||
                newSession.getDate() == null ||
                newSession.getStartTime() == null ||
                newSession.getEndTime() == null) {
            return;
        }

        if (!newSession.getEndTime().isAfter(newSession.getStartTime())) {
            throw new TimetableConflictException("End time must be later than start time.");
        }

        List<TimetableSession> sameDaySessions = timetableSessionRepository.findByTeacherIdAndDate(
                newSession.getTeacherId(),
                newSession.getDate());

        for (TimetableSession existing : sameDaySessions) {

            if (newSession.getId() != null && newSession.getId().equals(existing.getId())) {
                continue;
            }

            LocalDateTime newStart = newSession.getStartTime();
            LocalDateTime newEnd = newSession.getEndTime();
            LocalDateTime existingStart = existing.getStartTime();
            LocalDateTime existingEnd = existing.getEndTime();

            boolean overlap = newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);

            if (overlap) {
                throw new TimetableConflictException(
                        "Time conflict detected. Another timetable already exists on this date and time.");
            }
        }
    }
}
