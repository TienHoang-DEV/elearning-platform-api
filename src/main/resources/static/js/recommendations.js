// Initialize Lucide Icons
document.addEventListener("DOMContentLoaded", () => {
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
    
    initializeCartButtons();
});

// Shopping Cart Actions
function initializeCartButtons() {
    const cartButtons = document.querySelectorAll(".btn-cart");
    const cartBadge = document.querySelector(".cart-count-badge");
    
    // Get static toast elements from HTML
    const toast = document.getElementById("cart-toast");
    const toastMsgText = document.getElementById("toast-message-text");
    const toastCloseBtn = document.getElementById("toast-close-btn");
    
    if (toastCloseBtn && toast) {
        toastCloseBtn.addEventListener("click", () => {
            toast.classList.remove("show");
        });
    }

    cartButtons.forEach(btn => {
        btn.addEventListener("click", (e) => {
            e.preventDefault();
            e.stopPropagation();
            
            const courseId = btn.getAttribute("data-course-id");
            if (!courseId) {
                console.error("Course ID is missing on the button.");
                return;
            }

            btn.disabled = true;

            fetch(`/api/cart/add?courseId=${courseId}`, {
                method: 'POST'
            })
            .then(response => response.json())
            .then(data => {
                btn.disabled = false;
                if (data.success) {
                    // Update cart count
                    if (cartBadge) {
                        cartBadge.textContent = data.cartSize;
                        cartBadge.classList.remove("d-none");
                        
                        // Visual bounce animation for the badge
                        cartBadge.style.transform = 'scale(1.4)';
                        setTimeout(() => {
                            cartBadge.style.transform = '';
                        }, 300);
                    }

                    // Get course title
                    const card = btn.closest(".card");
                    const title = card ? card.querySelector(".course-card-title").textContent.trim() : "Khóa học";
                    
                    // Trigger custom hybrid toast
                    if (toast && toastMsgText) {
                        if (data.message && data.message.includes("có sẵn")) {
                            toastMsgText.textContent = data.message;
                        } else {
                            toastMsgText.textContent = `Đã thêm khóa học "${title}" vào giỏ hàng thành công!`;
                        }
                        
                        toast.classList.add("show");
                        
                        // Auto-hide after 3 seconds
                        if (window.toastTimeout) {
                            clearTimeout(window.toastTimeout);
                        }
                        window.toastTimeout = setTimeout(() => {
                            toast.classList.remove("show");
                        }, 3000);
                    }
                } else {
                    alert(data.message || 'Không thể thêm vào giỏ hàng.');
                }
            })
            .catch(err => {
                btn.disabled = false;
                console.error('Error adding course to cart:', err);
                alert('Có lỗi xảy ra khi thêm vào giỏ hàng.');
            });
        });
    });
}

