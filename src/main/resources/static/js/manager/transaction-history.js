document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('.filter-search-wrapper form');
    const fromDateInput = form.querySelector('input[name="fromDate"]');
    const toDateInput = form.querySelector('input[name="toDate"]');

    form.addEventListener('submit', function(e) {
        // Reset state
        fromDateInput.classList.remove('is-invalid');
        toDateInput.classList.remove('is-invalid');
        const existingAlert = document.getElementById('dateValidationError');
        if (existingAlert) {
            existingAlert.remove();
        }

        const fromDateVal = fromDateInput.value;
        const toDateVal = toDateInput.value;
        const today = new Date();

        if (fromDateVal) {
            const fromDate = new Date(fromDateVal);

            if (fromDate > today) {
                e.preventDefault();

                fromDateInput.classList.add('is-invalid');

                const alertDiv = document.createElement('div');
                alertDiv.id = 'dateValidationError';
                alertDiv.className = 'alert alert-danger d-flex align-items-center mt-3';
                alertDiv.role = 'alert';
                alertDiv.innerHTML = `
            <i class="ph ph-warning-circle me-2" style="font-size: 1.25rem;"></i>
            <div>"Từ ngày" không được lớn hơn ngày hiện tại.</div>
        `;
                form.parentNode.insertBefore(alertDiv, form);
                alertDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
                return;
            }
        }

        if (fromDateVal && toDateVal) {
            if (new Date(fromDateVal) > new Date(toDateVal)) {
                e.preventDefault();

                fromDateInput.classList.add('is-invalid');
                toDateInput.classList.add('is-invalid');

                const alertDiv = document.createElement('div');
                alertDiv.id = 'dateValidationError';
                alertDiv.className = 'alert alert-danger d-flex align-items-center mt-3';
                alertDiv.role = 'alert';
                alertDiv.innerHTML = `
                        <i class="ph ph-warning-circle me-2" style="font-size: 1.25rem;"></i>
                        <div>"Từ ngày" không được lớn hơn "Đến ngày".</div>
                    `;
                form.parentNode.insertBefore(alertDiv, form);
                alertDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    });
});