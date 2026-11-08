#!/usr/bin/env bash
set -euo pipefail

ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
TARGET="${TV_ADB_TARGET:-}"
PKG=com.familyboard.tv
GOOGLE_HOME=com.google.android.apps.tv.launcherx
APK="$(dirname "$0")/../app/build/outputs/apk/release/app-release.apk"

adb_tv() {
  if [ -n "$TARGET" ]; then "$ADB" -s "$TARGET" "$@"; else "$ADB" "$@"; fi
}

case "${1:-}" in
  install)
    adb_tv install -r "$APK"
    adb_tv shell cmd package compile -m speed -f "$PKG"
    ;;
  enable)
    adb_tv shell pm path "$PKG" >/dev/null
    adb_tv shell cmd role add-role-holder android.app.role.HOME "$PKG"
    adb_tv shell pm disable-user --user 0 "$GOOGLE_HOME"
    adb_tv shell input keyevent KEYCODE_HOME
    ;;
  recover)
    adb_tv shell pm enable "$GOOGLE_HOME"
    adb_tv shell cmd role remove-role-holder android.app.role.HOME "$PKG" || true
    adb_tv shell input keyevent KEYCODE_HOME
    ;;
  status)
    adb_tv shell cmd role get-role-holders android.app.role.HOME
    adb_tv shell pm list packages -d | grep -E "launcherx|familyboard" || echo "no launcher packages disabled"
    adb_tv shell dumpsys activity activities | grep ResumedActivity
    ;;
  *)
    echo "usage: TV_ADB_TARGET=<ip:port> $0 install|enable|recover|status" >&2
    exit 1
    ;;
esac
