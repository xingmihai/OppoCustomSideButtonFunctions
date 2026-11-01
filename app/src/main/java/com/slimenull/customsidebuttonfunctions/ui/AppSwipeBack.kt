package com.slimenull.customsidebuttonfunctions.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.minOf

/**
 * iOS 风格滑动返回手势。
 *
 * 逻辑对照 Glasmio 的 glasmio-scaffold/navigation/IosSwipeBack.kt 实现，
 * 包括子视图所有权仲裁、UIKit 三采样释放投影、以及同步提交。
 * 库内对应符号为 internal，无法直接引用，故按同一算法在本项目内重建。
 */

@Stable
class AppSwipeBackState {
    var tracking by mutableStateOf(false)
    var progress by mutableFloatStateOf(0f)
    var startProgress = 0f
    var releaseVelocity = 0f
    var lastEventTimeMillis = 0L
    var generation = 0
    fun cancel() {
        if (tracking) {
            generation++
            releaseVelocity = 0f
            tracking = false
        }
    }
}

/**
 * 子视图所有权仲裁：一旦判定归子视图，本次手势序列内永久放弃返回。
 *
 * 反向位移、纵向位移占优、或事件已被子视图消费，都会让出所有权。
 * 由于 down 与仲裁阶段都走 PointerEventPass.Final，能观察到子视图的消费结果，
 * 因此横向滑块这类控件会自动接管，不会被误判成返回手势。
 */
internal class AppSwipeBackArbitrator(private val slop: Float) {
    private var travel = 0f
    private var cross = 0f
    private var childOwned = false
    var initialTravel = 0f
        private set

    fun observe(delta: Float, crossDelta: Float, consumed: Boolean): Int {
        if (childOwned) return -1
        if (delta == 0f && crossDelta == 0f) return 0
        travel += delta
        cross += crossDelta
        if (travel < -slop || abs(cross) > slop && abs(cross) > abs(travel) || consumed) {
            childOwned = true
            return -1
        }
        if (consumed || travel <= slop || travel < abs(cross)) return 0
        initialTravel = (travel - slop).coerceAtLeast(0f)
        return 1
    }
}

/** UIKit 的三采样进度投影，以归一化的屏宽为单位。 */
internal class AppSwipeBackRelease(widthPoints: Float) {
    private val threshold = minOf(187.5f / widthPoints.coerceAtLeast(1f), 0.5f)
    private val velocities = FloatArray(3)
    private val accelerations = FloatArray(3)
    private var count = 0
    private var previousProgress = 0f
    private var previousTime = 0L
    private var previousVelocity = 0f
    private var previousAcceleration = 0f

    fun sample(progress: Float, timeMillis: Long, observedVelocity: Float? = null) {
        if (count > 0 && timeMillis <= previousTime) return
        val seconds = if (count == 0) 0f else (timeMillis - previousTime) / 1000f
        val velocity = observedVelocity ?: if (seconds > 0f) (progress - previousProgress) / seconds else 0f
        val acceleration = if (seconds > 0f) ((velocity - previousVelocity) / seconds)
            .coerceIn(previousAcceleration - 5f, previousAcceleration + 5f) else 0f
        velocities[count % 3] = velocity
        accelerations[count % 3] = acceleration
        previousProgress = progress
        previousTime = timeMillis
        previousVelocity = velocity
        previousAcceleration = acceleration
        count++
    }

    fun shouldCommit(progress: Float): Boolean {
        val samples = minOf(count, 3)
        if (samples == 0) return false
        val velocity = velocities.sum() / samples
        if (count < 3) return velocity > 0f
        val acceleration = accelerations.sum() / samples
        return progress + 0.35f * velocity + 0.5f * 0.35f * 0.35f * acceleration > threshold
    }
}

/**
 * 挂上 iOS 滑动返回。
 *
 * @param systemOwnsGesture 系统预测性返回正在进行时返回 true，此时让位给系统手势。
 * @param anchorProgress 手势开始时的基准进度，用于在已有进度上接续。
 * @param onCommit 判定返回时同步调用，不得包裹在协程中，
 *   否则离场页面的协程可能先于状态变更被取消。
 */
