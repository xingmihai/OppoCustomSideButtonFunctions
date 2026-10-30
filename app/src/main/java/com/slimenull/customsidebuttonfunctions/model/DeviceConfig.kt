package com.slimenull.customsidebuttonfunctions.model

/**
 * 只服务一台设备（一加 Ace 6 / PLQ110，ColorOS 17.0.0）时固化的侧键参数。
 *
 * 侧键事件有两层编号，本机实测两者不同，必须都写进来：
 *
 *   scanCode = 735
 *     Linux 内核 input 子系统给按键编的号，由驱动决定，跨 ROM 稳定。
 *     getevent 实测：/dev/input/event0 (gpio-keys): EV_KEY BTN_TRIGGER_HAPPY32
 *     BTN_TRIGGER_HAPPY1 = 0x2c0 = 704，故 HAPPY32 = 704 + 31 = 735。
 *
 *   keyCode = 780
 *     Android 框架层 KeyEvent 用的编号，由 ROM 翻译而来。
 *     本机 /system/usr/keylayout 与 /vendor/usr/keylayout 中都没有 735 的映射，
 *     说明 Oplus 没走标准 keylayout，而是在 framework 侧（StrategyActionButtonKeyLaunchApp
 *     这套策略）把它转成了自己的私有值 780。因此该值由 ROM 定义，OTA 后可能变化。
 *
 * 匹配逻辑对两者取或：scanCode 稳定但依赖内核值透传，keyCode 是当前框架实际给的。
 * 固化两个值后，即使 OTA 后其中一层变了，另一层仍有机会命中。
 */
object DeviceConfig {
    /** Linux 内核层按键码，即 KeyEvent.scanCode。驱动决定，跨 ROM 稳定。 */
    const val SIDE_KEY_SCANCODE = 735

    /** Android 框架层按键码，即 KeyEvent.keyCode。由 ROM 定义，升级后可能变化。 */
    const val SIDE_KEY_KEYCODE = 780
}
