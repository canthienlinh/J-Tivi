# J-TV

App Android IPTV, dựng lại từ phân tích **DL Tivi**. Media3/ExoPlayer, đọc danh sách kênh M3U.

## Cấu trúc

```
Base TV/            APK gốc để phân tích (DL Tivi.apk, vtvgotv.apk)
playlist/           Playlist đã lọc
  jtv_clean.m3u     107 kênh còn sống (M3U chuẩn)
  channels.json     Cùng dữ liệu, dạng JSON cho app
  parsed.json       316 kênh gốc (trước khi lọc sống/chết)
app/                Project Android (Gradle + Kotlin + Media3)
```

## Nguồn gốc DL Tivi (phân tích)

DL Tivi KHÔNG nhúng kênh trong app. Nó tải M3U từ repo bên thứ 3 lúc chạy:

```
https://raw.githubusercontent.com/DinhLap96/ListTivi/main/ListTiVi/dltivi.dl       (12 kênh)
https://raw.githubusercontent.com/DinhLap96/ListTivi/main/ListTiVi/dltivi.ndl      (261 kênh)
https://raw.githubusercontent.com/DinhLap96/ListTivi/main/ListTiVi/dltivi_v2.ndl   (96 kênh)
```

`.dl`/`.ndl` = M3U đổi đuôi, KHÔNG mã hoá. App lỗi vì phụ thuộc repo này + nhiều CDN relay đã chết
(`code.vthanhtivi.pw`, IP thô, `*.ngrok.io`...).

VTVgo TV dùng API token/DRM (`api.vtvdigital.org`, `api.msky.vn`) → không tái dùng trực tiếp, đã bỏ qua.

## Lọc nguồn

316 kênh gốc → test HTTP sống/chết → **107 kênh LIVE**. Bỏ: relay chết, ngrok, quảng cáo (ottera/publica).
Nguồn giữ lại chủ yếu: SCTV, VTVcab, đài địa phương, Radio (Zeno/Zing).

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

- `ChannelRepo` — tải kênh: thử `REMOTE_URL` trước (để update không cần build lại), fallback về `assets/channels.json`.
  **TODO:** đặt `REMOTE_URL` trỏ repo GitHub của BẠN, đừng phụ thuộc bên thứ 3 (chính là lỗi của DL Tivi).
- `Channel` — model + parser M3U/JSON + tách inline pipe-header.
- `PlayerActivity` — ExoPlayer, gắn UA/Referer/Origin vào `DefaultHttpDataSource`; HLS/DASH tự chọn; D-pad up/down đổi kênh (hỗ trợ Android TV).
- `MainActivity` — lưới kênh + lọc theo nhóm (Spinner).

## Cập nhật danh sách kênh (không cần build lại)

1. Sửa `playlist/channels.json`.
2. Push lên GitHub repo của bạn.
3. Đặt raw URL vào `ChannelRepo.REMOTE_URL`.

Chạy lại test sống/chết định kỳ bằng script trong lịch sử (parse → urllib HEAD/GET + header).
