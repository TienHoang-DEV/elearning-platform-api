package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.CourseSection;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.service.AzureBlobService;
import vn.edu.fpt.service.CourseService;
import vn.edu.fpt.service.LessonService;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.dto.*;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import vn.edu.fpt.entity.LessonMaterial;

@Controller
@RequiredArgsConstructor
public class ListLessonCourseController {

    private final CourseService courseService;

    private final LessonService lessonService;

    private final AzureBlobService azureBlobService;

    @Autowired
    DtoMapper dtoMapper;

    @Transactional
    @GetMapping("/course/{courseId}")
    public String listSection(@PathVariable Integer courseId) {
        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));

        if (course.getSections() == null || course.getSections().isEmpty()) {
            throw new CourseNotFoundException("Khóa học không có section nào");
        }

        Iterator<CourseSection> iterator = course.getSections().iterator();
        CourseSection courseSection = iterator.next();

        if (courseSection.getLessons() == null || courseSection.getLessons().isEmpty()) {
            throw new CourseNotFoundException("Section không có bài học nào");
        }

        Integer firstLessonId = courseSection.getLessons().iterator().next().getId();
        return String.format("redirect:/course/%d/section/%d/lesson/%d", courseId, courseSection.getId(), firstLessonId);
    }

    @Transactional
    @GetMapping("/course/{courseId}/section/{sectionId}/lesson/{lessonId}")
    public String viewLesson(Model model, @PathVariable Integer courseId, @PathVariable Integer sectionId, @PathVariable Integer lessonId) {
        Course course = courseService.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));

        Lesson lesson = lessonService.findByIdWithMaterials(lessonId)
                .orElseThrow(() -> new CourseNotFoundException("Bài học không tìm thấy"));

        CourseDto courseDto = dtoMapper.toCourseDto(course);
        LessonDto lessonDto = dtoMapper.toLessonDto(lesson);
        List<LessonMaterialDto> materialDtos = new ArrayList<>();
        for (LessonMaterial m : lesson.getMaterials()) {
            materialDtos.add(dtoMapper.toLessonMaterialDto(m));
        }

        model.addAttribute("course", courseDto);
        model.addAttribute("courseSections", courseDto.getSections());
        model.addAttribute("lesson", lessonDto);
        model.addAttribute("materials", materialDtos);

        String thumbnailUrl = course.getThumbnailUrl();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            model.addAttribute("posterUrl", AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/" + thumbnailUrl);
        }

        return "learning/learning";
    }

    @GetMapping("/lesson/{lessonId}")
    @ResponseBody
    public String lessonView(@PathVariable("lessonId") Integer lessonId) {
        try {
            Lesson lesson = lessonService.findById(lessonId).orElse(null);
            if (lesson == null || lesson.getVideoUrl() == null || lesson.getVideoUrl().trim().isEmpty()) {
                return "https://www.w3schools.com/html/mov_bbb.mp4";
            }
            return azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_VIDEOS, lesson.getVideoUrl());
        } catch (Exception e) {
            // Fallback sang video test công cộng nếu Azure bị lỗi ở local dev
            return "https://www.w3schools.com/html/mov_bbb.mp4";
        }
    }
}
