package de.dp_coding.zammadplugin.model;

/**
 * Request object for creating a new time accounting entry.
 */
public class TimeAccountingRequest {
    private final int ticketId;
    private final String time;
    private final String note;

    /**
     * Creates a new time accounting request.
     *
     * @param ticketId The ID of the ticket to record time for
     * @param time The time to record in the format "HH:MM:SS"
     * @param note Optional note for the time entry
     */
    public TimeAccountingRequest(int ticketId, String time, String note) {
        this.ticketId = ticketId;
        this.time = time;
        this.note = note;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getTime() {
        return time;
    }

    public String getNote() {
        return note;
    }
}