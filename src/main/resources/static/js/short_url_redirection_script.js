document.addEventListener("DOMContentLoaded", function() {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    if (code) {
        document.getElementById("code-display").textContent = code;        
    }
});

$(document).ready(function() {
    // 解析URL參數
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    
    // 檢查code是否存在
    if (code) {
        // 顯示短網址
        const shortUrl = `${window.location.origin}/s/${code}`;
        $('.shorten-url').text(shortUrl);
        
        // 向後端API發送POST請求以獲取原始網址
        $.ajax({
            url: '/su_api/get_original_url',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ code: code }),
            success: function(response) {
                // 顯示返回的原始網址
                $('.original-url').text(response);
                
                // 綠色按鈕點擊事件
                $('.button.green').on('click', function() {
                    window.location.href = response;
                });
            },
            error: function(xhr) {
                let errorMessage;
                if (xhr.status === 404) {
                    errorMessage = xhr.responseText === "此短網址已失效!" ? "此短網址已失效!" : "原始網址未找到!";
                } else {
                    errorMessage = "內部伺服器錯誤!請等待站方維修!";
                }
                console.error('Error fetching original URL:', xhr);
                $('.original-url').text(errorMessage);
            }
        });
    } else {
        $('.shorten-url').text('短網址無效');
        $('.original-url').text('無法獲取原始網址');
    }

    // 紅色按鈕點擊事件
    $('.button.red').on('click', function() {
        window.location.href = '/';
    });
});


