#!/usr/bin/env bash
# 从仓库密钥恢复签名配置，供 Gradle 的 keystore/keystore.properties 读取
set -euo pipefail

missing=0
for name in KEYSTORE_BASE64 KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD; do
    if [ -z "${!name:-}" ]; then
        echo "::error::缺少签名密钥 $name"
        missing=1
    fi
done
if [ "$missing" -ne 0 ]; then
    exit 1
fi

mkdir -p keystore
echo "$KEYSTORE_BASE64" | base64 -d > keystore/custom-side-button.jks

# Properties 以 ISO-8859-1 读取，中文别名必须写成 Unicode 转义
ALIAS=$(python3 -c 'import os;print(os.environ["KEY_ALIAS"].encode("unicode_escape").decode())')
{
    echo "storeFile=keystore/custom-side-button.jks"
    echo "storePassword=$KEYSTORE_PASSWORD"
    echo "keyAlias=$ALIAS"
    echo "keyPassword=$KEY_PASSWORD"
} > keystore/keystore.properties

keytool -list -keystore keystore/custom-side-button.jks \
    -storepass "$KEYSTORE_PASSWORD" -alias "$KEY_ALIAS"
