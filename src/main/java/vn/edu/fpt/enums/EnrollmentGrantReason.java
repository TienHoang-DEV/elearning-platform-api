package vn.edu.fpt.enums;

import lombok.Getter;

@Getter
public enum EnrollmentGrantReason {

    PAYMENT_RECOVERY("Khôi phục sau thanh toán"),

    SUPPORT_REQUEST("Yêu cầu hỗ trợ"),

    GIFT("Tặng khóa học"),

    MIGRATION("Chuyển dữ liệu từ hệ thống cũ"),

    COMPENSATION("Đền bù"),

    OTHER("Khác");

    private final String label;

    EnrollmentGrantReason(String label) {
        this.label = label;
    }
}
