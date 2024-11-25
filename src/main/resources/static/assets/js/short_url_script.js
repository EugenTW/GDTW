$(document).ready(function () {
    // Validate the URL input on blur event
    $("#long_url").on('blur', function () {
        var url = $(this).val();
        var httpsRegex = /^https:\/\/[a-zA-Z0-9\-\.]+\.[a-z]{2,}(:\d+)?(\/.*)?$/;

        // Check if URL length exceeds 200 characters
        if (url.length > 200) {
            alert("您輸入的網址長度已超過 200 字元。 - The URL you entered exceeds 200 characters.");
        }

        // Ensure the URL matches HTTPS pattern
        if (!httpsRegex.test(url)) {
            alert("請輸入正確的 URL，必須使用 HTTPS 並且符合最基本的 URL 結構。 - The URL must start with HTTPS and follow the basic structure.");
            $(this).val(''); // Clear invalid input
        }
    });

    // Handle "Generate Short URL" button click
    $("#generate").on('click', function () {
        const longUrl = $("#long_url").val();
        if (!longUrl) {
            alert("請輸入一個網址 / Please enter a valid URL.");
            return;
        }

        const maxRetries = 5; // Maximum retry attempts for 429 response
        let attempt = 0;

        // Function to send the AJAX request for creating a short URL
        function tryCreateShortUrl() {
            $.ajax({
                url: '/su_api/create_new_short_url',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ originalUrl: longUrl }),
                success: function (response) {
                    // Process the successful response
                    if (response && typeof response === "object") {
                        const shortUrl = response.fullShortUrl;
                        const message = response.message;
                        const safeUrlResult = response.safeUrlResult;

                        console.log("Safe URL Result: ", safeUrlResult);

                        if (shortUrl) {
                            // Display the generated short URL
                            $("#shorten_url").text(shortUrl).css("background-color", "yellow");
                            $("#shorten_url").off('click').on('click', copyToClipboard);

                            // Generate a QR Code for the short URL
                            $("#qrcode").empty(); // Clear previous QR Code
                            new QRCode(document.getElementById("qrcode"), {
                                text: shortUrl, // Short URL as QR Code content
                                width: 100,
                                height: 100
                            });

                            $("#qrcode").css("display", "block"); // Show the QR Code
                        } else {
                            // Display response message if short URL is not provided
                            $("#shorten_url").text(message).css("background-color", "pink");
                            $("#qrcode").css("display", "none"); // Hide QR Code
                        }
                    } else {
                        console.error("Invalid response!");
                        $("#shorten_url").text("無效的回應! 請稍後重試! Invalid response! Please try again later.").css("background-color", "pink");
                        $("#qrcode").css("display", "none"); // Hide QR Code
                    }
                },
                error: function (xhr) {
                    // Handle 429 Too Many Requests response
                    if (xhr.status === 429) {
                        attempt++;
                        console.warn(`Request rate limit hit. Retry attempt: ${attempt}`);
                        if (attempt < maxRetries) {
                            setTimeout(tryCreateShortUrl, 1000); // Retry after 1 second
                        } else {
                            // Inform the user if maximum retries are exceeded
                            $("#shorten_url").text("伺服器忙碌，稍後再試。 - Server busy. Try later.")
                                .css("background-color", "pink");
                            $("#qrcode").css("display", "none"); // Hide QR Code
                        }
                    } else {
                        // Handle other errors
                        console.error(`Error: ${xhr.status} - ${xhr.responseText}`);
                        $("#shorten_url").text("錯誤: 請稍後再試! Error: Please try again later.")
                            .css("background-color", "pink");
                        $("#qrcode").css("display", "none"); // Hide QR Code
                    }
                }
            });
        }

        // Initiate the first request
        tryCreateShortUrl();
    });
});

// Copy the short URL to clipboard
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

// Display alert message in the center of the screen
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

    // Remove alert after 1 second
    setTimeout(function () {
        document.body.removeChild(alertDiv);
    }, 1000);
}
