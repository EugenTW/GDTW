document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    if (code) {
        document.getElementById("code-display").textContent = code;        
    }
});

$(document).ready(function() {    
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');    
    
    if (code) {        
        const shortUrl = `${window.location.origin}/${code}`;
        $('.shorten-url').text(shortUrl);        
        
        $.ajax({
            url: '/su_api/get_original_url',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ code: code }),
            success: function(response) {              
                $('.original-url').text(response);                
                $('.button.green').on('click', function() {
                    window.location.href = response;
                });
            },
            error: function(xhr) {
                let errorMessage;
                if (xhr.status === 404 || xhr.status === 410) {                    
                    errorMessage = xhr.responseText;
                } else {
                    errorMessage = "內部伺服器錯誤! Internal Server Error!";
                }
                console.error('Error fetching original URL:', xhr);
                $('.original-url').text(errorMessage);
            }
        });
    } else {
        $('.shorten-url').text('短網址無效 / Invalid short URL.');
         $('.original-url').text('請提供有效的短網址 / Please provide a valid short URL.');
    }
    
    $('.button.red').on('click', function() {
        window.location.href = '/';
    });
});






