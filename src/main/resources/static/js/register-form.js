/**
 * Registro: validacion de nombre (letras, espacios, acentos) y contrasena en tiempo real.
 */
(function () {
    'use strict';

    /** /^[a-zA-ZaeiouAEIOUnN ]+$/ con acentos y enie */
    var NOMBRE_RE = /^[a-zA-Z\u00e1\u00e9\u00ed\u00f3\u00fa\u00c1\u00c9\u00cd\u00d3\u00da\u00f1\u00d1 ]+$/;
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

    function updateRequirements(reqItems, rules, hasInput) {
        for (var i = 0; i < reqItems.length; i++) {
            var li = reqItems[i];
            var key = li.getAttribute('data-rule');
            var ok = rules[key];
            li.classList.remove('pw-req--ok', 'pw-req--bad', 'pw-req--idle');
            if (!hasInput) {
                li.classList.add('pw-req--idle');
            } else {
                li.classList.add(ok ? 'pw-req--ok' : 'pw-req--bad');
            }
        }
    }

    function updateStrengthBar(strengthFill, strength) {
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

    function updateFeedback(feedbackEl, strength, hasInput) {
        if (!feedbackEl) {
            return;
        }
        feedbackEl.classList.remove('pw-feedback--weak', 'pw-feedback--medium', 'pw-feedback--strong');
        if (!hasInput) {
            feedbackEl.textContent = '';
            return;
        }
        if (strength === 'weak') {
            feedbackEl.textContent = 'Tu contrase\u00f1a es d\u00e9bil';
            feedbackEl.classList.add('pw-feedback--weak');
        } else if (strength === 'medium') {
            feedbackEl.textContent = 'Puede mejorar';
            feedbackEl.classList.add('pw-feedback--medium');
        } else {
            feedbackEl.textContent = 'Contrase\u00f1a segura';
            feedbackEl.classList.add('pw-feedback--strong');
        }
    }

    function isPasswordValid(rules) {
        return rules.len && rules.upper && rules.lower && rules.digit && rules.special;
    }

    function initRegisterValidation() {
        var nombreInput = document.getElementById('nombre');
        var nombreErr = document.getElementById('nombre-error');
        var passwordInput = document.getElementById('password');
        var submitBtn = document.getElementById('register-submit');
        var strengthFill = document.getElementById('pw-strength-fill');
        var feedbackEl = document.getElementById('pw-feedback');
        var reqItems = document.querySelectorAll('#pw-req-list li[data-rule]');

        if (!nombreInput || !passwordInput || !submitBtn) {
            return;
        }

        function updateNombreUi() {
            var v = nombreInput.value;
            nombreInput.classList.remove('hp-field-input--ok', 'hp-field-input--err');
            if (nombreErr) {
                nombreErr.textContent = '';
                nombreErr.style.display = 'none';
            }

            if (v.length === 0) {
                return false;
            }
            if (!NOMBRE_RE.test(v)) {
                nombreInput.classList.add('hp-field-input--err');
                if (nombreErr) {
                    nombreErr.textContent =
                        'Solo se permiten letras (incluye acentos y \u00f1) y espacios.';
                    nombreErr.style.display = 'block';
                }
                return false;
            }
            nombreInput.classList.add('hp-field-input--ok');
            return true;
        }

        function refreshPasswordUi() {
            var value = passwordInput.value;
            var hasInput = value.length > 0;
            var rules = evaluateRules(value);
            var strength = computeStrength(rules);

            updateRequirements(reqItems, rules, hasInput);
            updateStrengthBar(strengthFill, hasInput ? strength : '');
            updateFeedback(feedbackEl, strength, hasInput);

            return isPasswordValid(rules);
        }

        function syncSubmitState() {
            var nombreOk = updateNombreUi();
            var passOk = refreshPasswordUi();
            submitBtn.disabled = !(nombreOk && passOk);
        }

        document.getElementById('nombre').addEventListener('input', syncSubmitState);
        document.getElementById('password').addEventListener('input', syncSubmitState);
        document.getElementById('nombre').addEventListener('paste', function () {
            requestAnimationFrame(syncSubmitState);
        });
        document.getElementById('password').addEventListener('paste', function () {
            requestAnimationFrame(syncSubmitState);
        });

        syncSubmitState();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initRegisterValidation);
    } else {
        initRegisterValidation();
    }
})();
