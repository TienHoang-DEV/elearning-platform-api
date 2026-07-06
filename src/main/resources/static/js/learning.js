// Course Player JS

document.addEventListener("DOMContentLoaded", () => {
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    initializeTabs();
    initializeMaterial();
    initializeVideo();
    progressVideo();
    initializeSidebarToggle();
    initializeNotes();
    initializeReviews();
});

// 1. Tab Panels Switching
function initializeTabs() {
    const tabs = {
        'tab-overview-btn': 'panel-overview',
        'tab-quiz-btn': 'panel-quiz',
        'tab-document-btn': 'panel-document'
    };

    const tabBtns = document.querySelectorAll(".player-nav-tabs .nav-link");
    const panels = document.querySelectorAll(".tab-panel-item");

    tabBtns.forEach(btn => {
        btn.addEventListener("click", () => {
            const targetPanelId = tabs[btn.id];
            if (!targetPanelId) return;

            tabBtns.forEach(b => b.classList.remove("active"));
            panels.forEach(p => p.classList.add("d-none"));

            btn.classList.add("active");
            document.getElementById(targetPanelId).classList.remove("d-none");
        });
    });
}

function progressVideo() {
    const video = document.querySelector("#lesson-video");
    if (!video) return;

    const totalLessonTag = document.querySelector("#total-lesson-completed");
    let lessonProgressStatus = video.dataset.lessonProgressStatus;
    let totalLesson = parseInt(totalLessonTag.dataset.totalLesson);
    let totalLessonCompleted = parseInt(totalLessonTag.dataset.totalLessonCompleted);
    let completed = false;
    const lessonId = video.dataset.lessonId;

    video.addEventListener("timeupdate", function () {
        if (completed) return;

        let percent = (video.currentTime / video.duration) * 100;
        if (percent >= 90) {
            completed = true;
            fetch("/lesson-completed/" + lessonId)
                .catch(() => {
                    completed = false;
                });

            if (lessonProgressStatus == "false") {
                totalLessonCompleted++;
                totalLessonTag.textContent = totalLessonCompleted + "/" + totalLesson + " Bai hoc";
                lessonProgressStatus = "true";

                const currentLessonItem = document.querySelector(`.lesson-item[data-sidebar-lesson-id="${lessonId}"]`);
                if (currentLessonItem) {
                    const indicator = currentLessonItem.querySelector(".lesson-icon-indicator");
                    if (indicator) {
                        indicator.className = "lesson-icon-indicator rounded-circle d-flex align-items-center justify-content-center flex-shrink-0 mt-0.5 text-white bg-success";
                        indicator.style.border = "1px solid #198754";
                        indicator.innerHTML = `<i data-lucide="check" style="width: 10px; height: 10px;"></i>`;
                        if (typeof lucide !== 'undefined') {
                            lucide.createIcons();
                        }
                    }

                    const accordionItem = currentLessonItem.closest('.accordion-item');
                    if (accordionItem) {
                        const allLessonItems = accordionItem.querySelectorAll('.lesson-item');
                        let allCompleted = true;
                        allLessonItems.forEach(item => {
                            const isCurrent = item.dataset.sidebarLessonId === lessonId;
                            const hasCheck = item.querySelector('.lesson-icon-indicator.bg-success');
                            if (!isCurrent && !hasCheck) {
                                allCompleted = false;
                            }
                        });

                        if (allCompleted) {
                            const sectionBadge = accordionItem.querySelector('.section-status-badge');
                            if (sectionBadge) {
                                sectionBadge.classList.remove('d-none');
                                sectionBadge.classList.add('d-flex', 'align-items-center', 'justify-content-center');
                            }
                        }
                    }
                }
            }
        }
    });
}

function initializeVideo() {
    const videoTag = document.querySelector("#lesson-video");
    if (!videoTag) return;

    const lessonId = videoTag.dataset.lessonId;
    fetch("/lesson/" + lessonId)
        .then(response => response.text())
        .then(text => {
            videoTag.src = text;
        })
        .catch(error => {
            console.log(error);
        });
}

function initializeMaterial() {
    const viewers = document.querySelectorAll(".lesson-document-viewer");
    viewers.forEach(viewer => {
        const docFrame = viewer.querySelector(".lesson-document-frame");
        const openLink = viewer.querySelector(".btn-open-lesson-document");
        const downloadLink = viewer.querySelector(".btn-download-lesson-document");
        const materialId = docFrame?.dataset?.materialId;

        if (!materialId) {
            console.warn("initializeMaterial: missing materialId for viewer", viewer);
            return;
        }

        if (downloadLink) {
            downloadLink.href = `/material/${materialId}/download`;
        }

        fetch(`/material/${materialId}`)
            .then((response) => {
                return response.text();
            })
            .then(url => {
                if (openLink) openLink.href = url;
                if (docFrame) docFrame.src = url;
            })
            .catch(err => {
                console.error("initializeMaterial error for materialId", materialId, err);
                if (openLink) openLink.classList.add('disabled');
                if (downloadLink) downloadLink.classList.add('disabled');
            });
    });
}

