package org.jetbrains.skiko

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.coroutines.CoroutineContext

/**
 * Dispatch frame after call of [scheduleFrame].
 *
 * After the creating there should be no scheduled frame in the frame loop.
 */
class FrameDispatcher(
    scope: CoroutineScope,
    private val onFrame: suspend () -> Unit
) {
    constructor(
        context: CoroutineContext,
        onFrame: suspend () -> Unit
    ) : this(
        CoroutineScope(context),
        onFrame
    )

    private var needFrame = CompletableDeferred<Unit>()

    private val job = scope.launch {
        while (true) {
            needFrame.await()
            needFrame = CompletableDeferred()
            onFrame()
            yield()
        }
    }

    fun cancel() {
        job.cancel()
    }

    /**
     * Schedule next frame to render in the frame loop.
     *
     * Multiple calls of scheduleFrame before beginning of the frame will cause only one onFrame.
     *
     * Multiple calls of scheduleFrame after beginning of the frame but before its ending
     * will schedule next single onFrame after the current one.
     */
    fun scheduleFrame() {
        needFrame.complete(Unit)
    }
}