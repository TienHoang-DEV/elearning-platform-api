package vn.edu.fpt.dto.quizdto;

import lombok.*;
import vn.edu.fpt.entity.QuizAnswer;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionDTO {
    private String questionText;
    private String questionType;
    private Integer points;
    private Integer position;
    private Set<QuizAnswer> answers = new HashSet<>();
    private Integer id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
