package com.sqz.historytimeline.data

/** 一个历史事件条目。date 为展示用日期文本，已包含“年/月/日”。 */
data class TimelineEvent(
    val date: String,
    val title: String,
    val desc: String,
    val major: Boolean,
)

/** 一个朝代或历史时期（近代为教材分期）。 */
data class HistoryPeriod(
    val id: String,
    val era: String,
    val name: String,
    val short: String,
    val range: String,
    val summary: String,
    val events: List<TimelineEvent>,
) {
    val isAncient: Boolean get() = era == "ancient"
}
