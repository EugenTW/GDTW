document.addEventListener('DOMContentLoaded', function () {
    const inputArea = document.querySelector('.code-input-area');
    const compressBtn = document.getElementById('start-compress');
    const resultArea = document.querySelector('.compress-result-area');
    const resultBox = document.querySelector('.compressed-output');
    const statsDisplay = document.querySelector('.stats-display');
    const copyBtn = document.querySelector('.copy-button');

    let lastInput = "";

    function normalizeInput(text) {
        return text.replace(/\r\n|\r/g, '\n').replace(/[^\x09\x0A\x0D\x20-\x7E\u4E00-\u9FFF]/g, '').trim();
    }

    function clearResultArea() {
        resultBox.textContent = '';
        resultBox.removeAttribute('data-raw');
        statsDisplay.textContent = '';
        resultArea.style.display = 'none';
    }

    function updateCompressButton() {
        const raw = inputArea.textContent.trim();
        const cleaned = normalizeInput(raw);
        lastInput = cleaned;
        compressBtn.disabled = !cleaned;
        clearResultArea();
    }

    inputArea.addEventListener('input', updateCompressButton);

    inputArea.addEventListener('paste', function (e) {
        e.preventDefault();
        const pasted = (e.clipboardData || window.clipboardData).getData('text');
        const cleaned = normalizeInput(pasted);
        inputArea.textContent = cleaned;
        lastInput = cleaned;
        compressBtn.disabled = !cleaned;
        clearResultArea();
    });

    compressBtn.addEventListener('click', async function () {
        compressBtn.disabled = true;
        if (!lastInput) return;

        try {
            const resp = await fetch('/cj_api/css_js_minifier', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ source: lastInput })
            });

            const data = await resp.json();

            if (data.type === 'CSS' || data.type === 'JS') {
                resultBox.textContent = data.result || '';
                resultBox.setAttribute('data-raw', data.result || '');
                statsDisplay.textContent = data.status || '';
                resultArea.style.display = 'flex';
            } else {
                alert("輸入內容不符合 CSS 或 JavaScript 規範。\nInput is not valid CSS or JavaScript.");
                inputArea.textContent = '';
                lastInput = '';
                compressBtn.disabled = true;
                clearResultArea();
            }
        } catch (err) {
            alert("壓縮失敗，請稍後再試。\nCompression failed, please try again later.");
            console.error(err);
        }
    });

    copyBtn.addEventListener('click', function () {
        const raw = resultBox.getAttribute('data-raw');
        if (!raw) return;

        navigator.clipboard.writeText(raw).then(() => {
            const notice = document.createElement('div');
            notice.textContent = '已複製結果 - Result Copied.';
            notice.style.cssText = "position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%); background-color: #00FF00; color: #000000; padding: 8px 16px; border: 2px solid #227700; border-radius: 5px; font-size: 14px; z-index: 9999;";
            document.body.appendChild(notice);
            setTimeout(() => notice.remove(), 2000);
        });
    });

    compressBtn.disabled = true;
});
