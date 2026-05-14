/* ============================================================
   CyberGuard – Data Access Log Analyzer | script.js
   ============================================================ */

'use strict';

// ── State ───────────────────────────────────────────────────
const state = { total: 0, suspicious: 0, safe: 0, logs: [] };

// ── Init ────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  initParticles();
  setDefaultTimestamp();
  startClock();
  initForm();
  initThreatPreview();
  refreshDashboard();
  setInterval(refreshDashboard, 6000);
});

// ── Clock ────────────────────────────────────────────────────
function startClock() {
  const el = document.getElementById('headerTime');
  function tick() {
    el.textContent = new Date().toLocaleTimeString('en-GB', { hour12: false });
  }
  tick();
  setInterval(tick, 1000);
}

// ── Particles ────────────────────────────────────────────────
function initParticles() {
  const container = document.getElementById('bgParticles');
  for (let i = 0; i < 40; i++) {
    const p = document.createElement('div');
    p.className = 'particle';
    p.style.cssText = `
      left:${Math.random()*100}%;
      top:${Math.random()*100}%;
      animation-duration:${9 + Math.random()*10}s;
      animation-delay:${Math.random()*6}s;
      width:${Math.random() > 0.7 ? 3 : 2}px;
      height:${Math.random() > 0.7 ? 3 : 2}px;
    `;
    container.appendChild(p);
  }
}

// ── Default Timestamp ────────────────────────────────────────
function setDefaultTimestamp() {
  const el = document.getElementById('timestamp');
  if (!el) return;
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  el.value = now.toISOString().slice(0, 16);
}

// ── Threat Preview Banner ────────────────────────────────────
function initThreatPreview() {
  const accessType = document.getElementById('accessType');
  const resource   = document.getElementById('resource');
  const preview    = document.getElementById('threatPreview');
  const msg        = document.getElementById('threatMsg');

  function check() {
    const at  = accessType.value;
    const res = resource.value.trim().toLowerCase();
    const isDelete = at === 'DELETE';
    const isAdmin  = res === 'admin';

    if (isDelete || isAdmin) {
      let reasons = [];
      if (isDelete) reasons.push('<strong>DELETE</strong> operation');
      if (isAdmin)  reasons.push('<strong>admin</strong> resource');
      msg.innerHTML = `⚠ This event will be flagged as <strong>SUSPICIOUS</strong> — ${reasons.join(' + ')} detected`;
      preview.classList.add('show');
    } else {
      preview.classList.remove('show');
    }
  }

  accessType.addEventListener('change', check);
  resource.addEventListener('input', check);
}

// ── Form Handling ────────────────────────────────────────────
function initForm() {
  const form      = document.getElementById('logForm');
  const submitBtn = document.getElementById('submitBtn');
  const btnText   = document.getElementById('btnText');
  const btnLoader = document.getElementById('btnLoader');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!validate()) return;

    const data = {
      userId:     document.getElementById('userId').value.trim(),
      resource:   document.getElementById('resource').value.trim(),
      accessType: document.getElementById('accessType').value,
      timestamp:  document.getElementById('timestamp').value
    };

    const isSuspicious =
      data.accessType === 'DELETE' ||
      data.resource.toLowerCase() === 'admin';

    // Show loader
    btnText.style.display   = 'none';
    btnLoader.style.display = 'flex';
    submitBtn.disabled      = true;

    try {
      const res = await fetch('DataAccessServlet', {
        method:  'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body:    new URLSearchParams(data).toString()
      });

      if (res.ok) {
        updateLocalStats(isSuspicious);
        addToLocalFeeds(data, isSuspicious);
      }

      showResult(data, isSuspicious, res.ok);

    } catch (err) {
      console.warn('Servlet unreachable — demo mode active.');
      updateLocalStats(isSuspicious);
      addToLocalFeeds(data, isSuspicious);
      showResult(data, isSuspicious, false);
    } finally {
      btnText.style.display   = 'flex';
      btnLoader.style.display = 'none';
      submitBtn.disabled      = false;
      form.reset();
      setDefaultTimestamp();
      document.getElementById('threatPreview').classList.remove('show');
    }
  });
}

