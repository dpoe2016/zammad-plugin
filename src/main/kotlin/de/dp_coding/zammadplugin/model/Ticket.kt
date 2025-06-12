package de.dp_coding.zammadplugin.model

/**
 * Represents a ticket from the Zammad ticketing system.
 */
data class Ticket(
    val id: Int,
    val title: String,
    val number: String,
    val state: String,
    val priority: String,
    val group: String,
    val customer: String,
    val created_at: String,
    val updated_at: String
) {
    override fun toString(): String {
        return "#$number: $title"
    }
}