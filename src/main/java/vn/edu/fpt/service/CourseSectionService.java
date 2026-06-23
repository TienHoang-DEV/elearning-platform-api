package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.CourseSection;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.exception.CourseValidationException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.CourseSectionRepository;

import java.time.LocalDateTime;
import java.util.*;

import vn.edu.fpt.dto.CourseSectionDto;
import vn.edu.fpt.dto.LessonDto;

@Service
@Transactional
public class CourseSectionService {
    private final CourseSectionRepository repository;
    private final DtoMapper dtoMapper;


    public CourseSectionService(CourseSectionRepository courseSectionRepository, DtoMapper dtoMapper) {
        this.repository = courseSectionRepository;
        this.dtoMapper = dtoMapper;
    }

    public List<CourseSection> findAll() {
        return repository.findAll();
    }

    public Optional<CourseSection> findById(Integer id) {
        return repository.findById(id);
    }

    public CourseSection save(CourseSection entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public Set<CourseSection> findSectionsByCourse(Course course) {
        if (course.getSections() == null || course.getSections().isEmpty()) {
            throw new CourseNotFoundException("Khóa học không có section nào");
        }
        return course.getSections();
    }

    public Integer findCourseIdBySectionId(Integer sectionId) {
        return repository.findBySectionId(sectionId).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy Khóa học nào với Section id là " + sectionId));
    }

    public Map<Integer, Boolean> getSectionCompletedMap(Set<CourseSectionDto> sections, List<Integer> completedLessonIds) {
        Map<Integer, Boolean> sectionCompletedMap = new HashMap<>();
        if (sections != null) {
            for (CourseSectionDto sectionDto : sections) {
                boolean allCompleted = true;
                if (sectionDto.getLessons() == null || sectionDto.getLessons().isEmpty()) {
                    allCompleted = false;
                } else {
                    for (LessonDto l : sectionDto.getLessons()) {
                        if (completedLessonIds == null || !completedLessonIds.contains(l.getId())) {
                            allCompleted = false;
                            break;
                        }
                    }
                }
                sectionCompletedMap.put(sectionDto.getId(), allCompleted);
            }
        }
        return sectionCompletedMap;
    }

    ////Thêm Section vào Khoá Học hiện tại
    public CourseSection SaveSection(CourseSectionDto courseSectionDto, Course course){
        if(course == null){
            throw new CourseValidationException("courseRequets","Khoá học này không tồn tại");
        }

        Integer po = repository.FindMaxPositionByCourseId(course.getId());
        CourseSection courseSection = new CourseSection();
        courseSection.setTitle(courseSectionDto.getTitle());
        courseSection.setPosition(po + 1);
        courseSection.setCourse(course);
        courseSection.setCreatedAt(LocalDateTime.now());
        return repository.save(courseSection);
    }
    //// Danh sách Section theo Course
    public List<CourseSectionDto> FindSectionByCourseId(Integer courseId){
        List<CourseSection> courseSections = repository.findByCourseId(courseId);
        List<CourseSectionDto> courseSectionDtos  = new ArrayList<>();
        for(CourseSection c : courseSections){
            CourseSectionDto courseSectionDto = new CourseSectionDto();

            courseSectionDto.setId(c.getId());
            courseSectionDto.setTitle(c.getTitle());
            courseSectionDto.setPosition(c.getPosition());

            courseSectionDtos.add(courseSectionDto);
        }
         return courseSectionDtos;
    }

    public List<CourseSectionDto> findByCourseAndLesson(Integer courseId){
        List<CourseSection> courseSections = repository.findByCourseAndLesson(courseId);
        List<CourseSectionDto> courseSectionDtos = new ArrayList<>();
        for(CourseSection c : courseSections){
            CourseSectionDto courseSectionDto = new CourseSectionDto();

            courseSectionDto.setId(c.getId());
            courseSectionDto.setTitle(c.getTitle());
            courseSectionDto.setPosition(c.getPosition());

            List<LessonDto> lessons = c.getLessons().stream().
                      map(l -> {
                          LessonDto lessonDto = new LessonDto();
                          lessonDto.setId(l.getId());
                          lessonDto.setTitle(l.getTitle());
                          lessonDto.setPosition(l.getPosition());
                          lessonDto.setDurationSeconds(l.getDurationSeconds());
                          lessonDto.setIsFreePreview(l.getIsFreePreview());
                          return lessonDto;
                      }).toList();
            courseSectionDto.setLessons(lessons);

            courseSectionDtos.add(courseSectionDto);

        }
       return courseSectionDtos;
    }







    public CourseSection findBySectionId(Integer courseSectionId){
        return repository.findCourseSectionById(courseSectionId);
    }

    public CourseSectionDto findByCourseSectionId(Integer courseSectionId){
        return dtoMapper.toCourseSectionDto(repository.findCourseSectionById(courseSectionId));
    }
}