internal fun Modifier.appSwipeBack(
    enabled: Boolean,
    direction: LayoutDirection,
    state: AppSwipeBackState,
    systemOwnsGesture: () -> Boolean,
    anchorProgress: () -> Float,
    onCommit: () -> Unit
): Modifier = if (!enabled) this else pointerInput(state, direction) {
    val sign = if (direction == LayoutDirection.Ltr) 1f else -1f
    try {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)
            if (systemOwnsGesture()) return@awaitEachGesture
            val extent = size.width.toFloat().coerceAtLeast(1f)
            val arbitrator = AppSwipeBackArbitrator(max(viewConfiguration.touchSlop, 10.dp.toPx()))
            val velocity = VelocityTracker()
            velocity.addPosition(down.uptimeMillis, down.position)
            val release = AppSwipeBackRelease(extent / density)
            release.sample(anchorProgress().coerceIn(0f, 1f), down.uptimeMillis, 0f)
            var travel = 0f
            var claimed = false
            var claimGeneration = state.generation
            while (!claimed) {
                val event = awaitPointerEvent(PointerEventPass.Final)
                val change = event.changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
                if (!change.pressed || event.changes.count { it.pressed } != 1 || systemOwnsGesture() ||
                    change.uptimeMillis - down.uptimeMillis >= viewConfiguration.longPressTimeoutMillis
                ) return@awaitEachGesture
                velocity.addPosition(change.uptimeMillis, change.position)
                val delta = change.positionChangeIgnoreConsumed()
                when (arbitrator.observe(sign * delta.x, delta.y, change.isConsumed)) {
                    -1 -> return@awaitEachGesture
                    1 -> {
                        travel = arbitrator.initialTravel
                        state.generation++
                        claimGeneration = state.generation
                        state.releaseVelocity = 0f
                        state.startProgress = anchorProgress().coerceIn(0f, 1f)
                        state.progress = (state.startProgress + travel / extent).coerceIn(0f, 1f)
                        state.lastEventTimeMillis = change.uptimeMillis
                        release.sample(
                            state.progress,
                            change.uptimeMillis,
                            sign * velocity.calculateVelocity().x / extent
                        )
                        state.tracking = true
                        change.consume()
                        claimed = true
                    }
                }
            }
            var cancelled = false
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id }
                if (change != null && !change.pressed && change.isConsumed) cancelled = true
                if (systemOwnsGesture() || state.generation != claimGeneration || change == null ||
                    event.changes.any { it.id != down.id && it.pressed }
                ) {
                    cancelled = true
                    state.cancel()
                }
                event.changes.forEach { it.consume() }
                if (change != null) velocity.addPosition(change.uptimeMillis, change.position)
                if (event.changes.none { it.pressed }) {
                    if (change != null && !cancelled) {
                        travel += sign * change.positionChangeIgnoreConsumed().x
                        state.progress = (state.startProgress + travel / extent).coerceIn(0f, 1f)
                        release.sample(
                            state.progress,
                            change.uptimeMillis,
                            sign * velocity.calculateVelocity().x / extent
                        )
                    }
                    break
                }
                if (cancelled || change == null) continue
                velocity.addPosition(change.uptimeMillis, change.position)
                travel += sign * change.positionChangeIgnoreConsumed().x
                state.progress = (state.startProgress + travel / extent).coerceIn(0f, 1f)
                state.lastEventTimeMillis = change.uptimeMillis
                release.sample(
                    state.progress,
                    change.uptimeMillis,
                    sign * velocity.calculateVelocity().x / extent
                )
            }
            val speed = sign * velocity.calculateVelocity().x / extent
            state.releaseVelocity = if (cancelled) 0f else speed
            val commit = !cancelled && release.shouldCommit(state.progress)
            state.tracking = false
            // 同步提交：若放到协程里，离场页完成收尾时协程可能先于状态变更被取消。
            if (commit) onCommit()
        }
    } finally {
        state.cancel()
    }
}