// ── Validation ───────────────────────────────────────────────
function validate() {
  let ok = true;
  ['userId', 'resource', 'accessType', 'timestamp'].forEach(id => {
    const el  = document.getElementById(id);
    const err = document.getElementById('err-' + id);
    if (!el.value.trim()) {
      err.textContent    = 'This field is required.';
      el.style.borderColor = 'var(--red)';
      el.style.boxShadow   = '0 0 0 2px rgba(255,51,102,.18)';
      ok = false;
    } else {
      err.textContent    = '';
      el.style.borderColor = '';
      el.style.boxShadow   = '';
    }
  });
  return ok;
}

// ── Local State Update ───────────────────────────────────────
function updateLocalStats(isSuspicious) {
  state.total++;
  isSuspicious ? state.suspicious++ : state.safe++;
  animateCount('totalLogs',       state.total);
  animateCount('suspiciousCount', state.suspicious);
  animateCount('safeCount',       state.safe);
  updateGauge(state.suspicious, state.total);
}

function addToLocalFeeds(data, isSuspicious) {
  state.logs.unshift({ ...data, isSuspicious, ts: new Date() });
  renderActivityFeed(state.logs);
  renderLogFeed(state.logs);
  const countEl = document.getElementById('activityCount');
  if (countEl) countEl.textContent = state.logs.length;
}

// ── Animated Counter ─────────────────────────────────────────
function animateCount(id, target) {
  const el = document.getElementById(id);
  if (!el) return;
  const start = parseInt(el.textContent) || 0;
  const diff  = target - start;
  const steps = 20;
  let   step  = 0;
  const timer = setInterval(() => {
    step++;
    el.textContent = Math.round(start + (diff * step / steps));
    if (step >= steps) clearInterval(timer);
  }, 18);
}

// ── Threat Gauge ─────────────────────────────────────────────
function updateGauge(suspicious, total) {
  const pct      = total === 0 ? 0 : Math.min(100, Math.round((suspicious / total) * 100));
  const arc      = document.getElementById('gaugeArc');
  const needle   = document.getElementById('gaugeNeedle');
  const label    = document.getElementById('gaugeLabel');
  const pctEl    = document.getElementById('gaugePct');
  const maxDash  = 157;

  if (arc) arc.style.strokeDashoffset = maxDash - (pct / 100) * maxDash;

  // Needle: -90deg = 0% → +90deg = 100%
  if (needle) {
    const angle = -90 + (pct / 100) * 180;
    needle.setAttribute('transform', `rotate(${angle} 60 65)`);
  }

  if (pctEl) pctEl.textContent = pct + '%';

  if (label) {
    if (pct >= 70)       { label.textContent = 'CRITICAL'; label.style.color = 'var(--red)'; }
    else if (pct >= 40)  { label.textContent = 'HIGH';     label.style.color = '#ff8800'; }
    else if (pct >= 20)  { label.textContent = 'MEDIUM';   label.style.color = 'var(--yellow)'; }
    else                 { label.textContent = 'LOW';       label.style.color = 'var(--green)'; }
  }
}

// ── Dashboard Polling (server) ───────────────────────────────
async function refreshDashboard() {
  await Promise.all([fetchStats(), fetchLogs()]);
}

async function fetchStats() {
  try {
    const res = await fetch('/api/stats');
    if (!res.ok) throw new Error();
    const s = await res.json();
    state.total      = s.total      || state.total;
    state.suspicious = s.suspicious || state.suspicious;
    state.safe       = s.safe       || state.safe;
    animateCount('totalLogs',       state.total);
    animateCount('suspiciousCount', state.suspicious);
    animateCount('safeCount',       state.safe);
    updateGauge(state.suspicious, state.total);
  } catch (_) { /* servlet-only mode — local state only */ }
}

async function fetchLogs() {
  try {
    const res = await fetch('/api/logs');
    if (!res.ok) throw new Error();
    const logs = await res.json();
    if (logs && logs.length) {
      renderActivityFeed(logs);
      renderLogFeed(logs);
      const countEl = document.getElementById('activityCount');
      if (countEl) countEl.textContent = logs.length;
    }
  } catch (_) { /* servlet-only mode */ }
}

