package com.tuition.new_tuition.repository;

import com.tuition.new_tuition.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByOrderBySentAtDesc();

    List<Message> findBySenderIdAndSenderRoleOrReceiverIdAndReceiverRoleOrderBySentAtDesc(
            Long senderId, String senderRole, Long receiverId, String receiverRole);
}
