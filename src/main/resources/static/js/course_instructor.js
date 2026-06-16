/* =====================================================
   LEARNING HUB - Instructor Dashboard JS
   File: instructor_course.js
===================================================== */



document.addEventListener('DOMContentLoaded', function () {

    /* =====================================================
       1. SIDEBAR NAVIGATION (SPA mode - nếu dùng 1 file)
          Nếu đã tách file thì bỏ qua phần này,
          sidebar dùng href bình thường.
    ===================================================== */
    initSidebarNav();

    /* =====================================================
       2. TABS
    ===================================================== */
    initTabs();

    /* =====================================================
       3. MODALS
    ===================================================== */
    initModals();

    /* =====================================================
       4. RICH TEXT EDITOR
    ===================================================== */
    initRichEditors();

    /* =====================================================
       5. AVATAR PREVIEW
    ===================================================== */
    initAvatarPreview();

    /* =====================================================
       6. THUMBNAIL PREVIEW
    ===================================================== */
    initThumbnailPreview();

    /* =====================================================
       7. PROFILE PREVIEW (live update)
    ===================================================== */
    initProfilePreview();

    /* =====================================================
       8. COURSE PREVIEW (live update)
    ===================================================== */
    initCoursePreview();

    /* =====================================================
       9. CURRICULUM - Add Section & Lesson (JS fallback)
          Nếu backend xử lý thì bỏ qua.
    ===================================================== */
    initCurriculum();

    /* =====================================================
       10. QUIZ - Add Question (JS fallback)
    ===================================================== */
    initQuiz();

    /* =====================================================
       11. PRICING - Price display & Voucher
    ===================================================== */
    initPricing();

    /* =====================================================
       12. VIDEO PREVIEW
    ===================================================== */
    initVideoPreview();

    /* =====================================================
       13. SUBMIT REQUEST (step 5)
    ===================================================== */
    initSubmitRequest();

    /* =====================================================
       14. WIZARD STEP BUTTONS (nếu dùng 1 file)
    ===================================================== */
    initWizardButtons();

    /* =====================================================
       15. MATERIAL FILE PREVIEW
    ===================================================== */
    initMaterialFilePreview();

});

/* =====================================================
   FUNCTIONS
===================================================== */

function initSidebarNav() {
    // Highlight active link based on current URL
    const currentPath = window.location.pathname;
    document.querySelectorAll('.sidebar-nav a[href]').forEach(link => {
        if (link.href && currentPath.includes(link.getAttribute('href')) && link.getAttribute('href') !== '#') {
            link.classList.add('active');
        }
    });
}

function initTabs() {
    // === Xử lý click chuyển tab (giữ nguyên logic cũ) ===
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const group = this.dataset.tabGroup;
            const tab   = this.dataset.tab;
            activateTab(group, tab);
        });
    });

    // === Khôi phục tab active dựa vào query param ?tab=... (dùng cho pagination reload) ===
    const params = new URLSearchParams(window.location.search);
    const activeTab = params.get('tab');

    if (activeTab) {
        // Tab nào có data-tab khớp với param sẽ được active,
        // áp dụng cho tất cả tab-group có trên trang (an toàn nếu có nhiều group)
        document.querySelectorAll(`.tab-btn[data-tab="${activeTab}"]`).forEach(btn => {
            const group = btn.dataset.tabGroup;
            activateTab(group, activeTab);
        });
    }
}

/**
 * Active 1 tab cụ thể trong 1 group, ẩn các tab còn lại.
 */
function activateTab(group, tab) {
    document.querySelectorAll(`.tab-btn[data-tab-group="${group}"]`)
        .forEach(b => b.classList.toggle('active', b.dataset.tab === tab));

    document.querySelectorAll(`.tab-content[data-tab-group="${group}"]`)
        .forEach(c => {
            c.style.display = c.dataset.tab === tab ? '' : 'none';
        });
}




function initModals() {
    // Open modal via data-open-modal attribute
    document.querySelectorAll('[data-open-modal]').forEach(btn => {
        btn.addEventListener('click', function () {
            const id = this.dataset.openModal;
            openModal(id);
        });
    });

    // Close modal via data-close-modal attribute
    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', function () {
            const id = this.dataset.closeModal;
            closeModal(id);
        });
    });

    // Close modal on overlay click
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', function (e) {
            if (e.target === this) closeModal(this.id);
        });
    });

    // Close modal on ESC
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-overlay.active').forEach(m => closeModal(m.id));
        }
    });
}

function openModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.add('active');
}

function closeModal(id) {
    const el = document.getElementById(id);
    if (el) el.classList.remove('active');
}

function initRichEditors() {
    // Toolbar buttons
    document.querySelectorAll('.rich-toolbar button[data-cmd]').forEach(btn => {
        btn.addEventListener('click', function () {
            const cmd = this.dataset.cmd;
            const val = this.dataset.val || null;
            document.execCommand(cmd, false, val);
        });
    });

    // Sync hidden inputs before form submit
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', function () {
            syncRichEditor('bioEditor',          'bioHidden');
            syncRichEditor('courseDescEditor',   'descHidden');
            syncRichEditor('outcomeEditor',      'outcomeHidden');
            syncRichEditor('requirementEditor',  'requirementHidden');
        });
    });
}

function syncRichEditor(editorId, hiddenId) {
    const editor = document.getElementById(editorId);
    const hidden = document.getElementById(hiddenId);
    if (editor && hidden) hidden.value = editor.innerHTML;
}

function initAvatarPreview() {
    const input   = document.getElementById('avatarInput');
    const preview = document.getElementById('avatarPreview');
    if (!input || !preview) return;

    input.addEventListener('change', function () {
        const file = this.files[0];
        if (!file) return;
        const url = URL.createObjectURL(file);

        // Update small avatar in form
        preview.innerHTML = `<img src="${url}" alt="Avatar" style="width:100%;height:100%;object-fit:cover;border-radius:50%;"/>`;

        // Also update sidebar avatar
        const sidebarAvatar = document.getElementById('sidebarAvatar');
        if (sidebarAvatar) sidebarAvatar.innerHTML = `<img src="${url}" alt=""/>`;

        // Update profile preview card
        const ppAvatar = document.querySelector('.profile-preview-avatar img');
        if (ppAvatar) ppAvatar.src = url;
    });
}

function initThumbnailPreview() {
    const input = document.getElementById('thumbnailInput');
    if (!input) return;

    input.addEventListener('change', function () {
        const file = this.files[0];
        if (!file) return;
        const url = URL.createObjectURL(file);
        const banner = document.getElementById('courseBanner');
        if (banner) banner.style.backgroundImage = `url(${url})`;
        const uploadArea = input.closest('.form-group')?.querySelector('.file-upload-area');
        if (uploadArea) {
            uploadArea.innerHTML = `<img src="${url}" style="max-height:120px;border-radius:6px;" alt="Thumbnail"/>`;
        }
    });
}

function initProfilePreview() {
    const firstNameInput = document.getElementById('firstName') || document.querySelector('[name="firstName"]');
    const lastNameInput  = document.getElementById('lastName')  || document.querySelector('[name="lastName"]');
    const previewName    = document.getElementById('previewName');
    const bioEditor      = document.getElementById('bioEditor');
    const previewBio     = document.getElementById('previewBio');

    if (firstNameInput && lastNameInput && previewName) {
        const updateName = () => {
            previewName.textContent = (firstNameInput.value + ' ' + lastNameInput.value).trim();
        };
        firstNameInput.addEventListener('input', updateName);
        lastNameInput.addEventListener('input', updateName);
    }

    if (bioEditor && previewBio) {
        bioEditor.addEventListener('input', function () {
            previewBio.innerHTML = this.innerHTML || 'Giới thiệu bản thân của bạn sẽ hiện ở đây...';
        });
    }
}

function initCoursePreview() {
    const titleInput = document.getElementById('courseTitleInput');
    const shortInput = document.getElementById('courseShortInput');
    const previewTitle = document.getElementById('previewCourseTitle');
    const previewShort = document.getElementById('previewCourseShort');

    if (titleInput && previewTitle) {
        titleInput.addEventListener('input', function () {
            previewTitle.textContent = this.value || 'Tiêu đề khóa học sẽ hiện ở đây';
        });
    }
    if (shortInput && previewShort) {
        shortInput.addEventListener('input', function () {
            previewShort.textContent = this.value || 'Mô tả ngắn về khóa học...';
        });
    }
}

