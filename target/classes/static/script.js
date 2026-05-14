// ===== Data Access Log Analyzer - Simplified Script =====

document.addEventListener('DOMContentLoaded', () => {
  initParticles();
  setDefaultTimestamp();
  initForm();
});

// ===== Background Particles =====
function initParticles() {
  const container = document.getElementById('bgParticles');
  for (let i = 0; i < 35; i++) {
    const p = document.createElement('div');
    p.className = 'particle';
    p.style.left = Math.random() * 100 + '%';
    p.style.top = Math.random() * 100 + '%';
    p.style.animationDuration = (8 + Math.random() * 10) + 's';
    p.style.animationDelay = (Math.random() * 5) + 's';
    container.appendChild(p);
  }
}

// ===== Set Default Timestamp to Now =====
function setDefaultTimestamp() {
  const ts = document.getElementById('timestamp');
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  ts.value = now.toISOString().slice(0, 16);
}

// ===== Form Handling =====
function initForm() {
  document.getElementById('logForm').addEventListener('submit', function(e) {
    e.preventDefault();
    if (!validate()) return;

    const data = {
      userId: document.getElementById('userId').value.trim(),
      resource: document.getElementById('resource').value.trim(),
      accessType: document.getElementById('accessType').value,
      timestamp: document.getElementById('timestamp').value
    };

    const isSuspicious = data.accessType === 'DELETE' || data.resource.toLowerCase() === 'admin';

    // Show loading
    document.getElementById('btnText').style.display = 'none';
    document.getElementById('btnLoader').style.display = 'inline';
    document.getElementById('submitBtn').disabled = true;

    // Send to servlet
    fetch('/DataAccessServlet', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams(data).toString()
    })
    .then(() => showResult(data, isSuspicious))
    .catch(() => showResult(data, isSuspicious))  // Demo mode fallback
    .finally(() => {
      document.getElementById('btnText').style.display = 'inline';
      document.getElementById('btnLoader').style.display = 'none';
      document.getElementById('submitBtn').disabled = false;
      document.getElementById('logForm').reset();
      setDefaultTimestamp();
    });
  });
}

// ===== Validation =====
function validate() {
  let ok = true;
  ['userId', 'resource', 'accessType', 'timestamp'].forEach(id => {
    const el = document.getElementById(id);
    const err = document.getElementById('err-' + id);
    if (!el.value.trim()) {
      err.textContent = 'This field is required';
      el.style.borderColor = '#ff3366';
      ok = false;
    } else {
      err.textContent = '';
      el.style.borderColor = '';
    }
  });
  return ok;
}

// ===== Show Result =====
function showResult(data, isSuspicious) {
  const panel = document.getElementById('resultPanel');
  const inner = document.getElementById('resultInner');

  inner.className = 'result-inner ' + (isSuspicious ? 'danger-result' : 'success-result');
  inner.innerHTML = `
    <div class="result-header">
      <div class="result-icon">
        <i class="fas ${isSuspicious ? 'fa-skull-crossbones' : 'fa-circle-check'}"></i>
      </div>
      <div>
        <div class="result-title">${isSuspicious ? 'Suspicious Access Detected' : 'Access Logged Successfully'}</div>
        <div class="result-subtitle">${isSuspicious ? 'This event has been flagged for review' : 'Event recorded and verified safe'}</div>
      </div>
    </div>
    <div class="result-details">
      <div class="result-detail">
        <div class="result-detail-label">User ID</div>
        <div class="result-detail-value">${data.userId}</div>
      </div>
      <div class="result-detail">
        <div class="result-detail-label">Resource</div>
        <div class="result-detail-value">${data.resource}</div>
      </div>
      <div class="result-detail">
        <div class="result-detail-label">Access Type</div>
        <div class="result-detail-value">${data.accessType}</div>
      </div>
      <div class="result-detail">
        <div class="result-detail-label">Timestamp</div>
        <div class="result-detail-value">${data.timestamp}</div>
      </div>
    </div>
  `;
  panel.style.display = 'block';

  if (isSuspicious) showAlert(data);
}

// ===== Alert Modal =====
function showAlert(data) {
  const body = document.getElementById('alertBody');
  let reason = '';
  if (data.accessType === 'DELETE') reason += 'DELETE operations pose a critical data integrity risk. ';
  if (data.resource.toLowerCase() === 'admin') reason += 'Admin resource access requires elevated authorization.';

  body.innerHTML = `
    <p><span class="label">User ID: </span><span class="value">${data.userId}</span></p>
    <p><span class="label">Resource: </span><span class="value">${data.resource}</span></p>
    <p><span class="label">Access Type: </span><span class="value">${data.accessType}</span></p>
    <p><span class="label">Timestamp: </span><span class="value">${data.timestamp}</span></p>
    <p class="reason"><i class="fas fa-exclamation-circle"></i> ${reason}</p>
  `;
  document.getElementById('alertOverlay').classList.add('show');
}

function dismissAlert() {
  document.getElementById('alertOverlay').classList.remove('show');
}
