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

    public static final Double PLATFORM_FEE = 0.3D;

    public static final String PAYMENT_GATEWAY = "PAYOS";

    public static final String QR_CODE_BASE_URL = "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=";

    public static final Map<String, String> BANK_NAMES = Map.of(
            "970422", "MB Bank",
            "970436", "Vietcombank",
            "970418", "BIDV",
            "970415", "VietinBank",
            "970407", "Techcombank"
    );

    public static final int NUMBER_PAYMENT_RECORD_PER_PAGE = 6;
    public static final int NUMBER_PAGE_PER_BLOCK = 5;

    /**
     * Interval (in seconds) for checking and expiring PENDING payments that have passed their expiredAt time.
     * Runs frequently to quickly mark expired payments.
     * Default: 60-120 seconds (1-2 minutes)
     */
    public static final long EXPIRED_PAYMENT_CHECK_INTERVAL_SECONDS = 120;

    /**
     * Interval (in minutes) for syncing PENDING payments with PayOS.
     * Queries PayOS directly to check if payment status has changed (PENDING -> PAID/FAILED/EXPIRED).
     * This is the fallback mechanism when webhook might have failed.
     * Default: 5-10 minutes
     */
    public static final long PENDING_SYNC_CHECK_INTERVAL_MINUTES = 5;

    /**
     * Maximum age (in minutes) of PENDING payments to sync.
     * Only syncs payments created within the last X minutes to avoid querying very old records.
     * Default: 30 minutes (slightly more than payment expiration)
     */
    public static final long PENDING_PAYMENT_MAX_AGE_MINUTES = 30;

    /**
     * Interval (in minutes) to skip re-syncing the same payment if it was synced recently.
     * Prevents querying PayOS too frequently for the same payment.
     * Default: 5 minutes
     */
    public static final long PAYMENT_LAST_SYNC_SKIP_INTERVAL_MINUTES = 5;

    /**
     * Maximum number of times to retry webhook processing for a failed payment.
     * Default: 3 times
     */
    public static final int PAYMENT_WEBHOOK_RETRY_ATTEMPTS = 3;

    /**
     * Interval (in minutes) to check for failed webhooks and retry them.
     * Default: 10-15 minutes
     */
    public static final long FAILED_WEBHOOK_CHECK_INTERVAL_MINUTES = 10;

    /**
     * Interval (in minutes) to update the lastSyncedAt timestamp during scheduled sync.
     * Prevents the same payment from being synced too frequently.
     * Default: 5 minutes
     */
    public static final long PAYMENT_SYNC_TIMESTAMP_INTERVAL_MINUTES = 5;

    public static final double PERCENT_COMPLETED_LESSON_TO_COMMENT = 30.0;

}

