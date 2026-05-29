package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.Announcement;
import com.tuition.new_tuition.repository.AnnouncementRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository repo;
    private final ActivityLogService logService;

    public AnnouncementService(AnnouncementRepository repo, ActivityLogService logService) {
        this.repo = repo;
        this.logService = logService;
    }

    public List<Announcement> listAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public List<Announcement> listForStudentType(String studentType) {
        String normalized = normalizeStudentType(studentType);

        // Maths-only student -> only maths announcements
        if ("MATHS".equals(normalized)) {
            return repo.findByTargetGroupInOrderByCreatedAtDesc(
                    List.of("MATHS", "MATHEMATICS")
            );
        }

        // Science-only student -> only science announcements
        if ("SCIENCE".equals(normalized)) {
            return repo.findByTargetGroupInOrderByCreatedAtDesc(
                    List.of("SCIENCE")
            );
        }

        // Both-subject student -> can see maths, science, and both
        if ("BOTH".equals(normalized)) {
            return repo.findByTargetGroupInOrderByCreatedAtDesc(
                    List.of("MATHS", "MATHEMATICS", "SCIENCE", "BOTH")
            );
        }

        // fallback
        return List.of();
    }

    public String normalizeStudentType(String studentType) {
        if (studentType == null || studentType.trim().isEmpty()) {
            return "BOTH";
        }

        String value = studentType.trim().toUpperCase();

        if ("MATHEMATICS".equals(value)) return "MATHS";
        if ("MATHS".equals(value)) return "MATHS";
        if ("SCIENCE".equals(value)) return "SCIENCE";
        if ("BOTH".equals(value)) return "BOTH";

        return value;
    }

    public Announcement getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Announcement not found: " + id));
    }

    public void create(Announcement a) {
        a.setTargetGroup(normalizeStudentType(a.getTargetGroup()));
        repo.save(a);

        logService.log(a.getTeacherId(), "TEACHER", "ANNOUNCEMENT_CREATE",
                "Created announcement: " + a.getTitle() + " for " + a.getTargetGroup());
    }

    public void update(Long id, Announcement updated, Long teacherId) {
        Announcement existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setContent(updated.getContent());
        existing.setTargetGroup(normalizeStudentType(updated.getTargetGroup()));
        repo.save(existing);

        logService.log(teacherId, "TEACHER", "ANNOUNCEMENT_UPDATE",
                "Updated announcement id=" + id);
    }

    public void delete(Long id, Long teacherId) {
        repo.deleteById(id);

        logService.log(teacherId, "TEACHER", "ANNOUNCEMENT_DELETE",
                "Deleted announcement id=" + id);
    }
}