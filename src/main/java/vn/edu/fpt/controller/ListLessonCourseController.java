package vn.edu.fpt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.service.*;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.dto.*;
import vn.edu.fpt.util.SecurityUtils;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ListLessonCourseController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final LessonProgressService lessonProgressService;
    private final EnrollmentService enrollmentService;
    private final CourseSectionService courseSectionService;
    private final DtoMapper dtoMapper;

    @Transactional
    @GetMapping("/course/{courseId}")
    public String listSection(@PathVariable Integer courseId) {
        User user = SecurityUtils.getCurrentUser();

        // truyền vào user id để tìm cùng khóa học, nếu người dùng chưa tham gia vào khóa mà cố tình nhập trên thanh url sẽ báo lỗi
        Course course = courseService.findByCourseIdAndUserId(courseId, user.getId());

        Integer lessonIdFinalCompleted = lessonService.findLessonIdFinalCompletedByCourseIdAndUserId(course.getId(), user.getId());

        Integer sectionId = lessonService.findSectionIdByLessonId(lessonIdFinalCompleted);

        return String.format("redirect:/course/%d/section/%d/lesson/%d", courseId, sectionId, lessonIdFinalCompleted);
    }

    @Transactional
    @GetMapping("/course/{courseId}/section/{sectionId}/lesson/{lessonId}")
    public String viewLesson(Model model, @PathVariable Integer courseId, @PathVariable Integer sectionId, @PathVariable Integer lessonId) {
        User user = SecurityUtils.getCurrentUser();
        Course course = courseService.findById(courseId);

        Lesson lesson = lessonService.findByIdWithMaterials(lessonId);

        CourseDto courseDto = dtoMapper.toCourseDto(course);
        LessonDto lessonDto = dtoMapper.toLessonDto(lesson);
        List<LessonMaterialDto> materialDtos = new ArrayList<>();
        for (LessonMaterial m : lesson.getMaterials()) {
            materialDtos.add(dtoMapper.toLessonMaterialDto(m));
        }
//        String thumbnailUrl = courseService.(course);
        Integer totalNumberOfLesson = lessonService.findNumberOfLessonByCourseId(courseId);
        Enrollment enrollment = enrollmentService.findEnrollmentByCourseIdAndUserId(courseId, user.getId());
        Integer totalNumberOfLessonCompleted = lessonProgressService.findNumberOfLessonCompletedByEnrollment(enrollment);
        Boolean lessonProgressStatus = lessonProgressService.findStatusByLessonId(lessonId);

        List<Integer> completedLessonIds = lessonService.getCompletedLessonIdsByCourseIdAndUserId(courseId, user.getId());

        Map<Integer, Boolean> sectionCompletedMap = courseSectionService.getSectionCompletedMap(courseDto.getSections(), completedLessonIds);

        // nếu next lesson bằng null tức là người học đã hoàn thành hết khóa học, không có bài học tiếp theo
        Lesson nextLesson = lessonService.findNextLessonByCurrentLesson(lesson, totalNumberOfLesson, totalNumberOfLessonCompleted);

        model.addAttribute("nextLesson", nextLesson);
        model.addAttribute("currentSectionId", sectionId);
        model.addAttribute("sectionCompletedMap", sectionCompletedMap);
        model.addAttribute("completedLessonIds", completedLessonIds);
        model.addAttribute("lessonProgressStatus", lessonProgressStatus);
        model.addAttribute("totalNumberOfLesson", totalNumberOfLesson);
        model.addAttribute("totalNumberOfLessonCompleted", totalNumberOfLessonCompleted);
        model.addAttribute("course", courseDto);
        model.addAttribute("courseSections", courseDto.getSections());
        model.addAttribute("lesson", lessonDto);
        model.addAttribute("materials", materialDtos);
//        model.addAttribute("posterUrl", thumbnailUrl);
        return "learning/learning";
    }

    @GetMapping("/lesson/{lessonId}")
    @ResponseBody
    public String lessonView(@PathVariable("lessonId") Integer lessonId) {
       return lessonService.findLessonUrl(lessonId);
    }

    @GetMapping("/lesson-completed/{lessonId}")
    @ResponseBody
    public void updateLessonProgress(@PathVariable("lessonId") Integer lessonId) {
        User user = SecurityUtils.getCurrentUser();
        Integer sectionId = lessonService.findSectionIdByLessonId(lessonId);
        Integer courseId = courseSectionService.findCourseIdBySectionId(sectionId);
        Enrollment enrollment = enrollmentService.findEnrollmentByCourseIdAndUserId(courseId, user.getId());
        lessonProgressService.saveLessonProgressByEnrollmentAndLessonId(enrollment, lessonId);
        enrollmentService.updateEnrollmentProgressPercent(enrollment, courseId, sectionId);
    }


}
