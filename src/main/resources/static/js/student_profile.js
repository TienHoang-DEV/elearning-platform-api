document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Lucide Icons
    if (typeof lucide !== 'undefined') {
        lucide.createIcons();
    }

    // 2. Avatar Selection and Operations (Visual Previews before Form Submission)
    const btnUploadAvatar = document.getElementById('btn-upload-avatar');
    const btnDeleteAvatar = document.getElementById('btn-delete-avatar');
    const btnSaveAvatar = document.getElementById('btn-save-avatar');
    const avatarFileInput = document.getElementById('avatarFileInput');
    const deleteAvatarInput = document.getElementById('deleteAvatarInput');
    const avatarContainer = document.getElementById('avatarContainer');

    const enableSaveAvatarButton = () => {
        if (btnSaveAvatar) {
            btnSaveAvatar.removeAttribute('disabled');
            btnSaveAvatar.classList.remove('bg-opacity-25', 'cursor-not-allowed');
            btnSaveAvatar.classList.add('bg-primary');
        }
    };

    if (btnUploadAvatar && avatarFileInput) {
        btnUploadAvatar.addEventListener('click', () => {
            avatarFileInput.click();
        });

        avatarFileInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (file) {
                // Check size limit (1MB)
                if (file.size > 1024 * 1024) {
                    alert('Kích thước ảnh đại diện không được vượt quá 1MB!');
                    avatarFileInput.value = '';
                    return;
                }

                const reader = new FileReader();
                reader.onload = (event) => {
                    avatarContainer.innerHTML = `<img src="${event.target.result}" alt="Avatar" class="w-100 h-100 object-fit-cover" id="studentAvatarImg"/>`;
                    deleteAvatarInput.value = "false";
                    enableSaveAvatarButton();
                };
                reader.readAsDataURL(file);
            }
        });
    }

    if (btnDeleteAvatar && deleteAvatarInput) {
        btnDeleteAvatar.addEventListener('click', () => {
            if (confirm('Bạn có chắc chắn muốn xóa ảnh đại diện hiện tại?')) {
                avatarContainer.innerHTML = `<span id="studentAvatarInitial">H</span>`;
                deleteAvatarInput.value = "true";
                if (avatarFileInput) {
                    avatarFileInput.value = '';
                }
                enableSaveAvatarButton();
            }
        });
    }

    // 3. Persistent Global Theme Switcher
    const interfaceMode = document.getElementById('interface-mode');
    if (interfaceMode) {
        // Initialize state from localStorage
        const currentTheme = localStorage.getItem('theme');
        if (currentTheme === 'dark') {
            interfaceMode.value = 'Chế độ tối';
        } else {
            interfaceMode.value = 'Chế độ sáng';
        }

        interfaceMode.addEventListener('change', (e) => {
            const selectedMode = e.target.value;
            if (selectedMode === 'Chế độ tối') {
                localStorage.setItem('theme', 'dark');
                document.documentElement.classList.add('dark-theme');
            } else {
                localStorage.setItem('theme', 'light');
                document.documentElement.classList.remove('dark-theme');
            }
        });
    }

    // 4. Search Bar Redirect
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                const query = searchInput.value.trim();
                if (query) {
                    window.location.href = `/courses?search=${encodeURIComponent(query)}`;
                }
            }
        });
    }
});
