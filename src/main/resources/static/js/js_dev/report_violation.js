const REPORT_TYPE_MAP = {
    "url": 1,
    "album": 2,
    "image": 3
};
const REPORT_REASON_MAP = {
    "child": 1,
    "violence": 2,
    "hate": 3,
    "other": 4
};

let _captchaVal = "";

function generateCaptcha() {
    const part1 = Math.floor(100 + Math.random() * 900);
    const part2 = Math.floor(100 + Math.random() * 900);
    return part1.toString() + part2.toString();
}

function setCaptcha() {
    _captchaVal = generateCaptcha();
    document.getElementById('captcha-value').textContent = _captchaVal.slice(0, 3) + ' ' + _captchaVal.slice(3);
}

function showAlert(msg, success = true) {
    let alertDiv = document.createElement('div');
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
    alertDiv.style.zIndex = 9999;
    alertDiv.style.textAlign = "center";
    alertDiv.innerHTML = msg;
    document.body.appendChild(alertDiv);
    setTimeout(() => {
        document.body.removeChild(alertDiv);
    }, 2000);
}

function isValidCaptcha(val) {
    return val === _captchaVal;
}

function resetForm() {
    document.getElementById('report-type').selectedIndex = 0;
    document.getElementById('report-reason').selectedIndex = 0;
    document.getElementById('captcha-input').value = "";
    document.getElementById('captcha-input').placeholder = "請輸入上方六位數字 / Enter the 6-digit number above";
    setCaptcha();
    document.querySelector('.report-submit').disabled = true;
}

function checkFormReady() {
    const typeVal = document.getElementById('report-type').value;
    const reasonVal = document.getElementById('report-reason').value;
    const captchaVal = document.getElementById('captcha-input').value;
    const submitBtn = document.querySelector('.report-submit');
    if (typeVal && reasonVal && captchaVal.length === 6 && isValidCaptcha(captchaVal)) {
        submitBtn.disabled = false;
    } else {
        submitBtn.disabled = true;
    }
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

    document.getElementById('report-type').onchange = checkFormReady;
    document.getElementById('report-reason').onchange = checkFormReady;

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

        const payload = {
            reportType: REPORT_TYPE_MAP[reportTypeStr] || 0,
            reportReason: REPORT_REASON_MAP[reportReasonStr] || 0
        };

        try {
            const resp = await fetch('/rv_api/send_report', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
            });
            const data = await resp.json();

            if (data.reportStatus === true) {
                showAlert("舉報送出成功，謝謝您！<br>Report sent successfully, thank you!");
                resetForm();
            } else {
                showAlert("錯誤 / Error: <br>" + data.response, false);
                submitBtn.disabled = false;
            }
        } catch (err) {
            showAlert("錯誤 / Error:<br> 網路錯誤或無法送出 / Network error or unable to send", false);
            submitBtn.disabled = false;
        }
    };
});
