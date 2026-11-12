package app.fitia.design

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Press scale plus the Material ripple, for controls that use [androidx.compose.foundation.LocalIndication].
 * Color and alpha stay on [FitiaMotion.effects]; only the scale uses [FitiaMotion.press].
 */
internal class FitiaIndication(
    private val ripple: IndicationNodeFactory,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        FitiaIndicationNode(interactionSource, ripple)

    override fun equals(other: Any?): Boolean =
        other is FitiaIndication && other.ripple == ripple

    override fun hashCode(): Int = ripple.hashCode()
}

private class FitiaIndicationNode(
    private val interactionSource: InteractionSource,
    ripple: IndicationNodeFactory,
) : DelegatingNode(), DrawModifierNode {
    private val scaleAnim = Animatable(1f)
    private var presses = 0
    private var animation: Job? = null

    init {
        delegate(ripple.create(interactionSource))
    }

    override fun onAttach() {
        coroutineScope.launch {
            snapshotFlow { scaleAnim.value }.collect { invalidateDraw() }
        }
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        presses++
                        animateTo(0.96f)
                    }
                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        presses = (presses - 1).coerceAtLeast(0)
                        if (presses == 0) animateTo(1f)
                    }
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        val value = scaleAnim.value
        scale(value, value) {
            this@draw.drawContent()
        }
    }

    private fun animateTo(target: Float) {
        animation?.cancel()
        animation = coroutineScope.launch {
            scaleAnim.animateTo(target, FitiaMotion.press)
        }
    }
}
