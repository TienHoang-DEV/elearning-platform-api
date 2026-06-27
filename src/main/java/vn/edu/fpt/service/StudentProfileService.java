package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.user.StudentProfileDashboardDto;
import vn.edu.fpt.entity.Enrollment;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.mapper.DtoMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentProfileService {

    private final DtoMapper dtoMapper;

    public StudentProfileDashboardDto getDashboardData(User user) {
        int enrollmentsCount = user.getEnrollments().size();
        long certificatesCount = 0;
        for (Enrollment e : user.getEnrollments()) {
            if (e.getProgressPercent() != null && e.getProgressPercent().doubleValue() >= 100) {
                certificatesCount++;
            }
        }
                
        int totalHours = 0;
        for (Enrollment en : user.getEnrollments()) {
            double pct = en.getProgressPercent() != null ? en.getProgressPercent().doubleValue() : 0.0;
            totalHours += (int) (pct * 8.0 / 100.0);
        }
        if (totalHours == 0 && enrollmentsCount > 0) {
            totalHours = 2;
        }

        return StudentProfileDashboardDto.builder()
                .currentUser(dtoMapper.toUserDto(user))
                .enrollmentsCount(enrollmentsCount)
                .certificatesCount(certificatesCount)
                .studyHours(totalHours)
                .build();
    }
}

