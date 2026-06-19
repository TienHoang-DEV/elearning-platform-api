package vn.edu.fpt.util;

import java.util.Map;

public final class AppConstants {

    private AppConstants() {
    }

    public static final long SAS_EXPIRATION_HOURS = 1L;
    public static final String AZURE_STORAGE_BASE_URL = "https://elearningstorageswp391.blob.core.windows.net";

    public static final String AZURE_STORAGE_CONTAINER_LOGS = "logs";
    public static final String AZURE_STORAGE_CONTAINER_COURSE_THUMBNAILS = "course-thumbnails";
    public static final String AZURE_STORAGE_CONTAINER_INSTRUCTOR_CVS = "instructor-cvs";
    public static final String AZURE_STORAGE_CONTAINER_MATERIALS = "materials";
    public static final String AZURE_STORAGE_CONTAINER_USER_AVATARS = "user-avatars";
    public static final String AZURE_STORAGE_CONTAINER_VIDEOS = "videos";

    public static final String OFFICE_VIEWER_BASE_URL = "https://view.officeapps.live.com/op/embed.aspx?src=";

    public static final Float DEFAULT_DISCOUNT = 0.3F;

    public static final int PAYMENT_EXPIRATION_MINUTES = 15;

    public static final String PAYMENT_GATEWAY = "PAYOS";

    public static final Map<String, String> BANK_NAMES = Map.of(
            "970422", "MB Bank",
            "970436", "Vietcombank",
            "970418", "BIDV",
            "970415", "VietinBank",
            "970407", "Techcombank"
    );

}

