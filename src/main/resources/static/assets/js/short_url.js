$(document).ready(function () {
    const $longUrlInput = $("#long_url");
    const $generateButton = $("#generate");
    const maxRetries = 5;

    $longUrlInput.on('blur', function () {
        const url = $(this).val().trim();

        if (!url) {
            $generateButton.prop('disabled', true);
            return;
        }

        if (url.length > 200) {
            alert("您輸入的網址長度已超過 200 字元。 - The URL exceeds 200 characters.");
            $(this).val('');
            $generateButton.prop('disabled', true);
            return;
        }

        const httpsRegex = /^https:\/\/[a-zA-Z0-9\-\.]+\.[a-zA-Z]{2,}(:\d+)?(\/.*)?$/;
        if (!httpsRegex.test(url)) {
            alert("請輸入正確的 URL，必須使用 HTTPS 並且符合最基本的 URL 結構。");
            $(this).val('');
            $generateButton.prop('disabled', true);
            return;
        }

        $generateButton.prop('disabled', false);
    });

    $generateButton.on('click', function () {
        const longUrl = $longUrlInput.val().trim();
        if (!longUrl) {
            alert("請輸入一個網址 / Please enter a valid URL.");
            return;
        }

        $generateButton.prop('disabled', true);

        let attempt = 0;
        function tryCreateShortUrl() {
            $.ajax({
                url: '/su_api/create_new_short_url',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ originalUrl: longUrl }),
                success: function (response) {
                    if (response && typeof response === "object") {
                        const shortUrl     = response.fullShortUrl;
                        const message      = response.message;
                        const safeUrlResult = response.safeUrlResult;

                        console.log("Safe URL Result: ", safeUrlResult);

                        if (shortUrl) {
                            $("#shorten_url")
                                .text(shortUrl)
                                .css("background-color", "yellow")
                                .off('click').on('click', copyToClipboard);

                            $("#qrcode").empty();
                            new QRCode(document.getElementById("qrcode"), {
                                text: shortUrl,
                                width: 100,
                                height: 100
                            });
                            $("#qrcode").css("display", "block");
                        } else {
                            $("#shorten_url")
                                .text(message)
                                .css("background-color", "pink");
                            $("#qrcode").css("display", "none");
                        }
                    } else {
                        console.error("Invalid response!");
                        $("#shorten_url")
                            .text("無效的回應! 請稍後重試!")
                            .css("background-color", "pink");
                        $("#qrcode").css("display", "none");
                    }
                },
                error: function (xhr) {
                    if (xhr.status === 429) {
                        attempt++;
                        console.warn(`Request rate limit hit. Retry attempt: ${attempt}`);
                        if (attempt < maxRetries) {
                            setTimeout(tryCreateShortUrl, 1000); // 1 秒後重試
                        } else {
                            $("#shorten_url")
                                .text("伺服器忙碌，稍後再試。 - Server busy. Try later.")
                                .css("background-color", "pink");
                            $("#qrcode").css("display", "none");
                        }
                    } else {
                        console.error(`Error: ${xhr.status} - ${xhr.responseText}`);
                        $("#shorten_url")
                            .text("錯誤: 請稍後再試! Error: Please try again later.")
                            .css("background-color", "pink");
                        $("#qrcode").css("display", "none");
                    }
                },
                complete: function() {
                    $generateButton.prop('disabled', true);
                }
            });
        }

        tryCreateShortUrl();
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

function showAlert(message, color) {
    const alertDiv = document.createElement("div");
    alertDiv.textContent = message;
    alertDiv.style.position = "fixed";
    alertDiv.style.top = "50%";
    alertDiv.style.left = "50%";
    alertDiv.style.transform = "translate(-50%, -50%)";
    alertDiv.style.backgroundColor = color || "#C10066";
    alertDiv.style.color = "#FFFFFF";
    alertDiv.style.padding = "15px";
    alertDiv.style.borderColor = "#FFB7DD";
    alertDiv.style.borderWidth = "3px";
    alertDiv.style.borderRadius = "5px";
    alertDiv.style.zIndex = "9999";
    document.body.appendChild(alertDiv);

    setTimeout(function () {
        document.body.removeChild(alertDiv);
    }, 1000);
}