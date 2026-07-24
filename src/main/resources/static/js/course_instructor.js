



document.addEventListener('DOMContentLoaded', function () {

    initToastNotifications();
    initSubmitReviewPolicy();
    initSidebarNav();
    initTabs();
    initModals();
    initRichEditors();
    initAvatarPreview();
    initThumbnailPreview();
    initProfilePreview();
    initCurriculum();
    initVideoUpload();
    initEditVideoUpload();
    initMaterialPreview();
    initMaterialFileValidation();
    initLessonUploadSubmitLock();
    initMaterialUploadSubmitLock();

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
    // Xử lý click chuyển tab.
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            const group = this.dataset.tabGroup;
            const tab   = this.dataset.tab;
            activateTab(group, tab);
        });
    });

    const params = new URLSearchParams(window.location.search);
    const activeTab = params.get('tab');

    if (activeTab) {
        document.querySelectorAll(`.tab-btn[data-tab="${activeTab}"]`).forEach(btn => {
            const group = btn.dataset.tabGroup;
            activateTab(group, activeTab);
        });
    }



    document.querySelectorAll('.media-tab').forEach(tab => {
        tab.addEventListener('click', function () {
            const group = this.dataset.group;
            const type  = this.dataset.type;

            document.querySelectorAll(`.media-tab[data-group="${group}"]`)
                .forEach(t => t.classList.toggle('active', t === this));


            document.querySelectorAll(`.media-content[data-group="${group}"]`)
                .forEach(c => {
                    c.style.display = c.dataset.type === type ? '' : 'none';
                });
        });
    });


    document.querySelectorAll('.media-tab.active').forEach(tab => {
        const group = tab.dataset.group;
        const type  = tab.dataset.type;
        document.querySelectorAll(`.media-content[data-group="${group}"]`)
            .forEach(c => {
                c.style.display = c.dataset.type === type ? '' : 'none';
            });
    });

}

function activateTab(group, tab) {
    document.querySelectorAll(`.tab-btn[data-tab-group="${group}"]`)
        .forEach(b => b.classList.toggle('active', b.dataset.tab === tab));

    document.querySelectorAll(`.tab-content[data-tab-group="${group}"]`)
        .forEach(c => {
            c.style.display = c.dataset.tab === tab ? '' : 'none';
        });
}




function initModals() {

    document.querySelectorAll('[data-open-modal]').forEach(btn => {
        btn.addEventListener('click', function () {
            const id = this.dataset.openModal;
            openModal(id);
        });
    });

    // Close modal via data-close-modal attribute
    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.dataset.closeModal;
            closeModal(id);
        });
    });

    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', e => {
            if (e.target === overlay) closeModal(overlay.id);
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
    if (!el) return;
    if (el.dataset.uploadLocked === 'true') return;
    el.classList.remove('active');
}

