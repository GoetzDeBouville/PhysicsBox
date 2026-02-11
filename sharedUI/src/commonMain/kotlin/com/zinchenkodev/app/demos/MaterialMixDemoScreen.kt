package com.zinchenkodev.app.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.zinchenko.physicsbox.BoundariesConfig
import dev.zinchenko.physicsbox.PhysicsBoxConfig
import dev.zinchenko.physicsbox.PhysicsVector2
import dev.zinchenko.physicsbox.layout.PhysicsBox
import dev.zinchenko.physicsbox.physicsbody.BodyType
import dev.zinchenko.physicsbox.physicsbody.PhysicsBodyConfig
import dev.zinchenko.physicsbox.physicsbody.PhysicsShape
import dev.zinchenko.physicsbox.physicsbody.PhysicsTransform
import dev.zinchenko.physicsbox.rememberPhysicsBoxState
import kotlin.random.Random

@Immutable
private data class MaterialGroup(
    val name: String,
    val color: Color,
    val description: String,
    val config: PhysicsBodyConfig,
)

@Immutable
private data class MaterialItem(
    val key: String,
    val label: String,
    val sizeDp: Dp,
    val isCircle: Boolean,
    val startXpx: Float,
    val startYpx: Float,
    val rotationDegrees: Float,
    val group: MaterialGroup,
)

@Composable
fun MaterialMixDemoScreen(
    modifier: Modifier = Modifier,
    resetSignal: Int = 0,
) {
    val state = rememberPhysicsBoxState()
    val groups = remember { buildMaterialGroups() }
    val items = remember { mutableStateListOf<MaterialItem>() }

    LaunchedEffect(Unit) {
        if (items.isEmpty()) {
            items.addAll(buildMaterialItems(groups))
        }
    }

    LaunchedEffect(resetSignal) {
        if (resetSignal <= 0) return@LaunchedEffect
        state.reset()
        items.clear()
        items.addAll(buildMaterialItems(groups))
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Materials overview",
            style = MaterialTheme.typography.titleMedium,
        )
        groups.forEach { group ->
            Text(
                text = "${group.name}: ${group.description}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PhysicsBox(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF2F4F7))
                .padding(10.dp),
            state = state,
            config = PhysicsBoxConfig(
                boundaries = BoundariesConfig(
                    enabled = true,
                    restitution = 0.18f,
                    friction = 0.9f,
                    thicknessPx = 56f,
                ),
            ),
        ) {
            items.forEach { item ->
                val shape = if (item.isCircle) CircleShape else RoundedCornerShape(12.dp)
                val physicsShape = if (item.isCircle) PhysicsShape.Circle() else PhysicsShape.Box

                Row(
                    modifier = Modifier
                        .size(item.sizeDp)
                        .physicsBody(
                            key = item.key,
                            config = item.group.config.copy(
                                initialTransform = PhysicsTransform(
                                    vector2 = PhysicsVector2(item.startXpx, item.startYpx),
                                    rotationDegrees = item.rotationDegrees,
                                ),
                            ),
                            shape = physicsShape,
                            isDraggable = true,
                        )
                        .clip(shape)
                        .background(item.group.color)
                        .border(1.dp, Color(0x22000000), shape),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = item.label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

private fun buildMaterialGroups(): List<MaterialGroup> = listOf(
    MaterialGroup(
        name = "Floaty",
        color = Color(0xFF64B5F6),
        description = "gravityScale=0.01f, linearDamping=0.05",
        config = PhysicsBodyConfig(
            bodyType = BodyType.Dynamic,
            density = 0.01f,
            friction = 0.3f,
            restitution = 0.25f,
            gravityScale = 0.01f,
            linearDamping = 0.05f,
        ),
    ),
    MaterialGroup(
        name = "Bouncy",
        color = Color(0xFF4DB6AC),
        description = "restitution=0.9, friction=0.2",
        config = PhysicsBodyConfig(
            bodyType = BodyType.Dynamic,
            density = 1f,
            friction = 0.2f,
            restitution = 0.9f,
            linearDamping = 0.05f,
            angularDamping = 0.05f,
            fixedRotation = false,
        ),
    ),
    MaterialGroup(
        name = "Sticky",
        color = Color(0xFFE57373),
        description = "friction=1.5, restitution=0.1",
        config = PhysicsBodyConfig(
            bodyType = BodyType.Dynamic,
            density = 1f,
            friction = 1.5f,
            restitution = 0.1f,
            linearDamping = 0.08f,
            angularDamping = 0.08f,
        ),
    ),
    MaterialGroup(
        name = "Heavy",
        color = Color(0xFF8D6E63),
        description = "density=4.5, friction=0.4, gravityScale=1.2",
        config = PhysicsBodyConfig(
            bodyType = BodyType.Dynamic,
            density = 50f,
            friction = 0.4f,
            restitution = 0.2f,
            gravityScale = 1.5f,
            linearDamping = 0.1f,
        ),
    ),
)

private fun buildMaterialItems(groups: List<MaterialGroup>): List<MaterialItem> {
    val items = ArrayList<MaterialItem>(16)
    var index = 0
    for (groupIndex in groups.indices) {
        repeat(4) {
            val column = index % 4
            val row = index / 4
            val isCircle = index % 2 == 0
            val size = if (isCircle) 60.dp else 72.dp
            val group = groups[groupIndex]

            items += MaterialItem(
                key = "material-${group.name}-$index",
                label = group.name.take(1),
                sizeDp = size,
                isCircle = isCircle,
                startXpx = 72f + column * 96f,
                startYpx = 84f + row * 90f,
                rotationDegrees = Random(index + 10).nextFloat() * 360f,
                group = group,
            )
            index++
        }
    }
    return items
}
