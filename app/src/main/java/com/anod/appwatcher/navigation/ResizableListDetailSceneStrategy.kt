package com.anod.appwatcher.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.MutableThreePaneScaffoldState
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldAdaptStrategies
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldState
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

@ExperimentalMaterial3AdaptiveApi
@Composable
fun <T : Any> rememberResizableListDetailSceneStrategy(
    backNavigationBehavior: BackNavigationBehavior = BackNavigationBehavior.PopUntilScaffoldValueChange,
    directive: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()),
    adaptStrategies: ThreePaneScaffoldAdaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
    minPaneWidth: Dp = 320.dp,
    sceneContainer: @Composable (content: @Composable () -> Unit) -> Unit = { content -> content() },
    paneExpansionDragHandle: @Composable ThreePaneScaffoldScope.(PaneExpansionState, Modifier) -> Unit,
): ResizableListDetailSceneStrategy<T> {
    return remember(
        backNavigationBehavior,
        directive,
        adaptStrategies,
        minPaneWidth,
        sceneContainer,
        paneExpansionDragHandle,
    ) {
        ResizableListDetailSceneStrategy(
            backNavigationBehavior = backNavigationBehavior,
            directive = directive,
            adaptStrategies = adaptStrategies,
            minPaneWidth = minPaneWidth,
            sceneContainer = sceneContainer,
            paneExpansionDragHandle = paneExpansionDragHandle,
        )
    }
}

@ExperimentalMaterial3AdaptiveApi
class ResizableListDetailSceneStrategy<T : Any>(
    val backNavigationBehavior: BackNavigationBehavior,
    val directive: PaneScaffoldDirective,
    val adaptStrategies: ThreePaneScaffoldAdaptStrategies,
    val minPaneWidth: Dp,
    val sceneContainer: @Composable (content: @Composable () -> Unit) -> Unit,
    val paneExpansionDragHandle: @Composable ThreePaneScaffoldScope.(PaneExpansionState, Modifier) -> Unit,
) : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastPaneMetadata = getPaneMetadata(entries.lastOrNull() ?: return null) ?: return null
        val sceneKey = lastPaneMetadata.sceneKey

        val scaffoldEntries = mutableListOf<NavEntry<T>>()
        val scaffoldEntryIndices = mutableListOf<Int>()
        val entriesAsNavItems = mutableListOf<ThreePaneScaffoldDestinationItem<Any>>()

        var detailPlaceholder: (@Composable ThreePaneScaffoldScope.() -> Unit)? = null

        var index = entries.lastIndex
        while (index >= 0) {
            val entry = entries[index]
            val paneMetadata = getPaneMetadata(entry) ?: break

            if (paneMetadata.sceneKey == sceneKey) {
                scaffoldEntryIndices.add(0, index)
                scaffoldEntries.add(0, entry)
                entriesAsNavItems.add(
                    0,
                    ThreePaneScaffoldDestinationItem(
                        pane = paneMetadata.role,
                        contentKey = entry.contentKey,
                    ),
                )
                if (paneMetadata is ListMetadata) {
                    detailPlaceholder = paneMetadata.detailPlaceholder
                }
            }
            index--
        }

        if (scaffoldEntries.isEmpty()) {
            return null
        }

        val scene = ResizableThreePaneScaffoldScene(
            key = sceneKey,
            onBack = onBack,
            backNavigationBehavior = backNavigationBehavior,
            directive = directive,
            adaptStrategies = adaptStrategies,
            allEntries = entries,
            scaffoldEntries = scaffoldEntries,
            scaffoldEntryIndices = scaffoldEntryIndices,
            entriesAsNavItems = entriesAsNavItems,
            getPaneRole = { getPaneMetadata(it)?.role },
            detailPlaceholder = detailPlaceholder ?: {},
            minPaneWidth = minPaneWidth,
            sceneContainer = sceneContainer,
            paneExpansionDragHandle = paneExpansionDragHandle,
        )

        return if (scene.currentScaffoldValue.paneCount > 1) scene else null
    }

    private sealed interface PaneMetadata {
        val sceneKey: Any
        val role: ThreePaneScaffoldRole
    }

    private class ListMetadata(
        override val sceneKey: Any,
        val detailPlaceholder: @Composable ThreePaneScaffoldScope.() -> Unit,
    ) : PaneMetadata {
        override val role: ThreePaneScaffoldRole
            get() = ListDetailPaneScaffoldRole.List
    }

    private class DetailMetadata(override val sceneKey: Any) : PaneMetadata {
        override val role: ThreePaneScaffoldRole
            get() = ListDetailPaneScaffoldRole.Detail
    }

    private class ExtraMetadata(override val sceneKey: Any) : PaneMetadata {
        override val role: ThreePaneScaffoldRole
            get() = ListDetailPaneScaffoldRole.Extra
    }

    companion object {
        private const val LIST_DETAIL_ROLE_KEY = "com.anod.appwatcher.navigation.ListDetailPaneScaffoldRole"

        fun listPane(
            sceneKey: Any = Unit,
            detailPlaceholder: @Composable ThreePaneScaffoldScope.() -> Unit = {},
        ): Map<String, Any> = mapOf(LIST_DETAIL_ROLE_KEY to ListMetadata(sceneKey, detailPlaceholder))

        fun detailPane(sceneKey: Any = Unit): Map<String, Any> =
            mapOf(LIST_DETAIL_ROLE_KEY to DetailMetadata(sceneKey))

        fun extraPane(sceneKey: Any = Unit): Map<String, Any> =
            mapOf(LIST_DETAIL_ROLE_KEY to ExtraMetadata(sceneKey))

        private fun <T : Any> getPaneMetadata(entry: NavEntry<T>): PaneMetadata? =
            entry.metadata[LIST_DETAIL_ROLE_KEY] as? PaneMetadata
    }
}

