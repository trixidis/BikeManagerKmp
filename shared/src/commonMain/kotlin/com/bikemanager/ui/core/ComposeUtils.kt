package com.bikemanager.ui.core

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow


@Composable
fun rememberFabVisibility(listState: LazyListState): Boolean {
    var isVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, scrollOffset) ->
                // Compare avec la position précédente
                isVisible = when {
                    index < previousIndex -> true  // Scroll vers le haut
                    index > previousIndex -> false // Scroll vers le bas
                    else -> scrollOffset <= previousScrollOffset // Même item, compare l'offset
                }

                previousIndex = index
                previousScrollOffset = scrollOffset
            }
    }

    return isVisible
}