function initPricing() {
    const priceInput   = document.getElementById('priceInput');
    const priceDisplay = document.getElementById('priceDisplay');
    if (!priceInput || !priceDisplay) return;

    priceInput.addEventListener('input', function () {
        const val = parseInt(this.value) || 0;
        priceDisplay.textContent = val.toLocaleString('vi-VN') + ' VNĐ';
    });
}

function initVideoPreview() {
    const input     = document.getElementById('videoFileInput');
    const container = document.getElementById('videoPreviewContainer');
    if (!input || !container) return;

    input.addEventListener('change', function () {
        const file = this.files[0];
        if (!file) return;
        const url = URL.createObjectURL(file);
        container.innerHTML = `
      <video src="${url}" controls style="width:100%;margin-top:10px;border-radius:6px;max-height:220px;"></video>
      <p style="font-size:12px;color:#6b7280;margin-top:4px;">${file.name} (${(file.size/1024/1024).toFixed(1)} MB)</p>
    `;
    });
}

/* =====================
   CURRICULUM (JS-only fallback - dùng nếu không submit form)
===================== */
let sectionCounter = 0;

function initCurriculum() {
    const addSectionBtn = document.getElementById('addSectionBtn');
    if (!addSectionBtn) return;

    addSectionBtn.addEventListener('click', function () {
        openModal('modal-add-section');
    });

    // Confirm add section (JS only mode)
    const confirmBtn = document.getElementById('confirmAddSection');
    if (confirmBtn) {
        confirmBtn.addEventListener('click', function () {
            const name = document.getElementById('sectionNameInput')?.value.trim();
            if (!name) { alert('Vui lòng nhập tên chương.'); return; }
            addSectionToUI(name);
            document.getElementById('sectionNameInput').value = '';
            closeModal('modal-add-section');
        });
    }
}

function addSectionToUI(title) {
    sectionCounter++;
    const list = document.getElementById('curriculumList');
    if (!list) return;

    const sectionEl = document.createElement('div');
    sectionEl.className = 'section-item';
    sectionEl.dataset.sectionIndex = sectionCounter;
    sectionEl.innerHTML = `
    <div class="section-header">
      <span class="drag-handle">⠿</span>
      <span class="section-number">Chương ${sectionCounter}</span>
      <span class="section-title">${escapeHtml(title)}</span>
      <div class="section-actions">
        <button type="button" class="btn btn-sm btn-outline add-lesson-btn" data-section-index="${sectionCounter}">
          + Thêm Bài Giảng
        </button>
        <button type="button" class="btn btn-sm btn-danger delete-section-btn">🗑</button>
      </div>
    </div>
    <div class="lesson-list" id="lessons-${sectionCounter}">
      <div class="table-empty" style="padding:10px 16px;font-size:.85rem;">Chưa có bài giảng</div>
    </div>
  `;

    sectionEl.querySelector('.add-lesson-btn').addEventListener('click', function () {
        currentSectionIndex = parseInt(this.dataset.sectionIndex);
        openModal('modal-add-lesson');
    });
    sectionEl.querySelector('.delete-section-btn').addEventListener('click', function () {
        if (confirm('Xóa chương này?')) sectionEl.remove();
    });

    list.appendChild(sectionEl);
    updateCurriculumPreview();
}

let currentSectionIndex = null;

// Called from curriculum step modal confirm
document.addEventListener('click', function (e) {
    const confirmLesson = e.target.closest('#confirmAddLesson');
    if (!confirmLesson) return;
    const name = document.getElementById('lessonNameInput')?.value.trim();
    if (!name) { alert('Vui lòng nhập tên bài giảng.'); return; }
    const isFree = document.getElementById('lessonFreeToggle')?.checked;
    addLessonToSection(currentSectionIndex, name, isFree);
    document.getElementById('lessonNameInput').value = '';
    closeModal('modal-add-lesson');
});

