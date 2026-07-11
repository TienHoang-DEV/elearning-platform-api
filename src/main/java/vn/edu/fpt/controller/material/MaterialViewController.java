package vn.edu.fpt.controller.material;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.entity.LessonMaterial;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.cloud.AzureBlobService;
import vn.edu.fpt.service.material.LessonMaterialService;
import vn.edu.fpt.service.lesson.LessonService;
import vn.edu.fpt.util.AppConstants;
import vn.edu.fpt.util.SecurityUtils;

@Controller
public class MaterialViewController {

    @Autowired
    private LessonMaterialService lessonMaterialService;

    @Autowired
    private AzureBlobService azureBlobService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private UserRepository userRepository;

    private User getSessionUser() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            return userRepository.findById(currentUser.getId()).orElse(currentUser);
        }
        return null;
    }

    @GetMapping("/material/{id}")
    @ResponseBody
    public String viewMaterial(@PathVariable Integer id, Model model) {
        User user = getSessionUser();
        if (user == null) {
            return null;
        }
        LessonMaterial lessonMaterial = lessonMaterialService.findById(id).orElse(null);
        if (lessonMaterial == null) {
            return null;
        }
        if (!lessonMaterialService.hasAccessToMaterial(user, lessonMaterial)) {
            return null;
        }
        String publicUrl = azureBlobService.getPublicUrl(AppConstants.AZURE_STORAGE_CONTAINER_MATERIALS, lessonMaterial.getFileUrl());
        String fileType = lessonMaterial.getFileType() != null ? lessonMaterial.getFileType().toLowerCase().trim() : "";
        if ("pdf".equals(fileType)) {
            return publicUrl;
        } else if ("doc".equals(fileType) || "docx".equals(fileType) || "xls".equals(fileType) || "xlsx".equals(fileType) || "ppt".equals(fileType) || "pptx".equals(fileType)) {
            return AppConstants.OFFICE_VIEWER_BASE_URL + publicUrl;
        }
        return null;
    }

    @GetMapping("/material/{id}/view")
    public String viewMaterialRedirect(@PathVariable Integer id) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }
        LessonMaterial lessonMaterial = lessonMaterialService.findById(id).orElse(null);
        if (lessonMaterial == null) {
            return "redirect:/";
        }
        if (!lessonMaterialService.hasAccessToMaterial(user, lessonMaterial)) {
            return "redirect:/course/" + lessonMaterial.getLesson().getCourseSection().getCourse().getId();
        }
        String publicUrl = azureBlobService.getPublicUrl(AppConstants.AZURE_STORAGE_CONTAINER_MATERIALS, lessonMaterial.getFileUrl());
        String fileType = lessonMaterial.getFileType() != null ? lessonMaterial.getFileType().toLowerCase().trim() : "";
        if ("pdf".equals(fileType)) {
            return "redirect:" + publicUrl;
        } else if ("doc".equals(fileType) || "docx".equals(fileType) || "xls".equals(fileType) || "xlsx".equals(fileType) || "ppt".equals(fileType) || "pptx".equals(fileType)) {
            return "redirect:" + AppConstants.OFFICE_VIEWER_BASE_URL + publicUrl;
        }
        return "redirect:/material/" + id + "/download";
    }

    @GetMapping("/lesson/{id}/video")
    public String viewLessonVideo(@PathVariable Integer id) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login";
        }
        Lesson lesson = lessonService.findById(id).orElse(null);
        if (lesson == null) {
            return "redirect:/";
        }
        if (!lessonService.hasAccessToLesson(user, lesson)) {
            return "redirect:/course/" + lesson.getCourseSection().getCourse().getId();
        }
        String videoUrl = lessonService.findLessonUrl(lesson);
        return "redirect:" + videoUrl;
    }

    @GetMapping("/material/{id}/download")
    public ResponseEntity<InputStreamResource> downloadMaterial(@PathVariable Integer id) {
        User user = getSessionUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LessonMaterial lessonMaterial = lessonMaterialService.findById(id).orElse(null);
        if (lessonMaterial == null) {
            return ResponseEntity.notFound().build();
        }
        if (!lessonMaterialService.hasAccessToMaterial(user, lessonMaterial)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return lessonMaterialService.dowloadFile(lessonMaterial);
    }
}
