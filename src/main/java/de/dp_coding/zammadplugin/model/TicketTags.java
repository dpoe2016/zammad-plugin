package de.dp_coding.zammadplugin.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the tags for a ticket from the Zammad ticketing system.
 */
public class TicketTags {
    private final List<String> tags;

    public TicketTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public List<String> getTags() {
        return tags;
    }
}
