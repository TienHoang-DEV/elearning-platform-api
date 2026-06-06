package vn.edu.fpt.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.LessonMaterial;
import vn.edu.fpt.repository.LessonMaterialRepository;
import vn.edu.fpt.util.AppConstants;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LessonMaterialService {
    private final LessonMaterialRepository repository;
    private final AzureBlobService azureBlobService;


    public List<LessonMaterial> findAll() {
        return repository.findAll();
    }

    public Optional<LessonMaterial> findById(Integer id) {
        return repository.findById(id);
    }

    public LessonMaterial save(LessonMaterial entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public Optional<LessonMaterial> findByLessonId(Integer lessonId) {
        return repository.findFirstByLesson_IdOrderByIdAsc(lessonId);
    }

    public ResponseEntity<InputStreamResource> dowloadFile(LessonMaterial lessonMaterial) {
        if (lessonMaterial == null) {
            return ResponseEntity.notFound().build();
        }

        BlobClient blobClient = azureBlobService.getBlobClient(
                AppConstants.AZURE_STORAGE_CONTAINER_MATERIALS,
                lessonMaterial.getFileUrl()
        );
        BlobProperties properties = blobClient.getProperties();

        String fileName = lessonMaterial.getFileName() != null && !lessonMaterial.getFileName().isBlank()
                ? lessonMaterial.getFileName()
                : "material-" + lessonMaterial.getId();

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (properties.getContentType() != null && !properties.getContentType().isBlank()) {
            mediaType = MediaType.parseMediaType(properties.getContentType());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(properties.getBlobSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"))
                .body(new InputStreamResource(blobClient.openInputStream()));
    }
}

