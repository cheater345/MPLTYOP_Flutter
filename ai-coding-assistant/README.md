# AI Coding Assistant (Android)

A native Android AI assistant app built with **Kotlin + Jetpack Compose** (Material 3). Chat with any OpenAI-compatible API or Google Gemini directly from your phone — with streaming responses, full offline history, multiple API keys, and a clean Material You interface.

## Features

- **Multiple providers** — Gemini, OpenAI-compatible (OpenAI, DeepSeek, OpenRouter, Ollama, LM Studio, OpenCode, Zen, and any custom URL)
- **SSE streaming** — responses stream token-by-token into the chat UI
- **Multiple API keys per provider** with priority ordering, usage stats, and automatic key rotation on rate-limit (429) / quota exhaustion
- **Full offline history** — all chats, messages, and prompts stored locally in Room; works without internet
- **Markdown rendering** — headings, lists, quotes, code blocks with syntax highlighting (kotlin/java/python/js/json/sql/shell), tables, LaTeX-style inline spans
- **Chat management** — search, pin, rename, delete; editable & regeneratable messages; stop streaming mid-response
- **Prompt library** — reusable prompt templates with favorites and search
- **Theme** — light / dark / pure AMOLED black, dynamic Material You color, font scaling, animation speed control
- **Security** — API keys encrypted with Android Keystore (AES/GCM), never stored in plaintext
- **Export/import** — full backup of chats, prompts and providers as JSON

## Tech Stack

- Kotlin 2.1, Jetpack Compose (Material 3), AGP 8.9
- Room 2.6 (KSP), DataStore Preferences, Hilt 2.56
- OkHttp (SSE streaming), Retrofit + Gson, Coroutines/Flow
- Android Keystore (AES/GCM) for key encryption
- Navigation Compose, Coil, Material Icons Extended

## Project Structure

```
ai-coding-assistant/
├── app/
│   └── src/main/java/com/aicoding/assistant/
│       ├── AiCodingApplication.kt        # Hilt app + notification channel + seed defaults
│       ├── MainActivity.kt
│       ├── domain/model/                 # Chat, Message, Provider, ApiKey, Prompt, ...
│       ├── data/local/                   # Room entities/DAO/db, DataStore settings, CryptoManager
│       ├── data/remote/                  # AiStreamEngine (SSE), ModelFetcher
│       ├── data/repository/              # Chat, Provider, Prompt, ApiKey repositories
│       ├── di/AppModule.kt               # Hilt modules
│       ├── ui/
│       │   ├── theme/                    # Light/Dark/AMOLED themes, typography
│       │   ├── components/               # MarkdownText, MdParser, CodeBlock, SyntaxHighlighter
│       │   └── screens/                  # Home, Chat, Settings, Providers, Keys, Prompts
│       └── util/                         # DataExporter, SeedDefaults
└── .github/workflows/build-ai-apk.yml    # CI: builds debug APK on every push
```

## Build

Requires JDK 17 and Android SDK (compileSdk 36, minSdk 26).

```bash
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

CI (`.github/workflows/build-ai-apk.yml`) builds the debug APK on every push and uploads it as an artifact named `ai-coding-assistant-apks`.

## Quick Start

1. Install the APK on your device.
2. Open **Providers** → add a provider (e.g. Gemini, OpenAI, or a custom OpenAI-compatible endpoint like Ollama at `http://<pc-ip>:11434/v1`).
3. Add one or more **API keys** for that provider (Gemini: `AIza...`; OpenAI-compatible: `sk-...`; Ollama/LM Studio local: any value or blank).
4. Start a chat, pick the provider/model, and send a message.

No network permission issues: the app talks directly to provider endpoints — for local servers (Ollama/LM Studio) make sure your phone can reach the PC on the same Wi-Fi.
