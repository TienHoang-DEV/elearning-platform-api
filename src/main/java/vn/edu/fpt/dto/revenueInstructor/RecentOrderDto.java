package vn.edu.fpt.dto.revenueInstructor;

import com.azure.core.annotation.Get;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderDto {
    private String firstName;
    private String lastName;
    private String courseName;
    private BigDecimal amount;
    private LocalDateTime date;

}