function addLessonToSection(sectionIndex, title, isFree) {
    const lessonList = document.getElementById(`lessons-${sectionIndex}`);
    if (!lessonList) return;

    // Remove empty state message
    const empty = lessonList.querySelector('.table-empty');
    if (empty) empty.remove();

    const lessonEl = document.createElement('div');
    lessonEl.className = 'lesson-item';
    lessonEl.innerHTML = `
    <span class="drag-handle">⠿</span>
    <span class="lesson-icon">🎬</span>
    <span class="lesson-title">${escapeHtml(title)}</span>
    ${isFree ? '<span class="badge badge-free">Miễn phí</span>' : ''}
    <div class="lesson-actions">
      <button type="button" class="btn btn-sm btn-danger delete-lesson-btn">🗑</button>
    </div>
  `;
    lessonEl.querySelector('.delete-lesson-btn').addEventListener('click', function () {
        if (confirm('Xóa bài giảng này?')) lessonEl.remove();
    });
    lessonList.appendChild(lessonEl);
    updateCurriculumPreview();
}

function updateCurriculumPreview() {
    const preview = document.getElementById('curriculumPreview');
    if (!preview) return;
    const sections = document.querySelectorAll('#curriculumList .section-item');
    if (!sections.length) {
        preview.innerHTML = '<p style="color:var(--text-muted);font-size:.85rem;">Các chương và bài giảng sẽ hiện ở đây...</p>';
        return;
    }
    let html = '';
    sections.forEach((sec, i) => {
        const title   = sec.querySelector('.section-title')?.textContent || '';
        const lessons = sec.querySelectorAll('.lesson-title');
        html += `<div style="margin-bottom:10px;"><strong>Chương ${i+1}: ${title}</strong>`;
        if (lessons.length) {
            html += '<ul style="margin:4px 0 0 16px;">';
            lessons.forEach(l => { html += `<li style="font-size:13px;color:var(--text-muted);">${l.textContent}</li>`; });
            html += '</ul>';
        }
        html += '</div>';
    });
    preview.innerHTML = html;
}

// Delete section/lesson from backend-rendered HTML (called via onclick in templates)
function deleteSection(sectionId) {
    if (!confirm('Xóa chương này và tất cả bài giảng bên trong?')) return;
    fetch(`/instructor/sections/${sectionId}/delete`, { method: 'POST' })
        .then(r => { if (r.ok) location.reload(); });
}

function deleteLesson(lessonId) {
    if (!confirm('Xóa bài giảng này?')) return;
    fetch(`/instructor/lessons/${lessonId}/delete`, { method: 'POST' })
        .then(r => { if (r.ok) location.reload(); });
}

// Open edit lesson modal with existing data
function openEditLessonModal(data) {
    document.getElementById('editLessonId').value  = data.lessonId  || '';
    document.getElementById('editLessonName').value = data.lessonTitle || '';
    const toggle = document.getElementById('editLessonFreeToggle');
    if (toggle) toggle.checked = data.lessonFree === 'true';
    openModal('modal-edit-lesson');
}

// Open add lesson modal from curriculum (backend mode)
function openAddLessonModal(sectionId) {
    const hiddenInput = document.getElementById('addLessonSectionId');
    if (hiddenInput) hiddenInput.value = sectionId;
    // Update form action
    const form = document.getElementById('addLessonForm');
    if (form) form.action = `/instructor/sections/${sectionId}/lessons`;
    openModal('modal-add-lesson');
}

/* =====================
   QUIZ
===================== */
function initQuiz() {
    const addQuizBtn = document.getElementById('addQuizBtn');
    if (addQuizBtn) {
        addQuizBtn.addEventListener('click', () => openModal('modal-add-quiz'));
    }

    // JS-only fallback confirm
    const confirmQuiz = document.getElementById('confirmAddQuiz');
    if (confirmQuiz) {
        confirmQuiz.addEventListener('click', function () {
            const question = document.getElementById('quizQuestion')?.value.trim();
            const a = document.getElementById('quizA')?.value.trim();
            const b = document.getElementById('quizB')?.value.trim();
            const c = document.getElementById('quizC')?.value.trim();
            const d = document.getElementById('quizD')?.value.trim();
            const correct = parseInt(document.getElementById('quizCorrect')?.value) || 0;
            if (!question) { alert('Vui lòng nhập câu hỏi.'); return; }

            addQuizToUI({ question, options: [a,b,c,d], correct });
            ['quizQuestion','quizA','quizB','quizC','quizD'].forEach(id => {
                const el = document.getElementById(id);
                if (el) el.value = '';
            });
            closeModal('modal-add-quiz');
        });
    }
}

