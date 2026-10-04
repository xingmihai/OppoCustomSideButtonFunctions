package com.slimenull.customsidebuttonfunctions.ui

import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavKey

/** 对 miuix-nav 返回栈的轻量封装，提供 push / replace / pop / popUntil。 */
class Navigator(
    val backStack: NavBackStack,
) {
    /**
     * 压入页面。重复压入同一个值会被运行时视为重复 contentKey，因此先判断是否已存在。
     */
    fun push(key: NavKey) {
        if (key !in backStack) {
            backStack.add(key)
        }
    }

    /** 替换栈顶页面，栈为空时直接压入。 */
    fun replace(key: NavKey) {
        if (backStack.isNotEmpty()) {
            backStack[backStack.lastIndex] = key
        } else {
            backStack.add(key)
        }
    }

    /** 弹出栈顶页面，栈中只剩首页时不再弹出。 */
    fun pop() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    /** 持续弹出直到栈顶满足 [predicate]。 */
    fun popUntil(predicate: (NavKey) -> Boolean) {
        while (backStack.size > 1 && !predicate(backStack.last())) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun current() = backStack.lastOrNull()

    fun backStackSize() = backStack.size
}
