package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.CourseLevel;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.repository.CourseRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {
    private final CourseRepository repository;
   private CategoryService categoryService;
   private AzureBlobService azureBlobService;
    public CourseService(CourseRepository courseRepository, CategoryService categoryService, AzureBlobService azureBlobService) {
        this.repository = courseRepository;
        this.categoryService = categoryService;
        this.azureBlobService = azureBlobService;
    }

    public List<Course> findAll() { return repository.findAll(); }
    public Optional<Course> findById(Integer id) { return repository.findByIdWithSectionsAndLessons(id); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }


    @Transactional
    public Course save(User instructor, String tiltle, String shortdesc, String desc, String outcome, String requirment, CourseLevel level, Integer categoryId, MultipartFile file, BigDecimal price) {
        if(instructor == null){
            throw new RuntimeException("User do not have");
        }
        if(tiltle == null || tiltle.isEmpty()){
            throw new RuntimeException("Title can not null. Please try again");
        }
        if(shortdesc == null || shortdesc.isEmpty()){
            throw new RuntimeException("Short can not null. Please try again");
        }
        if(desc == null || desc.isEmpty()){
            throw new RuntimeException("Description can not null. Please try again");
        }
        if(outcome == null || outcome.isEmpty()){
            throw new RuntimeException(("Outcome can not null. Please try again"));
        }
        if(requirment == null || requirment.isEmpty()){
            throw new RuntimeException("Requirement can not null. Please try again");
        }

        if(level == null){
            throw new RuntimeException(("Level can not null. Please try again"));
        }

        Category category = categoryService.findByIdAndStatus(categoryId, "ACTIVE");
        if(file == null || file.isEmpty()){
            throw new RuntimeException("File can not null. Please try again");
        }
        if(price == null){
            throw new RuntimeException("price can not null");
        }if(price.compareTo(BigDecimal.ZERO) < 0){
            throw new RuntimeException("Price have to >= 0");
        }
        String url = azureBlobService.saveFile(file, "user-avatars");
        Course course = new Course();
        course.setTitle(tiltle);
        course.setShort_desc(shortdesc);
        course.setRequirement(requirment);
        course.setOutcome(outcome);
        course.setThumbnailUrl(url);
        course.setCategory(category);
        course.setCreatedAt(LocalDateTime.now());
        course.setStatus("DRAFT");
        course.setPrice(price);
        course.setInstructor(instructor);

        return repository.save(course);
    }

    public List<Course> findByInstructorAndStatus(User user, String status){
        if(user == null){
            throw new RuntimeException("User not found");
        }
        if(status == null || status.isEmpty()){
            throw new RuntimeException("Status can not null");
        }

        return repository.findByInstructorAndStatus(user, status);
    }
}
