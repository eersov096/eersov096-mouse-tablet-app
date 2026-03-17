# GG Mouse Low Latency v3

Практически лучший вариант в рамках этого проекта — это не accessibility-путь, а persistent shell backend через Shizuku.

## Что изменено в v3

- backend по умолчанию: `SHIZUKU_SHELL`
- mouse pump: до `1000 Hz`
- отдельный urgent thread для discrete events
- persistent shell session вместо запуска отдельного процесса на каждое событие
- конфиг backend/hz через UI
- fallback на accessibility для сравнения
- GitHub Actions workflow для debug APK

## Что это даёт

Это убирает лишние прыжки между scheduler / coroutine / process spawn и уменьшает джиттер.

## Чего это не даёт

Это не настоящий kernel/uinput driver path. Цель `<1 ms end-to-end>` для Android-игры нереалистична. Но это лучший практический вариант из того, что можно собрать как обычный GitHub-проект без полноценного нативного драйвера и root-модуля.

## Что дальше для ещё более низкой задержки

Следующий уровень — отдельный native sidecar с доступом к `/dev/uinput` и raw HID pipeline. Для этого уже нужен другой deployment-путь.

## Сборка

```bash
./gradlew assembleDebug
```

## GitHub Actions

Workflow лежит в `.github/workflows/android.yml`.
# trigger
