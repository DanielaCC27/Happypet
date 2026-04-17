// HappyPet - Municipios Colombia. UTF-8. Lista de municipios tras elegir departamento (API, JSON estatico, JSON en pagina).
(function () {
    'use strict';

    var PLACEHOLDER_SIN_DEPT = 'Primero elige un departamento';
    var PLACEHOLDER_LISTA = 'Seleccione municipio';
    var PLACEHOLDER_CARGA = 'Cargando municipios…';
    var PLACEHOLDER_ERROR = 'No se pudieron cargar los municipios. Recarga la página.';

    function norm(s) {
        if (s === undefined || s === null) {
            return '';
        }
        s = String(s).trim();
        return s.normalize ? s.normalize('NFC') : s;
    }

    function findDepartamento(arr, nombre) {
        var n = norm(nombre);
        if (!n || !arr) {
            return undefined;
        }
        var i;
        for (i = 0; i < arr.length; i++) {
            if (norm(String(arr[i].departamento)) === n) {
                return arr[i];
            }
        }
        var nl = n.toLowerCase();
        for (i = 0; i < arr.length; i++) {
            if (norm(String(arr[i].departamento)).toLowerCase() === nl) {
                return arr[i];
            }
        }
        return undefined;
    }

    function parseEmbeddedJson() {
        var embed = document.getElementById('checkout-colombia-json');
        if (!embed || !embed.textContent) {
            return undefined;
        }
        var raw = embed.textContent.trim();
        if (raw.length > 0 && raw.charCodeAt(0) === 0xfeff) {
            raw = raw.slice(1);
        }
        raw = raw.trim();
        if (!raw) {
            return undefined;
        }
        try {
            return JSON.parse(raw);
        } catch (e) {
            return undefined;
        }
    }

    function fetchJson(url) {
        return fetch(url, {
            credentials: 'same-origin',
            headers: { Accept: 'application/json' }
        }).then(function (r) {
            if (!r.ok) {
                throw new Error('HTTP ' + r.status);
            }
            return r.json();
        });
    }

    function tryUrlsSequentially(urls, index, onSuccess, onFail) {
        if (!urls || index >= urls.length) {
            onFail();
            return;
        }
        var u = urls[index];
        if (!u) {
            tryUrlsSequentially(urls, index + 1, onSuccess, onFail);
            return;
        }
        fetchJson(u)
            .then(function (data) {
                if (data && Array.isArray(data) && data.length) {
                    onSuccess(data);
                } else {
                    tryUrlsSequentially(urls, index + 1, onSuccess, onFail);
                }
            })
            .catch(function () {
                tryUrlsSequentially(urls, index + 1, onSuccess, onFail);
            });
    }

    function clearSelectToSingleOption(sel, text) {
        while (sel.options.length > 0) {
            sel.remove(0);
        }
        sel.add(new Option(text, ''));
    }

    function rebuildMunicipios(munSel, colombia, departamentoNombre, municipioSeleccionado, emptyLabel) {
        while (munSel.options.length > 0) {
            munSel.remove(0);
        }
        var ph = emptyLabel || PLACEHOLDER_LISTA;
        munSel.add(new Option(ph, ''));
        if (!departamentoNombre || !colombia || !Array.isArray(colombia)) {
            return;
        }
        var dep = findDepartamento(colombia, departamentoNombre);
        if (!dep || !dep.ciudades || !dep.ciudades.length) {
            return;
        }
        for (var j = 0; j < dep.ciudades.length; j++) {
            var c = dep.ciudades[j];
            var opt = new Option(c, c);
            if (municipioSeleccionado && norm(c) === norm(municipioSeleccionado)) {
                opt.selected = true;
            }
            munSel.add(opt);
        }
    }

    function init() {
        var deptSel = document.getElementById('departamentoEnvio') ||
            document.querySelector('select[name="departamentoEnvio"]');
        var munSel = document.getElementById('municipioEnvio') ||
            document.querySelector('select[name="municipioEnvio"]');
        if (!deptSel || !munSel) {
            return;
        }

        var initialMun = norm(munSel.getAttribute('data-initial-municipio') || '');
        var colombia = parseEmbeddedJson();
        var dataReady = !!(colombia && Array.isArray(colombia) && colombia.length);

        var cfg = document.getElementById('checkout-ubicacion-config');
        var apiUrl = cfg ? cfg.getAttribute('data-api-json') : '';
        var fallbackUrl = cfg ? cfg.getAttribute('data-fallback-json') : '';
        var urls = [];
        if (apiUrl) {
            urls.push(apiUrl);
        }
        if (fallbackUrl && urls.indexOf(fallbackUrl) < 0) {
            urls.push(fallbackUrl);
        }

        function applyData(data) {
            if (data && Array.isArray(data) && data.length) {
                colombia = data;
                dataReady = true;
            }
            if (!dataReady || !colombia) {
                clearSelectToSingleOption(munSel, PLACEHOLDER_ERROR);
                return;
            }
            if (deptSel.value) {
                rebuildMunicipios(munSel, colombia, deptSel.value, initialMun, PLACEHOLDER_LISTA);
            } else {
                clearSelectToSingleOption(munSel, PLACEHOLDER_SIN_DEPT);
            }
        }

        function onDepartamentoChange() {
            var d = deptSel.value;
            if (!d) {
                clearSelectToSingleOption(munSel, PLACEHOLDER_SIN_DEPT);
                return;
            }
            if (!dataReady || !colombia) {
                clearSelectToSingleOption(munSel, PLACEHOLDER_CARGA);
                return;
            }
            rebuildMunicipios(munSel, colombia, d, '', PLACEHOLDER_LISTA);
        }

        deptSel.addEventListener('change', onDepartamentoChange);
        deptSel.addEventListener('input', onDepartamentoChange);

        if (dataReady) {
            if (deptSel.value) {
                rebuildMunicipios(munSel, colombia, deptSel.value, initialMun, PLACEHOLDER_LISTA);
            } else {
                clearSelectToSingleOption(munSel, PLACEHOLDER_SIN_DEPT);
            }
        } else if (deptSel.value) {
            clearSelectToSingleOption(munSel, PLACEHOLDER_CARGA);
        } else {
            clearSelectToSingleOption(munSel, PLACEHOLDER_SIN_DEPT);
        }

        tryUrlsSequentially(
            urls,
            0,
            function (data) {
                applyData(data);
            },
            function () {
                applyData(parseEmbeddedJson());
            }
        );
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
