package de.dp_coding.zammadplugin.model;

import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a ticket from the Zammad ticketing system.
 */
public class Ticket {
    private final int id;
    private final String title;
    private final String number;
    private final String state_id;
    private final String priority;
    private final String group;
    private final String customer_id;
    private final String created_at;
    private final String updated_at;

    // Cache for ticket tags to avoid unnecessary API calls
    private static final Map<Integer, List<String>> tagCache = new HashMap<>();
    private List<String> tags;

    public Ticket(int id, String title, String number, String state, String priority, 
                  String group, String customer, String created_at, String updated_at) {
        this.id = id;
        this.title = title;
        this.number = number;
        this.state_id = state;
        this.priority = priority;
        this.group = group;
        this.customer_id = customer;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getNumber() {
        return number;
    }

    public String getState_id() {
        return state_id;
    }

    public String getPriority() {
        return priority;
    }

    public String getGroup() {
        return group;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }


    @Override
    public String toString() {
        return "#" + id + ": " + title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return id == ticket.id &&
                Objects.equals(title, ticket.title) &&
                Objects.equals(number, ticket.number) &&
                Objects.equals(state_id, ticket.state_id) &&
                Objects.equals(priority, ticket.priority) &&
                Objects.equals(group, ticket.group) &&
                Objects.equals(customer_id, ticket.customer_id) &&
                Objects.equals(created_at, ticket.created_at) &&
                Objects.equals(updated_at, ticket.updated_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, number, state_id, priority, group, customer_id, created_at, updated_at);
    }
}
