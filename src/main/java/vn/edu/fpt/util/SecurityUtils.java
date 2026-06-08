package vn.edu.fpt.util;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.edu.fpt.security.CustomUserDetails;
import vn.edu.fpt.entity.User;
public class SecurityUtils {

    /**
     * Lấy User từ SecurityContext
     * @return User entity hoặc null nếu chưa login
     */
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();

            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUser();
            }

            if (principal instanceof vn.edu.fpt.security.CustomOAuth2User) {
                return ((vn.edu.fpt.security.CustomOAuth2User) principal).getUser();
            }

            if (principal instanceof User) {
                return (User) principal;
            }
        }

        return null;
    }

    /**
     * Kiểm tra user có authenticated không
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }

    /**
     * Lấy User ID
     */
    public static Integer getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Lấy User Email
     */
    public static String getCurrentUserEmail() {
        User user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }
}