$(document).ready(function() {
    $("#generate").click(function() {
        var longUrl = $("#long_url").val();
        if (longUrl) {
            $.ajax({
                url: '/su_api/create_new_short_url',
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ originalUrl: longUrl }),
                success: function(response) {
                    console.log("Response received: ", response); 
                    $("#shorten_url").text(response).css("background-color", "yellow");

                    // Rebind click event to ensure it works after content update
                    $("#shorten_url").off('click').on('click', copyToClipboard);
                },
                error: function(xhr, textStatus, errorThrown) {
                    var errorMessage = "Error: " + xhr.responseText;
                    $("#shorten_url").text(errorMessage).css("background-color", "red");
                }
            });
        } else {
            alert("請輸入一個網址 / Please enter a valid url.");
        }
    });
});

function copyToClipboard() {
    const copyText = document.getElementById("shorten_url").textContent;

    if (navigator.clipboard) {
        navigator.clipboard.writeText(copyText).then(() => {
            showAlert("短網址已複製到剪貼簿 / The short URL is copied!", "lightgreen");
        }).catch(err => {
            console.error('Failed to copy text: ', err);
        });
    } else {        
        const textArea = document.createElement("textarea");
        textArea.value = copyText;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand("copy");
        document.body.removeChild(textArea);
        showAlert("短網址已複製到剪貼簿 / The short URL is copied!", "lightgreen");
    }
}

function showAlert(message, color) {
    const alertDiv = document.createElement("div");
    alertDiv.textContent = message;
    alertDiv.style.position = "fixed";
    alertDiv.style.top = "50%";
    alertDiv.style.left = "50%";
    alertDiv.style.transform = "translate(-50%, -50%)";
    alertDiv.style.backgroundColor = color;
    alertDiv.style.border = "2px solid green";
    alertDiv.style.padding = "10px";
    alertDiv.style.borderRadius = "5px";
    alertDiv.style.boxShadow = "0 0 10px rgba(0, 0, 0, 0.2)";
    document.body.appendChild(alertDiv);

    setTimeout(function() {
        document.body.removeChild(alertDiv);
    }, 1500);
}
