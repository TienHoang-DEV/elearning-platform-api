package vn.edu.fpt.enums;

public enum ReportStatus {
    PENDING("Chờ xử lý"),
    RESOLVED("Đã xử lý"),
    REJECTED("Từ chối");

    private final String label;

    ReportStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
