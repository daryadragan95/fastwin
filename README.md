# W Fast (Android, MVP)

A lightweight 2D Android game built with Jetpack Compose Canvas: drag the ball, dodge obstacles, and collect bonuses.

## MVP Plan

1. Kotlin + Compose Android project setup.
2. Pure game logic in `GameEngine`, separate from UI.
3. State management with `ViewModel + StateFlow`.
4. Canvas rendering and drag controls.
5. Local high score storage with DataStore.
6. Basic game logic tests.

## Structure

- `app/src/main/java/com/striklewin/apps/MainActivity.kt` - app entry point and WebView gate.
- `app/src/main/java/com/striklewin/apps/data/web/` - Firestore WebView config.
- `app/src/main/java/com/striklewin/apps/ui/web/` - fullscreen WebView.
- `app/src/main/java/com/striklewin/apps/core/model/` - game models and events.
- `app/src/main/java/com/striklewin/apps/core/persistence/` - high score persistence.
- `app/src/main/java/com/striklewin/apps/feature/game/domain/` - game simulation logic.
- `app/src/main/java/com/striklewin/apps/feature/game/presentation/` - `UiState` and `ViewModel`.
- `app/src/main/java/com/striklewin/apps/feature/game/ui/` - game screen and HUD.
- `app/src/main/java/com/striklewin/apps/ui/theme/` - Material 3 theme.

## Implemented

- Ready / Running / Paused / Game Over states.
- Drag-based ball controls.
- 3 obstacle types: `CONE`, `DEFENDER`, `PIT`.
- 2 bonuses: `SHIELD` (3 seconds), `SCORE_X2` (5 seconds).
- Score, distance, and local high score.
- Haptic feedback for critical events.

## Run

```bash
./gradlew :app:assembleDebug
```

If the Android SDK is not configured locally, add `local.properties` with `sdk.dir`.
