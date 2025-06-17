package de.dp_coding.zammadplugin.api;

import de.dp_coding.zammadplugin.model.Ticket;
import de.dp_coding.zammadplugin.model.TimeAccountingEntry;
import de.dp_coding.zammadplugin.model.TimeAccountingRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

/**
 * Retrofit interface for the Zammad API.
 */
public interface ZammadApi {
    /**
     * Get open tickets assigned to the current user with default query "state_id:4".
     */
    @GET("api/v1/tickets/search")
    default Call<List<Ticket>> getTicketsForCurrentUser() {
        return getTicketsForCurrentUser("state_id:4");
    }

    /**
     * Get tickets assigned to the current user with a specific query.
     */
    @GET("api/v1/tickets/search")
    Call<List<Ticket>> getTicketsForCurrentUser(@Query("query") String query);

    /**
     * Get time accounting entries for a specific ticket.
     */
    @GET("api/v1/tickets/{ticketId}/time_accounting")
    Call<List<TimeAccountingEntry>> getTimeAccountingEntries(@Path("ticketId") int ticketId);

    /**
     * Create a new time accounting entry for a ticket.
     */
    @POST("api/v1/tickets/{ticketId}/time_accounting")
    Call<TimeAccountingEntry> createTimeAccountingEntry(
        @Path("ticketId") int ticketId,
        @Body TimeAccountingRequest request);
}
