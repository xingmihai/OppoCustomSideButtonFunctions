package com.slimenull.customsidebuttonfunctions.model

/**
 * 只服务一台设备（一加 Ace 6 / PLQ110，ColorOS 17.0.0）时固化的参数。
 *
 * 侧键实测值：
 *   getevent 输出 /dev/input/event0 (gpio-keys): EV_KEY BTN_TRIGGER_HAPPY32
 *   BTN_TRIGGER_HAPPY1 = 0x2c0 = 704，因此 HAPPY32 = 704 + 31 = 735
 *
 * 系统 keylayout 中没有 735 的 Android keyCode 映射，所以命中靠的是 scanCode。
 * 模块的匹配逻辑是 keyCode 或 scanCode 任一相等，固定为 735 即可。
 */
object DeviceConfig {
    /** 侧键的 Linux 按键码（同时也是 Android scanCode）。 */
    const val SIDE_KEY_CODE = 735
}
