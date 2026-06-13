// Countdown timer
let totalSeconds = 14 * 60 + 32;
const circle = document.getElementById('timerCircle');
const full = 2 * Math.PI * 19; // circumference

function updateTimer() {
    if (totalSeconds <= 0) return;
    totalSeconds--;
    const m = String(Math.floor(totalSeconds / 60)).padStart(2,'0');
    const s = String(totalSeconds % 60).padStart(2,'0');
    const label = `${m}:${s}`;
    document.getElementById('timerText').textContent = label;
    document.getElementById('expireTimer').textContent = label;
    // Update ring
    const pct = totalSeconds / (15 * 60);
    const offset = full * (1 - pct);
    circle.setAttribute('stroke-dasharray', full.toFixed(1));
    circle.setAttribute('stroke-dashoffset', offset.toFixed(1));
    if (totalSeconds <= 60) circle.style.stroke = '#D32F2F';
}
setInterval(updateTimer, 1000);

// Copy
function copyText(text, label) {
    navigator.clipboard.writeText(text).catch(() => {});
    const t = document.getElementById('toastCopy');
    t.textContent = `✔ Đã sao chép ${label}!`;
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), 2000);
}