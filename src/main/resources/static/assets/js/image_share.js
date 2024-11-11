const selectedFiles = [];

function handleFileSelect(event) {
    const files = event.target.files;
    const fileList = document.getElementById('file-list');
    fileList.innerHTML = ''; 

    for (let i = 0; i < files.length; i++) {
        const file = files[i];
        const fileType = file.type;
  
        if (!fileType.match(/(jpg|jpeg|png|gif|webp)$/i)) {
            alert(`不支援的檔案格式 - Unsupported file format: ${file.name}`);
            continue;
        }
       
        if (selectedFiles.length >= 50) {
            alert("單次最多50個檔案 - You can select up to 50 files only.");
            break;
        }
      
        const isDuplicate = selectedFiles.some(existingFile => 
            existingFile.name === file.name && existingFile.size === file.size
        );
        if (isDuplicate) {
            continue; 
        }
      
        selectedFiles.push(file);
    }

    displaySelectedFiles();
}

function displaySelectedFiles() {
    const fileList = document.getElementById('file-list');
    fileList.innerHTML = ''; 

    selectedFiles.forEach((file, index) => {
        const fileItem = document.createElement('div');
        fileItem.className = 'file-item';

        const reader = new FileReader();
        reader.onload = function (e) {
            fileItem.innerHTML = `
                <img src="${e.target.result}" class="thumbnail" alt="Image Preview">
                <img src="/images/delete.png" class="remove-button" onclick="removeFile(${index})" alt="Remove" title="Remove">
            `;
            fileList.appendChild(fileItem);
        };
        reader.readAsDataURL(file);
    });
}



function removeFile(index) {
    selectedFiles.splice(index, 1);
    displaySelectedFiles();
}


function validatePassword() {
    const passwordInput = document.getElementById('password-input');
    const passwordValue = passwordInput.value;
    
    if (!/^\d*$/.test(passwordValue)) {
        alert('僅允許輸入數字 - Numbers only allowed.');
        passwordInput.value = ''; 
    }
}



