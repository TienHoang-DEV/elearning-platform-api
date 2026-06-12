package vn.edu.fpt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.CourseCreateDto;
import vn.edu.fpt.entity.Category;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.CategoryRepository;

import vn.edu.fpt.enums.CourseLevel;
import vn.edu.fpt.mapper.DtoMapper;

import vn.edu.fpt.repository.CourseRepository;

import vn.edu.fpt.dto.CourseDto;
import vn.edu.fpt.util.AppConstants;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Service
@Transactional
public class CourseService {
    private CourseRepository repository;
    private final DtoMapper dtoMapper;
    private final CategoryRepository categoryRepository;
    private final AzureBlobService azureBlobService;
    private final CategoryService categoryService;


    public CourseService(CourseRepository courseRepository, DtoMapper dtoMapper, CategoryRepository categoryRepository, AzureBlobService azureBlobService, CategoryService categoryService) {
        this.categoryService = categoryService;
        this.repository = courseRepository;
        this.dtoMapper = dtoMapper;
        this.categoryRepository = categoryRepository;
        this.azureBlobService = azureBlobService;
    }

    // ben manager duyet khoa hoc
    public Page<CourseDto> searchAndFilter(String keyword, String statusStr, Pageable pageable) {
        CourseStatus status =
                (statusStr == null || statusStr.isEmpty()) ? null : CourseStatus.valueOf(statusStr);
        return repository.searchAndFilter(keyword, status, pageable)
                .map(dtoMapper::toCourseDto);
    }

    public Course save(User user, CourseCreateDto courseCreateDto) {

        if (repository.existsByInstructorAndTitle(user, courseCreateDto.getTitle())) {
            throw new RuntimeException("Bạn đã có khoá học với tiêu đề này rồi");
        }

        if (courseCreateDto.getTitle().length() < 3) {
            throw new RuntimeException("Tiêu đề khoá học với số lượng kí tự lớn hơn 3");
        }

        String thumbnailUrl = null;
        if (courseCreateDto.getThumbnailFile() != null && !courseCreateDto.getThumbnailFile().isEmpty()) {
            thumbnailUrl = azureBlobService.saveFile(courseCreateDto.getThumbnailFile(), "course-thumbnails");
        }

        Category category = categoryRepository.findById(courseCreateDto.getCategoryId()).orElseThrow(() -> new RuntimeException("Category không tồn tại"));

        Course course = new Course();
        course.setTitle(courseCreateDto.getTitle());
        course.setDescription(courseCreateDto.getDescription());
        course.setCategory(category);
        course.setPrice(courseCreateDto.getPrice());
        course.setThumbnailUrl(thumbnailUrl);
        course.setLevel(courseCreateDto.getLevel());
        course.setStatus(CourseStatus.DRAFT);
        course.setCreatedAt(LocalDateTime.now());
        course.setInstructor(user);
        return repository.save(course);
    }

    public List<CourseDto> getCoursesBySearch(String search) {
        List<Course> courses;
        if (search != null && !search.trim().isEmpty()) {
            courses = repository.findByTitleContainingIgnoreCase(search.trim());
        } else {
            courses = repository.findAll();
        }
        List<CourseDto> dtos = new java.util.ArrayList<>();
        for (Course course : courses) {
            dtos.add(dtoMapper.toCourseDto(course));
        }
        return dtos;
    }