function initializeSidebarToggle() {
    const toggleBtn = document.getElementById("btn-sidebar-toggle");
    const sidebar = document.querySelector(".sidebar-curriculum-container");
    const mainContent = document.querySelector(".main-player-content");

    if (toggleBtn && sidebar && mainContent) {
        toggleBtn.addEventListener("click", () => {
            sidebar.classList.toggle("d-none");

            if (sidebar.classList.contains("d-none")) {
                mainContent.className = "col-12 main-player-content";
            } else {
                mainContent.className = "col-lg-8 col-12 main-player-content";
            }
        });
    }
}

function formatTime(seconds) {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    const s = Math.floor(seconds % 60);

    let minute = m.toString();
    if (m < 10) {
        minute = "0" + minute;
    }

    let second = s.toString();
    if (s < 10) {
        second = "0" + second;
    }

    if (h > 0) {
        return h + ":" + minute + ":" + second;
    }

    return minute + ":" + second;
}

function initializeNotes() {
    const tabCurriculumBtn = document.getElementById("sidebar-tab-curriculum-btn");
    const tabNotesBtn = document.getElementById("sidebar-tab-notes-btn");
    const panelCurriculum = document.getElementById("sidebar-panel-curriculum");
    const panelNotes = document.getElementById("sidebar-panel-notes");
    const video = document.getElementById("lesson-video");

    const btnSaveNoteVideo = document.getElementById("btn-save-note");
    const btnCancelNote = document.getElementById("btn-cancel-note");
    const noteInputText = document.getElementById("note-input-text");
    const currentNoteTimeText = document.getElementById("current-note-time");
    const savedNotesList = document.getElementById("saved-notes-list");

    let activeNoteSeconds = 0;
    let editingNoteElement = null;

    const formNote = document.querySelector('#form-note');
    if (formNote) {
        formNote.addEventListener("submit", async (e) => {
            e.preventDefault();
            if (!noteInputText || !noteInputText.value.trim()) return;
            const formData = new FormData(formNote);
            formData.append("noteContent", noteInputText.value.trim());
            formData.append("videoTimeSeconds", activeNoteSeconds);
            formData.append("lessonId", video.dataset.lessonId);

            try {
                const response = await fetch('/api/lesson-note/save', {
                    method: "POST",
                    body: formData,
                });

                if (response.ok) {
                    const result = await response.json();
                    if (result.success) {
                        if (editingNoteElement) {
                            editingNoteElement.querySelector(".note-content").textContent = noteInputText.value.trim();
                            const seekBtn = editingNoteElement.querySelector(".note-seek-btn");
                            if (seekBtn) {
                                seekBtn.dataset.seconds = activeNoteSeconds;
                                seekBtn.innerHTML = `<i data-lucide="play" style="width: 10px; height: 10px; fill: currentColor;"></i> ${formatTime(activeNoteSeconds)}`;
                            }
                        } else {
                            // Prepend newly created note item in UI
                            const timeStr = formatTime(activeNoteSeconds);
                            const lessonTitle = document.querySelector(".course-lesson-title")?.textContent || "Bài học hiện tại";
                            const newNoteId = result.noteId || result.id || "";
                            const newNoteHTML = `
                                <div class="note-item p-3 rounded-3 border border-light-subtle bg-white shadow-sm transition-all" data-lesson-note-id="${newNoteId}">
                                    <div class="d-flex justify-content-between align-items-start mb-2">
                                        <button class="btn btn-primary-subtle text-primary btn-sm rounded-pill px-2.5 py-0.5 fs-8 fw-bold d-inline-flex align-items-center gap-1 note-seek-btn" data-seconds="${activeNoteSeconds}">
                                            <i data-lucide="play" style="width: 10px; height: 10px; fill: currentColor;"></i>
                                            ${timeStr}
                                        </button>
                                        <div class="d-flex gap-1">
                                            <button class="btn btn-link text-muted p-0 border-0 btn-edit-note" title="Sửa ghi chú">
                                                <i data-lucide="edit-3" style="width: 14px; height: 14px;"></i>
                                            </button>
                                            <button class="btn btn-link text-danger p-0 border-0 btn-delete-note" title="Xóa ghi chú">
                                                <i data-lucide="trash-2" style="width: 14px; height: 14px;"></i>
                                            </button>
                                        </div>
                                    </div>
                                    <h4 class="fs-8 fw-bold text-dark mb-1">${lessonTitle}</h4>
                                    <p class="note-content text-muted fs-7 mb-0">${noteInputText.value.trim()}</p>
                                </div>
                            `;
                            if (savedNotesList) {
                                savedNotesList.insertAdjacentHTML("afterbegin", newNoteHTML);
                            }
                        }

                        // Clear input and state
                        noteInputText.value = "";
                        editingNoteElement = null;
                        const noteIdInput = formNote.querySelector('input[name="noteId"]');
                        if (noteIdInput) {
                            noteIdInput.remove();
                        }
                        if (typeof lucide !== 'undefined') {
                            lucide.createIcons();
                        }
                    } else {
                        alert(result.message || "Lỗi khi lưu ghi chú");
                    }
                } else {
                    alert("Có lỗi xảy ra khi kết nối tới server.");
                }
            } catch (err) {
                console.error("Error saving note:", err);
                alert("Không thể kết nối đến server để lưu ghi chú.");
            }
        });
    }

    // Khôi phục lại icon Lucide play cho nút chuyển đoạn (seek) bị Thymeleaf th:text ghi đè
    if (savedNotesList) {
        savedNotesList.querySelectorAll(".note-seek-btn").forEach(btn => {
            const seconds = btn.dataset.seconds;
            if (seconds !== undefined) {
                const timeText = btn.textContent.trim();
                btn.innerHTML = `<i data-lucide="play" style="width: 10px; height: 10px; fill: currentColor;"></i> ${timeText}`;
            }
        });
        if (typeof lucide !== 'undefined') {
            lucide.createIcons();
        }
    }

    // Tab switching
    if (tabCurriculumBtn && tabNotesBtn && panelCurriculum && panelNotes) {
        tabCurriculumBtn.addEventListener("click", () => {
            tabCurriculumBtn.classList.add("active");
            tabCurriculumBtn.classList.remove("text-muted");
            tabNotesBtn.classList.remove("active");
            tabNotesBtn.classList.add("text-muted");
            panelCurriculum.classList.remove("d-none");
            panelCurriculum.classList.add("d-flex");
            panelNotes.classList.add("d-none");
            panelNotes.classList.remove("d-flex");
        });

        tabNotesBtn.addEventListener("click", () => {
            tabNotesBtn.classList.add("active");
            tabNotesBtn.classList.remove("text-muted");
            tabCurriculumBtn.classList.remove("active");
            tabCurriculumBtn.classList.add("text-muted");
            panelNotes.classList.remove("d-none");
            panelNotes.classList.add("d-flex");
            panelCurriculum.classList.add("d-none");
            panelCurriculum.classList.remove("d-flex");
        });
    }

    // Save note from video player
    if (btnSaveNoteVideo) {
        btnSaveNoteVideo.addEventListener("click", () => {
            // Pause video
            if (video) {
                video.pause();
                activeNoteSeconds = Math.floor(video.currentTime); // video.currentTime return second
            } else {
                activeNoteSeconds = 0;
            }

            const formNote = document.querySelector("#form-note");
            const noteId = formNote.querySelector('input[name="noteId"]');
            if (noteId) {
                formNote.removeChild(noteId);
            }
            noteInputText.value = "";

            // Update timestamp
            if (currentNoteTimeText) {
                currentNoteTimeText.textContent = formatTime(activeNoteSeconds);
            }

            // Switch to notes tab
            if (tabNotesBtn) {
                tabNotesBtn.click();
            }

            // Focus input
            if (noteInputText) {
                noteInputText.focus();
            }
            editingNoteElement = null;
        });
    }

    // Cancel note
    if (btnCancelNote) {
        btnCancelNote.addEventListener("click", () => {
            if (noteInputText) {
                noteInputText.value = "";
            }
            editingNoteElement = null;
            if (formNote) {
                const noteIdInput = formNote.querySelector('input[name="noteId"]');
                if (noteIdInput) {
                    noteIdInput.remove();
                }
            }
        });
    }

    // The user will implement backend note saving logic on click/submit of btnAddNote

    // Handle actions on saved notes list (Seek, Edit, Delete)
    if (savedNotesList) {
        savedNotesList.addEventListener("click", (e) => {
            // Seek button
            const seekBtn = e.target.closest(".note-seek-btn");
            if (seekBtn && video) {
                const secs = parseInt(seekBtn.dataset.seconds);
                video.currentTime = secs;
                video.play();
                return;
            }

            // Edit button
            const editBtn = e.target.closest(".btn-edit-note");
            if (editBtn) {
                const item = editBtn.closest(".note-item");
                const content = item.querySelector(".note-content").textContent;
                const seekButton = item.querySelector(".note-seek-btn");
                const secs = parseInt(seekButton.dataset.seconds);
                const lessonNoteId = parseInt(item.dataset.lessonNoteId);

                const formNote = document.querySelector("#form-note");

                let noteIdInput = formNote.querySelector('input[name="noteId"]');
                if (!noteIdInput) {
                    noteIdInput = document.createElement("input");
                    noteIdInput.type = "hidden";
                    noteIdInput.name = "noteId";
                    formNote.appendChild(noteIdInput);
                }
                noteIdInput.setAttribute("value", lessonNoteId);


                editingNoteElement = item;
                activeNoteSeconds = secs;

                if (currentNoteTimeText) {
                    currentNoteTimeText.textContent = formatTime(activeNoteSeconds);
                }
                if (noteInputText) {
                    noteInputText.value = content;
                    noteInputText.focus();
                }
                return;
            }

        });
    }
}

