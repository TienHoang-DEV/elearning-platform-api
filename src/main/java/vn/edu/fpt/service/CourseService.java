package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.repository.CourseRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {
    private final CourseRepository repository;

    public CourseService(CourseRepository courseRepository) {
        this.repository = courseRepository;
    }

    public List<Course> findAll() { return repository.findAll(); }
    public Optional<Course> findById(Integer id) { return repository.findByIdWithSectionsAndLessons(id); }
    public Course save(Course entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }
}
