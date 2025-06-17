package de.dp_coding.zammadplugin.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Request object for creating a new time accounting entry.
 */
public class TimeAccountingRequest {
    private final int ticket_id;
    private final String time_unit;
    // private final String note;

    /**
     * Creates a new time accounting request.
     *
     * @param ticketId The ID of the ticket to record time for
     * @param time The time to record in the format "HH:MM:SS"
     */
    public TimeAccountingRequest(int ticketId, String time) {
        this.ticket_id = ticketId;
        this.time_unit = parseDurationToSeconds(time) / 60 + "";
        // this.note = note;
    }

    public int parseDurationToSeconds(String duration) {
        String[] parts = duration.split(":");

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return hours * 3600 + minutes * 60 + seconds;
    }

    public String getTime() {
        return time_unit;
    }
}