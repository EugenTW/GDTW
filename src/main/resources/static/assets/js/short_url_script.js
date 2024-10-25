$(document).ready(function() {
    $("#long_url").on('blur', function() {
        var url = $(this).val();
        var httpsRegex = /^https:\/\/[a-zA-Z0-9\-\.]+\.[a-z]{2,}(:\d+)?(\/.*)?$/;
       
        if (url.length > 200) {
            alert("您輸入的網址長度已超過 200 字元，請確認是否需要縮短網址。");
        }
       
        if (!httpsRegex.test(url)) {
            alert("請輸入正確的 URL，必須使用 HTTPS 並且符合最基本的 URL 結構。The URL must start with HTTPS and follow the basic structure.");
            $(this).val(''); 
        }
    });

    $("#generate").click(function() {
        var longUrl = $("#long_url").val();
        if (longUrl) {
            $.ajax({
                url: '/su_api/create_new_short_url',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ originalUrl: longUrl }),
                success: function(response) {
                    try {
                        
                        if (response && typeof response === "object") {
                            // Extract values from JSON response
                            var shortUrl = response.fullShortUrl;
                            var message = response.message;
                            var safeUrlResult = response.safeUrlResult;
    
                            // Console log for debugging purposes
                            console.log("Safe URL Result: ", safeUrlResult);
    
                            // Check if shortUrl is available, else display the message
                            if (shortUrl) {
                                $("#shorten_url").text(shortUrl).css("background-color", "yellow");
                                $("#shorten_url").off('click').on('click', copyToClipboard);
    
                                // Generate QR code only if shortUrl is present
                                $("#qrcode").empty(); // Clear previous QR code
                                var qrcode = new QRCode(document.getElementById("qrcode"), {
                                    text: shortUrl,  // Use the short URL for the QR code
                                    width: 100,
                                    height: 100
                                });
    
                                // Show the QR code
                                $("#qrcode").css("display", "block");
                            } else {
                                // Display the message from the response
                                $("#shorten_url").text(message).css("background-color", "pink");
    
                                // Hide the QR code if there's no short URL
                                $("#qrcode").css("display", "none");
                            }
                        } else {                            
                            $("#shorten_url").text("無效的回應! 請稍後重試! Invalid response! Please try again later.").css("background-color", "pink");
                            $("#qrcode").css("display", "none");
                        }
                    } catch (e) {                        
                        console.error("Error processing the response: ", e);
                        $("#shorten_url").text("回應處理錯誤! 請稍後重試! Error processing response! Please try again later.").css("background-color", "pink");
                        $("#qrcode").css("display", "none");
                    }
                },
                error: function(xhr, textStatus, errorThrown) {                    
                    var errorMessage = "錯誤: " + xhr.responseText + " / Error: " + xhr.responseText;
                    $("#shorten_url").text(errorMessage).css("background-color", "pink");    
                    // Hide the QR code on error
                    $("#qrcode").css("display", "none");
                }
            });
        } else {
            alert("請輸入一個網址 / Please enter a valid url.");
        }
    });
    
    
});


function copyToClipboard() {
    const copyText = document.getElementById("shorten_url").textContent;
    navigator.clipboard.writeText(copyText)
        .then(() => {
            showAlert("短網址已複製到剪貼簿 / The short URL is copied!", "green");
        })
        .catch(err => {
            console.error('Failed to copy text: ', err);
            showAlert("無法複製短網址 / Unable to copy the short URL.", "red");
        });
}


function showAlert(message) {
    const alertDiv = document.createElement("div");
    alertDiv.textContent = message;
    alertDiv.style.position = "fixed"; 
    alertDiv.style.top = "50%";
    alertDiv.style.left = "50%";
    alertDiv.style.transform = "translate(-50%, -50%)";
    alertDiv.style.backgroundColor = "#C10066"; 
    alertDiv.style.color = "#FFFFFF"; 
    alertDiv.style.padding = "15px";
    alertDiv.style.borderColor = "#FFB7DD";
    alertDiv.style.borderWidth ="3px"
    alertDiv.style.borderRadius = "5px";
    alertDiv.style.zIndex = "9999"; 
    document.body.appendChild(alertDiv);
    setTimeout(function() {
        document.body.removeChild(alertDiv);
    }, 1000);
}


