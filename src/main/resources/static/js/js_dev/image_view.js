let authToken;

document.addEventListener('DOMContentLoaded', async function () {
    const backToTopButton = document.getElementById('scrollToTop');
    if (backToTopButton) {
        const minBottom = 175;
        const maxBottom = 40;
        window.addEventListener('scroll', () => {
            const scrollTop = window.scrollY;
            const windowHeight = window.innerHeight;
            const docHeight = document.documentElement.scrollHeight;
            const distanceToBottom = docHeight - (scrollTop + windowHeight);
            backToTopButton.style.bottom = distanceToBottom <= minBottom ? `${minBottom}px` : `${maxBottom}px`;
            backToTopButton.classList.toggle('show', scrollTop > 500);
        });
        backToTopButton.addEventListener('click', () => {
            document.documentElement.scrollTo({ top: 0, behavior: 'smooth' });
            document.body.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    const path = window.location.pathname;
    if (path === '/img_view') return;

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
            body: JSON.stringify({ token: authToken })
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
    const cleanUrl = new URL(window.location.href).host + window.location.pathname;
    pageUrlDiv.innerHTML = `Share Gallery: ${cleanUrl}`;
    pageUrlDiv.addEventListener('click', () => {
        copyToClipboard(cleanUrl);
        showCopiedMessage(pageUrlDiv);
    });

    const downloadAlbumButton = document.createElement('button');
    downloadAlbumButton.classList.add('download-album-button');
    downloadAlbumButton.innerHTML = '&#11015;&#65039;';
    downloadAlbumButton.title = '下載所有圖片 - Download Gallery';
    downloadAlbumButton.addEventListener('click', () => {
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
        imgElement.onload = () => resolve();

        if (isNsfw) {
            const nsfwMask = document.createElement('div');
            nsfwMask.classList.add('nsfw-mask');
            nsfwMask.innerHTML = 'R15 or R18 Content<br>NSFW - Click to reveal';
            nsfwMask.addEventListener('click', () => {
                document.querySelectorAll('.nsfw-mask').forEach(mask => mask.style.display = 'none');
            });
            photoDiv.appendChild(imgElement);
            photoDiv.appendChild(nsfwMask);
        } else {
            photoDiv.appendChild(imgElement);
        }

        const urlDiv = document.createElement('div');
        urlDiv.classList.add('single-mode-text');
        urlDiv.innerHTML = `Share : ${image.imageSingleModeUrl}`;
        urlDiv.addEventListener('click', () => {
            copyToClipboard(image.imageSingleModeUrl);
            showCopiedMessage(urlDiv);
        });

        const openLink = document.createElement('a');
        openLink.classList.add('open-link-button');
        openLink.innerHTML = '&#128269;';
        openLink.title = '原始尺寸 - Full Size';
        openLink.href = image.imageUrl;
        openLink.target = '_blank';
        openLink.rel = 'noopener noreferrer';

        const downloadLink = document.createElement('a');
        downloadLink.classList.add('download-link-button');
        downloadLink.innerHTML = '&#11015;&#65039;';
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

function setupPasswordValidation(passwordApiUrl, code, isAlbumMode) {
    const passwordForm = document.getElementById('password-form');
    const passwordInput = document.getElementById('password-input');
    const passwordSubmit = document.getElementById('password-submit');
    passwordSubmit.disabled = true;

    passwordInput.addEventListener('blur', () => {
        const password = passwordInput.value.trim();
        const isValid = /^\d{4,10}$/.test(password);
        if (!isValid && password !== '') {
            alert('密碼格式不符合，請輸入4~10位數字\nPassword format is incorrect. Please enter 4-10 digits.');
            passwordInput.value = '';
            passwordSubmit.disabled = true;
            setTimeout(() => passwordInput.focus(), 0);
        }
    });

    passwordInput.addEventListener('input', () => {
        const password = passwordInput.value.trim();
        passwordSubmit.disabled = !/^\d{4,10}$/.test(password);
    });

    passwordForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const password = passwordInput.value.trim();
        if (password === '') return;
        const passwordRequestData = { code, password };

        try {
            const response = await fetch(passwordApiUrl, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(passwordRequestData)
            });
            if (!response.ok) {
                alert('密碼驗證失敗，請再試一次。\nPassword verification failed.');
                return;
            }

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
                setTimeout(() => passwordInput.focus(), 0);
            }
        } catch (error) {
            alert('系統錯誤，請稍後再試。\nSystem error, please try again later.');
        }
    });
}

async function fetchWithRetry(url, data, maxRetries = 3) {
    let retries = 0;
    while (retries < maxRetries) {
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            if (response.status === 429) {
                await new Promise(resolve => setTimeout(resolve, 1000));
                retries++;
            } else {
                if (!response.ok) {
                    const text = await response.text();
                    throw new Error(`HTTP ${response.status}: ${text}`);
                }
                return response;
            }
        } catch {
            break;
        }
    }
    throw new Error('Too many attempts, please try again later.');
}

function showPasswordModal() {
    const modal = document.getElementById('password-modal');
    if (modal) modal.classList.remove('hidden');
}

function hidePasswordModal() {
    const modal = document.getElementById('password-modal');
    if (modal) modal.classList.add('hidden');
}

function copyToClipboard(text) {
    navigator.clipboard.writeText(text).catch(() => {});
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
    } catch {
        alert('下載失敗! - Download Failed!');
    }
}
