package vn.edu.fpt.controller;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.entity.LessonMaterial;
import vn.edu.fpt.service.AzureBlobService;
import vn.edu.fpt.service.LessonMaterialService;
import vn.edu.fpt.util.AppConstants;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        return azureBlobService.generateSasUrl(AppConstants.AZURE_STORAGE_CONTAINER_MATERIALS, lessonMaterial.getFileUrl());
    }

    @GetMapping("/material/{id}/download")
    public ResponseEntity<InputStreamResource> downloadMaterial(@PathVariable Integer id) {
        LessonMaterial lessonMaterial = lessonMaterialService.findById(id).orElse(null);
        return lessonMaterialService.dowloadFile(lessonMaterial);
    }
}
