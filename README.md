# Dribble Master (Android, MVP)

Легкая 2D игра на Jetpack Compose Canvas: веди мяч, избегай препятствий, собирай бонусы.

## План MVP

1. Каркас Android-проекта на Kotlin + Compose.
2. Чистая игровая логика (`GameEngine`) отдельно от UI.
3. Состояние через `ViewModel + StateFlow`.
4. Canvas-рендер и drag-управление.
5. Локальный рекорд в DataStore.
6. Базовые тесты логики.

## Структура

- `app/src/main/java/com/striklewin/apps/MainActivity.kt` — вход в приложение и WebView gate.
- `app/src/main/java/com/striklewin/apps/data/web/` — Firestore-конфиг WebView.
- `app/src/main/java/com/striklewin/apps/ui/web/` — полноэкранный WebView.
- `app/src/main/java/com/striklewin/apps/core/model/` — модели и события игры.
- `app/src/main/java/com/striklewin/apps/core/persistence/` — сохранение рекорда.
- `app/src/main/java/com/striklewin/apps/feature/game/domain/` — логика симуляции.
- `app/src/main/java/com/striklewin/apps/feature/game/presentation/` — `UiState` и `ViewModel`.
- `app/src/main/java/com/striklewin/apps/feature/game/ui/` — игровой экран и HUD.
- `app/src/main/java/com/striklewin/apps/ui/theme/` — тема Material 3.

## Реализовано в MVP

- Экран Ready / Running / Paused / Game Over.
- Drag-управление мячом.
- 3 типа препятствий: `CONE`, `DEFENDER`, `PIT`.
- 2 бонуса: `SHIELD` (3 сек), `SCORE_X2` (5 сек).
- Счет, дистанция, локальный high score.
- Haptic-отклик на критические события.

## Запуск

```bash
./gradlew :app:assembleDebug
```

Если Android SDK не настроен локально, добавьте `local.properties` с `sdk.dir`.
