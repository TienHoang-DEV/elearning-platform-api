package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.CourseCreateDto;
import vn.edu.fpt.entity.Category;
import vn.edu.fpt.entity.Course;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.CategoryRepository;
import vn.edu.fpt.repository.CourseRepository;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.dto.CourseDto;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.util.AppConstants;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {
    private final CourseRepository repository;
    private final DtoMapper dtoMapper;
    private final AzureBlobService azureBlobService;
    private final CategoryRepository categoryRepository;

    public CourseService(CourseRepository courseRepository, DtoMapper dtoMapper, AzureBlobService azureBlobService, CategoryRepository categoryRepository) {
        this.repository = courseRepository;
        this.dtoMapper = dtoMapper;
        this.azureBlobService = azureBlobService;
        this.categoryRepository = categoryRepository;
    }


    public Course save(User user, CourseCreateDto courseCreateDto){

         if(repository.existsByInstructorAndTitle(user,courseCreateDto.getTitle())){
             throw new RuntimeException("Bạn đã có khoá học với tiêu đề này rồi");
         }

         if(courseCreateDto.getTitle().length() < 3){
             throw new RuntimeException("Tiêu đề khoá học với số lượng kí tự lớn hơn 3");
         }

         String thumbnailUrl = null;
         if(courseCreateDto.getThumbnailFile() != null && !courseCreateDto.getThumbnailFile().isEmpty()){
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
         course.setStatus(CourseStatus.DRAFT.toString());
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


    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public Course findByIdWithEnrollmentAndLessonProgress(Integer courseId) {
        return repository.findByIdWithEnrollmentAndLessonProgress(courseId).orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học với id " + courseId));
    }


    public Course findById(Integer courseId) {
        return repository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học có id " + courseId));
    }

    public String getThumbnailUrl(Course course) {
        String thumbnailUrl = course.getThumbnailUrl();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            return AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/" + thumbnailUrl;
        }
        return null;
    }

}