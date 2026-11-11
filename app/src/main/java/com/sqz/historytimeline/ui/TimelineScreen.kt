package com.sqz.historytimeline.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sqz.historytimeline.R
import com.sqz.historytimeline.data.HistoryPeriod
import com.sqz.historytimeline.data.TimelineEvent
import com.sqz.historytimeline.data.TimelineRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

private val RailWidth = 58.dp

@Composable
fun TimelineScreen() {
    val context = LocalContext.current
    var periods by remember { mutableStateOf<List<HistoryPeriod>?>(null) }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching {
            withContext(Dispatchers.IO) { TimelineRepository.load(context) }
        }.onSuccess { periods = it }
            .onFailure { loadError = it.message ?: "数据加载失败" }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        val data = periods
        when {
            data == null && loadError == null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            data == null -> Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "时间轴数据加载失败\n$loadError",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
            }

            else -> TimelineContent(data)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimelineContent(periods: List<HistoryPeriod>) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 每个时期标题行在扁平列表中的下标：标题占 1 行，事件各占 1 行
    val periodStartIndices = remember(periods) {
        var acc = 0
        periods.map { period ->
            acc.also { acc += 1 + period.events.size }
        }
    }
    // 扁平列表每一行所属的时期下标，用于跟随高亮
    val rowPeriod = remember(periods) {
        buildList {
            periods.forEachIndexed { index, period ->
                add(index)
                repeat(period.events.size) { add(index) }
            }
        }
    }
    val currentPeriodIndex by remember(periods) {
        derivedStateOf {
            if (rowPeriod.isEmpty()) {
                0
            } else {
                rowPeriod[listState.firstVisibleItemIndex.coerceIn(0, rowPeriod.lastIndex)]
            }
        }
    }
    val totalEvents = remember(periods) { periods.sumOf { it.events.size } }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            currentPeriod = periods.getOrNull(currentPeriodIndex),
            totalEvents = totalEvents,
        )
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp),
            ) {
                periods.forEachIndexed { periodIndex, period ->
                    stickyHeader(key = "period-${period.id}") {
                        PeriodHeaderBlock(period)
                    }
                    itemsIndexed(
                        items = period.events,
                        key = { eventIndex, _ -> "${period.id}-$eventIndex" },
                    ) { _, event ->
                        EventRow(event = event)
                    }
                }
            }

            DynastyRail(
                periods = periods,
                currentPeriodIndex = currentPeriodIndex,
                onSelect = { targetIndex ->
                    val target = periodStartIndices[targetIndex]
                    scope.launch {
                        if (abs(target - listState.firstVisibleItemIndex) < 40) {
                            listState.animateScrollToItem(target)
                        } else {
                            listState.scrollToItem(target)
                        }
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
private fun TopBar(currentPeriod: HistoryPeriod?, totalEvents: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 19.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "先秦至 1949 · 共 $totalEvents 个事件",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            if (currentPeriod != null) {
                Column(
                    modifier = Modifier.widthIn(max = 132.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = currentPeriod.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = currentPeriod.range,
                        fontSize = 9.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    }
}

/** 朝代/时期分段标识：名称、起止年、一句话概述与条目数。吸顶展示，滚动时始终可见。 */
@Composable
private fun PeriodHeaderBlock(period: HistoryPeriod) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 20.dp, end = RailWidth + 10.dp, top = 18.dp, bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .width(3.dp)
                    .height(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(2.dp),
                    )
            )
            Spacer(modifier = Modifier.width(9.dp))
            Column(modifier = Modifier.weight(1f)) {
                // 标题独占一行，长标题按容器宽度自然折行，不与时间区间争横向空间
                Text(
                    text = period.name,
                    fontSize = 22.sp,
                    lineHeight = 29.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Row(
                    modifier = Modifier.padding(top = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = period.range,
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = if (period.isAncient) "古代" else "近代",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = period.summary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(9.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f))
    }
}

/** 单个事件：左侧日期、中间时间轴节点、右侧标题与正文说明。 */
@Composable
private fun EventRow(event: TimelineEvent) {
    val accent = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f)
    val dotColor = if (event.major) accent else MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = RailWidth + 8.dp)
            .height(IntrinsicSize.Min)
    ) {
        Text(
            text = event.date,
            modifier = Modifier
                .width(66.dp)
                .padding(start = 14.dp, top = 11.dp),
            fontSize = 10.sp,
            lineHeight = 14.sp,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.End,
            color = if (event.major) accent else muted,
        )
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(lineColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 15.dp)
                    .size(if (event.major) 8.dp else 5.dp)
                    .background(
                        color = if (event.major) dotColor else dotColor.copy(alpha = 0.65f),
                        shape = CircleShape,
                    )
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 5.dp, top = 9.dp, bottom = 20.dp, end = 12.dp)
        ) {
            Text(
                text = event.title,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                fontWeight = if (event.major) FontWeight.SemiBold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = event.desc,
                fontSize = 12.5.sp,
                lineHeight = 19.sp,
                color = muted,
            )
        }
    }
}

private sealed interface RailEntry {
    data class Group(val label: String) : RailEntry
    data class PeriodItem(val index: Int, val label: String) : RailEntry
}

private fun buildRailEntries(periods: List<HistoryPeriod>): List<RailEntry> = buildList {
    var lastEra: String? = null
    periods.forEachIndexed { index, period ->
        if (period.era != lastEra) {
            add(RailEntry.Group(if (period.isAncient) "古代" else "近代"))
            lastEra = period.era
        }
        add(RailEntry.PeriodItem(index, period.short))
    }
}

/** 侧边朝代快速选择轴：点击定位到该时期起点，当前时期高亮跟随。 */
@Composable
private fun DynastyRail(
    periods: List<HistoryPeriod>,
    currentPeriodIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = remember(periods) { buildRailEntries(periods) }
    val railState = rememberLazyListState()
    val currentRailIndex = remember(entries, currentPeriodIndex) {
        entries.indexOfFirst { it is RailEntry.PeriodItem && it.index == currentPeriodIndex }
    }

    LaunchedEffect(currentRailIndex) {
        if (currentRailIndex >= 0 &&
            railState.layoutInfo.visibleItemsInfo.none { it.index == currentRailIndex }
        ) {
            railState.animateScrollToItem(currentRailIndex)
        }
    }

    LazyColumn(
        state = railState,
        modifier = modifier
            .width(RailWidth)
            .fillMaxHeight()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp),
            ),
        contentPadding = PaddingValues(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        entries.forEachIndexed { entryIndex, entry ->
            when (entry) {
                is RailEntry.Group -> item(key = "group-$entryIndex-${entry.label}") {
                    RailGroupLabel(entry.label)
                }

                is RailEntry.PeriodItem -> item(key = "rail-${periods[entry.index].id}") {
                    RailPeriodLabel(
                        label = entry.label,
                        selected = entry.index == currentPeriodIndex,
                        onClick = { onSelect(entry.index) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RailGroupLabel(label: String) {
    Column(
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontSize = 8.5.sp,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
private fun RailPeriodLabel(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(if (selected) 12.dp else 0.dp)
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0f),
                    shape = RoundedCornerShape(1.dp),
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 9.5.sp,
            lineHeight = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
