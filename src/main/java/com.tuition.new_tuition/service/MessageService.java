package com.tuition.new_tuition.service;

import com.tuition.new_tuition.entity.Message;
import com.tuition.new_tuition.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository repo;
    private final ActivityLogService logService;

    public MessageService(MessageRepository repo, ActivityLogService logService) {
        this.repo = repo;
        this.logService = logService;
    }

    public void send(Message m) {
        repo.save(m);
        logService.log(m.getSenderId(), m.getSenderRole(), "MESSAGE_SEND",
                "Sent message to " + m.getReceiverRole() + " #" + m.getReceiverId());
    }

    public List<Message> listAll() {
        return repo.findAllByOrderBySentAtDesc();
    }

    public List<Message> listForUser(Long userId, String role) {
        return repo.findBySenderIdAndSenderRoleOrReceiverIdAndReceiverRoleOrderBySentAtDesc(
                userId, role, userId, role
        );
    }
}
