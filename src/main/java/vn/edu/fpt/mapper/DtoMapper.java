package vn.edu.fpt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.fpt.dto.*;
import vn.edu.fpt.dto.quizdto.QuizAnswerDTO;
import vn.edu.fpt.dto.quizdto.QuizAttemptDTO;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.dto.quizdto.QuizQuestionDTO;
import vn.edu.fpt.entity.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface DtoMapper {

    @Mapping(target = "avatarUrl", expression = "java(user.getFullAvatarUrl())")
    UserDto toUserDto(User user);

    @Named("toSimpleUserDto")
    @Mapping(target = "avatarUrl", expression = "java(user.getFullAvatarUrl())")
    @Mapping(target = "role", ignore = true)
    UserDto toSimpleUserDto(User user);

    @Mapping(target = "averageRating", expression = "java(course.getAverageRating())")
    @Mapping(target = "ratingCount", expression = "java(course.getRatingCount())")
    @Mapping(target = "totalLessonsCount", expression = "java(course.getTotalLessonsCount())")
    @Mapping(target = "firstLessonVideoUrl", expression = "java(course.getFirstLessonVideoUrl())")
    @Mapping(target = "firstLessonId", expression = "java(course.getFirstLessonId())")
    @Mapping(target = "thumbnailPath", expression = "java(course.getThumbnailPath())")
    @Mapping(target = "rejectionReason", source = "rejectionReason")
    @Mapping(target = "category", qualifiedByName = "toSimpleCategoryDto")
    @Mapping(target = "instructor", qualifiedByName = "toSimpleUserDto")
    CourseDto toCourseDto(Course course);

    @Named("toSimpleCourseDto")
    @Mapping(target = "sections", ignore = true)
    @Mapping(target = "feedbacks", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "totalLessonsCount", ignore = true)
    @Mapping(target = "firstLessonVideoUrl", ignore = true)
    @Mapping(target = "firstLessonId", ignore = true)
    @Mapping(target = "enrollmentsCount", ignore = true)
    @Mapping(target = "category", qualifiedByName = "toSimpleCategoryDto")
    @Mapping(target = "instructor", qualifiedByName = "toSimpleUserDto")
    CourseDto toSimpleCourseDto(Course course);

    @Mapping(target = "courseCount", expression = "java(category.getCourses() != null ? category.getCourses().size() : 0)")
    @Mapping(
            target = "parentId",
            expression = "java(category.getParent() != null ? category.getParent().getId() : null)"
    )
    CategoryDto toCategoryDto(Category category);

    @Named("toSimpleCategoryDto")
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "courseCount", ignore = true)
    @Mapping(target = "parentId", expression = "java(category.getParent() != null ? category.getParent().getId() : null)")
    CategoryDto toSimpleCategoryDto(Category category);

    @Mapping(source = "lesson.id", target = "lessonId")
    QuizDTO toQuizDto(Quiz quiz);

    List<QuizDTO> toQuizDtos(List<Quiz> quizzes);

    QuizQuestionDTO toQuizQuestionDto(QuizQuestion quizQuestion);

    QuizAnswerDTO toQuizAnswerDto(QuizAnswer quizAnswer);

    QuizAttemptDTO toQuizAttemptDto(QuizAttempt quizAttempt);

    CourseSectionDto toCourseSectionDto(CourseSection section);
    LessonDto toLessonDto(Lesson lesson);

    @Mapping(target = "user", qualifiedByName = "toSimpleUserDto")
    FeedbackDto toFeedbackDto(Feedback feedback);

    EnrollmentDto toEnrollmentDto(Enrollment enrollment);

    @Mapping(target = "user", qualifiedByName = "toSimpleUserDto")
    CartDto toCartDto(Cart cart);

    @Mapping(target = "course", qualifiedByName = "toSimpleCourseDto")
    CartItemDto toCartItemDto(CartItem cartItem);

    OrderDto toOrderDto(Order order);
    OrderItemDto toOrderItemDto(OrderItem orderItem);
    LessonMaterialDto toLessonMaterialDto(LessonMaterial lessonMaterial);
}
