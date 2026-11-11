package com.sqz.historytimeline.data

import android.content.Context
import org.json.JSONObject

/** 从 assets/timeline.json 读取离线数据，按文件中的先后顺序返回。 */
object TimelineRepository {

    fun load(context: Context): List<HistoryPeriod> {
        val text = context.assets.open("timeline.json").use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        }
        val root = JSONObject(text)
        val periodsArray = root.getJSONArray("periods")
        return buildList {
            for (i in 0 until periodsArray.length()) {
                val obj = periodsArray.getJSONObject(i)
                val name = obj.getString("name")
                val eventsArray = obj.getJSONArray("events")
                val events = buildList {
                    for (j in 0 until eventsArray.length()) {
                        val event = eventsArray.getJSONObject(j)
                        add(
                            TimelineEvent(
                                date = event.getString("date"),
                                title = event.getString("title"),
                                desc = event.getString("desc"),
                                major = event.optBoolean("major", false),
                            )
                        )
                    }
                }
                add(
                    HistoryPeriod(
                        id = obj.getString("id"),
                        era = obj.getString("era"),
                        name = name,
                        short = obj.optString("short", name),
                        range = obj.getString("range"),
                        summary = obj.getString("summary"),
                        events = events,
                    )
                )
            }
        }
    }
}
