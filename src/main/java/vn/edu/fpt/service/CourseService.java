package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.dto.CourseDto;
import vn.edu.fpt.exception.CourseNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {
    private final CourseRepository repository;
    private final DtoMapper dtoMapper;

    public CourseService(CourseRepository courseRepository, DtoMapper dtoMapper) {
        this.repository = courseRepository;
        this.dtoMapper = dtoMapper;
    }

    public List<CourseDto> getCoursesBySearch(String search) {
        List<Course> courses;
        if (search != null && !search.trim().isEmpty()) {
            courses = repository.findByTitleContainingIgnoreCase(search.trim());
        } else {
            courses = repository.findAll();
        }
        List<CourseDto> dtos = new java.util.ArrayList<>();
        for (Course course : courses) {
            dtos.add(dtoMapper.toCourseDto(course));
        }
        return dtos;
    }

    public CourseDto getCourseDetail(Integer id) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
        return dtoMapper.toCourseDto(course);
    }

    public List<Course> findAll() {
        return repository.findAll();
    }

    public Course findByIdWithSectionsAndLessons(Integer id) {
        return repository.findByIdWithSectionsAndLessons(id).orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
    }

    public Course save(Course entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public Course findByIdWithEnrollmentAndLessonProgress(Integer courseId) {
        return repository.findByIdWithEnrollmentAndLessonProgress(courseId).orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học với id " + courseId));
    }


    public Course findById(Integer courseId) {
        return repository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học có id " + courseId));
    }
}
