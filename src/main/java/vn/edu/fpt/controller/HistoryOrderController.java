package vn.edu.fpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import vn.edu.fpt.dto.user.StudentPurchaseHistoryDto;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.HistoryOrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HistoryOrderController {

    private final UserRepository userRepository;
    private final HistoryOrderService historyOrderService;

    public HistoryOrderController(UserRepository userRepository, HistoryOrderService historyOrderService) {
        this.userRepository = userRepository;
        this.historyOrderService = historyOrderService;
    }

    private User getSessionUser() {
        try {
            User currentUser = vn.edu.fpt.util.SecurityUtils.getCurrentUser();
            if (currentUser != null) {
                return userRepository.findById(currentUser.getId()).orElse(currentUser);
            }
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            HttpSession session = request.getSession(false);
            if (session != null) {
                User sessionUser = (User) session.getAttribute("user");
                if (sessionUser != null) {
                    return userRepository.findById(sessionUser.getId()).orElse(sessionUser);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @GetMapping("/student/purchase-history")
    public String showPurchaseHistory(Model model) {
        User user = getSessionUser();
        if (user == null) {
            return "redirect:/login_no";
        }
        
        StudentPurchaseHistoryDto purchaseHistoryData = historyOrderService.getPurchaseHistoryData(user);
        
        model.addAttribute("currentUser", purchaseHistoryData.getCurrentUser());
        model.addAttribute("orders", purchaseHistoryData.getOrders());
        
        return "purchase_history/purchase_history";
    }
}
