package vn.edu.fpt.service.lesson;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.dto.LessonDto;
import vn.edu.fpt.entity.CourseSection;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.LessonModerationStatus;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.LessonRepository;
import vn.edu.fpt.service.section.CourseSectionService;
import vn.edu.fpt.service.cloud.AzureBlobService;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository repository;
    private final AzureBlobService azureBlobService;
    private final DtoMapper dtoMapper;
    private final CourseSectionService courseSectionService;

    public List<Lesson> findAll() {
        return repository.findAll();
    }

    public Optional<Lesson> findById(Integer id) {
        return repository.findById(id);
    }

    public Lesson findByIdWithMaterials(Integer id) {
        return repository.findByIdWithMaterials(id).orElseThrow(() -> new CourseNotFoundException("Bài học không tìm thấy"));
    }

    @Transactional(readOnly = true)
    public Lesson findByIdWithQuizzes(Integer id) {
        return repository.findByIdWithQuizzes(id).orElseThrow(() -> new ResourceNotFoundException("Lesson with id " + id + " not found"));
    }

    public Lesson save(Lesson entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    private Integer findMaxPositionLesson(Integer sectionId){
        return repository.FindMaxPositionByCourseSectionId(sectionId);
    }

    public Lesson saveLesson(Integer sectiondId, LessonDto lessonDto, MultipartFile file){
        boolean exist = courseSectionService.existsById(sectiondId);
        if(!exist){
            throw new RuntimeException("Tiêu đề khoá học không tìm thấy với id: " + sectiondId);
        }

        if(lessonDto == null){
            throw new RuntimeException("Dữ liệu bài học không tồn tại");
        }

        if(repository.existsByTitleAndCourseSection_Id(lessonDto.getTitle(), sectiondId)){
            throw new RuntimeException("Tiêu đề bài này đã được thiết lập");
        }

        if(lessonDto.getTitle() == null || lessonDto.getTitle().isEmpty()){
            throw new RuntimeException("Tiêu đề bài học được để trống");
        }
        if(lessonDto.getTitle().length() > 255){
            throw new RuntimeException("Tiêu đề bài học đã dài quá mức cho phép");
        }

        if( file == null || file.isEmpty()){
            throw new RuntimeException("Video bài học không được để trống");
        }

        if(lessonDto.getDurationSeconds() == null || lessonDto.getDurationSeconds() <= 0){
            lessonDto.setDurationSeconds(0);
        }

        Integer po = repository.findMaxPositionLesson(sectiondId);  

        String video_url = azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_VIDEOS, file.getOriginalFilename());
        Lesson l = new Lesson();
        l.setTitle(lessonDto.getTitle());
        l.setVideoUrl(video_url);
        l.setPosition(po + 1);
        l.setDurationSeconds(lessonDto.getDurationSeconds());
        l.setCreatedAt(LocalDateTime.now());
        l.setPublished(false);

        l.setModerationStatus(LessonModerationStatus.PENDING.toString());
        l.setCourseSection(courseSectionService.findById(sectiondId).orElseThrow());
        l.setIsFreePreview(lessonDto.getIsFreePreview() != null ? lessonDto.getIsFreePreview() : false);
        return repository.save(l);
    }

    public Set<Lesson> findLessonByCourseSection(CourseSection courseSection) {
        if (courseSection.getLessons() == null || courseSection.getLessons().isEmpty()) {
            throw new CourseNotFoundException("Section không có bài học nào");
        }
        return courseSection.getLessons();
    }

    public Integer findLessonIdFinalCompletedByCourseIdAndUserId(Integer id, Integer id1) {
        List<Integer> lessonId = repository.getCompletedLessonIdByCourseIdAndUserId(id, id1);
        if (lessonId.isEmpty()) {
           return repository.findFirstLessonIdByCourseId(id);
        }
        return lessonId.get(0);
    }

    public List<Integer> getCompletedLessonIdsByCourseIdAndUserId(Integer courseId, Integer userId) {
        return repository.getCompletedLessonIdByCourseIdAndUserId(courseId, userId);
    }

    public Integer findSectionIdByLessonId(Integer lessonIdFinalCompleted) {
        if (lessonIdFinalCompleted == null) {
            return null;
        }
        return repository.findSectionIdByLessonId(lessonIdFinalCompleted);
    }

    public Integer findNumberOfLessonByCourseId(Integer courseId) {
        return repository.findNumberOfLessonByCourseId(courseId);
    }

    public String findLessonUrl(Integer lessonId) {
        try {
            Lesson lesson = this.findById(lessonId).orElse(null);
            if (lesson == null || lesson.getVideoUrl() == null || lesson.getVideoUrl().trim().isEmpty()) {
                return "https://www.w3schools.com/html/mov_bbb.mp4";
            }
            return azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_VIDEOS, lesson.getVideoUrl());
        } catch (Exception e) {
            return "https://www.w3schools.com/html/mov_bbb.mp4";
        }
    }

    public Lesson findNextLessonByCurrentLesson(Integer lessonId,Integer courseId, Integer totalNumberOfLesson, Integer totalNumberOfLessonCompleted) {
        if (totalNumberOfLesson == totalNumberOfLessonCompleted) {
            return null;
        }
        User user = SecurityUtils.getCurrentUser();
        List<Lesson> nextLessons = repository.findNotCompletedLessons(user, courseId);
        if (nextLessons == null || nextLessons.isEmpty()) {
            return null;
        }
        for (Lesson nextLesson : nextLessons) {
            if ((nextLesson.getId() > lessonId) || (lessonId == nextLessons.get(nextLessons.size() - 1).getId())) {
                return nextLesson;
            }
        }
        return null;
    }

    public LessonDto getLessonById(Integer lessonId){

        return dtoMapper.toLessonDto(repository.findDetailById(lessonId));
    }

    public Lesson findLessonById(Integer lessonId){
        return repository.findDetailById(lessonId);
    }

    public String findLessonUrl(Lesson lesson) {
        try {
            if (lesson == null || lesson.getVideoUrl() == null || lesson.getVideoUrl().trim().isEmpty()) {
                return "https://www.w3schools.com/html/mov_bbb.mp4";
            }
            return azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_VIDEOS, lesson.getVideoUrl());
        } catch (Exception e) {
            return "https://www.w3schools.com/html/mov_bbb.mp4";
        }
    }

}
