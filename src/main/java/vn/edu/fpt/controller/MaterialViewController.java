package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.entity.LessonMaterial;
import vn.edu.fpt.service.AzureBlobService;
import vn.edu.fpt.service.LessonMaterialService;
import vn.edu.fpt.util.AppConstants;

@Controller
public class MaterialViewController {

    @Autowired
    LessonMaterialService lessonMaterialService;

    @Autowired
    AzureBlobService azureBlobService;

    @GetMapping("/material/{id}")
    @ResponseBody
    public String viewMaterial(@PathVariable Integer id, Model model) {
        LessonMaterial lessonMaterial = lessonMaterialService.findById(id).orElse(null);
        if (lessonMaterial == null) {
            return null;
        }
        String url = azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_MATERIALS, lessonMaterial.getFileUrl());
        return url;
    }
}
