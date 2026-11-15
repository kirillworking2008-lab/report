# Передача контекста: приложение «Стержень»

> Этот файл — самодостаточная копия рабочего чата. Загрузи его в новый чат (вложением или текстом), чтобы продолжить работу без потери контекста. Всё, что агенту нужно знать, — ниже.

---

## 1. Задача

Android-приложение для личных ежедневных/недельных отчётов. Ядро — «стержень»: список принципов, по каждому ведётся «огонёк» (серия чистых дней). Принципы **пассивные** — отмечаться вручную не нужно, серия обновляется автоматически. Главный экран — круговая 3D-карусель с карточками принципов.

Ориентир по продукту: приложение-аналог называется **Habit Streak**.

## 2. Принципы (точный список, менять нельзя)

| # | Название | emoji | id |
|---|----------|-------|-----|
| 1 | Не употреблять алкоголь | 🍺 | alcohol |
| 2 | Не употреблять энергетические напитки | ⚡ | energy |
| 3 | Не употреблять кофе | ☕ | coffee |
| 4 | Не употреблять нарко/психотроп веществ | 💊 | drugs |
| 5 | Никаких таутировок | ⏳ | procrastination |
| 6 | Не употреблять маты/сквернословия/оскорбления | 🤬 | profanity |
| 7 | Не листать рилсы/шортсы/тиктоки | 📱 | reels |

## 3. Требования пользователя (актуальные)

- Огонёк обновляется автоматически (пассивные привычки, ручной чек-ин **отвергнут**).
- Круговая 3D-карусель принципов.
- Дизайн: «тёмное стекло», много объёма. Отдельно просили **сгладить карточки** (мягче углы/переходы) и **«3D подкачать в объёме»** — сдельно, пользователь оценил как «неплохо получилось».
- **Вкладка «Стержень» не нужна** — её содержимое переехало на вкладку «Сегодня».
- **Виджет — отдельный на каждый принцип** (не один «лучшая серия»), с подписью названия принципа на виджете.
- Дата старта огонька **редактируется вручную**.
- Нужна возможность **скачать APK**, причём **вне GitHub** — у пользователя GitHub-ссылки не открываются.

## 4. Что уже сделано

**UI**
- Тёмное «стекло»: `ui/theme/Theme.kt`, `ui/components/Design.kt` (компонент `GlassCard`).
- Цилиндрическая 3D-карусель на «Сегодня»: `ui/screens/TodayScreen.kt`.
- Вкладки: **Сегодня** (карусель + карта дней), **Неделя** (heatmap 12 недель, вехи), **Отчёты**.
- Ручная правка даты старта: `ui/components/StartDateDialog.kt`.
- Анимированные переходы между экранами.

**Логика серий** (`data/`, `ui/AppViewModel.kt`)
- `autoStreak` — от (последний срыв + 1) до сегодня.
- `autoBestStreak` — по интервалам между срывами.
- `dayState` — `null` до старта, `false` при срыве, `true` иначе.

**Виджеты** (`widget/`)
- `StreakWidget`, `StreakWidgetProvider`, `WidgetConfigActivity`, `BootReceiver`; layout `res/layout/widget_streak.xml` 2×1 с подписью.
- Привязка принципа к виджету: SharedPreferences `sterzhen_widgets`, ключ `p_<appWidgetId>`, дефолт — первый принцип.
- Обновление: `AlarmManager` на полночь (`updatePeriodMillis` не годится — минимум 30 минут), перепланирование в `BootReceiver` на загрузку/смену времени/обновление приложения. requestCode 4242, будильник = завтра 00:00:05.
- emoji 🧊 при серии 0.

**Сборка и доставка APK**
- `.github/workflows/build-apk.yml`: ubuntu-24.04, JDK 17, Gradle 8.7 (не wrapper), `assembleDebug` + `assembleRelease`.
- Шаги после сборки: публикация `dist/sterzhen.apk` в репозиторий (коммит бота `dist: apk <sha> [skip ci]`), зеркала (pixeldrain/filebin/uguu/transfer.sh/temp.sh), GitHub Release `latest`, upload-artifact.
- `tools/preview_server.py` — локальный сервер превью и раздачи APK (страница `preview/index.html`).

## 5. Структура репозитория

```
app/src/main/java/com/sterzhen/app/
  MainActivity.kt
  data/            Principles.kt, Repository.kt, db/AppDatabase.kt
  ui/              AppViewModel.kt, Formatters.kt
    components/    Design.kt, StartDateDialog.kt
    screens/       TodayScreen.kt, WeekScreen.kt, ReportsScreen.kt
    theme/         Theme.kt
  widget/          StreakWidget.kt, StreakWidgetProvider.kt, WidgetConfigActivity.kt, BootReceiver.kt
app/src/main/res/  layout/widget_streak.xml, xml/streak_widget_info.xml, drawable/*, values/*
.github/workflows/build-apk.yml
preview/index.html          # превью 390×812, 3 экрана + кнопка скачивания APK
tools/preview_server.py     # отдаёт preview/ на $PORT, POST /upload?token=...
dist/sterzhen.apk           # APK от CI (мост CI → песочница)
```

## 6. Дизайн-токены (текущие, «Ink»)

