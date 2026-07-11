package vn.edu.fpt.service.material;

import com.azure.storage.blob.BlobClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.entity.Lesson;
import vn.edu.fpt.entity.LessonMaterial;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.LessonMaterialRepository;
import vn.edu.fpt.repository.LessonRepository;
import vn.edu.fpt.repository.EnrollmentRepository;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.enums.RoleType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import vn.edu.fpt.service.cloud.AzureBlobService;
import vn.edu.fpt.util.AppConstants;

@Service
@Transactional
@RequiredArgsConstructor
public class LessonMaterialService {
    private final LessonMaterialRepository repository;
    private final AzureBlobService azureBlobService;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;

    public boolean hasAccessToMaterial(User user, LessonMaterial material) {
        if (user == null || material == null || material.getLesson() == null ||
                material.getLesson().getCourseSection() == null) {
            return false;
        }
        Course course = material.getLesson().getCourseSection().getCourse();
        RoleType role = user.getRole();
        if (role == RoleType.ADMIN || role == RoleType.MANAGER) {
            return true;
        }
        if (role == RoleType.INSTRUCTOR) {
            return course.getInstructor() != null && course.getInstructor().getId().equals(user.getId());
        }
        return enrollmentRepository.existsByUserAndCourse(user, course);
    }


    public void saveAllMaterial(List<MultipartFile> file, Integer lessonId, User user){

        if(file == null || file.isEmpty()) return;

        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow();

        for(MultipartFile f : file){
            if(f == null || f.isEmpty()) continue;

            String fileName = f.getOriginalFilename();
            String fileType = f.getContentType();
            Long fileSize = f.getSize();

            String url = azureBlobService.saveFile(f, "materials");
            LessonMaterial lessonMaterial = new LessonMaterial();
            lessonMaterial.setLesson(lesson);
            lessonMaterial.setFileType(fileName.substring(fileName.lastIndexOf(".") + 1));
            lessonMaterial.setFileName(fileName);
            lessonMaterial.setFileSize(fileSize);
            lessonMaterial.setFileUrl(url);
            lessonMaterial.setInstructor(user);
            lesson.setCreatedAt(LocalDateTime.now());

            repository.save(lessonMaterial);
        }

    }

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
        return azureBlobService.dowloadFile(blobClient);
    }


    @Transactional
    public void deleteMaterialById(Integer materialId) {
        if (materialId == null || materialId <= 0) {
            throw new RuntimeException("ID tài liệu không hợp lệ");
        }

        LessonMaterial material = repository.findById(materialId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài liệu không tìm thấy với id: " + materialId));

        if (material.getFileUrl() != null && !material.getFileUrl().isEmpty()) {
            try {
                azureBlobService.deleteFile(AppConstants.AZURE_STORAGE_CONTAINER_MATERIALS, material.getFileUrl());
            } catch (Exception e) {
                System.err.println("Cảnh báo: Không thể xóa file: " + e.getMessage());
            }
        }

        repository.deleteById(materialId);
    }
}

