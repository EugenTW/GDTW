function copyToClipboard() {
    const copyText = document.getElementById("shorten_url");
    const textArea = document.createElement("textarea");
    textArea.value = copyText.textContent;  // 使用textContent以防HTML標籤被複製
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand("copy");
    document.body.removeChild(textArea);

    // 顯示提示訊息
    const alertDiv = document.createElement("div");
    alertDiv.textContent = "短網址已複製到剪貼簿";
    alertDiv.style.position = "fixed";
    alertDiv.style.top = "50%";
    alertDiv.style.left = "50%";
    alertDiv.style.transform = "translate(-50%, -50%)";
    alertDiv.style.backgroundColor = "lightgreen";
    alertDiv.style.border = "2px solid green";
    alertDiv.style.padding = "10px";
    alertDiv.style.borderRadius = "5px";
    alertDiv.style.boxShadow = "0 0 10px rgba(0, 0, 0, 0.2)";
    document.body.appendChild(alertDiv);

    // 1.5 秒後自動關閉提示訊息
    setTimeout(function(){
        document.body.removeChild(alertDiv);
    }, 1500);
}

