# J-TV

App Android IPTV. Media3/ExoPlayer, đọc danh sách kênh M3U/JSON.

## Cấu trúc

```
playlist/           Playlist đã lọc
  jtv_clean.m3u     Kênh còn sống (M3U chuẩn)
  channels.json     Cùng dữ liệu, dạng JSON cho app
app/                Project Android (Gradle + Kotlin + Media3)
```

## Nguồn kênh

Danh sách lọc qua test HTTP sống/chết strict (body phải bắt đầu `#EXTM3U` hoặc chứa
`#EXT-X`/`<MPD`). Nguồn chính: SCTV, VTVcab, đài địa phương, Radio (Zeno/Zing).

Lưu ý: nhiều kênh cần header `User-Agent`/`Referer`. URL kiểu SCTV nhúng header inline
(`...index.m3u8?|Referer=...`) — app tự tách trong `Channel.parseJson`.

## Build

Cần JDK 17 (KHÔNG dùng JDK 25/27 — Gradle 8.11 Kotlin-DSL compiler lỗi).

```bash
cd app
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17"
./gradlew assembleDebug
# out: app/build/outputs/apk/debug/app-debug.apk
```

## Kiến trúc app

- `ChannelRepo` — tải kênh: thử `REMOTE_URL` (raw GitHub) trước để update không cần build lại,
  fallback về `assets/channels.json` khi mất mạng.
- `Channel` — model + parser M3U/JSON + tách inline pipe-header.
- `PlayerActivity` — ExoPlayer, gắn UA/Referer vào `DefaultHttpDataSource`; HLS/DASH tự chọn;
  nút ✕ Thoát + phím BACK/ESC quay lại danh sách; D-pad up/down đổi kênh (hỗ trợ Android TV).
- `MainActivity` — lưới kênh + lọc theo nhóm (Spinner).

## Cập nhật danh sách kênh (không cần build lại)

1. Sửa `playlist/channels.json`.
2. Push lên GitHub repo của bạn.
3. Raw URL đã trỏ sẵn trong `ChannelRepo.REMOTE_URL`.

Chạy lại test sống/chết định kỳ (parse → urllib GET + header, kiểm tra `#EXTM3U`).
