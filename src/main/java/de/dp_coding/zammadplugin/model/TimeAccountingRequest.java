package de.dp_coding.zammadplugin.model;

/**
 * Request object for creating a new time accounting entry.
 */
public class TimeAccountingRequest {
    private final String time_unit;
    // private final String note;

    /**
     * Creates a new time accounting request.
     *

     * @param time The time to record in the format "HH:MM:SS"
     */
    public TimeAccountingRequest(String time) {
        this.time_unit = time;
    }



    public String getTime() {
        return time_unit;
    }
}