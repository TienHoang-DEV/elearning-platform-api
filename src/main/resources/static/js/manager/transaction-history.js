document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('.filter-search-wrapper form');
    const fromDateInput = form.querySelector('input[name="fromDate"]');
    const toDateInput = form.querySelector('input[name="toDate"]');

    form.addEventListener('submit', function(e) {
        fromDateInput.classList.remove('is-invalid');
        toDateInput.classList.remove('is-invalid');
        const existingAlert = document.getElementById('dateValidationError');
        if (existingAlert) existingAlert.remove();

        const fromDateVal = fromDateInput.value;
        const toDateVal = toDateInput.value;
        const today = new Date();

        if (fromDateVal && new Date(fromDateVal) > today) {
            e.preventDefault();
            fromDateInput.classList.add('is-invalid');
            const alertDiv = document.createElement('div');
            alertDiv.id = 'dateValidationError';
            alertDiv.className = 'alert alert-danger d-flex align-items-center mt-3';
            alertDiv.innerHTML = '<i class="ph ph-warning-circle me-2" style="font-size: 1.25rem;"></i><div>"Từ ngày" không được lớn hơn ngày hiện tại.</div>';
            form.parentNode.insertBefore(alertDiv, form);
            alertDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
            return;
        }

        if (fromDateVal && toDateVal && new Date(fromDateVal) > new Date(toDateVal)) {
            e.preventDefault();
            fromDateInput.classList.add('is-invalid');
            toDateInput.classList.add('is-invalid');
            const alertDiv = document.createElement('div');
            alertDiv.id = 'dateValidationError';
            alertDiv.className = 'alert alert-danger d-flex align-items-center mt-3';
            alertDiv.innerHTML = '<i class="ph ph-warning-circle me-2" style="font-size: 1.25rem;"></i><div>"Từ ngày" không được lớn hơn "Đến ngày".</div>';
            form.parentNode.insertBefore(alertDiv, form);
            alertDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    });

    // Mở modal
    document.querySelectorAll('.btn-action[title="Xem chi tiết"]').forEach(btn => {
        btn.addEventListener('click', e => {
            e.preventDefault();
            const row = btn.closest('tr');
            const paymentId = row.dataset.paymentId;
            
            fetch(`/manager/transaction-detail/${paymentId}`)
                .then(r => r.text())
                .then(html => {
                    // Remove existing modal
                    const existing = document.getElementById('transactionDetailModal');
                    if (existing) existing.remove();
                    
                    // Insert HTML
                    document.body.insertAdjacentHTML('beforeend', html);
                    
                    // Open modal with data from table
                    setTimeout(() => {
                        openTransactionModal({
                            transactionCode: row.cells[0].textContent.trim(),
                            customerName: row.cells[1].querySelector('.fw-semibold').textContent.trim(),
                            customerEmail: row.cells[1].querySelector('.text-muted').textContent.trim(),
                            amount: parseInt(row.cells[2].querySelector('.fw-semibold').textContent.replace(/[^\d]/g, '')) || 0,
                            description: row.cells[2].querySelector('.text-muted').textContent.trim(),
                            paymentStatus: row.dataset.status,
                            gateway: row.dataset.gateway || 'PAYOS',
                            gatewayOrderCode: row.dataset.gatewayOrderCode || '-',
                            createdAt: row.dataset.createdAt || new Date().toISOString(),
                            paidAt: row.dataset.paidAt || null,
                            webhookReceived: row.dataset.webhookReceived === 'true',
                            orderItems: JSON.parse(row.dataset.orderItems || '[]')
                        });
                    }, 100);
                })
                .catch(e => console.error('Lỗi:', e));
        });
    });
});

// ====== MODAL FUNCTIONS ======
function openTransactionModal(data) {
    const modal = document.getElementById('transactionDetailModal');
    
    document.getElementById('transactionCode').textContent = data.transactionCode || '-';
    document.getElementById('customerName').textContent = data.customerName || '-';
    document.getElementById('customerEmail').textContent = data.customerEmail || '-';
    document.getElementById('transactionAmount').textContent = (data.amount || 0).toLocaleString('vi-VN') + ' VND';
    document.getElementById('paymentGateway').textContent = data.gateway || '-';
    document.getElementById('createdDate').textContent = formatDate(data.createdAt);
    document.getElementById('createdTime').textContent = formatTime(data.createdAt);
    document.getElementById('gatewayOrderCode').textContent = data.gatewayOrderCode || '-';
    document.getElementById('paymentDescription').textContent = data.description || '-';
    
    const statusEl = document.getElementById('transactionStatus');
    statusEl.textContent = getStatusText(data.paymentStatus);
    statusEl.className = 'info-value status-badge ' + getStatusClass(data.paymentStatus);
    document.getElementById('transactionTime').textContent = data.paidAt ? 'Thanh toán lúc: ' + formatDateTime(data.paidAt) : 'Chưa thanh toán';
    document.getElementById('webhookStatus').textContent = data.webhookReceived ? '✓ Đã nhận' : '✗ Chưa nhận';
    
    displayOrderItems(data.orderItems || []);
    
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closeTransactionModal() {
    const modal = document.getElementById('transactionDetailModal');
    modal.classList.remove('active');
    document.body.style.overflow = 'auto';
}

function displayOrderItems(items) {
    const itemsList = document.getElementById('orderItemsList');
    const itemCount = document.getElementById('itemCount');
    
    if (!items || items.length === 0) {
        itemsList.innerHTML = '<div class="empty-items"><i class="ph ph-package"></i><p>Không có khóa học</p></div>';
        itemCount.textContent = '0 khóa học';
        return;
    }
    
    itemCount.textContent = items.length + ' khóa học';
    itemsList.innerHTML = items.map(item => `
        <div class="order-item">
            <div class="order-item-thumbnail"><i class="ph ph-book"></i></div>
            <div class="order-item-content">
                <p class="order-item-title" title="${item.courseTitleSnapshot || '-'}">${item.courseTitleSnapshot || '-'}</p>
                <span class="order-item-price">${(item.priceSnapshot || 0).toLocaleString('vi-VN')} VND</span>
            </div>
        </div>
    `).join('');
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

function formatTime(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

function formatDateTime(dateString) {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('vi-VN');
}

function getStatusText(status) {
    const map = { 'PAID': 'Đã thanh toán', 'PENDING': 'Chờ thanh toán', 'FAILED': 'Thất bại', 'CANCELLED': 'Đã hủy', 'EXPIRED': 'Hết hạn' };
    return map[status] || 'Không xác định';
}

function getStatusClass(status) {
    const map = { 'PAID': 'status-paid', 'PENDING': 'status-pending', 'FAILED': 'status-failed', 'CANCELLED': 'status-cancelled', 'EXPIRED': 'status-expired' };
    return map[status] || '';
}

function printTransaction() {
    window.print();
}

// Close modal on outside click
document.addEventListener('click', function(e) {
    const modal = document.getElementById('transactionDetailModal');
    if (modal && e.target === modal) closeTransactionModal();
});

// Close modal on ESC
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') closeTransactionModal();
});