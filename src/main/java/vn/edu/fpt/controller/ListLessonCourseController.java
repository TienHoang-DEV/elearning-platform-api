package vn.edu.fpt.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.service.*;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.dto.*;
import vn.edu.fpt.util.SecurityUtils;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ListLessonCourseController {

    private final CourseService courseService;

    private final LessonService lessonService;

    private final CourseSectionService courseSectionService;

    private final AzureBlobService azureBlobService;

    private final DtoMapper dtoMapper;

    @Transactional
    @GetMapping("/course/{courseId}")
    public String listSection(@PathVariable Integer courseId) {
        User user = SecurityUtils.getCurrentUser();

        Course course = courseService.findById(courseId);

        Integer lessonIdFinalCompleted = lessonService.findLessonIdFinalCompletedByCourseIdAndUserId(course.getId(), user.getId());
        if (lessonIdFinalCompleted == null) {
            throw new CourseNotFoundException("Khóa học này chưa có bài học nào.");
        }

        Integer sectionId = lessonService.findSectionIdByLessonId(lessonIdFinalCompleted);
        if (sectionId == null) {
            throw new CourseNotFoundException("Không tìm thấy phần học tương ứng với bài học.");
        }

        return String.format("redirect:/course/%d/section/%d/lesson/%d", courseId, sectionId, lessonIdFinalCompleted);
    }

    @Transactional
    @GetMapping("/course/{courseId}/section/{sectionId}/lesson/{lessonId}")
    public String viewLesson(Model model, @PathVariable Integer courseId, @PathVariable Integer sectionId, @PathVariable Integer lessonId) {
        Course course = courseService.findByIdWithSectionsAndLessons(courseId);

        Lesson lesson = lessonService.findByIdWithMaterials(lessonId);

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
