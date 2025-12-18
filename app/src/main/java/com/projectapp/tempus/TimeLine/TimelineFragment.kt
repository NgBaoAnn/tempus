package com.projectapp.tempus.TimeLine

import android.content.Context
import android.os.Bundle
import android.util.Xml
import android.view.View
import com.projectapp.tempus.R
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.xmlpull.v1.XmlPullParser

class TimelineFragment : Fragment(R.layout.timeline_fragment) {

    private lateinit var rvTimeline: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTimeline = view.findViewById(R.id.rvTimeline)
        rvTimeline.layoutManager = LinearLayoutManager(requireContext())

        val data = loadTimelineFromRaw(requireContext())
        rvTimeline.adapter = TimelineAdapter(data)
    }
}

fun loadTimelineFromRaw(context: Context): List<TimelineItem> {
    val items = mutableListOf<TimelineItem>()
    val inputStream = context.resources.openRawResource(R.raw.timeline_mock)

    val parser = Xml.newPullParser()
    parser.setInput(inputStream, null)

    var eventType = parser.eventType
    var currentTag = ""

    var title = ""
    var type = ""
    var startTime = ""
    var duration = 0
    var iconId = 0
    var color = ""
    var status = ""
    var source = ""

    while (eventType != XmlPullParser.END_DOCUMENT) {
        when (eventType) {
            XmlPullParser.START_TAG -> currentTag = parser.name

            XmlPullParser.TEXT -> {
                when (currentTag) {
                    "title" -> title = parser.text
                    "type" -> type = parser.text
                    "startTime" -> startTime = parser.text
                    "durationMinutes" -> duration = parser.text.toInt()
                    "iconId" -> iconId = parser.text.toInt()
                    "color" -> color = parser.text
                    "status" -> status = parser.text
                    "source" -> source = parser.text
                }
            }

            XmlPullParser.END_TAG -> {
                if (parser.name == "item") {
                    items.add(
                        TimelineItem(
                            title,
                            type,
                            startTime,
                            duration,
                            iconId,
                            color,
                            status,
                            source
                        )
                    )
                }
                currentTag = ""
            }
        }
        eventType = parser.next()
    }
    return items
}
