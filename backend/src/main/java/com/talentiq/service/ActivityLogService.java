package com.talentiq.service;

import com.talentiq.dto.ActivityLogDTO;
import com.talentiq.entity.ActivityLog;
import com.talentiq.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public void log(String action, String title, String description) {
        ActivityLog log = new ActivityLog(action, title, description);
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDTO> getRecentActivity() {
        return activityLogRepository.findTop15ByOrderByCreatedAtDesc()
                .stream()
                .map(log -> new ActivityLogDTO(log.getId(), log.getAction(), log.getTitle(), log.getDescription(), log.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
