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

function progressVideo() {
    const video = document.querySelector("#lesson-video");
    let completed = false;
    if (!video) return;
    const lessonId = video.dataset.lessonId;
    video.addEventListener("timeupdate", function () {
        if (completed) return;
        let percent = (video.currentTime / video.duration) * 100;
        if (percent >= 90) {
            completed = true;
            fetch("/lesson-completed/" + lessonId).catch(
                () => {
                    completed = false;
                }
            )
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
    })
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
