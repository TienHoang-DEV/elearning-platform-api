package vn.edu.fpt.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.dto.CourseSectionDto;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.exception.CourseSectionValidation;
import vn.edu.fpt.service.section.CourseSectionService;
import vn.edu.fpt.service.CourseService;

@Controller
@RequestMapping("/instructorcourse")
public class InstructorSectionController {

    private final CourseService courseService;
    private final CourseSectionService courseSectionService;

    public InstructorSectionController(CourseService courseService, CourseSectionService courseSectionService) {
        this.courseService = courseService;
        this.courseSectionService = courseSectionService;
    }

    @PostMapping("/{courseId}/sections")
    public String createSection(@PathVariable("courseId") Integer courseId,
                                @RequestParam("source") String source,
                                @Valid @ModelAttribute("section") CourseSectionDto courseSectionDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu nhập không hợp lệ.");
            return "redirect:/instructorcourse/" + courseId + "/curriculum";
        }

        try {
            Course course = courseService.findById(courseId);
            courseSectionService.SaveSection(courseSectionDto, course);
            redirectAttributes.addFlashAttribute("success", "Thêm chương thành công!");
            if("edit".equals(source)){
                return "redirect:/instructorcourse/" + courseId + "/edit";
            }
        } catch (CourseSectionValidation e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/instructorcourse/" + courseId + "/curriculum";
    }


    @PostMapping("/{courseId}/sections/{sectionId}/edit")
    public String editSection(@RequestParam("source") String source,
                              @PathVariable("courseId") Integer courseId,
                              @PathVariable("sectionId") Integer sectionId,
                              @Valid @ModelAttribute("section") CourseSectionDto courseSectionDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes
                              ){


        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu nhập không hợp lệ.");
            return "redirect:/instructorcourse/" + courseId + "/curriculum";
        }

        try {
            courseSectionService.updateCourseSection(sectionId, courseSectionDto);
            redirectAttributes.addFlashAttribute("success", "Chỉnh sửa chương thành công!");
            if("edit".equals(source)){
                return "redirect:/instructorcourse/" + courseId + "/edit";
            }
        } catch (CourseSectionValidation e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
        }

        return "redirect:/instructorcourse/" + courseId + "/curriculum";
    }

    @Transactional
    @PostMapping("/{courseId}/sections/{sectionId}/delete")
    public String deleteSection(@RequestParam("source") String source,
                                @PathVariable("courseId") Integer courseId,
                                @PathVariable("sectionId") Integer sectionId,
                                RedirectAttributes redirectAttributes){
        courseSectionService.deleteSection(sectionId);
        if("edit".equals(source)){
            return "redirect:/instructorcourse/" + courseId + "/edit";
        }
        return "redirect:/instructorcourse/" + courseId + "/curriculum";
    }


}
