/**
 * HappyPet - Carrito (API REST + sesion)
 */
(function (global) {
    'use strict';

    var DEFAULT_IMG = 'https://upload.wikimedia.org/wikipedia/commons/thumb/a/a5/8kg_Beneful_Original_Dog_Food_Bag_%2833239682124%29.jpg/400px-8kg_Beneful_Original_Dog_Food_Bag_%2833239682124%29.jpg';
    var cartSnapshot = null;
    var cartToastFallbackTimer = null;

    var paths = {
        cart: '/api/cart',
        add: '/api/cart/add',
        update: '/api/cart/update',
        remove: function (id) { return '/api/cart/remove/' + id; },
        /** Web: primer paso del checkout (entrega). GET /checkout redirige aquí también. */
        checkoutPage: '/checkout/entrega',
        checkoutApi: '/api/orders/checkout',
        login: '/login'
    };

    function redirectLogin() {
        window.location.href = paths.login;
    }

    function isAuthError(status) {
        return status === 401 || status === 403;
    }

    function isAuthenticatedClientSide() {
        return document.body && document.body.getAttribute('data-authenticated') === 'true';
    }

    function formatMoney(n) {
        return new Intl.NumberFormat('es-CO', { maximumFractionDigits: 0 }).format(n);
    }

    function parseApiError(res, bodyText) {
        try {
            var j = JSON.parse(bodyText);
            return j.message || j.error || 'Error en la solicitud';
        } catch (e) {
            return res.statusText || 'Error';
        }
    }

    /**
     * @param {boolean} redirectOnAuth - si es false (solo lectura del carrito), 401 no redirige:
     * permite ver el catálogo en inicio/tienda sin sesión; agregar al carrito sigue usando redirect.
     */
    function fetchJson(url, options, redirectOnAuth) {
        var redirect = redirectOnAuth !== false;
        return fetch(url, options).then(function (res) {
            if (isAuthError(res.status)) {
                if (redirect) {
                    redirectLogin();
                }
                return Promise.reject(new Error('Sesion requerida'));
            }
            return res.text().then(function (text) {
                if (res.ok) {
                    if (!text) return null;
                    try { return JSON.parse(text); } catch (e) { return null; }
                }
                throw new Error(parseApiError(res, text));
            });
        });
    }

    function loadCart() {
        return fetch(paths.cart, { credentials: 'same-origin', headers: { Accept: 'application/json' } })
            .then(function (res) {
                if (isAuthError(res.status)) {
                    return { items: [], total: 0 };
                }
                return res.text().then(function (text) {
                    if (!res.ok) {
                        throw new Error(text ? parseApiError(res, text) : (res.statusText || 'Error'));
                    }
                    if (!text) return { items: [], total: 0 };
                    try {
                        return JSON.parse(text);
                    } catch (e) {
                        return { items: [], total: 0 };
                    }
                });
            });
    }

    function addToCart(productoId, cantidad) {
        return fetchJson(paths.add, {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            body: JSON.stringify({ productoId: productoId, cantidad: cantidad })
        });
    }

    function updateQuantity(itemId, cantidad) {
        return fetchJson(paths.update, {
            method: 'PUT',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            body: JSON.stringify({ itemId: itemId, cantidad: cantidad })
        });
    }

    function removeItem(itemId) {
        return fetchJson(paths.remove(itemId), {
            method: 'DELETE',
            credentials: 'same-origin',
            headers: { Accept: 'application/json' }
        });
    }

    function checkoutApi(payload) {
        return fetchJson(paths.checkoutApi, {
            method: 'POST',
            credentials: 'same-origin',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            body: JSON.stringify(payload || {})
        });
    }

    function extractProductIdFromForm(form) {
        var dataId = form.getAttribute('data-product-id');
        if (dataId) return parseInt(dataId, 10);
        var action = form.getAttribute('action') || '';
        var m = action.match(/\/carrito\/agregar\/(\d+)/);
        return m ? parseInt(m[1], 10) : null;
    }

    function getCartCount(cartData) {
        var n = 0;
        if (cartData && cartData.items && cartData.items.length) {
            cartData.items.forEach(function (i) { n += i.cantidad || 0; });
        }
        return n;
    }

    function updateNavCartBadge(cartData) {
        var count = getCartCount(cartData);
        var link = document.querySelector('a.nav-link[href*="/carrito"]');
        if (link) {
            var badge = link.querySelector('.hp-cart-badge');
            if (count > 0) {
                if (!badge) {
                    badge = document.createElement('span');
                    badge.className = 'hp-cart-badge badge rounded-pill bg-primary ms-1';
                    badge.setAttribute('aria-label', 'Articulos en carrito');
                    link.appendChild(badge);
                }
                badge.textContent = String(count);
            } else if (badge) {
                badge.remove();
            }
        }

        var compactCount = document.getElementById('catalog-cart-count');
        if (compactCount) compactCount.textContent = String(count);
    }

    function itemImageUrl(item) {
        if (item.imagenUrl && String(item.imagenUrl).trim()) return item.imagenUrl;
        return DEFAULT_IMG;
    }

    function showToast(message) {
        var el = document.getElementById('hp-cart-toast');
        var textEl = document.getElementById('hp-cart-toast-text');
        if (!el) {
            return;
        }
        var txt = message || 'Producto agregado al carrito';
        if (textEl) {
            textEl.textContent = txt;
        }
        if (typeof bootstrap !== 'undefined' && bootstrap.Toast) {
            var t = bootstrap.Toast.getOrCreateInstance(el, { autohide: true, delay: 5000 });
            t.show();
        } else {
            el.classList.add('hp-cart-toast-fallback');
            if (cartToastFallbackTimer) {
                clearTimeout(cartToastFallbackTimer);
            }
            cartToastFallbackTimer = setTimeout(function () {
                el.classList.remove('hp-cart-toast-fallback');
            }, 5000);
        }
    }

    function openMiniCart() {
        var mini = document.getElementById('mini-cart');
        if (mini) mini.classList.add('is-open');
    }

    function closeMiniCart() {
        var mini = document.getElementById('mini-cart');
        if (mini) mini.classList.remove('is-open');
    }

    function renderMiniCart(cartData) {
        var list = document.getElementById('mini-cart-items');
        var subtotal = document.getElementById('mini-cart-subtotal');
        if (!list || !subtotal) return;
        subtotal.textContent = '$' + formatMoney((cartData && cartData.total) ? cartData.total : 0);

        if (!cartData || !cartData.items || !cartData.items.length) {
            list.innerHTML = '<p class="text-muted small mb-0">Tu carrito está vacío.</p>';
            return;
        }

        list.innerHTML = '';
        cartData.items.forEach(function (item) {
            var row = document.createElement('div');
            row.className = 'mini-cart__item';
            row.innerHTML =
                '<span class="mini-cart__item-name"></span>' +
                '<span class="mini-cart__item-price"></span>' +
                '<span class="mini-cart__item-meta"></span>';
            row.querySelector('.mini-cart__item-name').textContent = item.nombre || 'Producto';
            row.querySelector('.mini-cart__item-price').textContent = '$' + formatMoney(item.subtotal || 0);
            row.querySelector('.mini-cart__item-meta').textContent = 'Cantidad: ' + (item.cantidad || 0);
            list.appendChild(row);
        });
    }

    function getItemByProductId(cartData, productId) {
        if (!cartData || !cartData.items || !cartData.items.length) return null;
        for (var i = 0; i < cartData.items.length; i += 1) {
            if (Number(cartData.items[i].productoId) === Number(productId)) return cartData.items[i];
        }
        return null;
    }

    function createQtyControls(form, productId, item) {
        var qty = item && item.cantidad ? item.cantidad : 0;
        form.classList.add('product-card__form--active');
        form.innerHTML =
            '<div class="hp-qty-controls" data-product-id="' + productId + '">' +
            '<button type="button" class="hp-qty-btn hp-qty-btn--remove" aria-label="Quitar una unidad">🗑️</button>' +
            '<span class="hp-qty-value">' + qty + '</span>' +
            '<button type="button" class="hp-qty-btn hp-qty-btn--plus" aria-label="Agregar una unidad">+</button>' +
            '</div>';
    }

    function refreshProductCardForms(cartData) {
        document.querySelectorAll('form[action*="/carrito/agregar/"], form[data-product-id]').forEach(function (form) {
            var productId = extractProductIdFromForm(form);
            if (!productId) return;
            var item = getItemByProductId(cartData, productId);
            if (item && item.cantidad > 0) {
                createQtyControls(form, productId, item);
            }
        });
    }

    function applyCartStateEverywhere(cartData) {
        cartSnapshot = cartData || { items: [], total: 0 };
        updateNavCartBadge(cartSnapshot);
        renderMiniCart(cartSnapshot);
        refreshProductCardForms(cartSnapshot);
    }

    function renderCartRows(container, cartData) {
        container.innerHTML = '';
        if (!cartData.items || !cartData.items.length) return;
        cartData.items.forEach(function (item) {
            var row = document.createElement('div');
            row.className = 'cart-row hp-cart-item card cart-card mb-3';
            row.dataset.itemId = String(item.id);
            row.innerHTML =
                '<div class="card-body cart-card-body hp-cart-item__body">' +
                '<div class="row g-3 align-items-center">' +
                '<div class="col-auto"><img class="cart-item-img hp-cart-item__img rounded" width="88" height="88" loading="lazy" alt=""/></div>' +
                '<div class="col"><h3 class="h6 mb-1 cart-item-name hp-cart-item__name"></h3>' +
                '<p class="mb-0 small text-muted hp-cart-item__unit-label">Precio unitario: $<span class="cart-item-unit"></span></p></div>' +
                '<div class="col-6 col-md-2"><label class="form-label small mb-0 hp-cart-qty-label">Cantidad</label>' +
                '<input type="number" min="1" class="form-control hp-cart-qty cart-qty" inputmode="numeric"/></div>' +
                '<div class="col-6 col-md-2 text-md-end"><p class="mb-0 small text-muted">Subtotal</p>' +
                '<p class="mb-0 fw-semibold hp-cart-item__sub">$<span class="cart-item-sub"></span></p></div>' +
                '<div class="col-12 col-md-2 text-md-end">' +
                '<button type="button" class="btn btn-outline-danger hp-cart-remove cart-remove">Quitar</button></div></div></div>';
            var img = row.querySelector('.cart-item-img');
            img.src = itemImageUrl(item);
            img.alt = item.nombre || 'Producto';
            row.querySelector('.cart-item-name').textContent = item.nombre || '';
            row.querySelector('.cart-item-unit').textContent = formatMoney(item.precioUnitario);
            row.querySelector('.cart-item-sub').textContent = formatMoney(item.subtotal);
            var qtyInput = row.querySelector('.cart-qty');
            qtyInput.value = String(item.cantidad);
            qtyInput.addEventListener('change', function () {
                var v = parseInt(qtyInput.value, 10);
                if (!v || v < 1) {
                    qtyInput.value = String(item.cantidad);
                    return;
                }
                updateQuantity(item.id, v).then(function (data) {
                    refreshCartPage(data);
                    applyCartStateEverywhere(data);
                }).catch(function (err) {
                    alert(err.message || 'Error al actualizar');
                    qtyInput.value = String(item.cantidad);
                });
            });
            row.querySelector('.cart-remove').addEventListener('click', function () {
                removeItem(item.id).then(function (data) {
                    refreshCartPage(data);
                    applyCartStateEverywhere(data);
                }).catch(function (err) {
                    alert(err.message || 'Error al quitar');
                });
            });
            container.appendChild(row);
        });
    }

    function refreshCartPage(cartData) {
        var empty = document.getElementById('cart-empty-state');
        var list = document.getElementById('cart-items-list');
        var summary = document.getElementById('cart-summary-wrap');
        var totalEl = document.getElementById('cart-total-value');
        var hasItems = cartData && cartData.items && cartData.items.length > 0;
        if (empty) empty.hidden = hasItems;
        if (list) {
            list.hidden = !hasItems;
            if (hasItems) renderCartRows(list, cartData);
            else list.innerHTML = '';
        }
        if (summary) summary.hidden = !hasItems;
        if (totalEl) totalEl.textContent = formatMoney(hasItems ? cartData.total : 0);
    }

    function onPlusClick(form, productId) {
        var current = getItemByProductId(cartSnapshot, productId);
        if (!current) return addToCart(productId, 1);
        return updateQuantity(current.id, (current.cantidad || 0) + 1);
    }

    function onRemoveClick(form, productId) {
        var current = getItemByProductId(cartSnapshot, productId);
        if (!current) return Promise.resolve(cartSnapshot);
        if ((current.cantidad || 0) <= 1) return removeItem(current.id);
        return updateQuantity(current.id, current.cantidad - 1);
    }

    function initAddToCartForms() {
        document.querySelectorAll('form[action*="/carrito/agregar/"], form[data-product-id]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                e.preventDefault();
                var productId = extractProductIdFromForm(form);
                if (!productId) return;
                if (!isAuthenticatedClientSide()) {
                    redirectLogin();
                    return;
                }
                addToCart(productId, 1).then(function (data) {
                    if (!data) return;
                    applyCartStateEverywhere(data);
                    showToast('Producto agregado al carrito');
                    openMiniCart();
                }).catch(function (err) {
                    alert(err.message || 'No se pudo agregar el producto');
                });
            });

            form.addEventListener('click', function (e) {
                var plusBtn = e.target.closest('.hp-qty-btn--plus');
                var removeBtn = e.target.closest('.hp-qty-btn--remove');
                if (!plusBtn && !removeBtn) return;
                e.preventDefault();
                var productId = extractProductIdFromForm(form);
                if (!productId) return;
                if (!isAuthenticatedClientSide()) {
                    redirectLogin();
                    return;
                }
                var promise = plusBtn ? onPlusClick(form, productId) : onRemoveClick(form, productId);
                promise.then(function (data) {
                    if (!data) return;
                    applyCartStateEverywhere(data);
                    if (plusBtn) {
                        showToast('Producto agregado al carrito');
                        openMiniCart();
                    }
                }).catch(function (err) {
                    alert(err.message || 'No se pudo actualizar el carrito');
                });
            });
        });
    }

    function initMiniCartActions() {
        var closeBtn = document.getElementById('mini-cart-close');
        var keepBtn = document.getElementById('mini-cart-keep-buying');
        if (closeBtn) closeBtn.addEventListener('click', closeMiniCart);
        if (keepBtn) keepBtn.addEventListener('click', closeMiniCart);
    }

    function initCartPage() {
        var root = document.querySelector('main.hp-cart-page');
        if (!root) return;
        var empty = document.getElementById('cart-empty-state');
        var list = document.getElementById('cart-items-list');
        var summary = document.getElementById('cart-summary-wrap');
        if (empty) empty.hidden = true;
        if (list) list.hidden = true;
        if (summary) summary.hidden = true;
        loadCart().then(function (data) {
            if (!data) return;
            applyCartStateEverywhere(data);
            refreshCartPage(data);
            var btn = document.getElementById('cart-checkout-btn');
            if (btn) {
                btn.onclick = function () {
                    if (!isAuthenticatedClientSide()) {
                        redirectLogin();
                        return;
                    }
                    var rows = document.getElementById('cart-items-list');
                    if (!rows || rows.hidden || !rows.children.length) return;
                    window.location.href = paths.checkoutPage;
                };
            }
        }).catch(function (err) {
            alert(err.message || 'Error al cargar el carrito');
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        initAddToCartForms();
        initMiniCartActions();
        loadCart().then(function (data) {
            if (data) applyCartStateEverywhere(data);
        }).catch(function () {});
        initCartPage();
    });

    global.HappyPetCart = {
        loadCart: loadCart,
        updateQuantity: updateQuantity,
        removeItem: removeItem,
        checkoutApi: checkoutApi,
        addToCart: addToCart,
        initAddToCartForms: initAddToCartForms,
        initCartPage: initCartPage,
        updateNavCartBadge: updateNavCartBadge
    };
})(typeof window !== 'undefined' ? window : this);