function initializeReviews() {
    const reviewDataEl = document.getElementById("course-review-data");
    if (!reviewDataEl) return;

    const showReviewPrompt = reviewDataEl.dataset.showPrompt === 'true';
    const courseId = reviewDataEl.dataset.courseId;
    const successMessage = reviewDataEl.dataset.successMessage;
    const errorMessage = reviewDataEl.dataset.errorMessage;

    // Show popup after 3 seconds if not hidden by user
    if (showReviewPrompt && courseId) {
        const dontShowKey = "dont_show_review_course_" + courseId;
        const isHidden = localStorage.getItem(dontShowKey);

        if (!isHidden) {
            setTimeout(function () {
                const modalEl = document.getElementById('courseReviewModal');
                if (modalEl) {
                    const reviewModal = new bootstrap.Modal(modalEl);
                    reviewModal.show();
                }
            }, 3000);
        }
    }

    // Handle "Don't show again" button click
    const dontShowBtn = document.getElementById("dontShowAgainBtn");
    if (dontShowBtn && courseId) {
        dontShowBtn.addEventListener("click", function () {
            const dontShowKey = "dont_show_review_course_" + courseId;
            localStorage.setItem(dontShowKey, "true");

            const modalEl = document.getElementById('courseReviewModal');
            const modalInstance = bootstrap.Modal.getInstance(modalEl);
            if (modalInstance) {
                modalInstance.hide();
            }
        });
    }

    // Handle tab review button click
    const tabReviewBtn = document.getElementById("tab-review-btn");
    if (tabReviewBtn) {
        tabReviewBtn.addEventListener("click", function () {
            const modalEl = document.getElementById('courseReviewModal');
            if (modalEl) {
                const reviewModal = new bootstrap.Modal(modalEl);
                reviewModal.show();
            }
        });
    }

    // Check backend messages and show status modal
    if (successMessage) {
        showStatusModal(true, successMessage);
    } else if (errorMessage) {
        showStatusModal(false, errorMessage);
    }
}

