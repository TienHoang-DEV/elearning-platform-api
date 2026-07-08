package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.SystemLog;
import vn.edu.fpt.repository.SystemLogRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SystemLogService {
    private final SystemLogRepository repository;

    public SystemLogService(SystemLogRepository systemLogRepository) {
        this.repository = systemLogRepository;
    }

    public void log(vn.edu.fpt.entity.User user, String action, String targetType, String targetId, String meta) {
        if (user == null) return;
        SystemLog log = SystemLog.builder()
                .user(user)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .meta(meta)
                .build();
        repository.save(log);
    }

    public List<SystemLog> getLogsByUserId(Integer userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<SystemLog> findAll() { return repository.findAll(); }
    public Optional<SystemLog> findById(Integer id) { return repository.findById(id); }
    public SystemLog save(SystemLog entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }
}
