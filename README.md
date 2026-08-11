# List Manager

A multi-list checklist app for Android — create separate lists (e.g. "Grocery", "To-do"), add items, check them off, and reorder everything with drag-and-drop. Data persists locally on the device.

Built as a hands-on project for learning modern Android development with AI-assisted coding.

## Features

- Create and delete multiple named lists
- Add items to a list; checking an item off hides it from view
- Each list shows a live count of remaining (unchecked) items
- Drag-and-drop reordering of both lists and items within a list
- Local persistence via Room/SQLite — data survives app restarts
- Custom adaptive app icon
- Follows the system's light/dark theme automatically

## Tech stack

- **Kotlin**
- **Jetpack Compose** + **Material 3** for the UI
- **Room** for local persistence, with **KSP** generating its code at compile time
- [**Reorderable**](https://github.com/Calvin-LL/Reorderable) for drag-and-drop list reordering
- Architecture: Composable screens observe a `Flow` exposed by an `AndroidViewModel`, which reads/writes through a Room DAO

## Data model

Two Room entities:

- `TodoList` — id, name, position
- `ChecklistItem` — id, listId (foreign key → `TodoList.id`, `ON DELETE CASCADE`), text, isChecked, position

Deleting a list automatically deletes its items.

## Requirements

- Android Studio
- `minSdk` 24, `targetSdk`/`compileSdk` 37

## Building & running

Open the project in Android Studio and run it, or from the command line:

```
./gradlew installDebug
```
