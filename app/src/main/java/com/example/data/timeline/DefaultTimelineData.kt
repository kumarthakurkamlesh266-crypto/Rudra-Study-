package com.example.data.timeline

import com.example.data.local.TimelineBlockEntity

object DefaultTimelineData {
    fun getMasterTimeline(): List<TimelineBlockEntity> {
        return listOf(
            TimelineBlockEntity(
                id = 1,
                title = "Wake System",
                subtitle = "Alarm + immediate light exposure + water",
                startTime = "05:45",
                endTime = "05:45",
                type = "ROUTINE",
                subjectTag = "Routine",
                triggerAction = "Alarm across room (forces standing up) + drink water",
                backupVersion = "If exhausted: hard cap 6:30 AM wake, Block 1 shrinks to 30 min",
                isAnchor = true,
                orderIndex = 1
            ),
            TimelineBlockEntity(
                id = 2,
                title = "Morning System",
                subtitle = "Freshen up, no phone, habit-stack into desk",
                startTime = "05:45",
                endTime = "06:15",
                type = "ROUTINE",
                subjectTag = "Habit Stack",
                triggerAction = "Water -> Wash face -> Sit at study desk (Phone in another room)",
                backupVersion = "10 min minimal setup if late",
                isAnchor = true,
                orderIndex = 2
            ),
            TimelineBlockEntity(
                id = 3,
                title = "Study Block 1 (Deep Focus)",
                subtitle = "Hardest subject — Physics / Chem numericals",
                startTime = "06:15",
                endTime = "07:30",
                type = "DEEP_FOCUS",
                subjectTag = "Physics / Chemistry",
                triggerAction = "Water peene ke turant baad, bina phone dekhe",
                backupVersion = "30 min minimum if late wake up; Low-energy: 15 min reading",
                isAnchor = true,
                orderIndex = 3
            ),
            TimelineBlockEntity(
                id = 4,
                title = "Breakfast + Buffer",
                subtitle = "Eat, get ready, pack bag",
                startTime = "07:30",
                endTime = "08:00",
                type = "REST",
                subjectTag = "Nutrition",
                triggerAction = "Fresh meal without screen addiction",
                backupVersion = "15 min quick breakfast",
                isAnchor = false,
                orderIndex = 4
            ),
            TimelineBlockEntity(
                id = 5,
                title = "Study Block 2 / Revision",
                subtitle = "Quick revision or yesterday's formulas & notes",
                startTime = "08:00",
                endTime = "09:00",
                type = "REVISION",
                subjectTag = "Formulas / Flashcards",
                triggerAction = "Open yesterday's summary notes",
                backupVersion = "20 min formula recall",
                isAnchor = false,
                orderIndex = 5
            ),
            TimelineBlockEntity(
                id = 6,
                title = "School Prep + Leave",
                subtitle = "Bag, uniform, cycle check — 4km cardio commute",
                startTime = "09:00",
                endTime = "09:30",
                type = "ROUTINE",
                subjectTag = "Cardio Commute",
                triggerAction = "Mental note during cycle: What was tough yesterday?",
                backupVersion = "Quick checkout",
                isAnchor = true,
                orderIndex = 6
            ),
            TimelineBlockEntity(
                id = 7,
                title = "School System",
                subtitle = "Active class engagement — note 1 question per class",
                startTime = "09:30",
                endTime = "14:00",
                type = "SCHOOL",
                subjectTag = "School",
                triggerAction = "Note keywords & 1 tough doubt in corner of notebook",
                backupVersion = "Read lunch notes (30s habit)",
                isAnchor = true,
                orderIndex = 7
            ),
            TimelineBlockEntity(
                id = 8,
                title = "Return + Decompress",
                subtitle = "Cycle home, uniform change, water, light snack",
                startTime = "14:00",
                endTime = "14:30",
                type = "REST",
                subjectTag = "Decompress",
                triggerAction = "Ghar pahunchte hi uniform change + water",
                backupVersion = "15 min extra rest allowed if exhausted",
                isAnchor = false,
                orderIndex = 8
            ),
            TimelineBlockEntity(
                id = 9,
                title = "Lunch + Rest Block",
                subtitle = "Real guilt-free rest — scheduled, not stolen",
                startTime = "14:30",
                endTime = "15:15",
                type = "REST",
                subjectTag = "Rest",
                triggerAction = "Healthy meal + 20 min power rest",
                backupVersion = "Shift till 3:30 PM if delayed, don't reduce Block 3",
                isAnchor = true,
                orderIndex = 9
            ),
            TimelineBlockEntity(
                id = 10,
                title = "Transition Ritual",
                subtitle = "Habit-stack trigger into study desk",
                startTime = "15:15",
                endTime = "15:30",
                type = "ROUTINE",
                subjectTag = "Transition",
                triggerAction = "Lunch ke baad kapde change karte hi desk par baitho",
                backupVersion = "2-minute rule: just open the first question",
                isAnchor = false,
                orderIndex = 10
            ),
            TimelineBlockEntity(
                id = 11,
                title = "Study Block 3 (Main)",
                subtitle = "New topic / weak subject (Biology / Chem theory)",
                startTime = "15:30",
                endTime = "17:00",
                type = "DEEP_FOCUS",
                subjectTag = "Biology / Chemistry",
                triggerAction = "Kapde change karte hi pehla sawaal khol do",
                backupVersion = "Low-energy: 30 min revision of known concept",
                isAnchor = true,
                orderIndex = 11
            ),
            TimelineBlockEntity(
                id = 12,
                title = "Physical Fitness Block",
                subtitle = "Home workout / walk / bodyweight circuit",
                startTime = "17:00",
                endTime = "17:45",
                type = "FITNESS",
                subjectTag = "Fitness",
                triggerAction = "Block 3 khatam hote hi, seedha shoes pehno",
                backupVersion = "15 min bodyweight circuit (pushups, squats, plank)",
                isAnchor = true,
                orderIndex = 12
            ),
            TimelineBlockEntity(
                id = 13,
                title = "Free Block",
                subtitle = "Guilt-free break — earned, not stolen",
                startTime = "17:45",
                endTime = "18:15",
                type = "FREE",
                subjectTag = "Free Time",
                triggerAction = "Relaxation / Phone allowed here safely",
                backupVersion = "20 min relaxation",
                isAnchor = false,
                orderIndex = 13
            ),
            TimelineBlockEntity(
                id = 14,
                title = "Study Block 4",
                subtitle = "Practice problems / Numericals / Writing practice",
                startTime = "18:15",
                endTime = "19:30",
                type = "DEEP_FOCUS",
                subjectTag = "Practice & Writing",
                triggerAction = "Timer start on targeted problem sheet",
                backupVersion = "45 min focused problem solving",
                isAnchor = true,
                orderIndex = 14
            ),
            TimelineBlockEntity(
                id = 15,
                title = "Dinner + Family/Free",
                subtitle = "No study talk, real disconnect & nourish",
                startTime = "19:30",
                endTime = "20:15",
                type = "REST",
                subjectTag = "Dinner",
                triggerAction = "Family time and mindful eating",
                backupVersion = "30 min dinner buffer",
                isAnchor = false,
                orderIndex = 15
            ),
            TimelineBlockEntity(
                id = 16,
                title = "Study Block 5 (Revision only)",
                subtitle = "Light revision — flashcards, quick recall, no new topics",
                startTime = "20:15",
                endTime = "21:15",
                type = "REVISION",
                subjectTag = "Revision Only",
                triggerAction = "Dinner plate uthate hi study log review",
                backupVersion = "15 min flashcard glance",
                isAnchor = true,
                orderIndex = 16
            ),
            TimelineBlockEntity(
                id = 17,
                title = "Evening Shutdown System",
                subtitle = "Plan tomorrow, 3-line journal, scorecard log",
                startTime = "21:15",
                endTime = "21:45",
                type = "SHUTDOWN",
                subjectTag = "Shutdown",
                triggerAction = "9:15 PM alarm — stop everything and complete 4 checklist items",
                backupVersion = "10 min: 3-line journal + tomorrow's Block 1 topic",
                isAnchor = true,
                orderIndex = 17
            ),
            TimelineBlockEntity(
                id = 18,
                title = "Wind Down",
                subtitle = "No screens — charge phone outside bedroom",
                startTime = "21:45",
                endTime = "22:00",
                type = "REST",
                subjectTag = "Wind Down",
                triggerAction = "Phone kept outside bedroom on charger",
                backupVersion = "5 min dark breathing",
                isAnchor = true,
                orderIndex = 18
            ),
            TimelineBlockEntity(
                id = 19,
                title = "Sleep",
                subtitle = "Non-negotiable lights out (~7h45m restorative sleep)",
                startTime = "22:00",
                endTime = "05:45",
                type = "SLEEP",
                subjectTag = "Restorative Sleep",
                triggerAction = "Lights out at 10:00 PM (Hard cap 10:30 PM)",
                backupVersion = "Wake time stays fixed at 5:45 AM regardless",
                isAnchor = true,
                orderIndex = 19
            )
        )
    }

    fun getWeeklySubjectAllocation(): Map<String, String> {
        return mapOf(
            "MONDAY" to "Physics (Block 1 & 3 focus)",
            "TUESDAY" to "Chemistry (Block 1 & 3 focus)",
            "WEDNESDAY" to "Biology (Block 1 & 3 focus)",
            "THURSDAY" to "Physics + English Writing practice",
            "FRIDAY" to "Chemistry + Hindi Writing practice",
            "SATURDAY" to "Biology + Full Weekly Revision",
            "SUNDAY" to "Weak-area catch-up + Weekly Review"
        )
    }
}
