#!/usr/bin/env bash
# 输出上一个稳定标签到 HEAD 的提交摘要，供 Release 正文使用
# 用法: generate-changelog.sh <当前标签>
set -euo pipefail

CURR_TAG="$1"
PREV_TAG=$(git tag --list | grep -E '^v[0-9]+(\.[0-9]+)*$' | sort -Vr | head -n 1)

if [ -z "$PREV_TAG" ]; then
    LOG_RANGE=""
else
    LOG_RANGE="$PREV_TAG..HEAD"
fi

# shellcheck disable=SC2086
CHANGELOG_LIST=$(git log $LOG_RANGE --pretty=format:"- %s (%an)" | grep -v '^-\s*Merge' | head -n 5)
# shellcheck disable=SC2086
CHANGELOG_COUNT=$(git log $LOG_RANGE --pretty=format:"- %s (%an)" | grep -v '^-\s*Merge' | wc -l)

{
    echo "body<<CHANGELOG_EOF"
    if [ -z "$CHANGELOG_LIST" ]; then
        echo "No changes since the last stable release."
    else
        echo "$CHANGELOG_LIST"
        if [ "$CHANGELOG_COUNT" -gt 5 ]; then
            echo ""
            echo "**Full Changelog:** ${PREV_TAG:-初始版本}...${CURR_TAG}"
        fi
    fi
    echo "CHANGELOG_EOF"
} >> "$GITHUB_OUTPUT"
