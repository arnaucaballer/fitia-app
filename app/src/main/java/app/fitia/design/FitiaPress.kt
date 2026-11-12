package app.fitia.design

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Scales the control while a finger is down. Does not consume the gesture. */
fun Modifier.fitiaPress(): Modifier = this.then(FitiaPressElement)

private data object FitiaPressElement : ModifierNodeElement<FitiaPressNode>() {
    override fun create(): FitiaPressNode = FitiaPressNode()

    override fun update(node: FitiaPressNode) = Unit
}

private class FitiaPressNode : Modifier.Node(), PointerInputModifierNode, DrawModifierNode {
    private val scaleAnim = Animatable(1f)
    private var pressed = false
    private var animation: Job? = null

    override fun onAttach() {
        coroutineScope.launch {
            snapshotFlow { scaleAnim.value }.collect { invalidateDraw() }
        }
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass != PointerEventPass.Initial) return
        setPressed(pointerEvent.changes.any { it.pressed })
    }

    override fun onCancelPointerInput() {
        setPressed(false)
    }

    override fun ContentDrawScope.draw() {
        val value = scaleAnim.value
        scale(value, value) {
            this@draw.drawContent()
        }
    }

    private fun setPressed(next: Boolean) {
        if (!isAttached || pressed == next) return
        pressed = next
        animation?.cancel()
        animation = coroutineScope.launch {
            scaleAnim.animateTo(if (next) 0.96f else 1f, FitiaMotion.press)
        }
    }
}