@ExperimentalMaterial3AdaptiveApi
private class ResizableThreePaneScaffoldScene<T : Any>(
    override val key: Any,
    val onBack: () -> Unit,
    val backNavigationBehavior: BackNavigationBehavior,
    val directive: PaneScaffoldDirective,
    val adaptStrategies: ThreePaneScaffoldAdaptStrategies,
    val allEntries: List<NavEntry<T>>,
    val scaffoldEntries: List<NavEntry<T>>,
    val scaffoldEntryIndices: List<Int>,
    val entriesAsNavItems: List<ThreePaneScaffoldDestinationItem<Any>>,
    val getPaneRole: (NavEntry<T>) -> ThreePaneScaffoldRole?,
    val detailPlaceholder: @Composable ThreePaneScaffoldScope.() -> Unit,
    val minPaneWidth: Dp,
    val sceneContainer: @Composable (content: @Composable () -> Unit) -> Unit,
    val paneExpansionDragHandle: @Composable ThreePaneScaffoldScope.(PaneExpansionState, Modifier) -> Unit,
) : Scene<T> {
    override val entries: List<NavEntry<T>>
        get() = scaffoldEntries

    override val previousEntries: List<NavEntry<T>>
        get() = onBackResult.previousEntries

    val currentScaffoldValue: ThreePaneScaffoldValue
        get() = calculateScaffoldValue(destinationHistory = entriesAsNavItems)

    private val onBackResult: OnBackResult<T> = calculateOnBackResult()

    override val content: @Composable () -> Unit = {
        val scaffoldValue = currentScaffoldValue
        val scaffoldState = remember { MutableThreePaneScaffoldState(scaffoldValue) }
        LaunchedEffect(scaffoldValue) {
            scaffoldState.animateTo(scaffoldValue)
        }

        val previousScaffoldValue = onBackResult.previousScaffoldValue
        val gestureInfo = remember(key, entries) { ThreePaneScaffoldSceneInfo(key, entries) }
        val gestureState = rememberNavigationEventState(currentInfo = gestureInfo)

        NavigationBackHandler(
            state = gestureState,
            isBackEnabled = previousScaffoldValue != null,
            onBackCompleted = {
                repeat(allEntries.size - onBackResult.previousEntries.size) {
                    onBack()
                }
            },
        )

        val transitionState = gestureState.transitionState
        LaunchedEffect(transitionState) {
            if (
                transitionState is NavigationEventTransitionState.InProgress &&
                    previousScaffoldValue != null
            ) {
                scaffoldState.seekTo(
                    fraction = backProgressToStateProgress(
                        progress = transitionState.latestEvent.progress,
                        scaffoldValue = scaffoldValue,
                    ),
                    targetState = previousScaffoldValue,
                )
            } else {
                scaffoldState.animateTo(targetState = scaffoldValue)
            }
        }

        sceneContainer {
            ListDetailContent(scaffoldState)
        }
    }

    @Composable
    private fun ListDetailContent(scaffoldState: ThreePaneScaffoldState) {
        val lastList = entries.findLast { getPaneRole(it) == ListDetailPaneScaffoldRole.List }
        val lastDetail = entries.findLast { getPaneRole(it) == ListDetailPaneScaffoldRole.Detail }
        val lastExtra = entries.findLast { getPaneRole(it) == ListDetailPaneScaffoldRole.Extra }
        val density = LocalDensity.current
        var scaffoldWidthPx by remember { mutableIntStateOf(0) }
        var scaffoldLeftPx by remember { mutableFloatStateOf(0f) }
        var splitOffsetPx by remember { mutableFloatStateOf(Float.NaN) }
        val minPaneWidthPx = with(density) { minPaneWidth.toPx() }
        val paneExpansionState = rememberPaneExpansionState(
            keyProvider = scaffoldState.targetState,
            consumeDragDelta = { delta ->
                if (scaffoldWidthPx == 0 || splitOffsetPx.isNaN()) {
                    delta
                } else {
                    val minOffset = minPaneWidthPx.coerceAtMost(scaffoldWidthPx / 2f)
                    val coercedTarget = (splitOffsetPx + delta).coerceIn(
                        minOffset,
                        scaffoldWidthPx - minOffset,
                    )
                    val consumedDelta = coercedTarget - splitOffsetPx
                    splitOffsetPx = coercedTarget
                    consumedDelta
                }
            },
        )
        val dragHandlePositionModifier = Modifier.onGloballyPositioned { coordinates ->
            splitOffsetPx = coordinates.positionInRoot().x +
                coordinates.size.width / 2f -
                scaffoldLeftPx
        }

        ListDetailPaneScaffold(
            directive = directive,
            scaffoldState = scaffoldState,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .onSizeChanged { scaffoldWidthPx = it.width }
                .onGloballyPositioned { scaffoldLeftPx = it.positionInRoot().x },
            listPane = lastList?.let { { AnimatedPane { it.Content() } } } ?: {},
            detailPane = lastDetail?.let { { AnimatedPane { it.Content() } } } ?: detailPlaceholder,
            extraPane = lastExtra?.let { { AnimatedPane { it.Content() } } },
            paneExpansionDragHandle = { state ->
                paneExpansionDragHandle(state, dragHandlePositionModifier)
            },
            paneExpansionState = paneExpansionState,
        )
    }

    private fun calculateOnBackResult(): OnBackResult<T> {
        val previousDestinationRelativeIndex = getPreviousDestinationIndex()
        val previousDestinationAbsoluteIndex =
            if (previousDestinationRelativeIndex < 0) {
                scaffoldEntryIndices.first() - 1
            } else {
                scaffoldEntryIndices[previousDestinationRelativeIndex]
            }

        val scaffoldEntryIndicesSet = scaffoldEntryIndices.toSet()
        for (index in allEntries.lastIndex downTo 0) {
            if (index !in scaffoldEntryIndicesSet) {
                return OnBackResult(
                    previousScaffoldValue = null,
                    previousEntries = ArrayList(allEntries.subList(0, index + 1)),
                )
            }
            if (index == previousDestinationAbsoluteIndex) {
                val previousScaffoldValue =
                    calculateScaffoldValue(
                        destinationHistory = entriesAsNavItems.subList(
                            0,
                            previousDestinationRelativeIndex + 1,
                        )
                    )
                return OnBackResult(
                    previousScaffoldValue = previousScaffoldValue,
                    previousEntries = ArrayList(allEntries.subList(0, index + 1)),
                )
            }
        }

        return OnBackResult(previousScaffoldValue = null, previousEntries = emptyList())
    }

    private fun getPreviousDestinationIndex(): Int {
        if (entriesAsNavItems.size <= 1) {
            return -1
        }

        val currentDestination = entriesAsNavItems.last()
        val currentScaffoldValue = this.currentScaffoldValue

        when (backNavigationBehavior) {
            BackNavigationBehavior.PopLatest -> return entriesAsNavItems.lastIndex - 1
            BackNavigationBehavior.PopUntilScaffoldValueChange ->
                for (previousDestinationIndex in entriesAsNavItems.lastIndex - 1 downTo 0) {
                    val previousValue =
                        calculateScaffoldValue(
                            destinationHistory = entriesAsNavItems.subList(
                                0,
                                previousDestinationIndex + 1,
                            )
                        )
                    if (previousValue != currentScaffoldValue) {
                        return previousDestinationIndex
                    }
                }
            BackNavigationBehavior.PopUntilCurrentDestinationChange ->
                for (previousDestinationIndex in entriesAsNavItems.lastIndex - 1 downTo 0) {
                    val destination = entriesAsNavItems[previousDestinationIndex].pane
                    if (destination != currentDestination.pane) {
                        return previousDestinationIndex
                    }
                }
            BackNavigationBehavior.PopUntilContentChange ->
                for (previousDestinationIndex in entriesAsNavItems.lastIndex - 1 downTo 0) {
                    val contentKey = entriesAsNavItems[previousDestinationIndex].contentKey
                    if (contentKey != currentDestination.contentKey) {
                        return previousDestinationIndex
                    }
                    val previousValue =
                        calculateScaffoldValue(
                            destinationHistory = entriesAsNavItems.subList(
                                0,
                                previousDestinationIndex + 1,
                            )
                        )
                    if (previousValue != currentScaffoldValue) {
                        return previousDestinationIndex
                    }
                }
        }

        return -1
    }

    private fun calculateScaffoldValue(
        destinationHistory: List<ThreePaneScaffoldDestinationItem<*>>
    ): ThreePaneScaffoldValue =
        calculateThreePaneScaffoldValue(
            maxHorizontalPartitions = directive.maxHorizontalPartitions,
            maxVerticalPartitions = directive.maxVerticalPartitions,
            adaptStrategies = adaptStrategies,
            destinationHistory = destinationHistory,
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ResizableThreePaneScaffoldScene<*>

        return key == other.key &&
            backNavigationBehavior == other.backNavigationBehavior &&
            directive == other.directive &&
            adaptStrategies == other.adaptStrategies &&
            allEntries == other.allEntries &&
            previousEntries == other.previousEntries &&
            scaffoldEntries == other.scaffoldEntries &&
            scaffoldEntryIndices == other.scaffoldEntryIndices &&
            entriesAsNavItems == other.entriesAsNavItems
    }

    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + backNavigationBehavior.hashCode()
        result = 31 * result + directive.hashCode()
        result = 31 * result + adaptStrategies.hashCode()
        result = 31 * result + allEntries.hashCode()
        result = 31 * result + previousEntries.hashCode()
        result = 31 * result + scaffoldEntries.hashCode()
        result = 31 * result + scaffoldEntryIndices.hashCode()
        result = 31 * result + entriesAsNavItems.hashCode()
        return result
    }

    private class OnBackResult<T : Any>(
        val previousScaffoldValue: ThreePaneScaffoldValue?,
        val previousEntries: List<NavEntry<T>>,
    )
}

