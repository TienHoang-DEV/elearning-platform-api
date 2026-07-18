package vn.edu.fpt.dto.revenueInstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.fpt.enums.CourseStatus;
import vn.edu.fpt.enums.OrderStatus;
import vn.edu.fpt.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
public class RecentOrderDto {
    private Integer orderId;
    private String firstName;
    private String lastName;
    private String studentEmail;
    private Integer courseId;
    private String courseName;
    private String courseThumbnailUrl;
    private String courseCategoryName;
    private String courseDescription;
    private String courseLevel;
    private CourseStatus courseStatus;
    private LocalDateTime purchaseDate;
    private BigDecimal originalPrice;
    private BigDecimal finalPrice;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;

    public RecentOrderDto(Integer orderId,
                          String firstName,
                          String lastName,
                          String studentEmail,
                          Integer courseId,
                          String courseName,
                          String courseThumbnailUrl,
                          String courseCategoryName,
                          String courseDescription,
                          String courseLevel,
                          CourseStatus courseStatus,
                          LocalDateTime purchaseDate,
                          BigDecimal originalPrice,
                          BigDecimal finalPrice,
                          OrderStatus orderStatus,
                          PaymentStatus paymentStatus) {
        this.orderId = orderId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.studentEmail = studentEmail;
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseThumbnailUrl = courseThumbnailUrl;
        this.courseCategoryName = courseCategoryName;
        this.courseDescription = courseDescription;
        this.courseLevel = courseLevel;
        this.courseStatus = courseStatus;
        this.purchaseDate = purchaseDate;
        this.originalPrice = originalPrice;
        this.finalPrice = finalPrice;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
    }

    public String getStudentDisplayName() {
        String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
        return fullName.isEmpty() ? studentEmail : fullName;
    }

    public BigDecimal getDiscount() {
        BigDecimal original = originalPrice == null ? BigDecimal.ZERO : originalPrice;
        BigDecimal paid = finalPrice == null ? BigDecimal.ZERO : finalPrice;
        BigDecimal discount = original.subtract(paid);
        return discount.compareTo(BigDecimal.ZERO) > 0 ? discount : BigDecimal.ZERO;
    }

    public BigDecimal getInstructorRevenue() {
        return finalPrice == null ? BigDecimal.ZERO : finalPrice;
    }

    public String getPaymentStatusText() {
        if (paymentStatus != null) {
            return switch (paymentStatus) {
                case PAID -> "Paid";
                case PENDING -> "Pending";
                case FAILED -> "Failed";
                case CANCELLED -> "Cancelled";
                case EXPIRED -> "Expired";
            };
        }
        if (orderStatus == null) {
            return "Unknown";
        }
        return switch (orderStatus) {
            case PAID, COMPLETED -> "Paid";
            case PENDING -> "Pending";
            case CANCELLED -> "Cancelled";
            case EXPIRED -> "Expired";
        };
    }
}
