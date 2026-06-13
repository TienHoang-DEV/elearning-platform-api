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



// 4. Logic thanh toán (Checkout)
function initializeCheckoutButton() {
    const checkoutBtn = document.getElementById("btn-checkout");
    if (checkoutBtn) {
        checkoutBtn.addEventListener("click", () => {
            checkoutBtn.disabled = true;
            fetch("/api/cart/checkout", { method: "POST" })
                .then(r => r.json())
                .then(data => {
                    if (data.success) {
                        alert(data.message);
                        window.location.reload();
                    } else {
                        checkoutBtn.disabled = false;
                        alert(data.message || "Thanh toán thất bại.");
                    }
                })
                .catch(err => {
                    checkoutBtn.disabled = false;
                    console.error("Checkout error:", err);
                    alert("Có lỗi xảy ra khi xử lý thanh toán.");
                });
        });
    }
}
