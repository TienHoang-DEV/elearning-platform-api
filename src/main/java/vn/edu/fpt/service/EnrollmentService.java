package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Enrollment;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.repository.EnrollmentRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {
    private final EnrollmentRepository repository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.repository = enrollmentRepository;
    }

    public List<Enrollment> findAll() { return repository.findAll(); }
    public Optional<Enrollment> findById(Integer id) { return repository.findById(id); }
    public Enrollment save(Enrollment entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }
}
