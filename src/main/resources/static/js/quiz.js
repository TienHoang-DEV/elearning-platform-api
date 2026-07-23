document.addEventListener("click", function (e) {
    // Handle Quiz Sidebar collapse/expand toggle
    const toggleBtn = e.target.closest("#toggleQuizSidebarBtn, .quiz-toggle-btn");
    if (toggleBtn) {
        const container = toggleBtn.closest(".quiz-container-layout");
        if (container) {
            container.classList.toggle("is-collapsed");
            const isCollapsed = container.classList.contains("is-collapsed");
            toggleBtn.setAttribute("title", isCollapsed ? "Mở rộng danh sách" : "Thu gọn danh sách");
            toggleBtn.innerHTML = isCollapsed ? '&#10095;' : '&#10094;';
        }
        return;
    }

    // Toggle older attempt history
    const toggleAttemptsBtn = e.target.closest(".btn-toggle-attempts");
    if (toggleAttemptsBtn) {
        const list = toggleAttemptsBtn.closest(".quiz-attempt-list");
        if (list) {
            const olderItems = list.querySelectorAll(".older-attempt");
            const isExpanded = toggleAttemptsBtn.classList.contains("expanded");
            const count = toggleAttemptsBtn.getAttribute("data-count");

            olderItems.forEach(item => {
                item.style.display = isExpanded ? "none" : "flex";
            });

            if (isExpanded) {
                toggleAttemptsBtn.classList.remove("expanded");
                toggleAttemptsBtn.innerHTML = `Xem thêm ${parseInt(count) - 1} lần làm bài trước &#10095;`;
            } else {
                toggleAttemptsBtn.classList.add("expanded");
                toggleAttemptsBtn.innerHTML = `Thu gọn lịch sử &#10094;`;
            }
        }
        return;
    }

    // Handle Quiz option item AJAX fetching
    const link = e.target.closest(".quiz-option-link");
    if (!link) return;

    const href = link.getAttribute("href");
    if (href && typeof loadQuizContent === "function") {
        e.preventDefault();
        loadQuizContent(href);
    }
});