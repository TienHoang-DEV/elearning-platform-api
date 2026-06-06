package vn.edu.fpt.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.edu.fpt.entity.Cart;
import vn.edu.fpt.entity.User;
import vn.edu.fpt.repository.UserRepository;
import vn.edu.fpt.service.CartItemService;
import vn.edu.fpt.service.CartService;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserRepository userRepository;

    public GlobalControllerAdvice(CartService cartService, CartItemService cartItemService, UserRepository userRepository) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
        this.userRepository = userRepository;
    }

    @ModelAttribute("cartSize")
    public int getCartSize(HttpServletRequest request) {
        try {
            User user = vn.edu.fpt.util.SecurityUtils.getCurrentUser();
            if (user == null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    User sessionUser = (User) session.getAttribute("user");
                    if (sessionUser != null) {
                        user = userRepository.findById(sessionUser.getId()).orElse(null);
                    }
                }
            }
            if (user == null) {
                user = userRepository.findByEmail("28tech@gmail.com").orElse(null);
            }
            if (user != null) {
                Cart cart = cartService.getOrCreateCartForUser(user);
                return cartItemService.countItemsInCart(cart);
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}
