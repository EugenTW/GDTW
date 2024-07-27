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
       
        const shortUrl = `${window.location.origin}/s/${code}`;
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
                if (xhr.status === 404) {
                    errorMessage = xhr.responseText === "此短網址尚未建立! Original url not found!" ? "原始網址未找到!" : "此短網址已失效!";
                } else if (xhr.status === 410) {
                    errorMessage = "此短網址已失效!";
                } else {
                    errorMessage = "內部伺服器錯誤!";
                }
                console.error('Error fetching original URL:', xhr);
                $('.original-url').text(errorMessage);
            }
        });
    } else {
        $('.shorten-url').text('短網址無效 / Invalid short URL.');
        $('.original-url').text('無法獲取原始網址 / Original URL not found.');
    }
    // 紅色按鈕點擊事件
    $('.button.red').on('click', function() {
        window.location.href = '/';
    });
    
});





