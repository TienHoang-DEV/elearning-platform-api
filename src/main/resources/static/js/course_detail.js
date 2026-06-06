document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Lucide Icons
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    // 2. Lucide Icons re-render when Bootstrap Collapse / Tabs / Modal are triggered
    const modalEl = document.getElementById('videoModal');
    if (modalEl) {
        modalEl.addEventListener('shown.bs.modal', () => {
            if (typeof lucide !== 'undefined') {
                lucide.createIcons();
            }
        });
    }

    // 3. Review form validation
    const reviewForm = document.getElementById('reviewForm');
    if (reviewForm) {
        reviewForm.addEventListener('submit', (e) => {
            const rating = reviewForm.querySelector('input[name="rating"]:checked');
            if (!rating) {
                e.preventDefault();
                alert('Vui lòng chọn số sao đánh giá!');
            }
        });
    }
});
