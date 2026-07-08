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

    // 4. Load more reviews functionality
    const btnLoadMore = document.getElementById('btn-load-more-reviews');
    if (btnLoadMore) {
        btnLoadMore.addEventListener('click', () => {
            const hiddenReviews = document.querySelectorAll('.feedback-item.d-none');
            const limit = 5;
            let count = 0;

            hiddenReviews.forEach(review => {
                if (count < limit) {
                    review.classList.remove('d-none');
                    count++;
                }
            });

            // Check if there are still any hidden reviews left
            const remainingHidden = document.querySelectorAll('.feedback-item.d-none');
            if (remainingHidden.length === 0) {
                btnLoadMore.style.display = 'none';
            }
        });
    }
});
