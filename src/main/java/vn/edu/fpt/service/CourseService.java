package vn.edu.fpt.service;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import vn.edu.fpt.dto.*;
import vn.edu.fpt.dto.course.*;
import vn.edu.fpt.dto.home.HomeDto;
import vn.edu.fpt.dto.lesson.LessonNoteSiderbarDTO;
import vn.edu.fpt.dto.lesson.LessonSiderbarDTO;
import vn.edu.fpt.dto.lesson.SectionSiderbarDTO;
import vn.edu.fpt.entity.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.enums.FeedbackStatus;
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


    public Page<CourseDto> findByInstructorAndStatus(User instructor, Pageable pageable, CourseStatus courseStatus) {

        return repository.findByInstructorAndStatus(instructor, pageable, courseStatus).map(dtoMapper::toCourseDto);
    }


    public void deleteCourseById(Integer courseId){
        Course tmp = repository.findCourseById(courseId);
        if(tmp == null) return;
        repository.deleteCourseById(courseId);
    }


    public Course getInstructorOwnedCourse(Integer courseId, User user) {
        Course course = repository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học có id = " + courseId));
        if (user == null || course.getInstructor() == null || !course.getInstructor().getId().equals(user.getId())) {
            throw new AccessDeniedException("Bạn không có quyền thao tác với khóa học này");
        }
        return course;
    }


    public void hidePublishedCourse(Integer courseId, User user) {
        Course course = getInstructorOwnedCourse(courseId, user);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new CourseValidationException("status", "Chỉ khóa học đang bán mới được ẩn.");
        }
        course.setStatus(CourseStatus.HIDDEN);
        course.setUpdateAt(LocalDateTime.now());
        repository.save(course);
    }


    public void publishHiddenCourse(Integer courseId, User user) {
        Course course = getInstructorOwnedCourse(courseId, user);
        if (course.getStatus() != CourseStatus.HIDDEN) {
            throw new CourseValidationException("status", "Chỉ khóa học đã ẩn mới được hiện lại.");
        }
        course.setStatus(CourseStatus.PUBLISHED);
        course.setUpdateAt(LocalDateTime.now());
        repository.save(course);
    }

    public List<Feedback> getInstructorCourseReviews(Integer courseId, User user) {
        getInstructorOwnedCourse(courseId, user);
        return feedbackRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
    }

    public Course save(User user, CourseCreateDto courseCreateDto) {
        Course course;
        boolean isUpdate = courseCreateDto.getId() != null;
        String normalizedTitle = courseCreateDto.getTitle() != null ? courseCreateDto.getTitle().trim() : "";

        if (isUpdate) {
            course = repository.findById(courseCreateDto.getId())
                    .orElseThrow(() -> new CourseNotFoundException("Khóa học không tìm thấy"));
            if (repository.existsDuplicateTitleForInstructor(user.getId(), normalizedTitle, course.getId())) {
                throw new CourseValidationException(
                        "title",
                        "Bạn đã tạo một khóa học với tiêu đề này. Vui lòng sử dụng tiêu đề khác.");
            }
        } else {
            course = new Course();
            if (repository.existsDuplicateTitleForInstructor(user.getId(), normalizedTitle, null)) {
                throw new CourseValidationException(
                        "title",
                        "Bạn đã tạo một khóa học với tiêu đề này. Vui lòng sử dụng tiêu đề khác.");
            }
            course.setCreatedAt(LocalDateTime.now());
            course.setStatus(CourseStatus.DRAFT);
            course.setInstructor(user);
        }

        if (normalizedTitle.length() < 3) {
            throw new CourseValidationException(
                    "title",
                    "Tiêu đề khóa học phải có ít nhất 3 ký tự.");
        }

        String descriptionText = toPlainText(courseCreateDto.getDescription());
        if (descriptionText.length() < 50) {
            throw new CourseValidationException(
                    "description",
                    "Mô tả khóa học phải có tối thiểu 50 ký tự.");
        }

        validateCoursePrice(courseCreateDto.getPrice());

        String thumbnailUrl = course.getThumbnailUrl();
        if (courseCreateDto.getThumbnailFile() != null && !courseCreateDto.getThumbnailFile().isEmpty()) {
            thumbnailUrl = azureBlobService.saveFile(courseCreateDto.getThumbnailFile(), AppConstants.AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS);
        }

        Category category = categoryRepository.findById(courseCreateDto.getCategoryId())
                .orElseThrow(() -> new CourseValidationException("categoryId", "Category không tồn tại"));

        course.setTitle(normalizedTitle);
        course.setDescription(courseCreateDto.getDescription());
        course.setCategory(category);
        course.setPrice(courseCreateDto.getPrice());
        course.setThumbnailUrl(thumbnailUrl);
        course.setLevel(courseCreateDto.getLevel());
        course.setUpdateAt(LocalDateTime.now());
        return repository.save(course);
    }

    private void validateCoursePrice(BigDecimal price) {
        if (price == null) {
            throw new CourseValidationException("price", "Vui lòng nhập giá khóa học.");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new CourseValidationException("price", "Giá khóa học không được âm.");
        }
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        if (price.compareTo(BigDecimal.valueOf(2000)) <= 0) {
            throw new CourseValidationException("price", "Giá khóa học phải lớn hơn 2.000 VNĐ hoặc bằng 0 nếu miễn phí.");
        }
        if (price.remainder(BigDecimal.valueOf(1000)).compareTo(BigDecimal.ZERO) != 0) {
            throw new CourseValidationException("price", "Giá khóa học phải chia hết cho 1.000 VNĐ.");
        }
    }


    private String toPlainText(String html) {
        if (html == null) {
            return "";
        }
        return HtmlUtils.htmlUnescape(html)
                .replaceAll("<[^>]*>", " ")
                .replace('\u00A0', ' ')
                .trim();
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
        courseCreateDto.setDescription(course.getDescription());
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


        for (CourseDto course : allCourses) {

            // Lọc theo danh mục
            if (categoryId != null) {
                if (course.getCategory() == null || !course.getCategory().getId().equals(categoryId)) {
                    continue;
                }
            }


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
                    continue;
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

                        }
                    }
                }
                if (!matchPrice) {
                    continue;
                }
            }

            filteredCourses.add(course);
        }


        filteredCourses.sort((c1, c2) -> {
            if ("rating".equals(sort)) {
                return Double.compare(c2.getAverageRating(), c1.getAverageRating());
            } else if ("price-asc".equals(sort)) {
                double p1 = c1.getPrice() != null ? c1.getPrice().doubleValue() : 0.0;
                double p2 = c2.getPrice() != null ? c2.getPrice().doubleValue() : 0.0;
                return Double.compare(p1, p2);
            } else if ("price-desc".equals(sort)) {
                double p1 = c1.getPrice() != null ? c1.getPrice().doubleValue() : 0.0;
                double p2 = c2.getPrice() != null ? c2.getPrice().doubleValue() : 0.0;
                return Double.compare(p2, p1);
            } else {
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
        int currentPage = Math.max(page, 1);
        Specification<Course> specification = buildCourseListSpecification(search, categoryId, ratings, prices, sort);
        Page<Course> coursePage = repository.findAll(specification, PageRequest.of(currentPage - 1, size));

        if (coursePage.isEmpty() && currentPage > 1 && coursePage.getTotalPages() > 0) {
            coursePage = repository.findAll(specification, PageRequest.of(coursePage.getTotalPages() - 1, size));
        }

        return coursePage.map(this::toCourseListDto);
    }

    private CourseListDto toCourseListDto(Course course) {
        return CourseListDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .level(course.getLevel())
                .instructorFirstName(course.getInstructor() != null ? course.getInstructor().getFirstName() : null)
                .instructorLastName(course.getInstructor() != null ? course.getInstructor().getLastName() : null)
                .categoryId(course.getCategory() != null ? course.getCategory().getId() : null)
                .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                .averageRating(course.getAverageRating())
                .ratingCount((long) course.getRatingCount())
                .totalLessonsCount((long) course.getTotalLessonsCount())
                .enrollmentsCount((long) course.getEnrollmentsCount())
                .build();
    }

    private Specification<Course> buildCourseListSpecification(
            String search,
            Integer categoryId,
            List<Double> ratings,
            List<String> prices,
            String sort) {
        return fetchCourseListSummary()
                .and(courseListFilter(search, categoryId, ratings, prices, sort));
    }

    private Specification<Course> fetchCourseListSummary() {
        return (root, query, cb) -> {
            if (!isCountQuery(query.getResultType())) {
                root.fetch("instructor");
                root.fetch("category");
            }
            return cb.conjunction();
        };
    }

    private Specification<Course> courseListFilter(
            String search,
            Integer categoryId,
            List<Double> ratings,
            List<String> prices,
            String sort) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.<CourseStatus>get("status"), CourseStatus.PUBLISHED));

            if (search != null && !search.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.<String>get("title")), "%" + search.trim().toLowerCase() + "%"));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").<Integer>get("id"), categoryId));
            }

            Double minRating = resolveMinRating(ratings);
            if (minRating != null) {
                predicates.add(cb.ge(averageRatingSubquery(root, query, cb), minRating));
            }

            Predicate pricePredicate = buildPricePredicate(root, cb, prices);
            if (pricePredicate != null) {
                predicates.add(pricePredicate);
            }

            if (!isCountQuery(query.getResultType())) {
                query.orderBy(switch (sort == null ? "" : sort) {
                    case "price-asc" -> cb.asc(root.<BigDecimal>get("price"));
                    case "price-desc" -> cb.desc(root.<BigDecimal>get("price"));
                    case "rating" -> cb.desc(averageRatingSubquery(root, query, cb));
                    default -> cb.desc(root.<Integer>get("id"));
                });
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Double resolveMinRating(List<Double> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return null;
        }
        return ratings.stream()
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(null);
    }

    private Predicate buildPricePredicate(
            Root<Course> root,
            jakarta.persistence.criteria.CriteriaBuilder cb,
            List<String> prices) {
        if (prices == null || prices.isEmpty()) {
            return null;
        }

        List<Predicate> ranges = new ArrayList<>();
        for (String price : prices) {
            if (price == null) {
                continue;
            }
            String[] parts = price.split("-");
            if (parts.length != 2) {
                continue;
            }
            try {
                BigDecimal min = BigDecimal.valueOf(Double.parseDouble(parts[0]));
                BigDecimal max = BigDecimal.valueOf(Double.parseDouble(parts[1]));
                ranges.add(cb.between(root.<BigDecimal>get("price"), min, max));
            } catch (NumberFormatException ignored) {
            }
        }

        return ranges.isEmpty() ? null : cb.or(ranges.toArray(Predicate[]::new));
    }

    private Expression<Double> averageRatingSubquery(
            Root<Course> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder cb) {
        Subquery<Double> subquery = query.subquery(Double.class);
        Root<Feedback> feedbackRoot = subquery.from(Feedback.class);
        subquery.select(cb.coalesce(cb.avg(feedbackRoot.<Integer>get("rating")), 0.0));
        subquery.where(cb.equal(feedbackRoot.get("course").<Integer>get("id"), root.<Integer>get("id")));
        return subquery;
    }

    private boolean isCountQuery(Class<?> resultType) {
        return Long.class.equals(resultType) || long.class.equals(resultType);
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

    public CourseSubmitReviewDto getSubmitReview(Integer courseId, User user, boolean acceptedPolicy) {
        Course course = repository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học có id " + courseId));

        CourseSubmitReviewDto dto = new CourseSubmitReviewDto();
        dto.setCourseId(course.getId());
        dto.setCourseTitle(course.getTitle());
        dto.setCourseStatus(course.getStatus());

        boolean owner = user != null
                && course.getInstructor() != null
                && course.getInstructor().getId().equals(user.getId());
        boolean editableStatus = course.getStatus() == CourseStatus.DRAFT || course.getStatus() == CourseStatus.REJECTED;
        boolean noPendingRequest = course.getStatus() != CourseStatus.PENDING;

        long sectionCount = repository.countSectionsByCourseId(courseId);
        long lessonCount = repository.countLessonsByCourseId(courseId);

        addCheck(dto.getContentChecks(), dto.getMissingMessages(),
                "Tiêu đề khóa học: 10-120 ký tự",
                hasLengthBetween(course.getTitle(), 10, 120),
                "Tiêu đề khóa học phải có từ 10 đến 120 ký tự");
        addCheck(dto.getContentChecks(), dto.getMissingMessages(),
                "Mô tả khóa học: tối thiểu 50 ký tự",
                plainLength(course.getDescription()) >= 50,
                "Mô tả khóa học cần tối thiểu 50 ký tự");
        addCheck(dto.getContentChecks(), dto.getMissingMessages(),
                "Ảnh đại diện khóa học đã tải lên",
                hasText(course.getThumbnailUrl()),
                "Chưa tải ảnh đại diện khóa học");
        addCheck(dto.getContentChecks(), dto.getMissingMessages(),
                "Danh mục, trình độ và giá đã điền",
                course.getCategory() != null && hasText(course.getLevel()) && course.getPrice() != null,
                "Chưa điền đủ danh mục, trình độ hoặc giá khóa học");
        addCheck(dto.getContentChecks(), dto.getMissingMessages(),
                "Có ít nhất 1 section",
                sectionCount > 0,
                "Khóa học cần có ít nhất 1 section");
        addCheck(dto.getContentChecks(), dto.getMissingMessages(),
                "Mỗi section có ít nhất 1 bài học",
                sectionCount > 0 && repository.countSectionsWithoutLessons(courseId) == 0,
                "Mỗi section cần có ít nhất 1 bài học");
        addCheck(dto.getContentChecks(), dto.getMissingMessages(),
                "Có ít nhất 1 video hoặc tài liệu học tập",
                lessonCount > 0 && repository.countLessonsHavingVideoOrMaterial(courseId) > 0,
                "Thiếu video hoặc tài liệu học tập");
        addCheck(dto.getContentChecks(), dto.getMissingMessages(),
                "Có ít nhất 1 quiz / bài tập cuối khóa",
                repository.countQuizzesByCourseId(courseId) > 0,
                "Thiếu quiz / bài tập cuối khóa");

        addCheck(dto.getBusinessChecks(), dto.getMissingMessages(),
                "Người gửi là giảng viên sở hữu khóa học",
                owner,
                "Bạn không phải giảng viên sở hữu khóa học này");
        addCheck(dto.getBusinessChecks(), dto.getMissingMessages(),
                "Khóa học ở trạng thái Draft hoặc Rejected",
                editableStatus,
                "Chỉ khóa học bản nháp hoặc bị từ chối mới được gửi duyệt");
        addCheck(dto.getBusinessChecks(), dto.getMissingMessages(),
                "Không tồn tại request đang chờ duyệt",
                noPendingRequest,
                "Khóa học đang chờ duyệt, không thể gửi thêm request");
        addCheck(dto.getBusinessChecks(), dto.getMissingMessages(),
                "Manager / bộ phận duyệt được gán tự động",
                true,
                null);

        CourseSubmitReviewDto.CheckItem policy = new CourseSubmitReviewDto.CheckItem(
                "Đã tick cam kết bản quyền và chính sách",
                acceptedPolicy,
                true);
        dto.getBusinessChecks().add(policy);
        if (!acceptedPolicy) {
            dto.getMissingMessages().add("Chưa tick cam kết bản quyền và chính sách");
        }

        int total = dto.getContentChecks().size() + dto.getBusinessChecks().size();
        int completed = 0;
        for (CourseSubmitReviewDto.CheckItem item : dto.getContentChecks()) {
            if (item.isPassed()) completed++;
        }
        for (CourseSubmitReviewDto.CheckItem item : dto.getBusinessChecks()) {
            if (item.isPassed()) completed++;
        }

        dto.setTotalCount(total);
        dto.setCompletedCount(completed);
        dto.setPercent(total == 0 ? 0 : (int) Math.round(completed * 100.0 / total));
        dto.setSubmitReady(completed == total);
        return dto;
    }

    @Transactional
    public void submitCourseForApproval(Integer courseId, User user, boolean acceptedPolicy) {
        CourseSubmitReviewDto review = getSubmitReview(courseId, user, acceptedPolicy);
        if (!review.isSubmitReady()) {
            throw new CourseValidationException("submitReview", String.join("; ", review.getMissingMessages()));
        }

        Course course = repository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học có id " + courseId));
        course.setStatus(CourseStatus.PENDING);
        course.setRejectionReason(null);
        course.setApprovedAt(null);
        course.setApprovedBy(null);
        course.setUpdateAt(LocalDateTime.now());
        repository.save(course);
    }

    private void addCheck(List<CourseSubmitReviewDto.CheckItem> checks, List<String> missingMessages,
                          String label, boolean passed, String missingMessage) {
        checks.add(new CourseSubmitReviewDto.CheckItem(label, passed));
        if (!passed && missingMessage != null) {
            missingMessages.add(missingMessage);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean hasLengthBetween(String value, int min, int max) {
        if (!hasText(value)) {
            return false;
        }
        int length = value.trim().length();
        return length >= min && length <= max;
    }

    private int plainLength(String value) {
        if (value == null) {
            return 0;
        }
        return value.replaceAll("<[^>]*>", "").trim().length();
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

    public Feedback addCourseReview(User user, Integer courseId, Integer rating, String comment) {
        if (feedbackService.hasUserReviewedCourse(user.getId(), courseId)) {
            throw new IllegalArgumentException("Bạn đã gửi đánh giá cho khóa học này trước đó rồi!");
        }

        if (!canUserReviewCourse(user, courseId)) {
            throw new IllegalArgumentException("Bạn cần hoàn thành ít nhất 30% tiến trình bài học để đánh giá khóa học này!");
        }

        Course course = repository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("Không tìm thấy khóa học"));

        Feedback feedback = Feedback.builder()
                .user(user)
                .course(course)
                .rating(rating)
                .comment(comment)
                .status(FeedbackStatus.VISIBLE)
                .createdAt(LocalDateTime.now())
                .build();

        return feedbackRepository.save(feedback);
    }

    public List<CourseGrantDTO> findAllCourseGrant() {
        return repository.findAllCourseGrantDTO();
    }

    public List<CourseGrantDTO> findAvailableCoursesForUser(Integer userId) {
        return repository.findAvailableCoursesForUser(userId);
    }
}

