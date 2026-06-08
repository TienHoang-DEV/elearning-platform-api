package vn.edu.fpt.dto.quizdto;

import lombok.*;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;

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
    @Builder.Default
    private List<QuizAnswerDTO> answers = new ArrayList<>();
    private Integer id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
