package vn.edu.fpt.dto.quizdto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDTO {

    private Integer id;
    private String title;
    private Double passScorePercent;
    @Builder.Default
    private List<QuizQuestionDTO> questions = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Integer getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }

    public Integer getTotalPoints() {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }
        int totalPoints = 0;
        for (QuizQuestionDTO question : questions) {
            if (question == null || question.getPoints() == null) {
                continue;
            }
            totalPoints += question.getPoints();
        }
        return totalPoints;
    }
}
