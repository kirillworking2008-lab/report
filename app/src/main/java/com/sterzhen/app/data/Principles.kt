package com.sterzhen.app.data

/**
 * Стержень — принципы, на которых строится жизнь.
 * Каждый принцип — пассивная задача: каждый день отмечаешь,
 * что держишься, а стрик-огонёк обновляется автоматически.
 */
data class Principle(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val colorHex: Long
)

object Principles {
    val ALL: List<Principle> = listOf(
        Principle(
            id = "alcohol",
            title = "Без алкоголя",
            subtitle = "Ни капли спиртного",
            emoji = "🍺",
            colorHex = 0xFFE53935
        ),
        Principle(
            id = "energy",
            title = "Без энергетиков",
            subtitle = "Никаких банок и бутылок",
            emoji = "⚡",
            colorHex = 0xFFF9A825
        ),
        Principle(
            id = "coffee",
            title = "Без кофе",
            subtitle = "Ни кофе, ни кофейных напитков",
            emoji = "☕",
            colorHex = 0xFF8D6E63
        ),
        Principle(
            id = "drugs",
            title = "Без наркотиков",
            subtitle = "Никаких веществ",
            emoji = "💊",
            colorHex = 0xFF8E24AA
        ),
        Principle(
            id = "smoking",
            title = "Без курения",
            subtitle = "Ни сигарет, ни вейпов",
            emoji = "🚭",
            colorHex = 0xFF1E88E5
        ),
        Principle(
            id = "profanity",
            title = "Без мата и оскорблений",
            subtitle = "Чистая речь, уважение",
            emoji = "🤬",
            colorHex = 0xFFEC407A
        ),
        Principle(
            id = "reels",
            title = "Без рилсов и тиктоков",
            subtitle = "Никакого скролла ленты",
            emoji = "📱",
            colorHex = 0xFF00ACC1
        )
    )

    /** Старые идентификаторы, которые могли остаться в базе или в настройках виджета. */
    private val LEGACY_IDS = mapOf("procrastination" to "smoking")

    fun byId(id: String): Principle? {
        val normalized = LEGACY_IDS[id] ?: id
        return ALL.firstOrNull { it.id == normalized }
    }
}
