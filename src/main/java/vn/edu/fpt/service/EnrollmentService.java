package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Enrollment;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.EnrollmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

@Service
@Transactional
public class EnrollmentService {
    private final EnrollmentRepository repository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.repository = enrollmentRepository;
    }

    public Set<Integer> getEnrolledCourseIds(User user) {
        Set<Integer> enrolledCourseIds = new HashSet<>();
        if (user != null) {
            List<Enrollment> userEnrollments = repository.findByUser(user);
            for (Enrollment e : userEnrollments) {
                if (e.getCourse() != null) {
                    enrolledCourseIds.add(e.getCourse().getId());
                }
            }
        }
        return enrolledCourseIds;
    }

    public List<Enrollment> findAll() { return repository.findAll(); }
    public Optional<Enrollment> findById(Integer id) { return repository.findById(id); }
    public Enrollment save(Enrollment entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }
}
