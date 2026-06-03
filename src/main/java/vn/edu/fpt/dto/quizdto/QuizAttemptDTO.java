package vn.edu.fpt.dto.quizdto;

import lombok.*;
import vn.edu.fpt.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttemptDTO {
    private User user;
    private BigDecimal score;
    private Boolean passed;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Integer id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
