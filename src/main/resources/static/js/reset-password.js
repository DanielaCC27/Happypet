/**
 * Restablecer contraseña: token en la URL, POST /api/auth/reset-password
 * (el backend exige token, password y confirmPassword coincidentes).
 */
(function () {
    'use strict';

    var params = new URLSearchParams(window.location.search);
    var token = params.get('token');

    var noTokenEl = document.getElementById('reset-no-token');
    var contentEl = document.getElementById('reset-content');
    var form = document.getElementById('reset-form');
    var passInput = document.getElementById('reset-password-input');
    var confirmInput = document.getElementById('reset-password-confirm');
    var submitBtn = document.getElementById('reset-submit');
    var confirmErr = document.getElementById('confirm-error');
    var apiErr = document.getElementById('reset-api-error');
    var strengthFill = document.getElementById('pw-strength-fill-reset');
    var feedbackEl = document.getElementById('pw-feedback-reset');
    var reqItems = document.querySelectorAll('#pw-req-list-reset .pw-req-r');

    if (!passInput || !confirmInput || !submitBtn || !form) {
        return;
    }

    var serverTokenValid =
        typeof window.__HP_RESET_TOKEN_VALID__ !== 'undefined'
            ? window.__HP_RESET_TOKEN_VALID__ === true
            : null;
    if (serverTokenValid === false) {
        if (noTokenEl) {
            noTokenEl.classList.remove('reset-panel-hidden');
        }
        if (contentEl) {
            contentEl.classList.add('reset-panel-hidden');
        }
        return;
    }

    if (!token || !token.trim()) {
        if (noTokenEl) {
            noTokenEl.classList.remove('reset-panel-hidden');
        }
        if (contentEl) {
            contentEl.classList.add('reset-panel-hidden');
        }
        return;
    }

    token = token.trim();

    var SPECIAL_RE = /[@#$%^&+=!]/;

    function evaluateRules(value) {
        return {
            len: value.length >= 8,
            upper: /[A-Z]/.test(value),
            lower: /[a-z]/.test(value),
            digit: /\d/.test(value),
            special: SPECIAL_RE.test(value)
        };
    }

    function countMet(rules) {
        var n = 0;
        for (var k in rules) {
            if (Object.prototype.hasOwnProperty.call(rules, k) && rules[k]) {
                n++;
            }
        }
        return n;
    }

    function computeStrength(rules) {
        var met = countMet(rules);
        if (met === 5) {
            return 'strong';
        }
        if (met >= 3) {
            return 'medium';
        }
        return 'weak';
    }

    /** Verde si cumple, gris si no (sin rojo). */
    function updateRequirements(rules, hasInput) {
        for (var i = 0; i < reqItems.length; i++) {
            var li = reqItems[i];
            var key = li.getAttribute('data-rule');
            var ok = rules[key];
            li.classList.remove('pw-req--ok', 'pw-req--bad', 'pw-req--idle');
            if (!hasInput) {
                li.classList.add('pw-req--idle');
            } else if (ok) {
                li.classList.add('pw-req--ok');
            } else {
                li.classList.add('pw-req--idle');
            }
        }
    }

    function updateStrengthBar(strength) {
        if (!strengthFill) {
            return;
        }
        strengthFill.classList.remove('pw-strength-fill--weak', 'pw-strength-fill--medium', 'pw-strength-fill--strong');
        strengthFill.style.width = '0%';
        if (!strength) {
            return;
        }
        if (strength === 'weak') {
            strengthFill.classList.add('pw-strength-fill--weak');
            strengthFill.style.width = '33%';
        } else if (strength === 'medium') {
            strengthFill.classList.add('pw-strength-fill--medium');
            strengthFill.style.width = '66%';
        } else {
            strengthFill.classList.add('pw-strength-fill--strong');
            strengthFill.style.width = '100%';
        }
    }

    function updateFeedback(strength, hasInput) {
        if (!feedbackEl) {
            return;
        }
        feedbackEl.classList.remove('pw-feedback--weak', 'pw-feedback--medium', 'pw-feedback--strong');
        if (!hasInput) {
            feedbackEl.textContent = '';
            return;
        }
        if (strength === 'weak') {
            feedbackEl.textContent = 'Weak';
            feedbackEl.classList.add('pw-feedback--weak');
        } else if (strength === 'medium') {
            feedbackEl.textContent = 'Medium';
            feedbackEl.classList.add('pw-feedback--medium');
        } else {
            feedbackEl.textContent = 'Strong';
            feedbackEl.classList.add('pw-feedback--strong');
        }
    }

    function isPasswordValid(rules) {
        return rules.len && rules.upper && rules.lower && rules.digit && rules.special;
    }

    function hideApiErr() {
        if (apiErr) {
            apiErr.classList.add('reset-panel-hidden');
            apiErr.textContent = '';
        }
    }

    function showApiErr(msg) {
        if (apiErr) {
            apiErr.textContent = msg;
            apiErr.classList.remove('reset-panel-hidden');
        }
    }

    function friendlyApiMessage(d) {
        if (!d || typeof d !== 'object') {
            return 'No se pudo actualizar la contrase\u00f1a. Intenta de nuevo.';
        }
        if (d.fieldErrors && typeof d.fieldErrors === 'object') {
            var fe = d.fieldErrors;
            var first = fe.password || fe.confirmPassword || fe.token;
            if (first) {
                return first;
            }
        }
        if (d.message && typeof d.message === 'string' && d.message.trim()) {
            return d.message.trim();
        }
        if (d.error === 'MISMATCH') {
            return 'Las contrase\u00f1as no coinciden.';
        }
        if (d.error === 'INVALID_RESET') {
            return 'El enlace ha expirado o no es v\u00e1lido.';
        }
        if (d.error) {
            return String(d.error).replace(/_/g, ' ');
        }
        return 'No se pudo actualizar la contrase\u00f1a. Intenta de nuevo.';
    }

    function refresh() {
        hideApiErr();
        var pv = passInput.value;
        var cv = confirmInput.value;
        var hasInput = pv.length > 0;
        var rules = evaluateRules(pv);
        var strength = computeStrength(rules);

        updateRequirements(rules, hasInput);
        updateStrengthBar(hasInput ? strength : '');
        updateFeedback(strength, hasInput);

        if (confirmErr) {
            confirmErr.style.display = 'none';
            confirmErr.textContent = '';
        }

        var passOk = isPasswordValid(rules);
        var match = cv.length > 0 && pv === cv;
        if (cv.length > 0 && pv !== cv) {
            if (confirmErr) {
                confirmErr.textContent = 'Las contrase\u00f1as no coinciden';
                confirmErr.style.display = 'block';
            }
        }

        submitBtn.disabled = !(passOk && match);
    }

    passInput.addEventListener('input', refresh);
    passInput.addEventListener('paste', function () {
        requestAnimationFrame(refresh);
    });
    confirmInput.addEventListener('input', refresh);
    confirmInput.addEventListener('paste', function () {
        requestAnimationFrame(refresh);
    });

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        var rules = evaluateRules(passInput.value);
        if (!isPasswordValid(rules)) {
            return;
        }
        var pv = passInput.value;
        var cv = confirmInput.value;
        if (pv !== cv) {
            if (confirmErr) {
                confirmErr.textContent = 'Las contrase\u00f1as no coinciden';
                confirmErr.style.display = 'block';
            }
            return;
        }
        if (submitBtn.disabled) {
            return;
        }

        hideApiErr();
        submitBtn.disabled = true;
        fetch('/api/auth/reset-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json'
            },
            body: JSON.stringify({
                token: token,
                password: pv,
                confirmPassword: cv
            })
        })
            .then(function (res) {
                return res.text().then(function (text) {
                    var data = {};
                    if (text) {
                        try {
                            data = JSON.parse(text);
                        } catch (err) {
                            data = { message: text };
                        }
                    }
                    return { status: res.status, data: data, ok: res.ok };
                });
            })
            .then(function (result) {
                if (result.ok && result.status >= 200 && result.status < 300) {
                    window.location.href = '/login?passwordUpdated=true';
                    return;
                }
                showApiErr(friendlyApiMessage(result.data));
            })
            .catch(function () {
                showApiErr('No se pudo conectar. Intenta de nuevo.');
            })
            .finally(function () {
                refresh();
            });
    });

    refresh();
})();
