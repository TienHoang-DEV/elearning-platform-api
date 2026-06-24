package vn.edu.fpt.enums;

public enum ReportType {
    LESSON("Bài học"),
    FEEDBACK("Đánh giá");

    private final String label;

    ReportType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
