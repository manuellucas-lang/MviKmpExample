package com.example.mviexample.designsystem.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

private val PullTriggerDistance = 72.dp
private val PullMaxDistance = 128.dp
private val PullRestDistance = 52.dp

@Composable
fun PullToRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val triggerPx = with(density) { PullTriggerDistance.toPx() }
    val maxPx = with(density) { PullMaxDistance.toPx() }
    val restPx = with(density) { PullRestDistance.toPx() }

    var pullPx by remember { mutableFloatStateOf(0f) }
    var refreshTriggered by remember { mutableStateOf(false) }
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    val currentIsRefreshing by rememberUpdatedState(isRefreshing)

    suspend fun settle(target: Float) {
        animate(initialValue = pullPx, targetValue = target) { value, _ ->
            pullPx = value
        }
    }

    val connection = remember(triggerPx, maxPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.UserInput || available.y <= 0f || pullPx <= 0f) {
                    return Offset.Zero
                }
                val delta = (pullPx + available.y).coerceIn(0f, maxPx) - pullPx
                if (delta != 0f) pullPx += delta
                return Offset(0f, delta)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (source != NestedScrollSource.UserInput || available.y <= 0f) {
                    return Offset.Zero
                }
                val delta = ((pullPx + available.y).coerceAtMost(maxPx) - pullPx) * 0.5f
                if (delta != 0f) pullPx += delta

                if (!currentIsRefreshing && !refreshTriggered && pullPx >= triggerPx) {
                    refreshTriggered = true
                    currentOnRefresh()
                }

                return Offset(0f, available.y)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!currentIsRefreshing && pullPx > 0f) {
                    if (pullPx >= triggerPx) {
                        settle(restPx)
                    } else {
                        settle(0f)
                    }
                }
                return Velocity.Zero
            }
        }
    }

    LaunchedEffect(currentIsRefreshing) {
        if (currentIsRefreshing) {
            refreshTriggered = true
            if (pullPx < restPx) settle(restPx)
        } else {
            refreshTriggered = false
            if (pullPx > 0f) settle(0f)
        }
    }

    Box(modifier = modifier.nestedScroll(connection)) {
        content()
        if (currentIsRefreshing || pullPx > 0f) {
            val heightPx = maxOf(pullPx, if (currentIsRefreshing) restPx else 0f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(density) { heightPx.toDp() }),
                contentAlignment = Alignment.TopCenter,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(30.dp),
                    strokeWidth = 3.dp,
                )
            }
        }
    }
}
