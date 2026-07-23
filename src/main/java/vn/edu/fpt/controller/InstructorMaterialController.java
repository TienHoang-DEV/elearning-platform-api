
package vn.edu.fpt.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.service.material.LessonMaterialService;
import vn.edu.fpt.util.SecurityUtils;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/materials")
public class InstructorMaterialController {
    private final LessonMaterialService lessonMaterialService;

    @PostMapping("/lesson/{lessonId}")
    public String createMaterial(@RequestParam(value = "source", defaultValue = "create") String source,
                                 @RequestParam("courseId") Integer courseId,
                                 @PathVariable("lessonId") Integer lessonId,
                                 @RequestParam(value = "materialFiles", required = false) List<MultipartFile> newMaterials,
                                 RedirectAttributes redirectAttributes) {
        User user = SecurityUtils.getCurrentUser();
        try {
            lessonMaterialService.saveAllMaterial(newMaterials, lessonId, user);
            redirectAttributes.addFlashAttribute("success", "Thêm tài liệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm tài liệu: " + e.getMessage());
        }
        return redirectAfterCurriculumAction(source, courseId);
    }

    @PostMapping("/{materialId}/delete")
    public String deleteMaterial(
                                  @RequestParam(value = "source", defaultValue = "create") String source,
                                  @PathVariable("materialId") Integer materialId,
                                  @RequestParam("courseId") Integer courseId,
                                  RedirectAttributes redirectAttributes) {
        try {
            lessonMaterialService.deleteMaterialById(materialId);
            redirectAttributes.addFlashAttribute("success", "Xoá tài liệu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xoá tài liệu: " + e.getMessage());
        }
        return redirectAfterCurriculumAction(source, courseId);
    }

    private String redirectAfterCurriculumAction(String source, Integer courseId) {
        return "edit".equals(source)
                ? "redirect:/instructor/" + courseId + "/edit"
                : "redirect:/instructor/" + courseId + "/curriculum";
    }
}
