package vn.edu.fpt.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.LessonDto;
import vn.edu.fpt.service.LessonService;

@Controller
@RequestMapping("/instructorcourse/sections/{sectionId}/lessons")
public class InstructorLessonController {

    private final LessonService lessonService;

    public InstructorLessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping
    public String createLesson(@PathVariable("sectionId") Integer sectionId,
                               @RequestParam("courseId") Integer courseId,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               @Valid @ModelAttribute("lesson") LessonDto lessonDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (videoFile != null && !videoFile.isEmpty()) {
            lessonDto.setVideoUrl(videoFile.getOriginalFilename());
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu nhập không hợp lệ.");
            return "redirect:/instructorcourse/" + courseId + "/curriculum";
        }

        try {
            lessonService.saveLesson(sectionId, lessonDto);
            redirectAttributes.addFlashAttribute("success", "Thêm bài giảng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/instructorcourse/" + courseId + "/curriculum";
    }
}