// ── Render Activity Feed ─────────────────────────────────────
function renderActivityFeed(logs) {
  const el = document.getElementById('activityFeed');
  if (!el) return;
  if (logs.length === 0) return;

  el.innerHTML = logs.slice(0, 8).map(log => {
    const time = log.ts
      ? log.ts.toLocaleTimeString('en-GB', { hour12: false })
      : (log.timestamp || '').toString().split('T')[1]?.slice(0, 8) || '--:--:--';

    return `
      <div class="feed-item ${log.isSuspicious ? 'sus' : ''}">
        <div class="feed-item-top">
          <span class="feed-user">${escHtml(log.userId)}</span>
          <span class="feed-time">${time}</span>
        </div>
        <div class="feed-detail">
          ${escHtml(log.accessType)} → ${escHtml(log.resource)}
          ${log.isSuspicious ? '<span class="feed-badge">THREAT</span>' : ''}
        </div>
      </div>`;
  }).join('');
}

// ── Render Log Terminal Feed ─────────────────────────────────
function renderLogFeed(logs) {
  const el = document.getElementById('logFeed');
  if (!el) return;
  if (logs.length === 0) return;

  el.innerHTML = logs.slice(0, 20).map(log => {
    const time = log.ts
      ? log.ts.toISOString().replace('T', ' ').slice(0, 19)
      : (log.timestamp || '').toString().slice(0, 19);
    const threat = log.isSuspicious ? ' !! THREAT' : '';
    return `<div class="log-line${log.isSuspicious ? ' threat' : ''}">[${time}] ${escHtml(log.userId)} ${escHtml(log.accessType)} ${escHtml(log.resource)}${threat}</div>`;
  }).join('');
}

// ── Show Result Panel ────────────────────────────────────────
function showResult(data, isSuspicious, dbSaved) {
  const panel = document.getElementById('resultPanel');
  const inner = document.getElementById('resultInner');

  inner.className = 'result-inner ' + (isSuspicious ? 'bad' : 'ok');
  inner.innerHTML = `
    <div class="result-head">
      <div class="result-icon">
        <i class="fas ${isSuspicious ? 'fa-skull-crossbones' : 'fa-circle-check'}"></i>
      </div>
      <div>
        <div class="result-title">${isSuspicious ? '⚠ Suspicious Access Detected' : '✓ Access Logged Successfully'}</div>
        <div class="result-subtitle">${isSuspicious ? 'Event flagged for security review' : 'Event recorded and verified safe'}</div>
      </div>
    </div>
    <div class="result-grid">
      <div class="result-item">
        <div class="result-item-label">User ID</div>
        <div class="result-item-value">${escHtml(data.userId)}</div>
      </div>
      <div class="result-item">
        <div class="result-item-label">Resource</div>
        <div class="result-item-value">${escHtml(data.resource)}</div>
      </div>
      <div class="result-item">
        <div class="result-item-label">Access Type</div>
        <div class="result-item-value">${escHtml(data.accessType)}</div>
      </div>
      <div class="result-item">
        <div class="result-item-label">Timestamp</div>
        <div class="result-item-value">${escHtml(data.timestamp)}</div>
      </div>
    </div>
    <div class="result-db ${dbSaved ? 'ok-db' : 'err-db'}">
      ${dbSaved ? '✔ Record saved to SQLite database' : '⚠ Running in demo mode — record not persisted'}
    </div>`;

  panel.style.display = 'block';
  panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });

  if (isSuspicious) showAlert(data);
}

// ── Alert Modal ──────────────────────────────────────────────
function showAlert(data) {
  const body = document.getElementById('alertBody');
  let reason = '';
  if (data.accessType === 'DELETE') reason += 'DELETE operations pose critical data-integrity risk. ';
  if (data.resource.toLowerCase() === 'admin') reason += 'Admin resource access requires elevated authorization.';

  body.innerHTML = `
    <p><span class="lbl">User ID: </span><span class="val">${escHtml(data.userId)}</span></p>
    <p><span class="lbl">Resource: </span><span class="val">${escHtml(data.resource)}</span></p>
    <p><span class="lbl">Access Type: </span><span class="val">${escHtml(data.accessType)}</span></p>
    <p><span class="lbl">Timestamp: </span><span class="val">${escHtml(data.timestamp)}</span></p>
    <p class="reason"><i class="fas fa-exclamation-circle"></i> ${reason}</p>`;

  document.getElementById('alertOverlay').classList.add('show');
}

function dismissAlert() {
  document.getElementById('alertOverlay').classList.remove('show');
}

// ── XSS Helper ───────────────────────────────────────────────
function escHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;');
}
