/**
 * Shared Add-to-Cart logic for Learning Hub
 */
document.addEventListener("DOMContentLoaded", () => {
    // Inject CSS style for custom toast dynamically if not already present
    if (!document.getElementById("custom-toast-style")) {
        const style = document.createElement("style");
        style.id = "custom-toast-style";
        style.textContent = `
            .custom-toast {
                background-color: #d1e7dd; 
                border: 1px solid #a3cfbb; 
                border-left: 5px solid #0f5132;
                border-radius: 8px;
                width: 350px;
                opacity: 0;
                transform: translateY(20px) scale(0.95);
                visibility: hidden;
                transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
            }
            .custom-toast.show {
                opacity: 1;
                transform: translateY(0) scale(1);
                visibility: visible;
            }
            .toast-success-icon {
                color: #0f5132; 
                width: 24px; 
                height: 24px;
            }
            .custom-toast .toast-title {
                color: #0f5132;
            }
            .custom-toast .toast-message {
                color: #0f5132; 
                font-weight: 500;
            }
            #toast-close-btn {
                filter: invert(20%) sepia(20%) saturate(1000%) hue-rotate(90deg) brightness(30%);
            }
        `;
        document.head.appendChild(style);
    }

    // Set up click listeners for any button with class .btn-cart
    // (For pages that don't use inline th:onclick but use class selectors)
    const cartButtons = document.querySelectorAll(".btn-cart");
    cartButtons.forEach(btn => {
        if (!btn.dataset.listenerAttached) {
            btn.dataset.listenerAttached = "true";
            btn.addEventListener("click", (e) => {
                e.preventDefault();
                e.stopPropagation();
                const courseId = btn.getAttribute("data-course-id");
                if (courseId) {
                    addToCart(btn, courseId);
                }
            });
        }
    });
});

/**
 * Main addToCart function called by either onClick attributes or event listeners
 * @param {HTMLButtonElement} btn 
 * @param {string|number} courseId 
 */
function addToCart(btn, courseId) {
    if (!courseId) return;

    btn.disabled = true;

    fetch(`/api/cart/add?courseId=${courseId}`, {
        method: 'POST'
    })
    .then(response => response.json())
    .then(data => {
        btn.disabled = false;
        if (data.success) {
            // Update all potential cart badges on the page
            const cartBadges = document.querySelectorAll(".cart-count-badge, #cart-badge-count");
            cartBadges.forEach(badge => {
                badge.textContent = data.cartSize;
                badge.classList.remove("d-none");
                
                // Visual bounce animation
                badge.style.transform = 'scale(1.4)';
                setTimeout(() => {
                    badge.style.transform = '';
                }, 300);
            });

            // Get course title from parent elements
            const card = btn.closest(".card, .course-list-item, .course-detail-container");
            let title = "Khóa học";
            if (card) {
                const titleEl = card.querySelector(".course-card-title, .course-item-title, .course-detail-title, h1, h2, h3");
                if (titleEl) {
                    title = titleEl.textContent.trim();
                }
            }

            let messageText = "";
            if (data.message && data.message.includes("có sẵn")) {
                messageText = data.message;
            } else {
                messageText = `Đã thêm khóa học "${title}" vào giỏ hàng thành công!`;
            }

            // Display Toast
            showToastMessage(messageText);

        } else {
            alert(data.message || 'Không thể thêm vào giỏ hàng.');
        }
    })
    .catch(err => {
        btn.disabled = false;
        console.error('Error adding course to cart:', err);
        alert('Có lỗi xảy ra khi thêm vào giỏ hàng.');
    });
}

/**
 * Shows the custom toast message. Uses existing markup if present, otherwise creates one dynamically.
 * @param {string} message 
 */
function showToastMessage(message) {
    let toast = document.getElementById("cart-toast") || document.getElementById("cartToast");
    let toastMsgText = document.getElementById("toast-message-text") || document.querySelector("#cartToast .toast-message") || document.querySelector(".toast-message");

    // If using bootstrap's native toast in detail.html (marked by id "cartToast" and has bootstrap classes)
    if (toast && toast.classList.contains("toast") && !toast.classList.contains("custom-toast") && typeof bootstrap !== "undefined") {
        if (toastMsgText) {
            toastMsgText.textContent = message;
        }
        const bsToast = new bootstrap.Toast(toast, { delay: 3000 });
        bsToast.show();
        return;
    }

    // Otherwise use our custom springy green toast
    if (!toast) {
        let toastContainer = document.querySelector(".toast-container");
        if (!toastContainer) {
            toastContainer = document.createElement("div");
            toastContainer.className = "toast-container position-fixed bottom-0 end-0 p-3";
            toastContainer.style.zIndex = "1100";
            document.body.appendChild(toastContainer);
        }

        toast = document.createElement("div");
        toast.id = "cart-toast";
        toast.className = "custom-toast shadow-lg";
        toast.innerHTML = `
            <div class="toast-body d-flex align-items-center gap-3 p-3 rounded-3">
                <i data-lucide="check-circle" class="toast-success-icon"></i>
                <div class="toast-content flex-grow-1">
                    <p class="toast-title fw-bold m-0 fs-7">Thành công!</p>
                    <p id="toast-message-text" class="toast-message m-0 fs-8">Thông báo</p>
                </div>
                <button type="button" class="btn-close btn-close-sm shadow-none" id="toast-close-btn" aria-label="Close"></button>
            </div>
        `;
        toastContainer.appendChild(toast);

        // Bind click event to close button
        const closeBtn = toast.querySelector("#toast-close-btn");
        if (closeBtn) {
            closeBtn.addEventListener("click", () => {
                toast.classList.remove("show");
            });
        }
        
        toastMsgText = toast.querySelector("#toast-message-text");

        // Initialize Lucide icons inside new toast
        if (typeof lucide !== 'undefined') {
            lucide.createIcons({ node: toast });
        }
    }

    if (toastMsgText) {
        toastMsgText.textContent = message;
    }

    // Trigger CSS show class with small timeout
    setTimeout(() => {
        toast.classList.add("show");
    }, 50);

    // Auto-hide timeout
    if (window.toastTimeout) {
        clearTimeout(window.toastTimeout);
    }
    window.toastTimeout = setTimeout(() => {
        toast.classList.remove("show");
    }, 3000);
}
