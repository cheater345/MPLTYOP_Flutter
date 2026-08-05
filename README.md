# MPLTYOP Flutter - Native Android YouTube Music Player

A native Android music streaming app that plays YouTube Music directly on-device using `yt_flutter_musicapi` (Chaquopy + yt-dlp + ytmusicapi). No backend server required.

## Features

- **On-device extraction** - No backend server, everything runs on your phone
- **YouTube Music search** - Real-time streaming search results
- **Background playback** - AudioService + JustAudio (lock screen, notifications, headset controls)
- **Playlists & Liked songs** - Local Hive storage, survives app restarts
- **Queue management** - Play next, add to queue, shuffle, repeat modes
- **Lyrics & Related songs** - Rich track details
- **Dark/Light theme** - Material 3 with Inter font
- **Pre-configured** - Works out of the box (US, VERY_HIGH quality)

## Screenshots

| Home | Search | Now Playing | Library |
|------|--------|-------------|---------|
| ![Home](screenshots/home.png) | ![Search](screenshots/search.png) | ![Now Playing](screenshots/now_playing.png) | ![Library](screenshots/library.png) |

## Architecture

```
lib/
├── main.dart                 # App entry, DI, routing
├── core/
│   ├── constants.dart        # Quality presets, Hive boxes, defaults
│   ├── di.dart               # Riverpod providers
│   ├── errors.dart           # Failure types, Result pattern
│   └── theme/                # Dark/Light Material 3 themes
├── data/
│   ├── datasources/yt_music_api.dart   # yt_flutter_musicapi wrapper
│   └── repositories/       # Music, Storage, Settings repositories
├── domain/
│   ├── entities/           # Track, Playlist, Artist, PlaybackState, QueueState
│   └── usecases/           # SearchTracks, GetRelated, GetLyrics, ManageQueue, ManageLibrary
├── presentation/
│   ├── providers/          # Riverpod controllers (Search, Queue, Library, Settings)
│   ├── screens/            # Home, Search, Library, PlaylistDetail, NowPlaying, Settings
│   └── widgets/            # TrackCard, TrackTile, PlaylistCard, PlayerBar, Skeletons
└── services/
    └── playback_service.dart  # AudioService + JustAudio background task
```

## Prerequisites

- Flutter 3.22+
- Dart 3.4+
- Android SDK 34 (compileSdk), minSdk 23
- JDK 17

## Quick Start

```bash
git clone https://github.com/cheater345/MPLTYOP_Flutter.git
cd MPLTYOP_Flutter/mpltyop_flutter
flutter pub get
dart run build_runner build --delete-conflicting-outputs
flutter run
```

## Build APK

```bash
flutter build apk --release --split-per-abi
# Output: build/app/outputs/flutter-apk/*.apk
```

## CI/CD

GitHub Actions workflow (`.github/workflows/build-apk.yml`):
- Runs on every push to main
- Builds split APKs (arm64, arm32, x86_64)
- Uploads as artifacts
- Attaches to GitHub Releases on tag push

## Tech Stack

| Component | Technology |
|-----------|------------|
| UI | Flutter 3.22, Material 3, Google Fonts (Inter) |
| State | hooks_riverpod + flutter_hooks |
| Serialization | freezed + json_serializable |
| Local DB | hive + hive_flutter |
| YouTube Music | yt_flutter_musicapi (Chaquopy + yt-dlp + ytmusicapi) |
| Playback | just_audio + audio_service + just_audio_background |
| Background | Android Foreground Service (mediaPlayback) |
| Image loading | cached_network_image |
| Lyrics | slate |

## Android Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## License

MIT - For personal/educational use. Respect YouTube's Terms of Service.

## Disclaimer

This app is not affiliated with YouTube or Google. It uses public YouTube Music endpoints for personal streaming. Use responsibly.