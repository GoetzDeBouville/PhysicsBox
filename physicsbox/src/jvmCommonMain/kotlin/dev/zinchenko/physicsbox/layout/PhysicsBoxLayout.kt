package dev.zinchenko.physicsbox.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Constraints
import dev.zinchenko.physicsbox.engine.PhysicsWorldEngine
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyRegistration

/**
 * Layout bridge between Compose children and [PhysicsWorldEngine].
 *
 * Physics convention in this container:
 * - body position in snapshots is the body center in px.
 * - child visuals are placed at `(0, 0)` and shifted via layer translation so their center
 *   matches physics center.
 * - rotation is applied through `graphics layer` (`rotationZ`) around child center.
 *
 * The world scale (`PxPerMeter`) affects simulation internals only; this layout operates in px.
 */
@Composable
internal fun PhysicsBoxLayout(
    modifier: Modifier = Modifier,
    engine: PhysicsWorldEngine,
    content: @Composable () -> Unit,
) {
    val trackedKeys = remember(engine) { mutableSetOf<Any>() }

    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val measuredChildren = measurables.map { measurable ->
            val placeable = measurable.measure(childConstraints)
            MeasuredChild(
                placeable = placeable,
                registration = measurable.parentData as? PhysicsBodyRegistration,
            )
        }

        val layoutWidth = resolveContainerWidth(constraints, measuredChildren)
        val layoutHeight = resolveContainerHeight(constraints, measuredChildren)

        val currentKeys = HashSet<Any>(measuredChildren.size)
        for (child in measuredChildren) {
            val registration = child.registration ?: continue
            currentKeys += registration.key
            engine.ensureBody(
                reg = registration,
                measuredWidthPx = child.placeable.width,
                measuredHeightPx = child.placeable.height,
            )
        }

        if (trackedKeys.isNotEmpty()) {
            for (oldKey in trackedKeys) {
                if (oldKey !in currentKeys) {
                    engine.removeBody(oldKey)
                }
            }
        }
        trackedKeys.clear()
        trackedKeys.addAll(currentKeys)

        engine.updateBoundaries(
            containerWidthPx = layoutWidth,
            containerHeightPx = layoutHeight,
        )

        val snapshot = engine.snapshotPx()
        val bodyByKey = snapshot.bodiesByKey

        layout(layoutWidth, layoutHeight) {
            for (child in measuredChildren) {
                val placeable = child.placeable
                val registration = child.registration
                val bodySnapshot = registration?.let { bodyByKey[it.key] }
                if (bodySnapshot == null) {
                    placeable.placeRelative(0, 0)
                    continue
                }

                val centerX = bodySnapshot.transformPx.positionPx.x
                val centerY = bodySnapshot.transformPx.positionPx.y
                val translationX = centerX - placeable.width * 0.5f
                val translationY = centerY - placeable.height * 0.5f
                val rotationZ = bodySnapshot.transformPx.rotationDegrees

                placeable.placeRelativeWithLayer(0, 0) {
                    this.translationX = translationX
                    this.translationY = translationY
                    this.rotationZ = rotationZ
                    this.transformOrigin = TransformOrigin(0.5f, 0.5f)
                }
            }
        }
    }
}

private data class MeasuredChild(
    val placeable: Placeable,
    val registration: PhysicsBodyRegistration?,
)

private fun resolveContainerWidth(
    constraints: Constraints,
    measuredChildren: List<MeasuredChild>,
): Int {
    if (constraints.hasBoundedWidth) return constraints.maxWidth
    val maxChildWidth = measuredChildren.maxOfOrNull { it.placeable.width } ?: 0
    return maxChildWidth.coerceIn(constraints.minWidth, constraints.maxWidth)
}

private fun resolveContainerHeight(
    constraints: Constraints,
    measuredChildren: List<MeasuredChild>,
): Int {
    if (constraints.hasBoundedHeight) return constraints.maxHeight
    val maxChildHeight = measuredChildren.maxOfOrNull { it.placeable.height } ?: 0
    return maxChildHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
}
