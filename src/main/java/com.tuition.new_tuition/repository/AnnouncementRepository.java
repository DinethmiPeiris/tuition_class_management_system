package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByCreatedAtDesc();

    // Filter by target group (MATHS, SCIENCE, ALL)
    List<Announcement> findByTargetGroupInOrderByCreatedAtDesc(List<String> groups);
}
