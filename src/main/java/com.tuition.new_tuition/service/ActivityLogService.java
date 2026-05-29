package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.ActivityLog;
import com.tuition.new_tuition.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repo;

    public ActivityLogService(ActivityLogRepository repo) {
        this.repo = repo;
    }

    public void log(Long actorId, String actorRole, String actionType, String description) {
        ActivityLog log = new ActivityLog();
        log.setActorId(actorId);
        log.setActorRole(actorRole);
        log.setActionType(actionType);
        log.setDescription(description);
        repo.save(log);
    }

    public List<ActivityLog> listAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }
}
