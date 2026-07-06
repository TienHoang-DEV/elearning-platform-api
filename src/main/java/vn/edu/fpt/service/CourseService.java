package vn.edu.fpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.*;
import vn.edu.fpt.dto.course.CategoryDto;
import vn.edu.fpt.dto.course.CourseContentSidebarDTO;
import vn.edu.fpt.dto.course.CourseDto;
import vn.edu.fpt.dto.course.CourseListDto;
import vn.edu.fpt.dto.home.HomeDto;
import vn.edu.fpt.dto.lesson.LessonNoteSiderbarDTO;
import vn.edu.fpt.dto.lesson.LessonSiderbarDTO;
import vn.edu.fpt.dto.lesson.SectionSiderbarDTO;
import vn.edu.fpt.entity.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.exception.CourseNotFoundException;
import vn.edu.fpt.exception.CourseValidationException;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.*;

import vn.edu.fpt.enums.CourseLevel;
import vn.edu.fpt.mapper.DtoMapper;

import vn.edu.fpt.service.cloud.AzureBlobService;
import vn.edu.fpt.service.lesson.LessonNoteService;
import vn.edu.fpt.service.lesson.LessonService;
import vn.edu.fpt.util.AppConstants;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository repository;
    private final DtoMapper dtoMapper;
    private final CategoryRepository categoryRepository;
    private final AzureBlobService azureBlobService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final LessonService lessonService;
    private final FeedbackService feedbackService;
    private final LessonNoteService lessonNoteService;

    // Page course của mỗi instructor
    public Page<CourseDto> findByInstructorAndStatus(User instructor, Pageable pageable, CourseStatus courseStatus) {
        return repository.findByInstructorAndStatus(instructor, pageable, courseStatus).map(dtoMapper::toCourseDto);
    }

    //Xoá khoá học
    public void deleteCourseById(Integer courseId){
        Course tmp = repository.findCourseById(courseId);
        if(tmp == null) return;
        repository.deleteCourseById(courseId);
    }

    public Course save(User user, CourseCreateDto courseCreateDto) {
        Course course;
        boolean isUpdate = courseCreateDto.getId() != null;

        if (isUpdate) {
            course = repository.findById(courseCreateDto.getId())
                    .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
            if (!course.getTitle().equals(courseCreateDto.getTitle()) &&
                repository.existsByInstructorAndTitle(user, courseCreateDto.getTitle())) {
                throw new CourseValidationException(
                        "title",
                        "Bạn đã tạo một khóa học với tiêu đề này. Vui lòng sử dụng tiêu đề khác.");
            }
        } else {
            course = new Course();
            if (repository.existsByInstructorAndTitle(user, courseCreateDto.getTitle())) {
                throw new CourseValidationException(
                        "title",
                        "Bạn đã tạo một khóa học với tiêu đề này. Vui lòng sử dụng tiêu đề khác.");
            }
            course.setCreatedAt(LocalDateTime.now());
            course.setStatus(CourseStatus.DRAFT);
            course.setInstructor(user);
        }

        if (courseCreateDto.getTitle().length() < 3) {
            throw new CourseValidationException(
                    "title",
                    "Tiêu đề khóa học phải có ít nhất 3 ký tự.");
        }

        String thumbnailUrl = course.getThumbnailUrl();
        if (courseCreateDto.getThumbnailFile() != null && !courseCreateDto.getThumbnailFile().isEmpty()) {
            thumbnailUrl = azureBlobService.saveFile(courseCreateDto.getThumbnailFile(), "course-thumbnails");
        }

        Category category = categoryRepository.findById(courseCreateDto.getCategoryId())
                .orElseThrow(() -> new CourseValidationException("categoryId", "Category không tồn tại"));

        course.setTitle(courseCreateDto.getTitle());
        course.setDescription(courseCreateDto.getDescription());
        course.setCategory(category);
        course.setPrice(courseCreateDto.getPrice());
        course.setThumbnailUrl(thumbnailUrl);
        course.setLevel(courseCreateDto.getLevel());
        course.setUpdateAt(LocalDateTime.now());
        return repository.save(course);
    }


    ///getCourseForEdit
    public CourseCreateDto getCourseForEdit(Integer coureId, User user){
        Course course = repository.findById(coureId).orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khoá học có id = " + coureId));

        if(!course.getInstructor().getId().equals(user.getId())){
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa khoá học này");
        }

        CourseCreateDto courseCreateDto = new CourseCreateDto();
        courseCreateDto.setId(course.getId());
        courseCreateDto.setTitle(course.getTitle());
        courseCreateDto.setPrice(course.getPrice());
        courseCreateDto.setThumbnailUrl(course.getThumbnailUrl());
        courseCreateDto.setLevel(course.getLevel());
        courseCreateDto.setCategoryId(course.getCategory() != null ? course.getCategory().getId() : null);

        return courseCreateDto;
    }

    ///Show Chi tiết khoá học
    public CourseRespon getCourseDetailToView(Integer courseId){
        Course course = repository.findDetailById(courseId);
        CourseRespon courseRespon = new CourseRespon();
        courseRespon.setTittle(course.getTitle());
        courseRespon.setId(course.getId());
        courseRespon.setCategory(course.getCategory().getName());
        courseRespon.setDescription(course.getDescription());
        courseRespon.setPrice(course.getPrice());
        courseRespon.setLevel(course.getLevel());
        courseRespon.setCreateAt(course.getCreatedAt());
        courseRespon.setThumnaiUrl(course.getThumbnailUrl());

        List<SectionRespon> sections = course.getSections().
                stream().map(section -> {
                    SectionRespon sr = new SectionRespon();
                    sr.setId(section.getId());
                    sr.setPosition(section.getPosition());
                    sr.setTitle(section.getTitle());
                    sr.setCreateAt(section.getCreatedAt());

                    List<LessonRespon> lessons = section.getLessons().
                            stream().map(lesson -> {
                                LessonRespon lr = new LessonRespon();
                                lr.setId(lesson.getId());
                                lr.setPosition(lesson.getPosition());
                                lr.setTitle(lesson.getTitle());
                                lr.setCreateAt(lesson.getCreatedAt());
                                lr.setVideoUrl(lesson.getVideoUrl());
                                lr.setDutationSecond(lesson.getDurationSeconds());

                                List<LessonMaterialRespon> materials = lesson.getMaterials().
                                        stream().map(material -> {
                                            LessonMaterialRespon lms = new LessonMaterialRespon();
                                            lms.setId(material.getId());
                                            lms.setFile_name(material.getFileName());
                                            return lms;
                                        }).toList();
                                lr.setMaterials(materials);
                                return lr;
                            }).toList();
                    sr.setLessons(lessons);
                    return sr;
                }).toList();

        courseRespon.setSections(sections);

        return courseRespon;
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
            List<Double> ratings,
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

            // Lọc theo số sao đánh giá (từ X sao trở lên)
            if (ratings != null && !ratings.isEmpty()) {
                boolean matchRating = false;
                double avgRating = course.getAverageRating();
                for (Double r : ratings) {
                    if (avgRating >= r) {
                        matchRating = true;
                        break;
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
        Course course = repository.findByIdWithDetails(id)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
        return dtoMapper.toCourseDto(course);
    }

    public Page<CourseListDto> getPagedCoursesSummary(
            String search,
            Integer categoryId,
            List<Double> ratings,
            List<String> prices,
            String sort,
            int page,
            int size) {
        return repository.getPagedCoursesSummary(search, categoryId, ratings, prices, sort, page, size);
    }

    public List<Course> findAll() {
        return repository.findAll();
    }

    public Course findByIdWithSectionsAndLessons(Integer id) {
        return repository.findByIdWithSectionsAndLessons(id)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
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
    public Course save(User instructor, String title, String shortdesc, String desc, String outcome, String requirement,
            CourseLevel level, Integer categoryId, MultipartFile file, BigDecimal price) {
        if (instructor == null) {
            throw new RuntimeException("User do not have");
        }
        if (title == null || title.isEmpty()) {
            throw new RuntimeException("Title can not null. Please try again");
        }
        if (shortdesc == null || shortdesc.isEmpty()) {
            throw new RuntimeException("Short can not null. Please try again");
        }
        if (desc == null || desc.isEmpty()) {
            throw new RuntimeException("Description can not null. Please try again");
        }
        if (outcome == null || outcome.isEmpty()) {
            throw new RuntimeException(("Outcome can not null. Please try again"));
        }
        if (requirement == null || requirement.isEmpty()) {
            throw new RuntimeException("Requirement can not null. Please try again");
        }

        if (level == null) {
            throw new RuntimeException(("Level can not null. Please try again"));
        }

        Category category = categoryService.findByIdAndStatus(categoryId, "ACTIVE");
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File can not null. Please try again");
        }
        if (price == null) {
            throw new RuntimeException("price can not null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
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

    public List<Course> findByInstructorAndStatus(User user, CourseStatus status) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (status == null) {
            throw new RuntimeException("Status can not null");
        }
        return repository.findByInstructorAndStatus(user, status);
    }

    public Course findByCourseIdAndUserId(Integer courseId, Integer userId) {
        return repository.findByCourseIdAndUserId(courseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng chưa mua khóa học này"));
    }



    @Transactional
    public void resubmitCourse(Integer id) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
        if (course.getStatus() == CourseStatus.REJECTED) {
            course.setStatus(CourseStatus.PENDING);
            repository.save(course);
        }
    }

    @Transactional
    public void submitForApproval(Integer id) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
        if (course.getStatus() == CourseStatus.DRAFT) {
            course.setStatus(CourseStatus.PENDING);
            repository.save(course);
        }
    }

    @Transactional
    public void withdrawCourse(Integer id) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
        if (course.getStatus() == CourseStatus.PENDING) {
            course.setStatus(CourseStatus.DRAFT);
            repository.save(course);
        }
    }
    public HomeDto getHomeData(User currentUser) {
        long totalCourses = repository.countByStatus(CourseStatus.PUBLISHED);
        long totalInstructors = userRepository.countInstructors();
        long totalLearners = userRepository.countLearners();

        long totalFeedbacks = feedbackRepository.count();
        int ratingPercent = 98; // Mặc định là 98% nếu chưa có đánh giá nào trong DB
        if (totalFeedbacks > 0) {
            long highRatingFeedbacks = feedbackRepository.countByRatingGreaterThanEqual(4);
            ratingPercent = (int) Math.round((double) highRatingFeedbacks / totalFeedbacks * 100);
        }

        HomeDto.HomeDtoBuilder builder = HomeDto.builder()
                .totalCourses(totalCourses)
                .totalInstructors(totalInstructors)
                .totalLearners(totalLearners)
                .fiveStarRatingPercent(ratingPercent);

        if (currentUser == null) {
            return builder.hasFavorites(false).build();
        }

        User user = userRepository.findById(currentUser.getId()).orElse(currentUser);
        if (!user.isFavoriteSetupCompleted()) {
            return builder.hasFavorites(false).build();
        }

        Set<Category> favorites = user.getFavoriteCategories();
        if (favorites == null || favorites.isEmpty()) {
            return builder.hasFavorites(false).build();
        }

        // Lấy danh mục con yêu thích (dùng for thay cho stream)
        List<CategoryDto> favoriteChildren = new java.util.ArrayList<>();
        for (Category c : favorites) {
            if ("ACTIVE".equals(c.getStatus()) && c.getParent() != null) {
                favoriteChildren.add(dtoMapper.toCategoryDto(c));
            }
        }

        if (favoriteChildren.isEmpty()) {
            return builder.hasFavorites(false).build();
        }

        // Lấy danh mục cha của các danh mục con yêu thích (dùng for thay cho stream)
        Category parent = null;
        for (Category c : favorites) {
            if (c.getParent() != null) {
                parent = c.getParent();
                break;
            }
        }

        if (parent == null) {
            return builder.hasFavorites(false).build();
        }

        CategoryDto parentCategoryDto = dtoMapper.toCategoryDto(parent);

        Map<Integer, List<CourseListDto>> coursesMap = new HashMap<>();

        // 1. Lấy top 4 khóa học cho từng danh mục con
        for (CategoryDto child : favoriteChildren) {
            List<CourseListDto> top4 = repository.findTop4ByCategoryIdsOrderByAverageRatingDesc(
                    java.util.Collections.singletonList(child.getId()), 
                    PageRequest.of(0, 4)
            );
            coursesMap.put(child.getId(), top4);
        }

        // 2. Lấy top 4 khóa học cho tab "Tất cả" (tổng hợp các danh mục con yêu thích) (dùng for thay cho stream)
        List<Integer> childIds = new java.util.ArrayList<>();
        for (CategoryDto child : favoriteChildren) {
            childIds.add(child.getId());
        }
        List<CourseListDto> allFavorites = repository.findTop4ByCategoryIdsOrderByAverageRatingDesc(
                childIds, 
                PageRequest.of(0, 4)
        );
        coursesMap.put(0, allFavorites);

        return builder
                .hasFavorites(true)
                .parentCategory(parentCategoryDto)
                .favoriteChildren(favoriteChildren)
                .coursesMap(coursesMap)
                .build();
    }

    @Transactional
    public CourseContentSidebarDTO viewCourseContent(User user, Integer courseId, Integer sectionId, Integer lessonId) {
        CourseContentSidebarDTO courseContentSidebarDTO = new CourseContentSidebarDTO();

        Course course = repository.findByCourseIdAndUserId(courseId, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Người dùng chưa mua khóa học này hoặc khóa học không tồn tại trong hệ thống!"));

        List<SectionSiderbarDTO> sectionSiderbarDTOS = courseSectionRepository.findSectionSiderbarDTOByCourseId(courseId);

        Set<Integer> lessonIdCompleted = lessonProgressRepository.findByUserIdAndCourseId(user.getId(), courseId);

        int totalLesson = 0;
        String currentLessonTitle = "";

        for (SectionSiderbarDTO dto : sectionSiderbarDTOS) {
            List<LessonSiderbarDTO> lessonSiderbarDTOS = lessonRepository.findLessonBySecionId(dto.getId());
            totalLesson += lessonSiderbarDTOS.size();
            boolean sectionCompleted = true;
            for (LessonSiderbarDTO lessonSiderbarDTO : lessonSiderbarDTOS) {
                if (lessonSiderbarDTO.getId().equals(lessonId)) {
                    lessonSiderbarDTO.setCurrentLesson(true);
                    currentLessonTitle = lessonSiderbarDTO.getTitle();
                }
                if (lessonIdCompleted.contains(lessonSiderbarDTO.getId())) {
                    lessonSiderbarDTO.setCompleted(true);
                } else  {
                    sectionCompleted = false;
                }
            }
            dto.setCompleted(sectionCompleted);
            dto.setTotalLessonBySection(lessonSiderbarDTOS.size());
            dto.setLessons(lessonSiderbarDTOS);
        }


        courseContentSidebarDTO.setSections(sectionSiderbarDTOS);
        courseContentSidebarDTO.setTotalLesson(totalLesson);
        courseContentSidebarDTO.setCompletedLesson(lessonIdCompleted.size());
        courseContentSidebarDTO.setCurrentLessonId(lessonId);
        courseContentSidebarDTO.setCurrentLessonTitle(currentLessonTitle);
        courseContentSidebarDTO.setCourseId(courseId);
        courseContentSidebarDTO.setThumbanailURL(AppConstants.AZURE_STORAGE_BASE_URL + "/" + AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS + "/" + course.getThumbnailUrl());

        Lesson nextLesson = lessonService.findNextLessonByCurrentLesson(lessonId, courseId ,totalLesson, lessonIdCompleted.size());
        if (nextLesson != null) {
            courseContentSidebarDTO.setNextLessonId(nextLesson.getId());
            courseContentSidebarDTO.setNextLessonTitle(nextLesson.getTitle());
            if (nextLesson.getCourseSection() != null) {
                courseContentSidebarDTO.setNextSectionId(nextLesson.getCourseSection().getId());
            }
        }

        double progressVal = 0.0;
        if (totalLesson != 0 && totalLesson > 0 && lessonIdCompleted != null) {
            progressVal = ((double) lessonIdCompleted.size() * 100.0) / totalLesson;
        }
        courseContentSidebarDTO.setProgressPercent(progressVal);

        Lesson currentLesson = lessonService.findByIdWithMaterials(lessonId);
        if (currentLesson != null) {
            courseContentSidebarDTO.setLesson(dtoMapper.toLessonDto(currentLesson));
            List<LessonMaterialDto> materialDtos = new ArrayList<>();
            if (currentLesson.getMaterials() != null) {
                for (LessonMaterial m : currentLesson.getMaterials()) {
                    materialDtos.add(dtoMapper.toLessonMaterialDto(m));
                }
            }
            courseContentSidebarDTO.setMaterials(materialDtos);
        }

        courseContentSidebarDTO.setCourseDescription(course.getDescription());
        courseContentSidebarDTO.setLessonProgressStatus(lessonIdCompleted.contains(lessonId));
        courseContentSidebarDTO.setCurrentSectionId(sectionId);

        boolean showReviewPrompt = false;
        if (progressVal >= AppConstants.PERCENT_COMPLETED_LESSON_TO_COMMENT) {
            boolean hasReviewed = feedbackService.hasUserReviewedCourse(user.getId(), courseId);
            if (!hasReviewed) {
                showReviewPrompt = true;
            }
        }

        courseContentSidebarDTO.setShowReviewPrompt(showReviewPrompt);

        // tải ghi chú của tôi
        List<LessonNoteSiderbarDTO> lessonNoteSiderbarDTOS = lessonNoteService.findLessonNoteByUserIdAndLessonId(user.getId(), lessonId);

        courseContentSidebarDTO.setLessonNoteSiderbarDTOS(lessonNoteSiderbarDTOS);
        return courseContentSidebarDTO;
    }

    public boolean canUserReviewCourse(User user, Integer courseId) {
        if (user == null || courseId == null) {
            return false;
        }
        Optional<Course> courseOpt = repository.findByCourseIdAndUserId(courseId, user.getId());
        if (courseOpt.isEmpty()) {
            return false;
        }
        List<SectionSiderbarDTO> sectionSiderbarDTOS = courseSectionRepository.findSectionSiderbarDTOByCourseId(courseId);
        Set<Integer> lessonIdCompleted = lessonProgressRepository.findByUserIdAndCourseId(user.getId(), courseId);
        int totalLesson = 0;
        for (SectionSiderbarDTO dto : sectionSiderbarDTOS) {
            List<LessonSiderbarDTO> lessonSiderbarDTOS = lessonRepository.findLessonBySecionId(dto.getId());
            totalLesson += lessonSiderbarDTOS.size();
        }
        double progressVal = 0.0;
        if (totalLesson > 0 && lessonIdCompleted != null) {
            progressVal = ((double) lessonIdCompleted.size() * 100.0) / totalLesson;
        }
        return progressVal >= AppConstants.PERCENT_COMPLETED_LESSON_TO_COMMENT;
    }

    public String addCourseReview(User user, Integer courseId, Integer rating, String comment) {
        if (feedbackService.hasUserReviewedCourse(user.getId(), courseId)) {
            return "Bạn đã gửi đánh giá cho khóa học này trước đó rồi!";
        }

        if (!canUserReviewCourse(user, courseId)) {
            return "Bạn cần hoàn thành ít nhất 30% tiến trình bài học để đánh giá khóa học này!";
        }

        Course course = repository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học"));

        Feedback feedback = Feedback.builder()
                .user(user)
                .course(course)
                .rating(rating)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();

        feedbackRepository.save(feedback);
        return null;
    }
}

