// Shopping Cart JS Logic (Server-driven AJAX reloads)
document.addEventListener("DOMContentLoaded", () => {
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }
    
    // Khôi phục trạng thái lửng lơ (indeterminate) cho checkbox từ thuộc tính server truyền xuống
    document.querySelectorAll('[data-indeterminate="true"]').forEach(cb => {
        cb.indeterminate = true;
    });
    
    initializeCheckboxes();
    initializeRemoveButtons();
    initializeInstructorVouchers();
    initializeCheckoutButton();
});

// 1. Logic Checkbox (Gửi API thay đổi selected rồi reload trang)
function initializeCheckboxes() {
    const selectAllCb = document.getElementById("select-all-checkout");
    const instCbs = document.querySelectorAll(".instructor-checkbox");
    const itemCbs = document.querySelectorAll(".item-checkbox");

    // A. Chọn tất cả
    if (selectAllCb) {
        selectAllCb.addEventListener("change", (e) => {
            const selected = e.target.checked;
            fetch(`/api/cart/toggle-select-all?selected=${selected}`, { method: 'POST' })
                .then(r => r.json())
                .then(data => {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.message || "Có lỗi xảy ra.");
                    }
                });
        });
    }

    // B. Chọn theo giảng viên
    instCbs.forEach(cb => {
        cb.addEventListener("change", (e) => {
            const instructorId = cb.getAttribute("data-instructor-id");
            const selected = e.target.checked;
            fetch(`/api/cart/toggle-select-instructor?instructorId=${instructorId}&selected=${selected}`, { method: 'POST' })
                .then(r => r.json())
                .then(data => {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.message || "Có lỗi xảy ra.");
                    }
                });
        });
    });

    // C. Chọn từng khóa học
    itemCbs.forEach(cb => {
        cb.addEventListener("change", (e) => {
            const cartItemId = cb.getAttribute("data-id");
            const selected = e.target.checked;
            fetch(`/api/cart/toggle-select?cartItemId=${cartItemId}&selected=${selected}`, { method: 'POST' })
                .then(r => r.json())
                .then(data => {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        alert(data.message || "Có lỗi xảy ra.");
                    }
                });
        });
    });
}

// 2. Logic xóa khóa học
function initializeRemoveButtons() {
    const removeBtns = document.querySelectorAll(".btn-remove-item");
    removeBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            const cartItemId = btn.getAttribute("data-item-id");
            if (!cartItemId) return;
            
            btn.disabled = true;
            fetch(`/api/cart/remove?cartItemId=${cartItemId}`, { method: 'POST' })
                .then(r => r.json())
                .then(data => {
                    if (data.success) {
                        window.location.reload();
                    } else {
                        btn.disabled = false;
                        alert(data.message || "Không thể xóa khóa học.");
                    }
                })
                .catch(err => {
                    btn.disabled = false;
                    console.error('Error removing item:', err);
                    alert('Có lỗi xảy ra khi xóa khóa học.');
                });
        });
    });
}

// 3. Logic áp dụng Voucher giảng viên
function initializeInstructorVouchers() {
    const applyBtns = document.querySelectorAll(".btn-apply-instructor-voucher");
    applyBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            const instructorId = btn.getAttribute("data-instructor-id");
            const input = document.getElementById(`voucher-input-${instructorId}`);
            if (!input) return;
            const code = input.value.trim();
            
            btn.disabled = true;
            fetch(`/api/cart/apply-voucher?instructorId=${instructorId}&code=${encodeURIComponent(code)}`, { method: 'POST' })
                .then(r => r.json())
                .then(data => {
                    btn.disabled = false;
                    if (data.success) {
                        window.location.reload();
                    } else {
                        // Hiển thị lỗi ngay bên dưới khung nhập mã
                        const msgContainer = document.getElementById(`voucher-msg-container-${instructorId}`);
                        const msgEl = document.getElementById(`voucher-msg-${instructorId}`);
                        if (msgContainer && msgEl) {
                            msgContainer.classList.remove("d-none");
                            msgEl.className = "fs-8 fw-medium text-danger";
                            msgEl.textContent = data.message;
                        } else {
                            alert(data.message);
                        }
                    }
                })
                .catch(err => {
                    btn.disabled = false;
                    console.error('Error applying voucher:', err);
                    alert('Có lỗi xảy ra khi áp dụng mã.');
                });
        });
    });
}

// 4. Logic thanh toán (Checkout) - Redirect to Payment Page
function initializeCheckoutButton() {
    const checkoutBtn = document.getElementById("btn-checkout");
    if (checkoutBtn) {
        checkoutBtn.addEventListener("click", async () => {
            checkoutBtn.disabled = true;
            
            try {
                const response = await fetch('/api/payments/checkout', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                if (!response.ok) {
                    const data = await response.json();
                    throw new Error(data.error || 'Có lỗi xảy ra khi tạo đơn hàng.');
                }

                const data = await response.json();
                // Redirect to payment page with payment ID
                window.location.href = `/payment?id=${data.id}`;
            } catch (error) {
                console.error('Checkout error:', error);
                alert(error.message);
                checkoutBtn.disabled = false;
            }
        });
    }
}

