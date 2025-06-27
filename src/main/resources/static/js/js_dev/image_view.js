let authToken;

document.addEventListener('DOMContentLoaded', async function () {
    // === Back to Top ===
    const backToTopButton = document.getElementById('scrollToTop');
    if (backToTopButton) {
        const minBottom = 175;
        const maxBottom = 40;

        window.addEventListener('scroll', () => {
            const scrollTop = window.scrollY;
            const windowHeight = window.innerHeight;
            const docHeight = document.documentElement.scrollHeight;
            const distanceToBottom = docHeight - (scrollTop + windowHeight);

            backToTopButton.style.bottom = distanceToBottom <= minBottom
                ? `${minBottom}px`
                : `${maxBottom}px`;

            backToTopButton.classList.toggle('show', scrollTop > 500);
        });

        backToTopButton.addEventListener('click', () => {
            document.documentElement.scrollTo({ top: 0, behavior: 'smooth' });
            document.body.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // === Image Gallery ===
    const path = window.location.pathname;
    if (path === '/img_view') {
        console.info('/img_view page accessed, skipping image loading logic.');
        return;
    }

    const isAlbumMode = path.startsWith('/a/');
    const code = path.split('/')[2];
    const statusApiUrl = isAlbumMode ? '/is_api/isAlbumPasswordNeeded' : '/is_api/isImagePasswordNeeded';
    const passwordApiUrl = isAlbumMode ? '/is_api/checkAlbumPassword' : '/is_api/checkImagePassword';
    const downloadApiUrl = isAlbumMode ? '/is_api/downloadAlbumImages' : '/is_api/downloadSingleImage';
    const requestData = { code };

    try {
        const response = await fetchWithRetry(statusApiUrl, requestData);
        const result = await response.json();

        if (!result.isValid) {
            window.location.href = '/error_410';
            return;
        }

        if (result.requiresPassword) {
            showPasswordModal();
            setupPasswordValidation(passwordApiUrl, code, isAlbumMode);
        } else {
            if (result.token) {
                authToken = result.token;
            }
            initPage(downloadApiUrl, isAlbumMode);
        }
    } catch (error) {
        window.location.href = '/error';
    }
});

async function initPage(downloadApiUrl, isAlbumMode) {
    hidePasswordModal();
    try {
        const response = await fetch(new URL(downloadApiUrl, window.location.origin), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({token: authToken})
        });

        const result = await response.json();
        if (result.error) {
            alert(result.error);
            return;
        }

        if (isAlbumMode) {
            await displayImagesSequentially(result);
        } else {
            displaySingleImage(result);
        }
    } catch (error) {
        console.error('Error occurred during initPage execution:', error);
        alert('載入圖片失敗，請稍後再試。\nFailed to load images. Please try again later.');
    }
}

async function displayImagesSequentially(data) {
    const gallery = document.getElementById('gallery');
    gallery.innerHTML = '';
    gallery.classList.remove('hidden');

    const endDate = data.siaEndDate ?? 'N/A';
    const totalVisited = data.siaTotalVisited ?? 0;
    const statusDiv = document.createElement('div');
    statusDiv.classList.add('status-area');
    statusDiv.innerHTML = `Expires on: ${endDate} | Views: ${totalVisited}`;
    gallery.appendChild(statusDiv);

    const pageUrlDiv = document.createElement('div');
    pageUrlDiv.classList.add('page-url-text');
    const currentPageUrl = window.location.href;
    const url = new URL(currentPageUrl);
    const cleanUrl = url.host + url.pathname;
    pageUrlDiv.innerHTML = `Share Gallery: ${cleanUrl}`;
    pageUrlDiv.addEventListener('click', function () {
        copyToClipboard(cleanUrl);
        showCopiedMessage(pageUrlDiv);
    });

    const downloadAlbumButton = document.createElement('button');
    downloadAlbumButton.classList.add('download-album-button');
    downloadAlbumButton.innerHTML = '&#11015;&#65039;'; // ⬇️
    downloadAlbumButton.title = '下載所有圖片 - Download Gallery';
    downloadAlbumButton.addEventListener('click', function () {
        downloadEntireAlbum(data.images);
    });

    const albumUrlContainer = document.createElement('div');
    albumUrlContainer.classList.add('album-url-container');
    albumUrlContainer.appendChild(pageUrlDiv);
    albumUrlContainer.appendChild(downloadAlbumButton);
    gallery.appendChild(albumUrlContainer);

    const isNsfw = data.siaNsfw === 1;
    for (const image of data.images) {
        await loadImageSequentially(image, gallery, isNsfw);
    }
}

function loadImageSequentially(image, gallery, isNsfw) {
    return new Promise((resolve) => {
        const photoDiv = document.createElement('div');
        photoDiv.classList.add('photo');

        const imgElement = document.createElement('img');
        imgElement.src = new URL(image.imageUrl, window.location.origin).href;
        imgElement.alt = image.siName;
        imgElement.classList.add('gallery-image');
        imgElement.onerror = function () {
            this.src = '/images/pic_not_found.webp';
        };
        imgElement.onload = () => {
            resolve();
        };

        if (isNsfw) {
            const nsfwMask = document.createElement('div');
            nsfwMask.classList.add('nsfw-mask');
            nsfwMask.innerHTML = 'R15 or R18 Content<br>NSFW - Click to reveal';
            nsfwMask.addEventListener('click', () => {
                document.querySelectorAll('.nsfw-mask').forEach(mask => {
                    mask.style.display = 'none';
                });
            });
            photoDiv.appendChild(imgElement);
            photoDiv.appendChild(nsfwMask);
        } else {
            photoDiv.appendChild(imgElement);
        }

        const urlDiv = document.createElement('div');
        urlDiv.classList.add('single-mode-text');
        urlDiv.innerHTML = `Share : ${image.imageSingleModeUrl}`;
        urlDiv.addEventListener('click', function () {
            copyToClipboard(image.imageSingleModeUrl);
            showCopiedMessage(urlDiv);
        });

        const openLink = document.createElement('a');
        openLink.classList.add('open-link-button');
        openLink.innerHTML = '&#128269;'; // 🔎
        openLink.title = '原始尺寸 - Full Size';
        openLink.href = image.imageUrl;
        openLink.target = '_blank';
        openLink.rel = 'noopener noreferrer';

        const downloadLink = document.createElement('a');
        downloadLink.classList.add('download-link-button');
        downloadLink.innerHTML = '&#11015;&#65039;'; // ⬇️
        downloadLink.title = '下載圖片 - Download';
        downloadLink.addEventListener('click', async (event) => {
            event.preventDefault();
            downloadByUrl(image.imageUrl, image.siName || 'download.jpg');
        });

        const urlContainer = document.createElement('div');
        urlContainer.classList.add('url-container');
        urlContainer.appendChild(urlDiv);
        urlContainer.appendChild(openLink);
        urlContainer.appendChild(downloadLink);

        photoDiv.appendChild(urlContainer);
        gallery.appendChild(photoDiv);
    });
}

function downloadEntireAlbum(images) {
    if (!images || images.length === 0) {
        console.error("No images to download.");
        return;
    }

    const zip = new JSZip();

    const currentPageUrl = window.location.href;
    const url = new URL(currentPageUrl);
    let albumName = url.host + url.pathname;

    albumName = albumName.replace(/[\/:?*"<>|]/g, "_");

    let count = 0;

    images.forEach((image) => {
        fetch(image.imageUrl)
            .then(response => response.blob())
            .then(blob => {
                let fileName = image.siName;
                fileName = fileName.replace(/[\/:?*"<>|]/g, "_");

                zip.file(`${fileName}.jpg`, blob);
                count++;

                if (count === images.length) {
                    zip.generateAsync({ type: "blob" }).then(content => {
                        downloadBlob(content, `${albumName}.zip`);
                    });
                }
            })
            .catch(error => console.error(`Error downloading ${image.imageUrl}:`, error));
    });
}

// Function to display a single image
function displaySingleImage(data) {
    const singlePhotoDiv = document.getElementById('single-photo');
    singlePhotoDiv.innerHTML = '';

    const endDate = data.siEndDate ?? 'N/A';
    const totalVisited = data.siTotalVisited ?? 0;
    const statusDiv = document.createElement('div');
    statusDiv.classList.add('status-area');
    statusDiv.innerHTML = `Expires on: ${endDate} | Views: ${totalVisited}`;

    const pageUrlDiv = document.createElement('div');
    pageUrlDiv.classList.add('page-url-text');
    const currentPageUrl = window.location.href;
    const url = new URL(currentPageUrl);
    const cleanUrl = url.origin + url.pathname;
    pageUrlDiv.innerHTML = `Share: ${cleanUrl}`;
    pageUrlDiv.addEventListener('click', function () {
        copyToClipboard(currentPageUrl);
        showCopiedMessage(pageUrlDiv);
    });

    const statusUrlContainer = document.createElement('div');
    statusUrlContainer.classList.add('photo-url-container');
    statusUrlContainer.appendChild(statusDiv);
    statusUrlContainer.appendChild(pageUrlDiv);
    singlePhotoDiv.appendChild(statusUrlContainer);

    const photoWrapper = document.createElement('div');
    photoWrapper.classList.add('photo-wrapper');

    const imgElement = document.createElement('img');
    const imageUrl = data.imageUrl.startsWith('http') ? data.imageUrl : new URL(data.imageUrl, window.location.origin).href;
    imgElement.src = imageUrl;
    imgElement.alt = data.siName;
    imgElement.id = 'photo-img';

    imgElement.addEventListener('error', function() {
        this.src = '/images/pic_not_found.webp';
    });

    photoWrapper.appendChild(imgElement);

    if (data.siNsfw === 1) {
        const nsfwMask = document.createElement('div');
        nsfwMask.classList.add('nsfw-mask');
        nsfwMask.textContent = 'NSFW - Click to reveal';
        nsfwMask.addEventListener('click', () => {
            nsfwMask.classList.add('hidden');
        });
        photoWrapper.appendChild(nsfwMask);
    }

    singlePhotoDiv.appendChild(photoWrapper);

    const openLink = document.createElement('a');
    openLink.classList.add('open-link-button');
    openLink.innerHTML = '&#128269;'; // 🔎
    openLink.title = '原始尺寸 - Full Size';
    openLink.href = imageUrl;
    openLink.target = '_blank';
    openLink.rel = 'noopener noreferrer';

    const downloadLink = document.createElement('a');
    downloadLink.classList.add('download-link-button');
    downloadLink.innerHTML = '&#11015;&#65039;'; // ⬇️
    downloadLink.title = '下載圖片 - Download';

    downloadLink.addEventListener('click', async (event) => {
        event.preventDefault();
        downloadByUrl(data.imageUrl, data.siName || 'download.jpg');
    });

    const urlContainer = document.createElement('div');
    urlContainer.classList.add('url-container');
    urlContainer.appendChild(openLink);
    urlContainer.appendChild(downloadLink);

    singlePhotoDiv.appendChild(urlContainer);
    singlePhotoDiv.classList.remove('hidden');
}


// Show the password input modal
function showPasswordModal() {
    const passwordModal = document.getElementById('password-modal');
    if (passwordModal) {
        passwordModal.classList.remove('hidden');
    }
}

// Hide the password input modal
function hidePasswordModal() {
    const passwordModal = document.getElementById('password-modal');
    if (passwordModal) {
        passwordModal.classList.add('hidden');
    }
}

// Set up password validation and submission
function setupPasswordValidation(passwordApiUrl, code, isAlbumMode) {
    const passwordForm = document.getElementById('password-form');
    const passwordInput = document.getElementById('password-input');
    const passwordSubmit = document.getElementById('password-submit');

    passwordSubmit.disabled = true;

    // Validate password input on blur
    passwordInput.addEventListener('blur', function () {
        const password = passwordInput.value.trim();
        const isValid = /^\d{4,10}$/.test(password);

        // If password format is invalid, clear the input and disable the submit button
        if (!isValid && password !== '') {
            alert('密碼格式不符合，請輸入4~10位數字\nPassword format is incorrect. Please enter 4-10 digits.');
            passwordInput.value = '';
            passwordSubmit.disabled = true;

            setTimeout(() => {
                passwordInput.focus();
            }, 0);
        }
    });

    // Enable the submit button if the input is valid
    passwordInput.addEventListener('input', function () {
        const password = passwordInput.value.trim();
        const isValid = /^\d{4,10}$/.test(password);
        passwordSubmit.disabled = !isValid;
    });

    // Handle password form submission
    passwordForm.addEventListener('submit', async function (event) {
        event.preventDefault();
        const password = passwordInput.value.trim();
        if (password === '') return;
        passwordSubmit.disabled = true
        const passwordRequestData = {code: code, password: password};

        try {
            const response = await fetchWithRetry(passwordApiUrl, passwordRequestData);
            const result = await response.json();

            if (result.checkPassword) {
                if (result.token) {
                    authToken = result.token;
                }
                hidePasswordModal();
                initPage(isAlbumMode ? '/is_api/downloadAlbumImages' : '/is_api/downloadSingleImage', isAlbumMode);
            } else {
                alert('密碼錯誤，請重新輸入\nIncorrect password, please try again.');
                passwordInput.value = '';
                passwordSubmit.disabled = true;

                setTimeout(() => {
                    passwordInput.focus();
                }, 0);
            }
        } catch (error) {
            alert('系統錯誤，請稍後再試。\nSystem error, please try again later.');
            passwordSubmit.disabled = false;
        }
    });
}

// Fetch with retry logic
async function fetchWithRetry(url, data, maxRetries = 3) {
    let retries = 0;
    while (retries < maxRetries) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(data)
            });
            if (response.status === 429) {
                await new Promise(resolve => setTimeout(resolve, 1000));
                retries++;
            } else {
                return response;
            }
        } catch (error) {
            break;
        }
    }
    throw new Error('Too many attempts, please try again later.');
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(() => {
        console.log('Text copied to clipboard:', text);
    }).catch(err => {
        console.error('Failed to copy text: ', err);
    });
}

function showCopiedMessage() {
    const toast = document.createElement('div');
    toast.textContent = '已複製網址 - URL Copied!';
    toast.classList.add('toast-message');
    document.body.appendChild(toast);

    setTimeout(() => {
        document.body.removeChild(toast);
    }, 1500);
}

function downloadBlob(blob, filename) {
    const blobUrl = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = blobUrl;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(blobUrl);
}

async function downloadByUrl(url, filename) {
    try {
        const response = await fetch(url, { mode: 'cors' });
        const blob = await response.blob();
        downloadBlob(blob, filename);
    } catch (err) {
        console.error('Download failed:', err);
        alert('下載失敗! - Download Failed!');
    }
}

