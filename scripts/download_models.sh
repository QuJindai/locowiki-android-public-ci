#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
AAR_DIR="$ROOT/app/libs"
ASSET_DIR="$ROOT/app/src/main/assets"
ASR_MODEL='sherpa-onnx-streaming-zipformer-small-bilingual-zh-en-2023-02-16'
SPEAKER_MODEL='3dspeaker_speech_campplus_sv_zh-cn_16k-common.onnx'
DOWNLOAD_DIR="${RUNNER_TEMP:-/tmp}/locowiki-model-downloads"
mkdir -p "$AAR_DIR" "$ASSET_DIR/$ASR_MODEL" "$ASSET_DIR/speaker" "$DOWNLOAD_DIR"

curl -L --fail --retry 3 -o "$AAR_DIR/sherpa-onnx-1.13.2.aar"   'https://github.com/k2-fsa/sherpa-onnx/releases/download/v1.13.2/sherpa-onnx-1.13.2.aar'
echo "aa5505c0ec4f8bdaee5f214a64ba3012be64f2aecc022e82a64f33392b8dd245  $AAR_DIR/sherpa-onnx-1.13.2.aar" | sha256sum -c -

curl -L --fail --retry 3 -o "$DOWNLOAD_DIR/asr-model.tar.bz2"   'https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-streaming-zipformer-small-bilingual-zh-en-2023-02-16.tar.bz2'
tar -xjf "$DOWNLOAD_DIR/asr-model.tar.bz2" -C "$DOWNLOAD_DIR"
ASR_SRC="$DOWNLOAD_DIR/$ASR_MODEL"
cp "$ASR_SRC/encoder-epoch-99-avg-1.int8.onnx" "$ASSET_DIR/$ASR_MODEL/"
cp "$ASR_SRC/decoder-epoch-99-avg-1.onnx" "$ASSET_DIR/$ASR_MODEL/"
cp "$ASR_SRC/joiner-epoch-99-avg-1.int8.onnx" "$ASSET_DIR/$ASR_MODEL/"
cp "$ASR_SRC/tokens.txt" "$ASSET_DIR/$ASR_MODEL/"

curl -L --fail --retry 3 -o "$ASSET_DIR/silero_vad.onnx"   'https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/silero_vad.onnx'
curl -L --fail --retry 3 -o "$ASSET_DIR/speaker/$SPEAKER_MODEL"   'https://github.com/k2-fsa/sherpa-onnx/releases/download/speaker-recongition-models/3dspeaker_speech_campplus_sv_zh-cn_16k-common.onnx'
echo "f682b514c05d947ee3fa91cd6ec6c5c7543479a128373fa29b1faedccd21fd11  $ASSET_DIR/speaker/$SPEAKER_MODEL" | sha256sum -c -
