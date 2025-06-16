package de.dp_coding.zammadplugin.model;

import java.util.Objects;

/**
 * Represents a ticket from the Zammad ticketing system.
 */
public class Ticket {
    private final int id;
    private final String title;
    private final String number;
    private final String state;
    private final String priority;
    private final String group;
    private final String customer;
    private final String created_at;
    private final String updated_at;

    public Ticket(int id, String title, String number, String state, String priority, 
                  String group, String customer, String created_at, String updated_at) {
        this.id = id;
        this.title = title;
        this.number = number;
        this.state = state;
        this.priority = priority;
        this.group = group;
        this.customer = customer;
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

    public String getState() {
        return state;
    }

    public String getPriority() {
        return priority;
    }

    public String getGroup() {
        return group;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    @Override
    public String toString() {
        return "#" + number + ": " + title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return id == ticket.id &&
                Objects.equals(title, ticket.title) &&
                Objects.equals(number, ticket.number) &&
                Objects.equals(state, ticket.state) &&
                Objects.equals(priority, ticket.priority) &&
                Objects.equals(group, ticket.group) &&
                Objects.equals(customer, ticket.customer) &&
                Objects.equals(created_at, ticket.created_at) &&
                Objects.equals(updated_at, ticket.updated_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, number, state, priority, group, customer, created_at, updated_at);
    }
}