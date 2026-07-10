function openGrantModal(button) {
    const userId = button.getAttribute('data-user-id');
    const userName = button.getAttribute('data-user-name');
    const userEmail = button.getAttribute('data-user-email');
    
    document.getElementById('modalUserId').value = userId;
    document.getElementById('modalUserName').textContent = userName;
    document.getElementById('modalUserEmail').textContent = userEmail;

    // Reset custom course dropdown
    const searchInput = document.getElementById('courseSearchInput');
    const hiddenInput = document.getElementById('modalCourseSelect');
    const optionsContainer = document.getElementById('courseDropdownOptions');
    
    searchInput.value = '';
    hiddenInput.value = '';
    optionsContainer.querySelectorAll('.option-item').forEach(el => {
        el.classList.remove('selected');
        el.style.display = 'block';
    });
    const noResult = optionsContainer.querySelector('.no-result');
    if (noResult) noResult.remove();
    
    const modal = new bootstrap.Modal(document.getElementById('grantAccessModal'));
    modal.show();
}

const form = document.querySelector("#formSubmit");
const button = document.querySelector("#btnSubmitForm");

if (form && button) {
    button.addEventListener("click", async (e) => {
        e.preventDefault();
        const courseId = document.getElementById('modalCourseSelect').value;
        const reason = document.getElementById('modalReasonSelect').value;
        if (!courseId) {
            showToast("Vui lòng chọn khóa học!", "warning");
            return;
        }
        if (!reason) {
            showToast("Vui lòng chọn lý do cấp quyền!", "warning");
            return;
        }
        button.disabled = true;
        const originalText = button.innerHTML;
        button.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-2"></i>Đang xử lý...';

        try {
            const formData = new FormData(form);
            const sendEmailCheckbox = document.getElementById('modalSendEmail');
            formData.set('sendEmail', sendEmailCheckbox ? sendEmailCheckbox.checked : false);

            const response = await fetch('/manager/grant-access', {
                method: 'POST',
                body: formData
            });
            if (!response.ok) {
                throw new Error("Lỗi hệ thống (HTTP " + response.status + ")");
            }
            const result = await response.json();
            if (result.success) {
                const modalEl = document.getElementById('grantAccessModal');
                const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                if (modal) {
                    modal.hide();
                    const backdrop = document.querySelector('.modal-backdrop');
                    if (backdrop) backdrop.remove();
                }

                showToast("Cấp quyền truy cập khóa học thành công!", "success");
                
                // Reload trang sau 1.5s để cập nhật danh sách
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            } else {
                showToast(result.message || "Đã xảy ra lỗi khi cấp quyền.", "danger");
            }
        } catch (error) {
            showToast(error.message || "Không thể kết nối đến máy chủ.", "danger");
        } finally {
            button.disabled = false;
            button.innerHTML = originalText;
        }
    });
}

function showToast(message, type) {
    const toastEl = document.getElementById('resultToast');
    const toastMessage = document.getElementById('toastMessage');
    
    toastEl.classList.remove('bg-success', 'bg-danger', 'bg-warning');
    
    if (type === 'success') {
        toastEl.classList.add('bg-success');
    } else if (type === 'danger') {
        toastEl.classList.add('bg-danger');
    } else {
        toastEl.classList.add('bg-warning');
    }
    
    toastMessage.textContent = message;
    const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
    toast.show();
}

document.addEventListener('DOMContentLoaded', function () {
    const searchInput = document.getElementById('courseSearchInput');
    const optionsContainer = document.getElementById('courseDropdownOptions');
    const hiddenInput = document.getElementById('modalCourseSelect');
    const wrapper = searchInput.closest('.custom-search-select');
    const optionItems = Array.from(optionsContainer.querySelectorAll('.option-item:not([style*="display: none"])'));
    
    if (searchInput && optionsContainer) {
        searchInput.addEventListener('click', function (e) {
            e.stopPropagation();
            wrapper.classList.add('active');
        });
        searchInput.addEventListener('input', function () {
            const query = searchInput.value.toLowerCase().trim();
            wrapper.classList.add('active');
            let matches = 0;
            const existingNoResult = optionsContainer.querySelector('.no-result');
            if (existingNoResult) existingNoResult.remove();

            optionItems.forEach(item => {
                const text = item.textContent.toLowerCase();
                if (text.includes(query)) {
                    item.style.display = 'block';
                    matches++;
                } else {
                    item.style.display = 'none';
                }
            });

            if (matches === 0) {
                const noResult = document.createElement('div');
                noResult.className = 'option-item no-result';
                noResult.textContent = 'Không tìm thấy khóa học nào phù hợp';
                optionsContainer.appendChild(noResult);
            }
        });
        optionsContainer.addEventListener('click', function (e) {
            const target = e.target;
            if (target.classList.contains('option-item') && !target.classList.contains('no-result')) {
                const value = target.getAttribute('data-value');
                const text = target.textContent;
                optionsContainer.querySelectorAll('.option-item').forEach(el => el.classList.remove('selected'));
                target.classList.add('selected');
                searchInput.value = text;
                hiddenInput.value = value;
                wrapper.classList.remove('active');
            }
        });
        document.addEventListener('click', function (e) {
            if (!wrapper.contains(e.target)) {
                wrapper.classList.remove('active');
                const selectedOption = optionsContainer.querySelector('.option-item.selected');
                if (selectedOption) {
                    searchInput.value = selectedOption.textContent;
                } else {
                    searchInput.value = '';
                    hiddenInput.value = '';
                }
                optionItems.forEach(item => item.style.display = 'block');
                const existingNoResult = optionsContainer.querySelector('.no-result');
                if (existingNoResult) existingNoResult.remove();
            }
        });
    }
});
