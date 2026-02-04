package dev.zinchenko.physicsbox.engine

internal data class StepResult(
    val stepped: Boolean,
    val bodiesCount: Int,
    val contactsCount: Int,
)
