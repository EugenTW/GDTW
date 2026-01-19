document.addEventListener('DOMContentLoaded', function () {
    const selectedFiles = [];
    const uploadButton = document.getElementById('upload-button');
    const passwordInput = document.getElementById('password-input');
    const fileInput = document.getElementById('file-input');
    const fileList = document.getElementById('file-list');
    const uploadArea = document.getElementById('upload-area');
    const uploadOverlay = document.getElementById('upload-overlay');

    uploadArea.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', handleFileSelect);
    passwordInput.addEventListener('blur', validatePassword);
    uploadButton.addEventListener('click', uploadImages);

    function handleFileSelect(event) {
        const files = event.target.files;
        let totalSize = 0;

        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const fileType = file.type;

            if (!fileType.match(/(jpg|jpeg|png|gif|webp)$/i)) {
                alert(`不支援的檔案格式 - Unsupported file format: ${file.name}`);
                continue;
            }

            if (file.size > 50 * 1024 * 1024) {
                alert(`單檔不得超過50MB - File too large (max 50MB): ${file.name}`);
                continue;
            }

            if (selectedFiles.length >= 50) {
                alert("單次最多50個檔案 - Up to 50 files only.");
                break;
            }

            totalSize += file.size;
            if (totalSize > 500 * 1024 * 1024) {
                alert("總檔案大小不得超過500MB - Total file size exceeds 500MB.");
                break;
            }

            const isDuplicate = selectedFiles.some(existingFile =>
                existingFile.name === file.name && existingFile.size === file.size
            );
            if (isDuplicate) continue;

            selectedFiles.push(file);
        }

        selectedFiles.sort((a, b) => a.name.localeCompare(b.name));
        displaySelectedFiles();
        toggleUploadButton();
    }

    function displaySelectedFiles() {
        fileList.innerHTML = '';

        const readerPromises = selectedFiles.map((file, index) => {
            return new Promise((resolve) => {
                const reader = new FileReader();
                reader.onload = function (e) {
                    resolve({ index, result: e.target.result, name: file.name });
                };
                reader.readAsDataURL(file);
            });
        });

        Promise.all(readerPromises).then(previews => {
            previews.sort((a, b) => a.name.localeCompare(b.name));

            previews.forEach(({ index, result, name }) => {
                const fileItem = document.createElement('div');
                fileItem.className = 'file-item';
                fileItem.innerHTML = `
                    <img src="${result}" class="thumbnail" alt="Image Preview" title="${name}">
                    <img src="/images/delete.png" class="remove-button" data-index="${index}" alt="Remove" title="Remove">
                `;
                fileList.appendChild(fileItem);
            });

            toggleUploadButton();
        });
    }

    fileList.addEventListener('click', function (event) {
        if (event.target.classList.contains('remove-button')) {
            const index = event.target.getAttribute('data-index');
            removeFile(index);
        }
    });

    function removeFile(index) {
        selectedFiles.splice(index, 1);
        selectedFiles.sort((a, b) => a.name.localeCompare(b.name));
        displaySelectedFiles();
        toggleUploadButton();
    }

    function toggleUploadButton() {
        uploadButton.disabled = selectedFiles.length === 0;
    }

    function validatePassword() {
        const passwordValue = passwordInput.value;
        if (passwordValue && !/^[A-Za-z0-9]{4,10}$/.test(passwordValue)) {
            alert('密碼限4~10位英文與數字。 -  Password must be 4–10 alphanumeric characters.');
            passwordInput.value = '';
        }
    }

    let isUploading = false;

    async function uploadImages() {
        if (isUploading) return;

        isUploading = true;
        uploadButton.disabled = true;
        uploadOverlay.style.display = "flex";

        if (selectedFiles.length === 0) {
            alert("請至少選擇一個檔案 - Please select at least one file.");
            isUploading = false;
            uploadButton.disabled = false;
            uploadOverlay.style.display = "none";
            return;
        }

        const expiryDays = document.getElementById('expiry-select').value;
        const nsfw = document.querySelector('input[name="nsfw"]:checked').value === 'true';
        const password = passwordInput.value;

        const formData = new FormData();
        selectedFiles.forEach(file => formData.append("files", file));
        formData.append("expiryDays", expiryDays);
        formData.append("nsfw", nsfw);
        formData.append("password", password);

        const maxRetries = 3;
        let attempt = 0;

        async function tryUpload() {
            try {
                const response = await fetch('/is_api/create_new_album', {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    const result = await response.json();
                    const siaCode = result.sia_code;
                    window.location.href = `/a/${siaCode}`;
                } else if (response.status === 429) {
                    if (attempt < maxRetries) {
                        attempt++;
                        setTimeout(tryUpload, 1000);
                    } else {
                        alert("伺服器忙碌，稍後再試。 - Server busy. Try later.");
                        uploadOverlay.style.display = "none";
                    }
                } else if (response.status === 507) {
                    alert("上傳失敗 - Upload failed: 伺服器容量不足，暫停此項服務。 - Insufficient server storage.");
                    uploadOverlay.style.display = "none";
                } else {
                    alert("上傳失敗 - Upload failed: 未知錯誤 - Unknown error.");
                    uploadOverlay.style.display = "none";
                }
            } catch (error) {
                console.error("上傳錯誤 - Error during upload:", error);
                alert("上傳過程中發生錯誤 - An error occurred during the upload.");
                uploadOverlay.style.display = "none";
            } finally {
                setTimeout(() => {
                    isUploading = false;
                    uploadButton.disabled = false;
                }, 1000);
            }
        }

        tryUpload();
    }
});
