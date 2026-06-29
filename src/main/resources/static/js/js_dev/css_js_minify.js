document.addEventListener('DOMContentLoaded', function () {
    const inputArea = document.querySelector('.code-input-area');
    const compressBtn = document.getElementById('start-compress');
    const resultArea = document.querySelector('.compress-result-area');
    const resultBox = document.querySelector('.compressed-output');
    const statsDisplay = document.querySelector('.stats-display');
    const copyBtn = document.querySelector('.copy-button');

    const MAX_INPUT_SIZE = 3 * 1024 * 1024; // 3MB
    const DISPLAY_LIMIT = 250 * 1024;       // 250KB

    let lastInput = "";

    function getSize(str) {
        return new Blob([str]).size;
    }

    function normalizeInput(text) {
        let cleaned = text
            .replace(/\r\n|\r/g, '\n')
            .replace(/[^\x09\x0A\x0D\x20-\x7E\u4E00-\u9FFF]/g, '')
            .trim();

        if (cleaned.length > MAX_INPUT_SIZE) {
            alert("輸入內容過長。\nInput is too long.");
            cleaned = "";
        }

        return cleaned;
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

        if (getSize(cleaned) > DISPLAY_LIMIT && cleaned) {
            inputArea.textContent = "超過250KB故不顯示, 但仍可執行壓縮。\nContent exceeds 250KB, not displayed, but compression is still available.";
        }
    }

    inputArea.addEventListener('input', updateCompressButton);

    inputArea.addEventListener('paste', function (e) {
        e.preventDefault();
        const pasted = (e.clipboardData || window.clipboardData).getData('text');
        const cleaned = normalizeInput(pasted);

        lastInput = cleaned;
        compressBtn.disabled = !cleaned;
        clearResultArea();

        if (getSize(cleaned) > DISPLAY_LIMIT) {
            inputArea.textContent = "超過250KB故不顯示, 但仍可執行壓縮。\nContent exceeds 250KB, not displayed, but compression is still available.";
        } else {
            inputArea.textContent = cleaned;
        }
    });

    compressBtn.addEventListener('click', async function () {
        compressBtn.disabled = true;
        if (!lastInput) return;

        try {
            const resp = await fetch('/cj_api/css_js_minifier', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({source: lastInput})
            });

            const data = await resp.json();

            if (data.type === 'CSS' || data.type === 'JS') {

                const result = data.result || '';
                const resultSize = getSize(result);

                resultBox.setAttribute('data-raw', result);

                if (resultSize > DISPLAY_LIMIT) {
                    resultBox.textContent = "壓縮成功, 但超過250KB不顯示, 可正常複製結果。\nCompression successful, but exceeds 250KB and is not displayed. You can still copy the result.";
                } else {
                    resultBox.textContent = result;
                }

                statsDisplay.textContent = data.status || '';
                resultArea.style.display = 'flex';

                navigator.clipboard.writeText(result).catch(() => {});

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
        } finally {
            compressBtn.disabled = false;
        }
    });

    copyBtn.addEventListener('click', function () {
        const raw = resultBox.getAttribute('data-raw');
        if (!raw) return;

        navigator.clipboard.writeText(raw).then(() => {
            const notice = document.createElement('div');
            notice.textContent = '已複製結果 - Result Copied.';
            notice.style.cssText =
                "position: fixed; top: 50%; left: 50%; transform: translate(-50%, -50%);" +
                "background-color: #00FF00; color: #000000; padding: 8px 16px;" +
                "border: 2px solid #227700; border-radius: 5px; font-size: 14px; z-index: 9999;";
            document.body.appendChild(notice);
            setTimeout(() => notice.remove(), 2000);
        });
    });

    compressBtn.disabled = true;
});
