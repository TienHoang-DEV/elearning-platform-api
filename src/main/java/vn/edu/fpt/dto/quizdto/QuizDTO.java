package vn.edu.fpt.dto.quizdto;

import lombok.*;
import vn.edu.fpt.entity.QuizAttempt;
import vn.edu.fpt.entity.QuizQuestion;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDTO {

    private Integer id;
    private String title;
    private Integer passScore;
    private Set<QuizQuestion> questions;
    private Set<QuizAttempt> attempts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