function initRichEditors() {
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

function setUploadModalLock(form, locked, fallbackLoadingText) {
    const overlay = form.closest('.modal-overlay');
    if (overlay) {
        overlay.dataset.uploadLocked = locked ? 'true' : 'false';
        overlay.classList.toggle('modal-upload-locked', locked);
        overlay.querySelectorAll('[data-close-modal]').forEach(button => {
            button.disabled = locked;
        });
    }

    form.querySelectorAll('input, textarea, select, button').forEach(control => {
        if (control.type === 'hidden' || control.type === 'submit' || control.matches('[data-close-modal]')) {
            return;
        }

        if (locked) {
            control.dataset.uploadLockPointerEvents = control.style.pointerEvents || '';
            control.dataset.uploadLockTabIndex = control.getAttribute('tabindex') || '';
            control.style.pointerEvents = 'none';
            control.setAttribute('tabindex', '-1');
            if (control.matches('input:not([type="checkbox"]):not([type="radio"]):not([type="file"]), textarea')) {
                control.dataset.uploadLockReadOnly = control.readOnly ? 'true' : 'false';
                control.readOnly = true;
            }
            if (document.activeElement === control) {
                control.blur();
            }
            return;
        }

        control.style.pointerEvents = control.dataset.uploadLockPointerEvents || '';
        if (control.dataset.uploadLockTabIndex) {
            control.setAttribute('tabindex', control.dataset.uploadLockTabIndex);
        } else {
            control.removeAttribute('tabindex');
        }
        if (control.dataset.uploadLockReadOnly !== undefined) {
            control.readOnly = control.dataset.uploadLockReadOnly === 'true';
        }
        delete control.dataset.uploadLockPointerEvents;
        delete control.dataset.uploadLockTabIndex;
        delete control.dataset.uploadLockReadOnly;
    });

    const submitButton = form.querySelector('button[type="submit"]');
    if (!submitButton) return;

    if (locked) {
        if (!submitButton.dataset.originalText) {
            submitButton.dataset.originalText = submitButton.textContent;
        }
        submitButton.disabled = true;
        submitButton.classList.add('btn-loading');
        submitButton.textContent = submitButton.dataset.loadingText || fallbackLoadingText || 'Dang luu...';
        return;
    }

    submitButton.disabled = false;
    submitButton.classList.remove('btn-loading');
    submitButton.textContent = submitButton.dataset.originalText || 'Luu';
}

function initLessonUploadSubmitLock() {
    document.querySelectorAll('form[data-lesson-upload-form]').forEach(form => {
        form.addEventListener('submit', async function (event) {
            if (form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }

            const videoInput = form.querySelector('input[type="file"][name="videoFile"]');
            const blobInput = form.querySelector('input[name="videoBlobName"]');
            const selectedVideo = videoInput?.files?.[0];

            form.dataset.submitting = 'true';
            setUploadModalLock(form, true);
            if (selectedVideo && blobInput && form.dataset.directUploaded !== 'true') {
                event.preventDefault();
                try {
                    const sectionId = resolveLessonFormSectionId(form);
                    const blobName = await uploadLessonVideoDirectToAzure(selectedVideo, sectionId);
                    blobInput.value = blobName;
                    videoInput.disabled = true;
                    form.dataset.directUploaded = 'true';
                    form.submit();
                } catch (error) {
                    console.error('Direct Azure video upload failed:', error);
                    alert(error.message || 'Upload video truc tiep len Azure that bai.');
                    form.dataset.submitting = 'false';
                    setUploadModalLock(form, false);
                }
            }
        });
    });
}

function initMaterialUploadSubmitLock() {
    const form = document.getElementById('addMaterialForm');
    if (!form) return;

    form.addEventListener('submit', function (event) {
        const materialInput = document.getElementById('addMaterialFile');
        if (!validateMaterialFiles(materialInput)) {
            event.preventDefault();
            return;
        }

        if (form.dataset.submitting === 'true') {
            event.preventDefault();
            return;
        }

        form.dataset.submitting = 'true';
        setUploadModalLock(form, true, 'Dang luu tai lieu...');
    });
}

function validateMaterialFiles(input) {
    if (!input || !input.files || input.files.length === 0) {
        return true;
    }

    const allowedExtensions = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'];
    const invalidFiles = Array.from(input.files).filter(file => {
        const extension = file.name.includes('.')
            ? file.name.split('.').pop().toLowerCase()
            : '';
        return !allowedExtensions.includes(extension);
    });

    if (invalidFiles.length === 0) {
        return true;
    }

    alert('Chi chap nhan material dang: pdf, doc, docx, xls, xlsx, ppt, pptx.');
    input.value = '';
    return false;
}

function initMaterialFileValidation() {
    const input = document.getElementById('addMaterialFile');
    if (!input) return;

    input.addEventListener('change', function () {
        validateMaterialFiles(input);
    });
}

function resolveLessonFormSectionId(form) {
    const sectionInput = form.querySelector('input[name="sectionId"]');
    if (sectionInput?.value) {
        return sectionInput.value;
    }

    const match = form.action.match(/\/sections\/(\d+)\/lessons/);
    if (match) {
        return match[1];
    }

    throw new Error('Khong tim thay section de upload video.');
}

async function uploadLessonVideoDirectToAzure(file, sectionId) {
    const requestBody = new FormData();
    requestBody.append('fileName', file.name);
    requestBody.append('sectionId', sectionId);

    let sasResponse;
    try {
        sasResponse = await fetch('/instructor/video-upload-url', {
            method: 'POST',
            body: requestBody
        });
    } catch (error) {
        throw new Error('Khong goi duoc server de xin URL upload video.');
    }

    if (!sasResponse.ok) {
        throw new Error('Khong lay duoc URL upload video.');
    }

    const uploadInfo = await sasResponse.json();
    let azureResponse;
    try {
        azureResponse = await fetch(uploadInfo.uploadUrl, {
            method: 'PUT',
            headers: {
                'x-ms-blob-type': 'BlockBlob',
                'Content-Type': file.type || 'application/octet-stream'
            },
            body: file
        });
    } catch (error) {
        throw new Error('Khong upload truc tiep len Azure duoc. Kiem tra CORS cua Azure Blob.');
    }

    if (!azureResponse.ok) {
        throw new Error('Upload video truc tiep len Azure that bai.');
    }

    return uploadInfo.blobName;
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

function initVideoUpload(){
    initVideoUploadById(
        'videoFileInput',
        'videoPreviewContainer',
        'durationSecondsInput'
    );
}


function initEditVideoUpload(){
    initVideoUploadById(
        'editVideoFileInput',
        'editVideoPreviewContainer',
        'editDurationSecondsInput'
    );
}


function initVideoUploadById(inputId, containerId, durationId) {
    const input = document.getElementById(inputId);
    const container = document.getElementById(containerId);
    const durationInput = document.getElementById(durationId);

    if (!input) return;

    input.addEventListener('change', function () {
        const file = this.files[0];
        if (!file) return;

        // Preview
        if (container) {
            const url = URL.createObjectURL(file);

            container.innerHTML = `
                <video src="${url}" controls
                       style="width:100%;margin-top:10px;border-radius:6px;max-height:220px;">
                </video>
                <p>${file.name}</p>
            `;
        }

        // Duration
        if (durationInput) {
            const video = document.createElement('video');
            video.preload = 'metadata';

            video.onloadedmetadata = function () {
                URL.revokeObjectURL(video.src);
                durationInput.value = Math.round(video.duration);
            };

            video.src = URL.createObjectURL(file);
        }
    });
}



function initCurriculum() {
    const addSectionBtn = document.getElementById('addSectionBtn');
    if (!addSectionBtn) return;

    addSectionBtn.addEventListener('click', function () {
        openModal('modal-add-section');
    });
}

function openEditSectionModal(dataset) {
    const source = dataset.source || 'create';
    const form = document.getElementById("editSectionForm");
    if (!form) {
        alert('Khong tim thay form chinh sua chuong!');
        return;
    }

    form.action =
        `/instructor/${dataset.courseId}/sections/${dataset.sectionId}/edit?source=${source}`;

    const sourceInput = form.querySelector('input[name="source"]');
    if (sourceInput) {
        sourceInput.value = source;
    }

    const titleInput = form.querySelector('[name="title"]');
    if (titleInput) {
        titleInput.value = dataset.sectionTitle || '';
    }

    openModal("modal-edit-section");
}



function openEditLessonModal(dataset) {
    const form = document.getElementById('editLessonForm');
    const source = dataset.source || 'create';
    const courseId = dataset.courseId;
    const sectionId = dataset.sectionId;
    const lessonId = dataset.lessonId;

    form.action = `/instructor/sections/${sectionId}/lessons/${lessonId}/edit?source=${source}`;

    document.getElementById('editCourseid').value = courseId;
    document.getElementById('editSectionId').value = sectionId;
    document.getElementById('editLessonId').value = lessonId;

    document.getElementById('editLessonName').value = dataset.lessonTitle || '';
    const videoPreviewContainer = document.getElementById('editVideoPreviewContainer');
    videoPreviewContainer.innerHTML = '';
    const editVideoInput = document.getElementById('editVideoFileInput');
    editVideoInput.value = '';
    editVideoInput.disabled = false;
    document.getElementById('editVideoBlobNameInput').value = '';
    form.dataset.submitting = 'false';
    form.dataset.directUploaded = 'false';
    setUploadModalLock(form, false);


    const oldVideoUrl = dataset.lessonVideoUrl || dataset.videoUrl;
    if (oldVideoUrl && oldVideoUrl !== 'null' && oldVideoUrl !== '') {
        videoPreviewContainer.innerHTML = `
            <div class="old-video-info" style="margin-top: 10px; padding: 10px; background: #f9f9f9; border: 1px dashed #ccc; border-radius: 6px;">
                <p style="font-size: 13px; color: #555; margin-bottom: 5px;"><strong>Video hiện tại của bài giảng:</strong></p>
                <video src="${oldVideoUrl}" controls style="width:100%; border-radius:4px; max-height:180px;"></video>
            </div>
        `;
    }

    // Reset media tabs
    document.querySelectorAll('.media-tab[data-group="edit-lesson-media"]').forEach(tab => {
        tab.classList.toggle('active', tab.dataset.type === 'video');
    });
    document.querySelectorAll('.media-content[data-group="edit-lesson-media"]').forEach(content => {
        content.style.display = content.dataset.type === 'video' ? '' : 'none';
    });

    openModal('modal-edit-lesson');
}



function deleteLesson(lessonId, courseId, source) {
    source = source || 'create';
    if (!lessonId || !courseId) {
        alert('ID không hợp lệ!');
        return;
    }

    if (!confirm('Xóa bài giảng này? Tất cả tài liệu và quiz sẽ bị xóa theo.')) {
        return;
    }


    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/instructor/lessons/${lessonId}/delete?courseId=${courseId}&source=${source}`;
    document.body.appendChild(form);
    form.submit();
}

function deleteMaterial(courseId, materialId, source) {
    source = source || 'create';
    if (!materialId || !courseId) {
        alert('ID không hợp lệ!');
        return;
    }

    if (!confirm('Xóa tài liệu này?')) {
        return;
    }

    // Tạo form ẩn để submit.
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = `/instructor/materials/${materialId}/delete?source=${source}`;
    const courseInput = document.createElement('input');
    courseInput.type = 'hidden';
    courseInput.name = 'courseId';
    courseInput.value = courseId;
    form.appendChild(courseInput);
    document.body.appendChild(form);
    form.submit();
}



function deleteSection(sectionId, courseId, source) {
    source = source || 'create';
    if (!confirm('Xóa chương này và tất cả bài giảng bên trong?')) return;
    fetch(`/instructor/${courseId}/sections/${sectionId}/delete?source=${source}`, { method: 'POST' })
        .then(r => { if (r.ok) location.reload(); });
}


function openAddLessonModal(sectionId, source, courseId) {
    const form = document.getElementById('addLessonForm');
    source = source || 'create';
    form.action = `/instructor/sections/${sectionId}/lessons?source=${source}`;

    document.getElementById('addLessonSectionId').value = sectionId;
    const courseInput = document.getElementById('addLessonCourseId');
    if (courseInput && courseId) {
        courseInput.value = courseId;
    }
    document.getElementById('videoBlobNameInput').value = '';
    const videoInput = document.getElementById('videoFileInput');
    if (videoInput) {
        videoInput.value = '';
        videoInput.disabled = false;
    }
    const preview = document.getElementById('videoPreviewContainer');
    if (preview) {
        preview.innerHTML = '';
    }
    form.dataset.submitting = 'false';
    form.dataset.directUploaded = 'false';
    setUploadModalLock(form, false);

    document.getElementById('modal-add-lesson').classList.add('active');
}



function initSubmitReviewPolicy() {
    const form = document.getElementById('submitReviewForm');
    const checkbox = document.getElementById('acceptPolicyInput');
    const button = document.getElementById('submitReviewBtn');
    if (!form || !checkbox || !button) return;

    const readyExceptPolicy = form.dataset.readyExceptPolicy === 'true';
    const policyItem = document.querySelector('[data-policy-item="true"]');
    const policyStatusIcon = policyItem?.querySelector('.result-icon i');
    const policyTrailingIcon = policyItem?.querySelector('.item-check, .item-empty');
    const policyMissing = document.querySelector('[data-policy-missing="true"]');
    const missingBox = policyMissing?.closest('.missing-box');
    const progressCard = document.querySelector('.progress-card[data-completed-count][data-total-count]');
    const percentText = document.getElementById('submitReviewPercentText');
    const ratioText = document.getElementById('submitReviewRatioText');
    const progressNumber = document.getElementById('submitReviewProgressNumber');
    const progressBar = document.getElementById('submitReviewProgressBar');
    const progressRing = progressCard?.querySelector('.progress-ring');

    const baseCompleted = progressCard ? Number(progressCard.dataset.completedCount || 0) : 0;
    const totalCount = progressCard ? Number(progressCard.dataset.totalCount || 0) : 0;
    const policyWasCompletedOnLoad = policyItem?.classList.contains('is-ok') === true;

    const updateState = () => {
        const accepted = checkbox.checked;
        button.disabled = !(readyExceptPolicy && accepted);
        const completedCount = baseCompleted + (accepted && !policyWasCompletedOnLoad ? 1 : 0) - (!accepted && policyWasCompletedOnLoad ? 1 : 0);
        const percent = totalCount === 0 ? 0 : Math.round(completedCount * 100 / totalCount);

        if (policyItem) {
            policyItem.classList.toggle('is-ok', accepted);
            policyItem.classList.toggle('is-missing', !accepted);
        }

        if (policyStatusIcon) {
            policyStatusIcon.className = accepted
                ? 'bi bi-check-lg'
                : 'bi bi-exclamation-triangle-fill';
        }

        if (policyTrailingIcon) {
            policyTrailingIcon.className = accepted
                ? 'bi bi-check-square-fill item-check'
                : 'bi bi-square item-empty';
        }

        if (policyMissing) {
            policyMissing.style.display = accepted ? 'none' : '';
        }

        if (missingBox) {
            const hasVisibleMissing = Array.from(missingBox.querySelectorAll('li'))
                .some(item => item.style.display !== 'none');
            missingBox.style.display = hasVisibleMissing ? '' : 'none';
        }

        if (progressRing) {
            progressRing.style.setProperty('--percent', percent);
        }
        if (percentText) {
            percentText.textContent = `${percent}%`;
        }
        if (ratioText) {
            ratioText.textContent = `${completedCount}/${totalCount}`;
        }
        if (progressNumber) {
            progressNumber.textContent = `${completedCount} / ${totalCount}`;
        }
        if (progressBar) {
            progressBar.style.width = `${percent}%`;
        }
    };

    checkbox.addEventListener('change', updateState);
    updateState();
}

/* =====================
   MATERIAL FILE PREVIEW
===================== */
function initMaterialPreview(){
    initMaterialPreviewByID(
        "addMaterialFile",
        "addMaterialPreview"
    );
}

function initMaterialPreviewByID(inputId, listId) {
    const input     = document.getElementById(inputId);
    const list      = document.getElementById(listId);
    const dt        = new DataTransfer(); // Giữ file trong input thật.

    if (!input || !list) return;

    input.addEventListener('change', function () {
        Array.from(this.files).forEach(file => {
            // Tránh trùng tên file.
            if (![...dt.files].find(f => f.name === file.name)) {
                dt.items.add(file);
            }
        });
        input.files = dt.files;
        renderList();
    });

    function renderList() {
        if (!dt.files.length) { list.innerHTML = ''; return; }

        const iconMap = {
            pdf:  { icon: 'ti-file-type-pdf', color: '#E24B4A' },
            docx: { icon: 'ti-file-type-doc', color: '#185FA5' },
            pptx: { icon: 'ti-file-type-ppt', color: '#D85A30' },
            xlsx: { icon: 'ti-file-type-xls', color: '#3B6D11' },
        };

        list.innerHTML = Array.from(dt.files).map((file, i) => {
            const ext = file.name.split('.').pop().toLowerCase();
            const m   = iconMap[ext] || { icon: 'ti-file', color: '#888' };
            const mb  = (file.size / 1024 / 1024).toFixed(1);
            return `
            <div style="display:flex; align-items:center; gap:10px; margin-top:8px; padding:10px 12px; border:0.5px solid var(--border-color); border-radius:8px;">
                <i class="ti ${m.icon}" style="font-size:20px; color:${m.color}; flex-shrink:0;"></i>
                <div style="flex:1; min-width:0;">
                    <div style="font-size:13px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">${file.name}</div>
                    <div style="font-size:12px; color:var(--text-muted);">${mb} MB</div>
                </div>
                <button type="button" onclick="removeMaterial(${i})"
                        style="background:none; border:none; cursor:pointer; color:var(--text-muted); padding:4px;">
                    <i class="ti ti-x" style="font-size:15px;"></i>
                </button>
            </div>`;
        }).join('');
    }

    window.removeMaterial = function(index) {
        dt.items.remove(index);
        input.files = dt.files; // Cập nhật lại input.
        renderList();
    };
}

function appendSourceParam(url, source){
    const sep = url.includes('?') ? '&' : '?';
    return url + sep + 'source=' + encodeURIComponent(source || 'create');
}
function openAddMaterialModal(dataset) {
    const form = document.getElementById('addMaterialForm');
   const source = dataset.source || 'create';
    if (!form || !dataset.lessonId) {
        alert('Không tìm thấy thông tin bài giảng!');
        return;
    }

    form.action = appendSourceParam(dataset.actionUrl, source);

    document.getElementById('addMaterialLessonId').value =
        dataset.lessonId;

    document.getElementById('addMaterialCourseId').value =
        dataset.courseId;

    document.getElementById('addMaterialFile').value = '';
    form.dataset.submitting = 'false';
    setUploadModalLock(form, false);

    const preview =
        document.getElementById('addMaterialPreview');

    if (preview) {
        preview.innerHTML = '';
    }

    openModal('modal-add-material');
}

function initToastNotifications() {
    document.querySelectorAll('.app-toast').forEach(toast => {
        const close = toast.querySelector('.toast-close');
        const hide = () => {
            toast.classList.add('toast-hide');
            window.setTimeout(() => toast.remove(), 180);
        };

        if (close) {
            close.addEventListener('click', hide);
        }

        window.setTimeout(hide, 4200);
    });
}


