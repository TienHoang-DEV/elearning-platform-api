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

            // Remove active classes
            tabBtns.forEach(b => b.classList.remove("active"));
            panels.forEach(p => p.classList.add("d-none"));

            // Activate current
            btn.classList.add("active");
            document.getElementById(targetPanelId).classList.remove("d-none");
        });
    });
}

// hàm này gọi đến một Controller sử lí việc cập nhật tiến độ leson bài học
function progressVideo() {
    const video = document.querySelector("#lesson-video");
    const totalLessonTag = document.querySelector("#total-lesson-completed");
    let lessonProgressStatus = video.dataset.lessonProgressStatus;
    let totalLesson = parseInt(totalLessonTag.dataset.totalLesson);
    let totalLessonCompleted = parseInt(totalLessonTag.dataset.totalLessonCompleted);
    let completed = false;
    if (!video) return;
    const lessonId = video.dataset.lessonId;
    video.addEventListener("timeupdate", function () {
        if (completed) return;
        let percent = (video.currentTime / video.duration) * 100;
        if (percent >= 90) {
            completed = true;
            fetch("/lesson-completed/" + lessonId)
                .catch(
                    () => {
                        completed = false;
                    }
                )
            if (lessonProgressStatus === "false") {
                totalLessonCompleted++;
                totalLessonTag.textContent = totalLessonCompleted + "/" + totalLesson + " Bài học";
                lessonProgressStatus = "true";

                // Tự động cập nhật biểu tượng dấu tích trong thanh bên
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

                    // Kiểm tra xem tất cả bài học trong section đã hoàn thành chưa để tích section
                    const accordionItem = currentLessonItem.closest('.accordion-item');
                    if (accordionItem) {
                        const allLessonItems = accordionItem.querySelectorAll('.lesson-item');
                        let allCompleted = true;
                        allLessonItems.forEach(item => {
                            // Bài học hiện tại vừa tích xanh xong (chưa kết xuất lại HTML nên ta check cả indicator hiện tại lẫn class bg-success)
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
    })
}

function initializeVideo() {
    const videoTag = document.querySelector("#lesson-video");
    const lessonId = videoTag.dataset.lessonId;
    fetch("/lesson/" + lessonId).then((response) => {
        return response.text();
    }).then(text => {
        videoTag.src = text;
    }).catch(
        error => {
            console.log(error);
        }
    )
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
        fetch(`/material/${materialId}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`Fetch /material/${materialId} failed: ${response.status}`);
                }
                return response.text();
            })
            .then(url => {
                if (openLink) openLink.href = url;
                if (downloadLink) downloadLink.href = url;
                if (docFrame) docFrame.src = url;
            })
            .catch(err => {
                console.error("initializeMaterial error for materialId", materialId, err);
                if (openLink) openLink.classList.add('disabled');
                if (downloadLink) downloadLink.classList.add('disabled');
            });
    });
}

// 5. Sidebar toggling
function initializeSidebarToggle() {
    const toggleBtn = document.getElementById("btn-sidebar-toggle");
    const sidebar = document.querySelector(".sidebar-curriculum-container");
    const mainContent = document.querySelector(".main-player-content");

    if (toggleBtn && sidebar && mainContent) {
        toggleBtn.addEventListener("click", () => {
            sidebar.classList.toggle("d-none");

            // Adjust left side width class
            if (sidebar.classList.contains("d-none")) {
                mainContent.className = "col-12 main-player-content";
            } else {
                mainContent.className = "col-lg-8 col-12 main-player-content";
            }
        });
    }
}
