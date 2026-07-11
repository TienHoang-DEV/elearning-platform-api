package vn.edu.fpt.controller.material;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.entity.LessonMaterial;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.service.cloud.AzureBlobService;
import vn.edu.fpt.service.material.LessonMaterialService;
import vn.edu.fpt.service.lesson.LessonService;
import vn.edu.fpt.util.AppConstants;

@Controller
public class MaterialViewController {

    @Autowired
    LessonMaterialService lessonMaterialService;

    @Autowired
    AzureBlobService azureBlobService;

    @Autowired
    private LessonService lessonService;

    @GetMapping("/material/{id}")
    @ResponseBody
    public String viewMaterial(@PathVariable Integer id, Model model) {
        LessonMaterial lessonMaterial = lessonMaterialService.findById(id).orElse(null);
        if (lessonMaterial == null) {
            return null;
        }
        return azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_MATERIALS, lessonMaterial.getFileUrl());
    }

    @GetMapping("/material/{id}/view")
    public String viewMaterialRedirect(@PathVariable Integer id) {
        LessonMaterial lessonMaterial = lessonMaterialService.findById(id).orElse(null);
        if (lessonMaterial == null) {
            return "redirect:/";
        }
        String sasUrl = azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_MATERIALS, lessonMaterial.getFileUrl());
        return "redirect:" + sasUrl;
    }

    @GetMapping("/lesson/{id}/video")
    public String viewLessonVideo(@PathVariable Integer id) {
        String videoUrl = lessonService.findLessonUrl(id);
        return "redirect:" + videoUrl;
    }

    @GetMapping("/material/{id}/download")
    public ResponseEntity<InputStreamResource> downloadMaterial(@PathVariable Integer id) {
        LessonMaterial lessonMaterial = lessonMaterialService.findById(id).orElse(null);
        return lessonMaterialService.dowloadFile(lessonMaterial);
    }

    @Transactional
    public void deleteMaterialById(Integer materialId) {
        if (materialId == null || materialId <= 0) {
            throw new RuntimeException("ID tài liệu không hợp lệ");
        }

        LessonMaterial material = lessonMaterialService.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tìm thấy với id: " + materialId));

        if (material.getFileUrl() != null && !material.getFileUrl().isEmpty()) {
            try {
                azureBlobService.deleteFile(AppConstants.AZURE_STORAGE_CONTAINER_MATERIALS, material.getFileUrl());
                System.out.println("✓ Xóa file material thành công: " + material.getFileUrl());
            } catch (Exception e) {
                System.err.println("⚠️ Warning: Không thể xóa file: " + e.getMessage());
            }
        }

        // Xóa record từ DB
        lessonMaterialService.deleteById(materialId);
        System.out.println("Xóa material record thành công: " + materialId);
    }
}
