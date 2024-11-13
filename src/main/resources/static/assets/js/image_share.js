document.addEventListener('DOMContentLoaded', function () {
    const selectedFiles = [];
    const uploadButton = document.getElementById('upload-button');
    const passwordInput = document.getElementById('password-input');
    const fileInput = document.getElementById('file-input');
    const fileList = document.getElementById('file-list');
    const uploadArea = document.getElementById('upload-area');

    // Click to trigger file input
    uploadArea.addEventListener('click', () => fileInput.click());

    // Handle file selection
    fileInput.addEventListener('change', handleFileSelect);

    // Validate password on blur
    passwordInput.addEventListener('blur', validatePassword);

    // Handle image upload
    uploadButton.addEventListener('click', uploadImages);

    // Handle file selection and filtering
    function handleFileSelect(event) {
        const files = event.target.files;

        for (let i = 0; i < files.length; i++) {
            const file = files[i];
            const fileType = file.type;

            // Check if the file format is supported
            if (!fileType.match(/(jpg|jpeg|png|gif|webp)$/i)) {
                alert(`不支援的檔案格式 - Unsupported file format: ${file.name}`);
                continue;
            }

            // Check if file size exceeds 20MB
            if (file.size > 15 * 1024 * 1024) {
                alert(`單檔不得超過15MB - File too large (max 15MB): ${file.name}`);
                continue;
            }

            // Limit the number of files to 50
            if (selectedFiles.length >= 50) {
                alert("單次最多50個檔案 - Up to 50 files only.");
                break;
            }

            // Skip if the file is already selected
            const isDuplicate = selectedFiles.some(existingFile =>
                existingFile.name === file.name && existingFile.size === file.size
            );
            if (isDuplicate) continue;

            // Add file to the selected list
            selectedFiles.push(file);
        }

        displaySelectedFiles();
        toggleUploadButton();
    }

    // Display selected files
    function displaySelectedFiles() {
        fileList.innerHTML = '';

        selectedFiles.forEach((file, index) => {
            const fileItem = document.createElement('div');
            fileItem.className = 'file-item';

            const reader = new FileReader();
            reader.onload = function (e) {
                fileItem.innerHTML = `
                    <img src="${e.target.result}" class="thumbnail" alt="Image Preview" title="${file.name}">
                    <img src="/images/delete.png" class="remove-button" data-index="${index}" alt="Remove" title="Remove">
                `;
                fileList.appendChild(fileItem);
            };
            reader.readAsDataURL(file);
        });

        toggleUploadButton();
    }

    // Handle file removal
    fileList.addEventListener('click', function (event) {
        if (event.target.classList.contains('remove-button')) {
            const index = event.target.getAttribute('data-index');
            removeFile(index);
        }
    });

    // Remove a specific file and update the list
    function removeFile(index) {
        selectedFiles.splice(index, 1);

        // Remove the corresponding DOM element
        const fileElement = fileList.querySelector(`.remove-button[data-index="${index}"]`).parentElement;
        if (fileElement) fileElement.remove();

        // Update upload button state
        toggleUploadButton();
    }

    // Enable or disable the upload button
    function toggleUploadButton() {
        uploadButton.disabled = selectedFiles.length === 0;
    }

    // Validate the password format (4-10 digits)
    function validatePassword() {
        const passwordValue = passwordInput.value;
        if (passwordValue && !/^\d{4,10}$/.test(passwordValue)) {
            alert('密碼限4~10位數字 - Password must be 4 to 10 digits.');
            passwordInput.value = '';
        }
    }

    let isUploading = false;

    async function uploadImages() {
        if (isUploading) return;
    
        isUploading = true;
        uploadButton.disabled = true;
        $("#upload-overlay").show();  
                
    
        if (selectedFiles.length === 0) {
            alert("請至少選擇一個檔案 - Please select at least one file.");
            isUploading = false;
            uploadButton.disabled = false;
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
    
        try {
            const response = await fetch('/is_api/create_new_album', {
                method: 'POST',
                body: formData
            });
    
            if (response.ok) {
                const result = await response.json();
                const siaCode = result.sia_code;
                
                window.location.href = `/a/${siaCode}`;
            } else {
                alert("上傳失敗 - Upload failed: " + response.statusText);
                $("#upload-overlay").hide();
            }
        } catch (error) {
            console.error("上傳錯誤 - Error during upload:", error);
            alert("上傳過程中發生錯誤 - An error occurred during the upload.");
            $("#upload-overlay").hide();
        } finally {
            setTimeout(() => {
                isUploading = false;
                uploadButton.disabled = false;
            }, 1000);
            $("#upload-overlay").hide();
        }
    }


});
