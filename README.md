# StickyNoteWidget

An Android home-screen **sticky note / checklist widget** with a lightweight editor and a “glass” look that adapts for readability against your current wallpaper.

## Features

- **Home screen widget** backed by a `RemoteViews` list (tap rows to toggle done/undone).
- **Edit screen** to add/reorder-style list items and save back to the widget.
- **Wallpaper-aware appearance** so text and surfaces stay readable when the wallpaper changes.
- **Fonts & accent colors** in the editor (font family + gradient accent choices).
- **Optional cleanup**: completed items can be purged on a schedule (see Midnight Purge components).

## Screenshot

If present in this repo, the current UI screenshot is:

- `current_screen.png`

## Requirements

- **Android Studio** (recommended) or Gradle CLI
- **Android SDK**
  - `minSdk`: 26 (Android 8.0)
  - `compileSdk` / `targetSdk`: 34 (Android 14)

## Run (Android Studio)

1. Open this folder in Android Studio.
2. Let Gradle sync finish.
3. Select the **app** configuration.
4. Click **Run**.

## Build (Gradle CLI)

From the project root:

```bash
./gradlew assembleDebug
```

The debug APK will be generated under:

- `app/build/outputs/apk/debug/`

## Generated app (APK)

This repo also includes a prebuilt debug APK for convenience:

- `generated/StickyNoteWidget-debug.apk`

## Use the widget

1. Long-press your home screen → **Widgets**
2. Find **StickyNoteWidget**
3. Drag it onto your home screen
4. Tap the **edit** button in the widget header to manage items
5. Tap a row in the widget to toggle completion

## Project structure (high-signal entry points)

- **Widget provider**: `app/src/main/java/com/example/stickynote/StickyNoteWidget.kt`
- **Widget list service/factory**: `NoteWidgetService.kt`, `NoteWidgetFactory.kt`
- **Row toggle receiver**: `WidgetActionReceiver.kt`
- **Editor**: `EditNoteActivity.kt`
- **Storage**: `NoteRepository.kt` (+ model `NoteItem.kt`)
- **Appearance**: `WidgetAppearanceResolver.kt`, `UiStylePrefs.kt`
- **Boot/scheduling**: `BootReceiver.kt`, `MidnightPurgeScheduler.kt`, `MidnightPurgeReceiver.kt`

## Notes

- The widget listens for `APPWIDGET_UPDATE` and `WALLPAPER_CHANGED` to refresh its appearance.
- `RECEIVE_BOOT_COMPLETED` is used to reschedule maintenance after reboot (see `BootReceiver`).
