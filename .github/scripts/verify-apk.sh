#!/usr/bin/env bash
# 校验签名后的 APK 仍然具备 Xposed / Shizuku 运行所需的入口
set -euo pipefail

APK="$1"
if [ -z "$APK" ] || [ ! -f "$APK" ]; then
    echo "::error::未找到 APK: ${APK:-<空>}"
    exit 1
fi

echo "校验产物：$APK"

if [ -n "${ANDROID_HOME:-}" ]; then
    APKSIGNER=$(find "$ANDROID_HOME/build-tools" -name apksigner | head -1)
    if [ -n "$APKSIGNER" ]; then
        "$APKSIGNER" verify --print-certs "$APK" | head -6
    fi
fi

# Xposed 入口清单不能被资源压缩移除
if ! unzip -l "$APK" | grep -q "META-INF/xposed/java_init.list"; then
    echo "::error::Xposed 入口清单 java_init.list 丢失"
    exit 1
fi

FOUND_ENTRY=0
FOUND_SHIZUKU=0
for DEX in $(unzip -Z1 "$APK" 'classes*.dex'); do
    if unzip -p "$APK" "$DEX" | strings | grep -q "xposed/XposedEntry"; then
        FOUND_ENTRY=1
    fi
    if unzip -p "$APK" "$DEX" | strings | grep -q "ShizukuShellUserService"; then
        FOUND_SHIZUKU=1
    fi
done

if [ "$FOUND_ENTRY" -ne 1 ]; then
    echo "::error::XposedEntry 被 R8 移除或改名"
    exit 1
fi
if [ "$FOUND_SHIZUKU" -ne 1 ]; then
    echo "::error::ShizukuShellUserService 被 R8 移除或改名"
    exit 1
fi

echo "校验通过：Xposed 入口与 Shizuku UserService 均存在"
