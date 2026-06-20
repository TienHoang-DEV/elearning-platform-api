package vn.edu.fpt.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.fpt.dto.*;
import vn.edu.fpt.dto.quizdto.QuizAnswerDTO;
import vn.edu.fpt.dto.quizdto.QuizAttemptDTO;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.dto.quizdto.QuizQuestionDTO;
import vn.edu.fpt.entity.*;


import java.util.Set;

@Mapper(componentModel = "spring")
public interface DtoMapper {

    @Mapping(target = "avatarUrl", expression = "java(user.getFullAvatarUrl())")
    UserDto toUserDto(User user);

    @Mapping(target = "averageRating", expression = "java(course.getAverageRating())")
    @Mapping(target = "ratingCount", expression = "java(course.getRatingCount())")
    @Mapping(target = "totalLessonsCount", expression = "java(course.getTotalLessonsCount())")
    @Mapping(target = "firstLessonVideoUrl", expression = "java(course.getFirstLessonVideoUrl())")
    @Mapping(target = "firstLessonId", expression = "java(course.getFirstLessonId())")
    @Mapping(target = "thumbnailPath", expression = "java(course.getThumbnailPath())")
    CourseDto toCourseDto(Course course);


    @Mapping(target = "courseCount", expression = "java(category.getCourses() != null ? category.getCourses().size() : 0)")
    @Mapping(
            target = "parentId",
            expression = "java(category.getParent() != null ? category.getParent().getId() : null)"
    )
    CategoryDto toCategoryDto(Category category);

    QuizDTO toQuizDto(Quiz quiz);

    Set<QuizDTO> toQuizDtos(Set<Quiz> quizzes);

    QuizQuestionDTO toQuizQuestionDto(QuizQuestion quizQuestion);

    QuizAnswerDTO toQuizAnswerDto(QuizAnswer quizAnswer);

    QuizAttemptDTO toQuizAttemptDto(QuizAttempt quizAttempt);

    CourseSectionDto toCourseSectionDto(CourseSection section);
    LessonDto toLessonDto(Lesson lesson);
    FeedbackDto toFeedbackDto(Feedback feedback);
    EnrollmentDto toEnrollmentDto(Enrollment enrollment);

    CartDto toCartDto(Cart cart);

    CartItemDto toCartItemDto(CartItem cartItem);
    OrderDto toOrderDto(Order order);
    OrderItemDto toOrderItemDto(OrderItem orderItem);
    LessonMaterialDto toLessonMaterialDto(LessonMaterial lessonMaterial);
}