private data class ThreePaneScaffoldSceneInfo(
    val sceneKey: Any,
    val sceneEntries: List<NavEntry<*>>,
) : NavigationEventInfo()

@ExperimentalMaterial3AdaptiveApi
private val ThreePaneScaffoldValue.paneCount: Int
    get() {
        var count = 0
        if (primary != PaneAdaptedValue.Hidden) count++
        if (secondary != PaneAdaptedValue.Hidden) count++
        if (tertiary != PaneAdaptedValue.Hidden) count++
        return count
    }

@ExperimentalMaterial3AdaptiveApi
private val ThreePaneScaffoldValue.expandedCount: Int
    get() {
        var count = 0
        if (primary == PaneAdaptedValue.Expanded) count++
        if (secondary == PaneAdaptedValue.Expanded) count++
        if (tertiary == PaneAdaptedValue.Expanded) count++
        return count
    }

@ExperimentalMaterial3AdaptiveApi
private fun backProgressToStateProgress(
    progress: Float,
    scaffoldValue: ThreePaneScaffoldValue,
): Float =
    THREE_PANE_SCAFFOLD_PREDICTIVE_BACK_EASING.transform(progress) *
        when (scaffoldValue.expandedCount) {
            1 -> SINGLE_PANE_PROGRESS_RATIO
            2 -> DUAL_PANE_PROGRESS_RATIO
            else -> TRIPLE_PANE_PROGRESS_RATIO
        }

private val THREE_PANE_SCAFFOLD_PREDICTIVE_BACK_EASING: Easing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)
private const val SINGLE_PANE_PROGRESS_RATIO = 0.1f
private const val DUAL_PANE_PROGRESS_RATIO = 0.15f
private const val TRIPLE_PANE_PROGRESS_RATIO = 0.2f
