package de.dp_coding.zammadplugin.model;

import java.util.Objects;

/**
 * Represents a time accounting entry for a Zammad ticket.
 */
public class TimeAccountingEntry {
    private final int id;
    private final int ticket_id;
    private final String time_unit;
    private final String created_at;
    private final String updated_at;
    private final String note;
    private final String created_by_id;

    public TimeAccountingEntry(int id, int ticketId, String time, String createdAt, 
                              String updatedAt, String note, String createdBy) {
        this.id = id;
        this.ticket_id = ticketId;
        this.time_unit = time;
        this.created_at = createdAt;
        this.updated_at = updatedAt;
        this.note = note;
        this.created_by_id = createdBy;
    }

    public int getId() {
        return id;
    }

    public int getTicketId() {
        return ticket_id;
    }

    public String getTime() {
        return time_unit;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getUpdatedAt() {
        return updated_at;
    }

    public String getNote() {
        return note;
    }

    public String getCreatedBy() {
        return created_by_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeAccountingEntry that = (TimeAccountingEntry) o;
        return id == that.id &&
                ticket_id == that.ticket_id &&
                Objects.equals(time_unit, that.time_unit) &&
                Objects.equals(created_at, that.created_at) &&
                Objects.equals(updated_at, that.updated_at) &&
                Objects.equals(note, that.note) &&
                Objects.equals(created_by_id, that.created_by_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ticket_id, time_unit, created_at, updated_at, note, created_by_id);
    }

    @Override
    public String toString() {
        return "TimeAccountingEntry{" +
                "id=" + id +
                ", ticketId=" + ticket_id +
                ", time='" + time_unit + '\'' +
                ", createdAt='" + created_at + '\'' +
                ", updatedAt='" + updated_at + '\'' +
                ", note='" + note + '\'' +
                ", createdBy='" + created_by_id + '\'' +
                '}';
    }
}