function showStatusModal(success, message) {
    const titleEl = document.getElementById("statusTitle");
    const messageEl = document.getElementById("statusMessage");
    const iconEl = document.getElementById("statusIcon");
    const iconBgEl = document.getElementById("statusIconBg");
    const confirmBtn = document.getElementById("statusConfirmBtn");

    if (messageEl) messageEl.textContent = message;

    if (success) {
        if (titleEl) {
            titleEl.textContent = "Thành công!";
            titleEl.className = "fw-bold font-heading text-success mb-2";
        }
        if (iconBgEl) iconBgEl.style.background = "#e8f5e9";
        if (iconEl) {
            iconEl.className = "text-success";
            iconEl.setAttribute("data-lucide", "check-circle");
        }
        if (confirmBtn) confirmBtn.className = "btn btn-success w-100 rounded-pill py-2.5 fw-bold";
    } else {
        if (titleEl) {
            titleEl.textContent = "Thất bại";
            titleEl.className = "fw-bold font-heading text-danger mb-2";
        }
        if (iconBgEl) iconBgEl.style.background = "#ffebee";
        if (iconEl) {
            iconEl.className = "text-danger";
            iconEl.setAttribute("data-lucide", "alert-circle");
        }
        if (confirmBtn) confirmBtn.className = "btn btn-danger w-100 rounded-pill py-2.5 fw-bold";
    }

    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    const statusModalEl = document.getElementById('reviewStatusModal');
    if (statusModalEl) {
        const statusModal = new bootstrap.Modal(statusModalEl);
        statusModal.show();
    }
}

