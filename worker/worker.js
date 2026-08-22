/**
 * FocusFlow Shop Worker: создание платежа ЮKassa + автовыдача кода.
 * Секреты (Variables → Secrets в Cloudflare):
 *   YOOKASSA_SHOP_ID, YOOKASSA_SECRET_KEY
 * KV-биндинг (Variables → KV): SHOP
 */
const SECRET = "FocusFlow-RuStore-2026-Secret";
const API = "https://api.yookassa.ru/v3";
const PRICE = "399.00";

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const auth = "Basic " + btoa(`${env.YOOKASSA_SHOP_ID}:${env.YOOKASSA_SECRET_KEY}`);

    // 1) Создание платежа (кнопка «Купить» на сайте)
    if (request.method === "POST" && url.pathname === "/create-payment") {
      const res = await fetch(`${API}/payments`, {
        method: "POST",
        headers: {
          Authorization: auth,
          "Content-Type": "application/json",
          "Idempotence-Key": crypto.randomUUID(),
        },
        body: JSON.stringify({
          amount: { value: PRICE, currency: "RUB" },
          capture: true,
          confirmation: { type: "redirect", return_url: `${url.origin}/success` },
          description: "FocusFlow Premium — код активации",
        }),
      });
      const data = await res.json();
      if (!res.ok) return json({ error: data.description || "yookassa error" }, 502);
      return json({ url: data.confirmation.confirmation_url, id: data.id });
    }

    // 2) Страница успеха: проверка платежа + выдача кода
    if (request.method === "GET" && url.pathname === "/success") {
      const paymentId = url.searchParams.get("payment_id");
      if (!paymentId) return html(errorPage("Не указан payment_id."), 400);

      const res = await fetch(`${API}/payments/${paymentId}`, {
        headers: { Authorization: auth },
      });
      const data = await res.json();
      if (data.status !== "succeeded") {
        return html(errorPage("Платёж не завершён. Если деньги списались — код придёт после подтверждения, обновите страницу через минуту."), 400);
      }

      const code = await issueForPayment(env, paymentId);
      return html(successPage(code));
    }

    // 3) Webhook ЮKassa (дублирующая выдача + журнал продаж)
    if (request.method === "POST" && url.pathname === "/webhook") {
      const data = await request.json();
      if (data.event === "payment.succeeded") {
        await issueForPayment(env, data.object.id);
      }
      return json({ ok: true });
    }

    return json({ ok: true, service: "focusflow-shop-worker" });
  },
};

// ===== выдача кода с идемпотентностью и защитой от повторов =====
async function issueForPayment(env, paymentId) {
  const existing = await env.SHOP.get(`issued:${paymentId}`);
  if (existing) return existing;

  const code = await generateUniqueCode(env);
  await env.SHOP.put(`issued:${paymentId}`, code);
  await env.SHOP.put(`code:${code}`, paymentId);
  await env.SHOP.put(`log:${Date.now()}`, `${paymentId} ${code}`);
  return code;
}

async function generateUniqueCode(env) {
  for (let i = 0; i < 5; i++) {
    const clean = randomPayload();
    const formatted = `${clean.slice(0, 4)}-${clean.slice(4, 8)}-${clean.slice(8, 12)}`;
    const code = `${formatted}-${await hmacChecksum(formatted)}`;
    const taken = await env.SHOP.get(`code:${code}`);
    if (!taken) return code;
  }
  throw new Error("failed to generate unique code");
}

function randomPayload() {
  const t = Date.now().toString(36).toUpperCase();
  const abc = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  const rnd = Array.from(crypto.getRandomValues(new Uint8Array(8)))
    .map((b) => abc[b % 36])
    .join("");
  return (t + rnd).replace(/[^A-Z0-9]/g, "").padEnd(12, "X").slice(0, 12);
}

async function hmacChecksum(formatted) {
  const enc = new TextEncoder();
  const key = await crypto.subtle.importKey(
    "raw", enc.encode(SECRET), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]
  );
  const sig = new Uint8Array(await crypto.subtle.sign("HMAC", key, enc.encode(formatted)));
  return Array.from(sig.slice(0, 2))
    .map((b) => b.toString(16).padStart(2, "0").toUpperCase())
    .join("");
}

// ===== страницы =====
function successPage(code) {
  return `<!DOCTYPE html><html lang="ru"><head><meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Оплата прошла — ваш код</title>
<style>body{font-family:system-ui,sans-serif;max-width:640px;margin:0 auto;padding:24px;line-height:1.6}
.code{font-size:26px;font-weight:700;letter-spacing:1px;background:#f4f4f4;border:2px dashed #d33;border-radius:12px;padding:16px;text-align:center;margin:16px 0;user-select:all}
.btn{display:block;background:#d33;color:#fff;padding:14px;border-radius:10px;text-align:center;text-decoration:none;font-weight:700}
.muted{color:#777;font-size:14px}</style></head><body>
<h1>✅ Оплата прошла!</h1>
<p>Ваш код активации FocusFlow Premium:</p>
<div class="code" id="c">${code}</div>
<a class="btn" href="#" onclick="navigator.clipboard.writeText(document.getElementById('c').innerText);this.innerText='Скопировано ✓';return false;">Скопировать код</a>
<p style="margin-top:16px"><b>Как активировать:</b> в приложении FocusFlow откройте Настройки → Premium → введите код.</p>
<p class="muted">Сохраните код (скриншот или заметки) — он выдаётся один раз. Вопросы: поддержка по email с сайта.</p>
</body></html>`;
}

function errorPage(msg) {
  return `<!DOCTYPE html><html lang="ru"><head><meta charset="UTF-8"><title>Ошибка</title>
<style>body{font-family:system-ui,sans-serif;max-width:640px;margin:0 auto;padding:24px}</style></head>
<body><h1>⚠️ Что-то пошло не так</h1><p>${msg}</p></body></html>`;
}

function json(obj, status = 200) {
  return new Response(JSON.stringify(obj), {
    status, headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
  });
}
function html(page, status = 200) {
  return new Response(page, { status, headers: { "Content-Type": "text/html; charset=utf-8" } });
}