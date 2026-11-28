package com.bikemanager.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.bikemanager.ui.theme.ErrorRed
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Fraction of the item width the user must drag before releasing triggers a delete.
 * Material's default (56dp, or any quick fling) is far too easy to trigger by accident;
 * iOS Mail / Gmail use roughly half the row width.
 */
private const val DELETE_THRESHOLD_FRACTION = 0.45f

/**
 * Swipe-to-delete container, deliberately harder to trigger than Material's SwipeToDismissBox.
 *
 * UX choices (Material 3 / Apple HIG / NN/g guidance on destructive gestures):
 * - One direction only (right-to-left), the convention for "delete". A rightward drag is
 *   not consumed, so it still reaches the parent HorizontalPager (tab switching).
 * - Distance-based only: the item must be dragged past [DELETE_THRESHOLD_FRACTION] of its
 *   width. A fast flick never deletes on its own.
 * - The item resists the drag (dragged at a reduced rate), so small horizontal movements
 *   while scrolling barely move it.
 * - Clear "armed" state: when the threshold is crossed the background turns fully red,
 *   the icon grows and a haptic tick fires; dragging back below the threshold cancels.
 * - Released below the threshold, the item springs back.
 * - Deletion stays reversible through the undo snackbar shown by the caller.
 * - Accessible alternative: a "delete" custom accessibility action, since gestures alone
 *   are not discoverable nor usable by everyone.
 *
 * @param onDelete Called once when the user releases past the threshold
 * @param deleteLabel Label of the accessibility action
 * @param cornerRadius Corner radius of the revealed red background
 * @param horizontalInset Horizontal padding applied to the background, matching the content
 */
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    deleteLabel: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    horizontalInset: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val currentOnDelete by rememberUpdatedState(onDelete)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }
    var width by remember { mutableIntStateOf(0) }

    val thresholdPx by remember { derivedStateOf { width * DELETE_THRESHOLD_FRACTION } }
    val isArmed by remember {
        derivedStateOf { width > 0 && -offsetX.value >= thresholdPx }
    }
    val progress by remember {
        derivedStateOf { if (thresholdPx > 0f) (-offsetX.value / thresholdPx).coerceIn(0f, 1f) else 0f }
    }

    // Haptic tick when crossing the threshold, in either direction
    LaunchedEffect(isArmed) {
        if (offsetX.value != 0f) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isArmed) ErrorRed else ErrorRed.copy(alpha = 0.35f),
        animationSpec = tween(150)
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isArmed) 1.25f else 0.8f,
        animationSpec = spring()
    )

    Box(
        modifier = modifier
            .onSizeChanged { width = it.width }
            .semantics {
                customActions = listOf(
                    CustomAccessibilityAction(deleteLabel) {
                        currentOnDelete()
                        true
                    }
                )
            }
    ) {
        // Revealed background, only drawn while dragging
        if (offsetX.value < 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = horizontalInset)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(backgroundColor),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            alpha = 0.5f + 0.5f * progress
                        }
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var startedLeftward = false
                        val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                            // Only claim leftward drags (or any drag once already offset):
                            // a rightward drag is left to the parent pager.
                            if (over < 0f || offsetX.value < 0f) {
                                startedLeftward = true
                                change.consume()
                            }
                        }
                        if (drag == null || !startedLeftward) return@awaitEachGesture

                        val completed = horizontalDrag(drag.id) { change ->
                            // Resistance: item moves at 70% of the finger, never to the right
                            val target = (offsetX.value + change.positionChange().x * 0.7f)
                                .coerceIn(-width.toFloat(), 0f)
                            scope.launch { offsetX.snapTo(target) }
                            change.consume()
                        }

                        scope.launch {
                            if (completed && width > 0 && abs(offsetX.value) >= thresholdPx) {
                                offsetX.animateTo(-width.toFloat(), tween(180))
                                currentOnDelete()
                            } else {
                                offsetX.animateTo(0f, spring())
                            }
                        }
                    }
                }
        ) {
            content()
        }
    }
}
