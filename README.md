# LocoWiki Android Public CI

Offline Android meeting fact-recall prototype for local ASR, speaker identification, deterministic fact retrieval and evidence display.

## Privacy boundary

- The Android manifest does not request `android.permission.INTERNET`.
- This repository contains synthetic examples only.
- Models are downloaded from pinned public upstream releases during CI and are not committed.
- Real meetings, imported industrial facts, voiceprints, databases, credentials and signing keys must never be committed.

## Build targets

- `s24uDebug`: `arm64-v8a` phone build.
- `qaDebug`: `x86_64` emulator QA build.

Run:

```bash
bash scripts/download_models.sh
./gradlew testQaDebugUnitTest assembleS24uDebug assembleQaDebug
```

## Source provenance

This public tree was exported from a private development baseline using a clean-history migration. No private Git history is included.

## License

No reuse license has been selected. Public visibility does not grant permission to copy, modify or redistribute the source. See `NOTICE-NO-LICENSE.md`.
