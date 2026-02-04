package dev.zinchenko.physicsbox

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.layout.LayoutCoordinates

/**
 * Internal state local used by modifier infrastructure in commonMain.
 */
internal val LocalPhysicsBoxState = staticCompositionLocalOf<PhysicsBoxState?> { null }

/**
 * Coordinates of the active [dev.zinchenko.physicsbox.layout.PhysicsBox] container.
 * Used by child gesture modifiers to convert local pointer positions into container-space px.
 */
internal val LocalPhysicsBoxCoordinates = staticCompositionLocalOf<LayoutCoordinates?> { null }
