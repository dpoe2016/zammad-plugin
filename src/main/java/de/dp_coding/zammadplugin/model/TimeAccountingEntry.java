package de.dp_coding.zammadplugin.model;

import java.util.Objects;

/**
 * Represents a time accounting entry for a Zammad ticket.
 */
public class TimeAccountingEntry {
    private final int id;
    private final int ticketId;
    private final String time;
    private final String createdAt;
    private final String updatedAt;
    private final String note;
    private final String createdBy;

    public TimeAccountingEntry(int id, int ticketId, String time, String createdAt, 
                              String updatedAt, String note, String createdBy) {
        this.id = id;
        this.ticketId = ticketId;
        this.time = time;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.note = note;
        this.createdBy = createdBy;
    }

    public int getId() {
        return id;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getTime() {
        return time;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getNote() {
        return note;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeAccountingEntry that = (TimeAccountingEntry) o;
        return id == that.id &&
                ticketId == that.ticketId &&
                Objects.equals(time, that.time) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt) &&
                Objects.equals(note, that.note) &&
                Objects.equals(createdBy, that.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ticketId, time, createdAt, updatedAt, note, createdBy);
    }

    @Override
    public String toString() {
        return "TimeAccountingEntry{" +
                "id=" + id +
                ", ticketId=" + ticketId +
                ", time='" + time + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", note='" + note + '\'' +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}