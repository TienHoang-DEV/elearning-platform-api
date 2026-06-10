/* ===== LEARNING HUB - INSTRUCTOR DASHBOARD JS ===== */
document.addEventListener('DOMContentLoaded', () => {

    // ========== NAVIGATION ==========
    const navLinks = document.querySelectorAll('.sidebar-nav a[data-page]');
    const pageSections = document.querySelectorAll('.page-section');

    function showPage(pageId) {
        pageSections.forEach(s => s.classList.remove('active'));
        navLinks.forEach(l => l.classList.remove('active'));
        const target = document.getElementById('page-' + pageId);
        if (target) target.classList.add('active');
        const link = document.querySelector(`.sidebar-nav a[data-page="${pageId}"]`);
        if (link) link.classList.add('active');
    }

    navLinks.forEach(link => {
        link.addEventListener('click', e => {
            e.preventDefault();
            showPage(link.dataset.page);
        });
    });

    showPage('profile');

    // ========== MODAL HELPERS ==========
    function openModal(id) {
        const m = document.getElementById(id);
        if (m) m.classList.add('open');
    }

    function closeModal(id) {
        const m = document.getElementById(id);
        if (m) m.classList.remove('open');
    }

    document.querySelectorAll('[data-open-modal]').forEach(btn => {
        btn.addEventListener('click', () => openModal(btn.dataset.openModal));
    });
    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', () => closeModal(btn.dataset.closeModal));
    });
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', e => {
            if (e.target === overlay) overlay.classList.remove('open');
        });
    });

    // ========== TABS ==========
    document.querySelectorAll('.tab-bar').forEach(bar => {
        const tabs = bar.querySelectorAll('.tab-btn');
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                const group = bar.dataset.tabGroup;
                tabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                document.querySelectorAll(`.tab-content[data-tab-group="${group}"]`).forEach(c => c.style.display = 'none');
                const target = document.querySelector(`.tab-content[data-tab-group="${group}"][data-tab="${tab.dataset.tab}"]`);
                if (target) target.style.display = 'block';
            });
        });
    });

    // ========== PROFILE SECTION ==========
    const avatarInput = document.getElementById('avatarInput');
    const avatarPreview = document.getElementById('avatarPreview');
    const sidebarAvatar = document.getElementById('sidebarAvatar');
    const profilePreviewAvatar = document.getElementById('profilePreviewAvatar');


    if (avatarInput) {
        avatarInput.addEventListener('change', function () {
            const file = this.files[0];
            if (!file) return;
            const reader = new FileReader();
            reader.onload = e => {
                const src = e.target.result;
                [avatarPreview, sidebarAvatar, profilePreviewAvatar].forEach(el => {
                    if (el) {
                        el.innerHTML = `<img src="${src}" alt="Avatar">`;
                    }
                });
            };
            reader.readAsDataURL(file);
        });
    }


    // Live profile preview update
    const nameInput = document.getElementById('profileName');
    const bioEditor = document.getElementById('bioEditor');
    const fieldSelect = document.getElementById('profileField');
    const previewName = document.getElementById('previewName');
    const previewBio = document.getElementById('previewBio');
    const previewField = document.getElementById('previewField');
    const sidebarName = document.getElementById('sidebarName');

    const bioHidden = document.getElementById('bioHidden');

    if (bioEditor && bioHidden) {
        bioEditor.addEventListener('input', function () {
            bioHidden.value = bioEditor.innerHTML;
        });
    }

    function updateProfilePreview() {
        if (previewName && nameInput) previewName.textContent = nameInput.value || 'Họ và Tên';
        if (previewBio && bioEditor) previewBio.textContent = bioEditor.textContent || 'Giới thiệu bản thân...';
        if (previewField && fieldSelect) {
            const opt = fieldSelect.options[fieldSelect.selectedIndex];
            previewField.textContent = opt && opt.value ? opt.text : 'Lĩnh vực';
        }
        if (sidebarName && nameInput) sidebarName.textContent = nameInput.value || 'Tên Giảng Viên';
    }

    if (nameInput) nameInput.addEventListener('input', updateProfilePreview);
    if (bioEditor) bioEditor.addEventListener('input', updateProfilePreview);
    if (fieldSelect) fieldSelect.addEventListener('change', updateProfilePreview);

    // ========== RICH TEXT TOOLBAR ==========
    document.querySelectorAll('.rich-toolbar').forEach(toolbar => {
        toolbar.querySelectorAll('button').forEach(btn => {
            btn.addEventListener('click', e => {
                e.preventDefault();
                const cmd = btn.dataset.cmd;
                const val = btn.dataset.val || null;
                document.execCommand(cmd, false, val);
            });
        });
    });

    // ========== COURSE CREATION WIZARD ==========
    let currentStep = 1;
    const totalSteps = 5;
    const stepNames = ['GIỚI THIỆU KHÓA HỌC', 'GIÁ KHOÁ HỌC', 'CHƯƠNG TRÌNH GIẢNG DẠY', 'CÂU HỎI ÔN TẬP', 'XUẤT BẢN'];

    function updateWizard() {
        document.querySelectorAll('.wizard-step').forEach((step, i) => {
            step.classList.remove('active', 'done');
            const n = i + 1;
            if (n < currentStep) step.classList.add('done');
            if (n === currentStep) step.classList.add('active');
        });

        document.querySelectorAll('.course-step-content').forEach((content, i) => {
            content.style.display = (i + 1 === currentStep) ? 'block' : 'none';
        });

        const prevBtn = document.getElementById('stepPrevBtn');
        const nextBtn = document.getElementById('stepNextBtn');
        if (prevBtn) prevBtn.disabled = currentStep === 1;
        if (nextBtn) {
            if (currentStep === totalSteps) {
                nextBtn.textContent = '✓ Hoàn Thành';
                nextBtn.className = 'btn btn-success';
            } else {
                nextBtn.textContent = 'Tiếp Theo →';
                nextBtn.className = 'btn btn-primary';
            }
        }

        // Update progress bar dot
        const progressLine = document.querySelector('.wizard-progress-fill');
        if (progressLine) {
            progressLine.style.width = ((currentStep - 1) / (totalSteps - 1) * 100) + '%';
        }
    }

    const prevBtn = document.getElementById('stepPrevBtn');
    const nextBtn = document.getElementById('stepNextBtn');

    if (prevBtn) prevBtn.addEventListener('click', () => {
        if (currentStep > 1) {
            currentStep--;
            updateWizard();
        }
    });
    if (nextBtn) nextBtn.addEventListener('click', () => {
        if (currentStep < totalSteps) {
            currentStep++;
            updateWizard();
        } else {
            alert('🎉 Khóa học đã hoàn thành!');
        }
    });

    // ========== COURSE PREVIEW ==========
    const courseTitleInput = document.getElementById('courseTitleInput');
    const courseShortInput = document.getElementById('courseShortInput');
    const previewCourseTitle = document.getElementById('previewCourseTitle');
    const previewCourseShort = document.getElementById('previewCourseShort');

    function updateCoursePreview() {
        if (previewCourseTitle && courseTitleInput) previewCourseTitle.textContent = courseTitleInput.value || 'Tiêu đề khóa học';
        if (previewCourseShort && courseShortInput) previewCourseShort.textContent = courseShortInput.value || 'Mô tả ngắn về khóa học...';
    }

    if (courseTitleInput) courseTitleInput.addEventListener('input', updateCoursePreview);
    if (courseShortInput) courseShortInput.addEventListener('input', updateCoursePreview);

    // Course thumbnail preview
    const thumbnailInput = document.getElementById('thumbnailInput');
    const courseBanner = document.getElementById('courseBanner');
    if (thumbnailInput && courseBanner) {
        thumbnailInput.addEventListener('change', function () {
            const file = this.files[0];
            if (!file) return;
            const reader = new FileReader();
            reader.onload = e => {
                courseBanner.style.backgroundImage = `url(${e.target.result})`;
                courseBanner.style.backgroundSize = 'cover';
                courseBanner.style.backgroundPosition = 'center';
            };
            reader.readAsDataURL(file);
        });
    }

    // ========== CURRICULUM BUILDER ==========
    let sectionCount = 0;
    const curriculumList = document.getElementById('curriculumList');

    function addSection(title) {
        sectionCount++;
        const sId = 'section-' + sectionCount;
        const div = document.createElement('div');
        div.className = 'curriculum-section';
        div.id = sId;
        div.innerHTML = `
      <div class="curriculum-section-header" onclick="toggleSection('${sId}')">
        <div class="section-title">
          <span>☰</span>
          <span class="sec-title-text">Chương ${sectionCount}: ${title || 'Tên chương'}</span>
        </div>
        <div class="section-actions">
          <button class="btn btn-sm btn-outline" onclick="event.stopPropagation(); openAddLessonModal('${sId}')">+ Thêm bài giảng</button>
          <button class="btn btn-sm btn-danger" onclick="event.stopPropagation(); removeSection('${sId}')">✕</button>
        </div>
      </div>
      <div class="curriculum-section-body" id="body-${sId}">
        <div class="lesson-list" id="lessons-${sId}"></div>
      </div>
    `;
        if (curriculumList) curriculumList.appendChild(div);
    }

    window.toggleSection = function (sId) {
        const body = document.getElementById('body-' + sId);
        if (body) body.style.display = body.style.display === 'none' ? 'block' : '';
    };

    window.removeSection = function (sId) {
        const el = document.getElementById(sId);
        if (el && confirm('Xóa chương này?')) el.remove();
    };

    // Add Section modal
    const addSectionBtn = document.getElementById('addSectionBtn');
    const confirmAddSection = document.getElementById('confirmAddSection');
    const sectionNameInput = document.getElementById('sectionNameInput');

    if (addSectionBtn) addSectionBtn.addEventListener('click', () => openModal('modal-add-section'));
    if (confirmAddSection) confirmAddSection.addEventListener('click', () => {
        const name = sectionNameInput ? sectionNameInput.value.trim() : '';
        addSection(name);
        if (sectionNameInput) sectionNameInput.value = '';
        closeModal('modal-add-section');
    });

    // Add Lesson
    let currentSectionTarget = null;

    window.openAddLessonModal = function (sId) {
        currentSectionTarget = sId;
        openModal('modal-add-lesson');
    };

    const confirmAddLesson = document.getElementById('confirmAddLesson');
    if (confirmAddLesson) confirmAddLesson.addEventListener('click', () => {
        const lessonName = document.getElementById('lessonNameInput').value.trim();
        const isFree = document.getElementById('lessonFreeToggle') ? document.getElementById('lessonFreeToggle').checked : false;
        if (!lessonName || !currentSectionTarget) return;
        addLessonToSection(currentSectionTarget, lessonName, isFree);
        document.getElementById('lessonNameInput').value = '';
        if (document.getElementById('lessonFreeToggle')) document.getElementById('lessonFreeToggle').checked = false;
        closeModal('modal-add-lesson');
    });

    let lessonCount = 0;

    function addLessonToSection(sId, name, isFree) {
        lessonCount++;
        const list = document.getElementById('lessons-' + sId);
        if (!list) return;
        const lId = 'lesson-' + lessonCount;
        const div = document.createElement('div');
        div.className = 'lesson-item';
        div.id = lId;
        div.innerHTML = `
      <span class="lesson-icon">🎬</span>
      <span class="lesson-name">${name}</span>
      ${isFree ? '<span class="lesson-free-badge">Miễn phí</span>' : ''}
      <div class="lesson-actions">
        <button class="btn btn-sm btn-outline" onclick="openEditLessonModal('${lId}', '${sId}')">✏️ Sửa</button>
        <button class="btn btn-sm btn-danger" onclick="removeLesson('${lId}')">✕</button>
      </div>
    `;
        list.appendChild(div);
    }

    window.removeLesson = function (lId) {
        const el = document.getElementById(lId);
        if (el) el.remove();
    };

    let editingLessonId = null;
    let editingSectionId = null;
    window.openEditLessonModal = function (lId, sId) {
        editingLessonId = lId;
        editingSectionId = sId;
        openModal('modal-edit-lesson');
    };

    // Media tabs in lesson modal
    document.querySelectorAll('.media-tab').forEach(tab => {
        tab.addEventListener('click', () => {
            const group = tab.closest('.lesson-media-tabs').dataset.group;
            document.querySelectorAll(`.media-tab[data-group="${group}"]`).forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            document.querySelectorAll(`.media-content[data-group="${group}"]`).forEach(c => c.style.display = 'none');
            const target = document.querySelector(`.media-content[data-group="${group}"][data-type="${tab.dataset.type}"]`);
            if (target) target.style.display = 'block';
        });
    });

    // Video upload preview
    const videoFileInput = document.getElementById('videoFileInput');
    const videoPreviewContainer = document.getElementById('videoPreviewContainer');
    if (videoFileInput && videoPreviewContainer) {
        videoFileInput.addEventListener('change', function () {
            const file = this.files[0];
            if (!file) return;
            const url = URL.createObjectURL(file);
            videoPreviewContainer.innerHTML = `<video controls style="width:100%;border-radius:8px;margin-top:8px;"><source src="${url}"></video>`;
        });
    }

    // ========== PRICE & VOUCHER ==========
    const priceInput = document.getElementById('priceInput');
    const priceDisplay = document.getElementById('priceDisplay');
    if (priceInput) {
        priceInput.addEventListener('input', function () {
            const val = parseInt(this.value.replace(/\D/g, '')) || 0;
            if (priceDisplay) priceDisplay.textContent = val.toLocaleString('vi-VN') + ' VNĐ';
        });
    }

    const addVoucherBtn = document.getElementById('addVoucherBtn');
    const voucherCodeInput = document.getElementById('voucherCodeInput');
    const voucherDiscountInput = document.getElementById('voucherDiscountInput');
    const voucherList = document.getElementById('voucherList');

    if (addVoucherBtn) {
        addVoucherBtn.addEventListener('click', () => {
            const code = voucherCodeInput ? voucherCodeInput.value.trim().toUpperCase() : '';
            const discount = voucherDiscountInput ? voucherDiscountInput.value.trim() : '';
            if (!code) return alert('Nhập mã voucher!');
            addVoucher(code, discount);
            if (voucherCodeInput) voucherCodeInput.value = '';
            if (voucherDiscountInput) voucherDiscountInput.value = '';
        });
    }

    function addVoucher(code, discount) {
        if (!voucherList) return;
        const div = document.createElement('div');
        div.className = 'voucher-item';
        div.innerHTML = `
      <div>
        <div class="voucher-code">${code}</div>
        <div class="voucher-info">Giảm: ${discount || '0'}%</div>
      </div>
      <button class="btn btn-sm btn-danger" onclick="this.closest('.voucher-item').remove()">✕ Xóa</button>
    `;
        voucherList.appendChild(div);
    }

    // ========== QUIZ SECTION ==========
    const addQuizBtn = document.getElementById('addQuizBtn');
    if (addQuizBtn) addQuizBtn.addEventListener('click', () => openModal('modal-add-quiz'));

    const confirmAddQuiz = document.getElementById('confirmAddQuiz');
    const quizList = document.getElementById('quizList');
    if (confirmAddQuiz) {
        confirmAddQuiz.addEventListener('click', () => {
            const question = document.getElementById('quizQuestion').value.trim();
            const answers = [
                document.getElementById('quizA').value.trim(),
                document.getElementById('quizB').value.trim(),
                document.getElementById('quizC').value.trim(),
                document.getElementById('quizD').value.trim(),
            ];
            const correctIdx = parseInt(document.getElementById('quizCorrect').value);
            if (!question) return alert('Nhập câu hỏi!');
            addQuizItem(question, answers, correctIdx);
            ['quizQuestion', 'quizA', 'quizB', 'quizC', 'quizD'].forEach(id => {
                const el = document.getElementById(id);
                if (el) el.value = '';
            });
            closeModal('modal-add-quiz');
        });
    }

    let quizCount = 0;

    function addQuizItem(question, answers, correctIdx) {
        if (!quizList) return;
        quizCount++;
        const div = document.createElement('div');
        div.className = 'card';
        div.style.marginBottom = '12px';
        div.innerHTML = `
      <div style="font-weight:700;margin-bottom:10px;">${quizCount}. ${question}</div>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;">
        ${answers.map((a, i) => `
          <div style="padding:8px 12px;border-radius:7px;border:2px solid ${i === correctIdx ? 'var(--success)' : 'var(--border)'};background:${i === correctIdx ? '#e0faf4' : '#f9fbff'};font-size:.85rem;">
            ${String.fromCharCode(65 + i)}. ${a || '(Trống)'}
            ${i === correctIdx ? ' ✅' : ''}
          </div>
        `).join('')}
      </div>
      <div style="margin-top:10px;text-align:right;">
        <button class="btn btn-sm btn-danger" onclick="this.closest('.card').remove()">✕ Xóa</button>
      </div>
    `;
        quizList.appendChild(div);
    }

    // ========== SUBMIT REQUEST ==========
    const submitRequestBtn = document.getElementById('submitRequestBtn');
    if (submitRequestBtn) {
        submitRequestBtn.addEventListener('click', () => {
            submitRequestBtn.disabled = true;
            submitRequestBtn.textContent = '⏳ Đang gửi yêu cầu...';
            setTimeout(() => {
                submitRequestBtn.textContent = '✅ Đã Gửi Yêu Cầu Phê Duyệt';
                submitRequestBtn.className = 'btn btn-success btn-lg';
                const msg = document.getElementById('submitSuccessMsg');
                if (msg) msg.style.display = 'flex';
            }, 1500);
        });
    }

    // ========== COURSE LIST NAV ==========
    const newCourseBtn = document.getElementById('newCourseBtn');
    if (newCourseBtn) {
        newCourseBtn.addEventListener('click', () => showPage('create-course'));
    }

    // ========== INIT ==========
    updateWizard();
    updateProfilePreview();
    updateCoursePreview();

    // Pre-add sample section
    addSection('Giới thiệu về guitar điện');
});

document.getElementById('courseForm').addEventListener('submit', function () {
    const priceInput = document.getElementById('priceInput');
    
    let hiddenPrice = document.getElementById('hiddenPriceInput');
    if (!hiddenPrice) {
        hiddenPrice = document.createElement('input');
        hiddenPrice.type = 'hidden';
        hiddenPrice.name = 'price';
        hiddenPrice.id = 'hiddenPriceInput';
        this.appendChild(hiddenPrice);
    }

    if (!priceInput.value) {
        priceInput.value = 0;
        hiddenPrice.value = 0;
    } else {
        hiddenPrice.value = parseInt(priceInput.value);
        priceInput.value = parseInt(priceInput.value);
    }
});

