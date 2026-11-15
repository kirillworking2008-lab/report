# Running Design System — v1.0

Переносимая дизайн-система из приложения **Running 1.0.19**: тёмный «стеклянный»
интерфейс, один акцентный цвет, мобильный каркас 440px.

## Файлы

- **`theme.css`** — всё, что нужно для стиля: токены, каркас, компоненты.
- **`index.html`** — демо-страница со всеми компонентами и переключателем акцента.
- **`STARTER-PROMPT.md`** — промпт для новой сессии + шпаргалка по стилю.

## Быстрый старт

```html
<link rel="stylesheet" href="theme.css">

<div class="app-shell">
  <header class="app-header">
    <a class="brand" href="#">
      <span class="brand-mark"><img src="icons/icon-192.png" alt=""></span>
      <span class="brand-copy">
        <strong>Название</strong>
        <small>ПОДЗАГОЛОВОК · 1.0</small>
      </span>
    </a>
    <div class="header-actions">
      <button class="header-icon" aria-label="Меню">…svg…</button>
    </div>
  </header>

  <main class="screen">
    <section class="card card-hero">…</section>
    <section class="stat-grid three">…</section>
  </main>

  <nav class="bottom-nav">
    <button class="nav-item" aria-current="page">…</button>
  </nav>
</div>
```

## Токены

| Переменная | Значение | Зачем |
|---|---|---|
| `--ink` | `#f4f7f8` | основной текст |
| `--muted` | `#849199` | вторичный текст |
| `--faint` | `#56636b` | самый тихий текст |
| `--panel` | `rgba(255,255,255,.045)` | поверхность |
| `--stroke` | `rgba(255,255,255,.085)` | бордер стекла |
| `--field` | `rgba(3,8,12,.45)` | фон полей ввода |
| `--accent` | `#73e3cf` | акцент (mint) |
| `--accent-rgb` | `115,227,207` | для `rgba()` и свечений |
| `--accent-deep` | `#173b3b` | глубокая тень акцента |

Акценты: `data-theme="violet"` → `#b9a4ff`, `data-theme="amber"` → `#ffb376`.

## Компоненты

`.app-shell` · `.ambient` · `.app-header` / `.brand` / `.header-icon` ·
`.screen` · `.section-heading` · `.card` / `.card-hero` · `.stat-grid` / `.stat-card` ·
`.list` / `.list-row` · `.empty` · `.bar-track` / `.bar-fill` · `.day-bars` ·
`.primary-button` / `.ghost-button` · `.chip` · `.segmented` · `.field` / `.input` ·
`.bottom-nav` / `.nav-item` · `.app-menu` · `.toast` · `.sheet` / `.sheet-backdrop` ·
утилиты `.muted` `.faint` `.accent` `.tabular` `.stack` `.row` `.spacer`

## Правила

1. Новые цвета не вводим — только существующие токены.
2. Один акцент на экран; свечение акцента — точечно, не на каждой карточке.
3. Скругления: 17 / 14 / 16 / 26 / 30 px по типу элемента.
4. Иконки — inline SVG, `stroke` 1.7–2.2, `currentColor`.
5. Уважаем `prefers-reduced-motion` (уже учтено в теме).