- Фон `#07070B`, карточка `#14141E`
- Ember `#FF7A2F`, EmberDeep `#FF3D6E`, Gold `#FFC46B`, Mint `#4ADE9B`, Danger `#FF5566`, Ice `#6EA8FF`
- Цвет числа серии `numberColor()`: ≥100 `#FF2D55`, ≥30 `#FF5722`, ≥7 `#FF7A2F`, >0 `#FFC46B`, 0 `#6EA8FF`
- Радиусы: карточки карусели и `GlassCard` — **30 dp**
- Карусель: шаг 51.43°, `rotationY = a % 360f`, наклон `rotationX = -9f`, `cameraDistance = 12f × density`, перспектива `0.74f + 0.26f × t`, `alpha = 0.16f + 0.84f × t`, `shadow((5 + 16t).dp)`, `zIndex(cosA)`, драг ×0.22f, тап при `cosA > 0.2f`, авто-вращение 0.14°/16 мс, `spring(0.82f, 260f)`; `BoxWithConstraints` высота 360 dp, карточка 150 × 196 dp, радиус кольца `width × 0.54f`.

## 7. Где сейчас код и APK

- Репозиторий: `https://github.com/kirillworking2008-lab/report`
- Ветка: **`arena/99c8574b-report`**, база `5fdf613`
- **PR #1 (открыт, MERGEABLE):** `https://github.com/kirillworking2008-lab/report/pull/1` — 38 файлов, +4435/−1
- Последние коммиты: `7931381` (сглаживание карточек + объём 3D + публикация APK в репозиторий), `c2ea033` (`dist: apk 7931381`), `196357e` (кнопка скачивания в превью)
- **APK:** `https://8090-ig19xn6k4lp0refvnderi.e2b.app/sterzhen.apk` (10 922 947 байт, md5 `dec7183cb46d2364609175264e3e8ddf`); страница с кнопкой — тот же адрес без пути.
- Резервное зеркало: `https://gofile.io/d/l2wPBIMQ`
- GitHub Release (у пользователя не открывается): `.../releases/download/latest/sterzhen-release.apk`

## 8. Что НЕ сделано — главная открытая задача

**Применить дизайн-кит `Running-Design-Kit.zip` с Google Drive.**

Состояние: файла нет в песочнице, ссылок на него у агента нет (в сообщениях были битые `http://running-design-kit.zip/` — это не адрес Drive). Коннектор **google_drive** подключён у пользователя, но **выключен для чата** — доступа к инструментам нет.

Когда файл появится — нужно: распаковать, вытащить палитру, типографику (шрифты, размеры, веса), радиусы, отступы, тени и перенести в `ui/theme/Theme.kt`, `ui/components/Design.kt`, `preview/index.html`; затем пересобрать APK и обновить ссылку.

## 9. Как дать агенту доступ к приложению (в новом чате)

1. Открой панель подключений **в самом чате** (иконка приложений рядом с полем ввода).
2. Найди **Google Drive**. Если он «Connected» — заново логиниться и выдавать права не нужно, подключение уже есть.
3. Переключи **тумблер для этого чата** в положение «вкл». Это отдельный тумблер от статуса подключения — именно он чаще всего и остаётся выключенным.
4. Если не сработало: выключи и включи заново, обнови страницу чата, проверь, не включился ли доступ в другом чате.
5. Скажи агенту «включил» — он проверит статус и пойдёт искать файл по имени.

**Проще и надёжнее:** перетащить `Running-Design-Kit.zip` в чат вложением. Файл попадёт в рабочую папку, Drive не нужен.

Важно: из песочницы **нет исходящего доступа к `drive.google.com`** (разрешены только github.com, codeload/api.github.com, registry.npmjs.org, pypi.org, files.pythonhosted.org). Поэтому «просто скачать по ссылке» не получится — только коннектор или вложение.

## 10. Ограничения среды (чтобы не повторять тупики)

- В песочнице **нет** java/gradle/Android SDK — Android-сборка возможна только в GitHub Actions.
- `gh run view --log-failed` и `gh run download` падают с EOF — ошибки читать из комментария к коммиту, APK брать из `dist/` или релиза.
- `[skip ci]` в GitHub Actions нативно не поддерживается — коммиты превью всё равно могут запускать сборку.
- `workflow_dispatch` для файла, которого нет в `main`, → 404.
- Публичные файлообменники почти все отвалились: catbox «Invalid uploader», 0x0 отключён, uguu 415, temp.sh 404, bashupload пусто, filebin отдаёт 404 после «успешной» загрузки. Работает **gofile**.
- CI **не может** достучаться до превью-песочницы по HTTP — мост сделан через git (`dist/sterzhen.apk` + `git pull`).
- Compose-виджеты невозможны без Glance; в `graphicsLayer` локальные scale/alpha затеняют внешний scope.

## 11. Готовый промпт для нового чата

> Продолжаем Android-приложение «Стержень». Контекст — в приложении `HANDOFF.md` (приложен). Репозиторий `kirillworking2008-lab/report`, ветка `arena/99c8574b-report`, PR #1 открыт.
>
> Задача: применить дизайн-кит `Running-Design-Kit.zip` с Google Drive — вытащить палитру, типографику, радиусы, отступы и тени и перенести в `ui/theme/Theme.kt`, `ui/components/Design.kt` и `preview/index.html`, затем пересобрать APK и обновить ссылку на скачивание.
>
> Google Drive я включил для этого чата. Если инструменты Drive всё ещё недоступны — скажи прямо, я приложу zip файлом.
