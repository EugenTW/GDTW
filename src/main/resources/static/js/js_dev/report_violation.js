const REPORT_TYPE_MAP = {
    "url": 1,
    "album": 2,
    "image": 3
};
const REPORT_REASON_MAP = {
    "child": 1,
    "violence": 2,
    "hate": 3,
    "scam": 4,
    "other": 5
};

let _captchaVal = "";
let targetClearTimeout = null;

function extractTargetId(str, reportType) {
    let url = str.trim();
    url = url.replace(/^https?:\/\//i, "");
    let match = url.match(/^(gdtw\.org|localhost)(\/.+)?$/i);
    if (!match) return null;
    let path = match[2] || "/";
    path = path.split('?')[0];
    let segments = path.split('/').filter(seg => seg.length > 0);

    if (reportType === "url") {
        if (segments.length === 1 && /^[A-Za-z0-9]{4}$/.test(segments[0])) {
            return segments[0];
        }
    } else if (reportType === "album") {
        if (segments.length === 2 && segments[0] === 'a' && /^[A-Za-z0-9]{6}$/.test(segments[1])) {
            return segments[1];
        }
    } else if (reportType === "image") {
        if (segments.length === 2 && segments[0] === 'i' && /^[A-Za-z0-9]{6}$/.test(segments[1])) {
            return segments[1];
        }
    }
    return null;
}

function isValidTargetUrl(str, reportType) {
    if (!reportType) return true;
    return !!extractTargetId(str, reportType);
}

function handleTargetInput() {
    const typeVal = document.getElementById('report-type').value;
    const targetInput = document.getElementById('report-target');
    const value = targetInput.value;

    if (targetClearTimeout) {
        clearTimeout(targetClearTimeout);
        targetClearTimeout = null;
    }

    if (typeVal && value && !isValidTargetUrl(value, typeVal)) {
        targetInput.style.borderColor = "#a1002b";
        targetClearTimeout = setTimeout(function () {
            targetInput.value = "";
            targetInput.placeholder = "請輸入正確的目標網址 / Please enter a valid report URL";
            targetInput.style.borderColor = "#a1002b";
            checkFormReady();
        }, 500);
    } else {
        targetInput.style.borderColor = "";
    }
    checkFormReady();
}

function generateCaptcha() {
    const part1 = Math.floor(100 + Math.random() * 900);
    const part2 = Math.floor(100 + Math.random() * 900);
    return part1.toString() + part2.toString();
}

function setCaptcha() {
    _captchaVal = generateCaptcha();
    document.getElementById('captcha-value').textContent = String(_captchaVal.slice(0, 3)) + ' ' + String(_captchaVal.slice(3));
}

function showAlert(msg, success = true) {
    const alertDiv = document.createElement('div');
    alertDiv.style.position = "fixed";
    alertDiv.style.left = "50%";
    alertDiv.style.top = "50%";
    alertDiv.style.transform = "translate(-50%, -50%)";
    alertDiv.style.background = success ? "#185a36" : "#a1002b";
    alertDiv.style.color = "#fff";
    alertDiv.style.padding = "15px 15px";
    alertDiv.style.borderRadius = "10px";
    alertDiv.style.fontSize = "16px";
    alertDiv.style.fontWeight = "bold";
    alertDiv.style.zIndex = "9999";
    alertDiv.style.textAlign = "center";
    alertDiv.innerHTML = msg;
    document.body.appendChild(alertDiv);
    setTimeout(function () {
        document.body.removeChild(alertDiv);
    }, 3000);
}

function isValidCaptcha(val) {
    return val === _captchaVal;
}

function resetForm() {
    document.getElementById('report-type').selectedIndex = 0;
    document.getElementById('report-reason').selectedIndex = 0;
    document.getElementById('report-target').value = "";
    document.getElementById('report-target').placeholder = "請輸入目標網址 / Enter the URL of report target";
    document.getElementById('report-target').style.borderColor = "";
    document.getElementById('captcha-input').value = "";
    document.getElementById('captcha-input').placeholder = "請輸入上方六位數字 / Enter the 6-digit number above";
    setCaptcha();
    document.querySelector('.report-submit').disabled = true;
    if (targetClearTimeout) {
        clearTimeout(targetClearTimeout);
        targetClearTimeout = null;
    }
}

function checkFormReady() {
    const typeVal = document.getElementById('report-type').value;
    const reasonVal = document.getElementById('report-reason').value;
    const targetVal = document.getElementById('report-target').value;
    const captchaVal = document.getElementById('captcha-input').value;
    const submitBtn = document.querySelector('.report-submit');
    submitBtn.disabled = !(typeVal &&
        reasonVal &&
        targetVal &&
        isValidTargetUrl(targetVal, typeVal) &&
        captchaVal.length === 6 &&
        isValidCaptcha(captchaVal));
}

document.addEventListener('DOMContentLoaded', function () {
    setCaptcha();

    document.getElementById('refresh-captcha').onclick = function (e) {
        e.preventDefault();
        setCaptcha();
        document.getElementById('captcha-input').value = "";
        document.getElementById('captcha-input').placeholder = "請輸入上方六位數字 / Enter the 6-digit number above";
        checkFormReady();
    };

    document.getElementById('report-type').onchange = function () {
        handleTargetInput();
        checkFormReady();
    };
    document.getElementById('report-reason').onchange = checkFormReady;

    document.getElementById('report-target').addEventListener('input', handleTargetInput);
    document.getElementById('report-target').addEventListener('blur', handleTargetInput);
    document.getElementById('report-target').addEventListener('paste', function(e){
        e.preventDefault();
        this.value = (e.clipboardData || window.clipboardData).getData('text');
        this.dispatchEvent(new Event('input'));
    });

    document.getElementById('captcha-input').addEventListener('input', function () {
        if (this.value.length > 6) {
            this.value = this.value.slice(0, 6);
        }
        checkFormReady();
    });

    document.getElementById('captcha-input').addEventListener('blur', function () {
        if (this.value && !isValidCaptcha(this.value)) {
            this.value = "";
            this.placeholder = "請重新輸入 / Please enter again";
            checkFormReady();
        }
    });

    document.querySelector('.report-form').onsubmit = async function (e) {
        e.preventDefault();
        const submitBtn = document.querySelector('.report-submit');
        submitBtn.disabled = true;

        const reportTypeStr = document.getElementById('report-type').value;
        const reportReasonStr = document.getElementById('report-reason').value;
        const targetInput = document.getElementById('report-target').value;
        const targetId = extractTargetId(targetInput, reportTypeStr);

        const payload = {
            reportType: REPORT_TYPE_MAP[reportTypeStr] || 0,
            reportReason: REPORT_REASON_MAP[reportReasonStr] || 0,
            targetUrl: targetId || ""
        };

        try {
            const resp = await fetch('/rv_api/send_report', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
            let data;
            try {
                data = await resp.json();
            } catch (err) {
                data = {};
            }
            if (data && String(data.reportStatus) === "true") {
                showAlert("舉報送出成功，謝謝您！<br>Report sent successfully, thank you!");
                resetForm();
            } else {
                showAlert("錯誤 / Error: <br>" + (data && data.response ? data.response : "Unknown error"), false);
                resetForm();
            }
        } catch (err) {
            showAlert("錯誤 / Error:<br> 網路錯誤或無法送出 / Network error or unable to send", false);
            resetForm();
        }
    };
});
