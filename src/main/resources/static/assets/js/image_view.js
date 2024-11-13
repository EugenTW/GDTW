document.addEventListener('DOMContentLoaded', async function () {
    const path = window.location.pathname;
    const isAlbumMode = path.startsWith('/a/');
    const isSingleMode = path.startsWith('/i/');
    const code = path.split('/')[2];

    
    const statusApiUrl = isAlbumMode ? '/is_api/isAlbumPasswordNeeded' : '/is_api/isImagePasswordNeeded';
    const passwordApiUrl = isAlbumMode ? '/is_api/checkAlbumPassword' : '/is_api/checkImagePassword';

    
    const requestData = {
        code: code
    };

    try {
        
        const response = await fetch(statusApiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        const result = await response.json();

        if (!result.isValid) {
            window.location.href = '/error';
            return;
        }

        
        if (result.requiresPassword) {
            showPasswordModal();
        } else {
            initPage();
        }
    } catch (error) {
        console.error('Error:', error);
        window.location.href = '/error'; 
    }
});


function showPasswordModal() {
    document.getElementById('password-modal').classList.remove('hidden');
}


function initPage() {
    console.log("Page initialized successfully");
   
}
