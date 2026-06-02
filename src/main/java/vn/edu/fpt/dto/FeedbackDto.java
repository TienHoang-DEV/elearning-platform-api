package vn.edu.fpt.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDto {
    private Integer id;
    private Integer rating;
    private String comment;
    private UserDto user;
    private LocalDateTime createdAt;
}