let quizCounter = 0;

function addQuizToUI({ question, options, correct }) {
    const list = document.getElementById('quizList');
    if (!list) return;

    const empty = list.querySelector('.table-empty');
    if (empty) { list.innerHTML = ''; }

    quizCounter++;
    const letters = ['A','B','C','D'];
    const item = document.createElement('div');
    item.style.cssText = 'border:1px solid var(--border);border-radius:6px;padding:14px 16px;margin-bottom:10px;background:#fff;';
    item.innerHTML = `
    <div style="display:flex;align-items:flex-start;gap:12px;">
      <span style="font-weight:700;color:var(--text-muted);font-size:12px;margin-top:2px;">#${quizCounter}</span>
      <div style="flex:1;">
        <div style="font-weight:600;margin-bottom:8px;">${escapeHtml(question)}</div>
        <div style="display:grid;grid-template-columns:1fr 1fr;gap:6px;">
          ${options.map((opt, i) => `
            <div style="padding:6px 10px;border-radius:4px;font-size:13px;
              background:${i===correct ? 'var(--success-light)':'#f9fafb'};
              border:1px solid ${i===correct ? '#bbf7d0':'var(--border)'};
              color:${i===correct ? 'var(--success)':'var(--text-secondary)'};">
              <strong>${letters[i]}.</strong> ${escapeHtml(opt || '—')}
              ${i===correct ? ' ✅' : ''}
            </div>
          `).join('')}
        </div>
      </div>
      <button type="button" class="btn btn-sm btn-danger" onclick="this.closest('div[style]').remove()">🗑</button>
    </div>
  `;
    list.appendChild(item);
}

function openEditQuizModal(quizId) {
    // Placeholder - load quiz data via fetch then populate modal
    openModal('modal-add-quiz');
}

/* =====================
   SUBMIT REQUEST (step 5)
===================== */
function initSubmitRequest() {
    const btn = document.getElementById('submitRequestBtn');
    const msg = document.getElementById('submitSuccessMsg');
    if (!btn || !msg) return;
    btn.addEventListener('click', function () {
        msg.style.display = 'flex';
        btn.disabled = true;
        btn.textContent = '✅ Đã Gửi';
    });
}

/* =====================
   WIZARD BUTTONS (single-file SPA mode)
===================== */
function initWizardButtons() {
    const prevBtn = document.getElementById('stepPrevBtn');
    const nextBtn = document.getElementById('stepNextBtn');
    if (!prevBtn || !nextBtn) return;

    let currentStep = 1;
    const totalSteps = 5;

    function showStep(step) {
        for (let i = 1; i <= totalSteps; i++) {
            const el = document.getElementById(`step-${i}`);
            if (el) el.style.display = i === step ? '' : 'none';
        }
        document.querySelectorAll('.wizard-step').forEach((s, i) => {
            s.classList.toggle('active', i + 1 === step);
        });
        prevBtn.disabled = step === 1;
        nextBtn.textContent = step === totalSteps ? '🚀 Gửi Duyệt' : 'Tiếp Theo →';
    }

    prevBtn.addEventListener('click', () => { if (currentStep > 1) showStep(--currentStep); });
    nextBtn.addEventListener('click', () => {
        if (currentStep < totalSteps) showStep(++currentStep);
        else document.getElementById('submitRequestBtn')?.click();
    });

    document.querySelectorAll('.wizard-step[data-step]').forEach(s => {
        s.addEventListener('click', function () {
            const step = parseInt(this.dataset.step);
            if (step) { currentStep = step; showStep(step); }
        });
    });
}

/* =====================
   MATERIAL FILE PREVIEW
===================== */
function initMaterialFilePreview() {
    const input   = document.getElementById('materialFileInput');
    const preview = document.getElementById('materialFilePreview');
    if (!input || !preview) return;

    input.addEventListener('change', function () {
        const file = this.files[0];
        if (!file) return;
        const size = (file.size / 1024 / 1024).toFixed(2);
        preview.textContent = `✅ ${file.name} (${size} MB)`;
        // Update upload area label
        const area = input.closest('.modal-body')?.querySelector('.file-upload-area p');
        if (area) area.textContent = file.name;
    });
}

/* =====================
   UTILS
===================== */
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}