    public List<CourseDto> getFilteredAndSortedCourses(
            String search,
            Integer categoryId,
            List<Integer> ratings,
            List<String> prices,
            String sort) {

        List<CourseDto> allCourses = getCoursesBySearch(search);
        List<CourseDto> filteredCourses = new java.util.ArrayList<>();

        // 1. Duyệt qua từng khóa học để lọc (Dùng vòng lặp for thay thế Stream)
        for (CourseDto course : allCourses) {

            // Lọc theo danh mục
            if (categoryId != null) {
                if (course.getCategory() == null || !course.getCategory().getId().equals(categoryId)) {
                    continue; // Bỏ qua khóa học này, chuyển sang khóa học tiếp theo
                }
            }

            // Lọc theo số sao đánh giá
            if (ratings != null && !ratings.isEmpty()) {
                boolean matchRating = false;
                double avgRating = course.getAverageRating();
                for (Integer r : ratings) {
                    if (r == 5) {
                        if (avgRating >= 5.0) {
                            matchRating = true;
                            break;
                        }
                    } else {
                        if (avgRating >= r && avgRating < r + 1) {
                            matchRating = true;
                            break;
                        }
                    }
                }
                if (!matchRating) {
                    continue; // Bỏ qua khóa học này vì không khớp đánh giá sao
                }
            }

            // Lọc theo khoảng giá
            if (prices != null && !prices.isEmpty()) {
                boolean matchPrice = false;
                double priceVal = course.getPrice() != null ? course.getPrice().doubleValue() : 0.0;
                for (String pRange : prices) {
                    String[] parts = pRange.split("-");
                    if (parts.length == 2) {
                        try {
                            double min = Double.parseDouble(parts[0]);
                            double max = Double.parseDouble(parts[1]);
                            if (priceVal >= min && priceVal <= max) {
                                matchPrice = true;
                                break;
                            }
                        } catch (NumberFormatException e) {
                            // Bỏ qua lỗi định dạng chuỗi
                        }
                    }
                }
                if (!matchPrice) {
                    continue; // Bỏ qua khóa học này vì không khớp khoảng giá
                }
            }

            // Nếu vượt qua tất cả các bộ lọc ở trên, thêm khóa học vào danh sách kết quả
            filteredCourses.add(course);
        }

        // 2. Sắp xếp danh sách kết quả (Dùng list.sort truyền thống)
        filteredCourses.sort((c1, c2) -> {
            if ("rating".equals(sort)) {
                // Đánh giá cao nhất xếp trước
                return Double.compare(c2.getAverageRating(), c1.getAverageRating());
            } else if ("price-asc".equals(sort)) {
                // Giá rẻ nhất xếp trước
                double p1 = c1.getPrice() != null ? c1.getPrice().doubleValue() : 0.0;
                double p2 = c2.getPrice() != null ? c2.getPrice().doubleValue() : 0.0;
                return Double.compare(p1, p2);
            } else if ("price-desc".equals(sort)) {
                // Giá đắt nhất xếp trước
                double p1 = c1.getPrice() != null ? c1.getPrice().doubleValue() : 0.0;
                double p2 = c2.getPrice() != null ? c2.getPrice().doubleValue() : 0.0;
                return Double.compare(p2, p1);
            } else {
                // "newest" hoặc mặc định: Khóa học mới nhất xếp trước (theo ID giảm dần)
                int id1 = c1.getId() != null ? c1.getId() : 0;
                int id2 = c2.getId() != null ? c2.getId() : 0;
                return Integer.compare(id2, id1);
            }
        });

        return filteredCourses;
    }


    public CourseDto getCourseDetail(Integer id) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
        return dtoMapper.toCourseDto(course);
    }

    public List<Course> findAll() {
        return repository.findAll();
    }

    public Course findByIdWithSectionsAndLessons(Integer id) {
        return repository.findByIdWithSectionsAndLessons(id).orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
    }

    public Course save(Course entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public Course findByIdWithEnrollmentAndLessonProgress(Integer courseId) {
        return repository.findByIdWithEnrollmentAndLessonProgress(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học với id " + courseId));
    }

    public Course findById(Integer courseId) {
        return repository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học có id " + courseId));
    }

    public String getThumbnailUrl(Course course) {
        String thumbnailUrl = course.getThumbnailUrl();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            return AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS
                    + "/" + thumbnailUrl;
        }
        return null;
    }

    @Transactional
    public Course save(User instructor, String title, String shortdesc, String desc, String outcome, String requirement, CourseLevel level, Integer categoryId, MultipartFile file, BigDecimal price) {
        if(instructor == null){
            throw new RuntimeException("User do not have");
        }
        if(title == null || title.isEmpty()){
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
        if(requirement == null || requirement.isEmpty()){
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
        course.setTitle(title);
        course.setThumbnailUrl(url);
        course.setCategory(category);
        course.setCreatedAt(LocalDateTime.now());
        course.setStatus(CourseStatus.DRAFT);
        course.setPrice(price);
        course.setInstructor(instructor);

        return repository.save(course);
    }

    public List<Course> findByInstructorAndStatus(User user, CourseStatus status){
        if(user == null){
            throw new RuntimeException("User not found");
        }
        if(status == null){
            throw new RuntimeException("Status can not null");
        }

        return repository.findByInstructorAndStatus(user, status);
    }

    public Course findByCourseIdAndUserId(Integer courseId, Integer userId) {
        return repository.findByCourseIdAndUserId(courseId, userId).orElseThrow(() -> new ResourceNotFoundException("Người dùng chưa mua khóa học này"));
    }




}
