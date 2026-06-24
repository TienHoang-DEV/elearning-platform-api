package vn.edu.fpt.service.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.edu.fpt.util.AppConstants;

/**
 * Component chứa tất cả các scheduled tasks để đồng bộ hóa thanh toán.
 * 
 * Các tác vụ này chạy ở nền (background) mà không cần tương tác người dùng.
 * Tất cả cấu hình thời gian được lưu trong AppConstants để dễ dàng điều chỉnh mà không cần sửa code.
 * 
 * IMPORTANT - Transaction Handling:
 * - Scheduled task methods KHÔNG có @Transactional (họ là wrappers)
 * - Actual transaction management được làm bởi PaymentSyncService methods
 * - Điều này tránh "Transaction silently rolled back" errors
 * - Mỗi service method có @Transactional riêng để handle transaction independently
 * 
 * Ghi chú quan trọng:
 * - Các task này là non-blocking, không ảnh hưởng đến user experience
 * - Cấu hình @EnableScheduling phải được bật trong main class
 * - Nếu app chạy multi-instance, cần dùng distributed lock (Redis + @SchedulerLock)
 * - Mỗi task có error handling riêng để tránh crash toàn bộ scheduler
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduledTasks {

    private final PaymentSyncService paymentSyncService;

    // ==================== Task 1: Đánh Dấu Hết Hạn ====================

    /**
     * Scheduled Task: Kiểm tra và đánh dấu các giao dịch PENDING đã hết hạn thành EXPIRED.
     * 
     * Chạy: Mỗi {EXPIRED_PAYMENT_CHECK_INTERVAL_SECONDS} giây (mặc định: 120 giây = 2 phút)
     * 
     * Mục đích:
     * - Đảm bảo các giao dịch hết hạn được xử lý sớm ngay cả khi người dùng không quay lại
     * - Cải thiện dữ liệu: tránh tình trạng giao dịch vẫn ở PENDING sau khi hết hạn
     * 
     * Quy trình:
     * 1. Query DB tìm các PENDING payments có expiredAt <= NOW
     * 2. Với mỗi record, update status -> EXPIRED
     * 3. Log số lượng record đã xử lý
     * 
     * Performance:
     * - Query thông minh: chỉ select những record PENDING + hết hạn
     * - Tránh quét cũ: lọc updatedAt > 5 phút trước
     * - Thường xử lý 0-10 records mỗi lần chạy
     * 
     * Error Handling:
     * - Bất kỳ lỗi nào sẽ được log nhưng không crash task scheduler
     * - Task sẽ retry tự động lần chạy tiếp theo
     */
    @Scheduled(fixedRateString = "#{T(vn.edu.fpt.util.AppConstants).EXPIRED_PAYMENT_CHECK_INTERVAL_SECONDS * 1000}")
    public void expirePaymentsByTimeout() {
        try {
            log.debug("[SCHEDULED TASK] Bắt đầu expirePaymentsByTimeout");
            
            // Gọi service để xử lý các giao dịch hết hạn
            int expiredCount = paymentSyncService.expirePaymentsByTimeout();
            
            log.debug("[SCHEDULED TASK] Hoàn tất expirePaymentsByTimeout: {} giao dịch đã expire", expiredCount);
            
        } catch (Exception e) {
            // Bắt toàn bộ exception để tránh crash task scheduler
            log.error("[SCHEDULED TASK] Lỗi trong expirePaymentsByTimeout: {}", e.getMessage(), e);
        }
    }

    // ==================== Task 2: Đồng Bộ Trạng Thái ====================

    /**
     * Scheduled Task: Đồng bộ trạng thái các giao dịch PENDING gần đây từ PayOS.
     * 
     * Chạy: Mỗi {PENDING_SYNC_CHECK_INTERVAL_MINUTES} phút (mặc định: 5 phút)
     * 
     * Mục đích:
     * - Phát hiện giao dịch đã thanh toán nhưng webhook bị miss/fail
     * - Đảm bảo người dùng không mất trạng thái PAID ngay cả khi webhook bị lỗi
     * - Giải quyết vấn đề: "User thanh toán nhưng đóng trang trước khi hệ thống cập nhật"
     * 
     * Quy trình:
     * 1. Query DB tìm PENDING payments trong 30 phút gần đây
     * 2. Chỉ xử lý những giao dịch chưa nhận webhook (webhookReceived = false)
     * 3. Tránh query PayOS quá tần suất: bỏ qua nếu synced < 5 phút trước
     * 4. Query PayOS API để lấy trạng thái mới nhất
     * 5. Cập nhật DB dựa trên trạng thái từ PayOS
     * 
     * Performance:
     * - Scope hạn chế: chỉ check 30 phút gần đây
     * - Query thông minh: lọc PENDING + createdAt > 30 phút + webhookReceived = false
     * - Rate limit: tối đa ~100 queries mỗi task, cách nhau 5 phút
     * - Thường xử lý 0-5 records mỗi lần chạy
     * 
     * Rate Limiting:
     * - PayOS API có giới hạn (tuỳ thuộc vào plan)
     * - Chỉ sync nếu lastSyncedAt IS NULL hoặc lastSyncedAt < NOW - 5 MINUTE
     * - Tránh duplicate calls cho cùng 1 payment
     * 
     * Error Handling:
     * - Lỗi query PayOS được log nhưng không crash
     * - Task tiếp tục xử lý các giao dịch khác
     */
    @Scheduled(fixedRateString = "#{T(vn.edu.fpt.util.AppConstants).PENDING_SYNC_CHECK_INTERVAL_MINUTES * 60 * 1000}")
    public void syncPendingPaymentsFromPayOs() {
        try {
            log.debug("[SCHEDULED TASK] Bắt đầu syncPendingPaymentsFromPayOs");
            
            // Gọi service để sync trạng thái từ PayOS
            int syncedCount = paymentSyncService.syncPendingPaymentsFromPayOs();
            
            log.debug("[SCHEDULED TASK] Hoàn tất syncPendingPaymentsFromPayOs: {} giao dịch đã sync", syncedCount);
            
        } catch (Exception e) {
            // Bắt toàn bộ exception để tránh crash task scheduler
            log.error("[SCHEDULED TASK] Lỗi trong syncPendingPaymentsFromPayOs: {}", e.getMessage(), e);
        }
    }

    // ==================== Task 3: Retry Webhook ====================

    /**
     * Scheduled Task: Retry xử lý webhook cho các giao dịch bị lỗi.
     * 
     * Chạy: Mỗi {FAILED_WEBHOOK_CHECK_INTERVAL_MINUTES} phút (mặc định: 10 phút)
     * 
     * Mục đích:
     * - Phát hiện giao dịch mà webhook bị fail/timeout/hang
     * - Cung cấp cơ chế retry thứ 2 ngoài webhook handler
     * - Tăng khả năng recovery từ lỗi webhook
     * 
     * Quy trình:
     * 1. Query DB tìm PENDING payments có webhookRetryCount < 3
     * 2. Với mỗi record, thử query PayOS để lấy trạng thái (bypass webhook)
     * 3. Tăng webhookRetryCount
     * 4. Nếu sync thành công, cập nhật trạng thái payment
     * 5. Nếu failedCount >= 3, bỏ qua (không retry nữa)
     * 
     * Performance:
     * - Chỉ check 30 phút gần đây (old payments không retry)
     * - Giới hạn 3 lần retry (tránh retry vô hạn)
     * - Thường xử lý 0-2 records mỗi lần chạy
     * 
     * Retry Logic:
     * - Lần 1: Webhook handler gửi request → fail/timeout
     * - Lần 2: Task này query PayOS (sau ~10 phút)
     * - Lần 3: Task này query PayOS lần nữa (sau ~20 phút)
     * - Sau đó: Bỏ qua, cho người dùng manual check hoặc manual retry
     * 
     * Error Handling:
     * - Lỗi không crash task, chỉ log warning
     */
    @Scheduled(fixedRateString = "#{T(vn.edu.fpt.util.AppConstants).FAILED_WEBHOOK_CHECK_INTERVAL_MINUTES * 60 * 1000}")
    public void retryFailedWebhooks() {
        try {
            log.debug("[SCHEDULED TASK] Bắt đầu retryFailedWebhooks");
            
            // Gọi service để retry webhook bị fail
            int retriedCount = paymentSyncService.retryFailedWebhooks();
            
            log.debug("[SCHEDULED TASK] Hoàn tất retryFailedWebhooks: {} giao dịch đã retry", retriedCount);
            
        } catch (Exception e) {
            // Bắt toàn bộ exception để tránh crash task scheduler
            log.error("[SCHEDULED TASK] Lỗi trong retryFailedWebhooks: {}", e.getMessage(), e);
        }
    }

    // ==================== Ghi Chú về Cấu Hình ====================

    /*
     * Cấu hình @EnableScheduling:
     * - Thêm @EnableScheduling vào main class hoặc @Configuration class để bật scheduler
     * - VD: @SpringBootApplication @EnableScheduling
     * 
     * Cấu hình Distributed Lock (Multi-Instance):
     * - Nếu app chạy trên nhiều instances, cần distributed lock để tránh duplicate execution
     * - Sử dụng Redis + @SchedulerLock annotation
     * - Tuỳ chọn: shedlock-provider-redis-springdata / shedlock-provider-jdbc
     * 
     * Cấu hình Thread Pool:
     * - Mặc định: TaskScheduler chạy trên single thread
     * - Để tăng parallelism: cấu hình SchedulingConfigurer
     * - VD: registrar.setPoolSize(5)
     * 
     * Monitoring:
     * - Theo dõi execution time, success/failure rate
     * - Cấu hình alert nếu task fail nhiều lần liên tiếp
     * - Dùng metrics: Micrometer, Prometheus
     */
}
