package de.dp_coding.zammadplugin.api;

import de.dp_coding.zammadplugin.model.Ticket;
import retrofit2.Call;
import retrofit2.http.GET;
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
}