package de.dp_coding.zammadplugin.api;

import de.dp_coding.zammadplugin.model.Article;
import de.dp_coding.zammadplugin.model.Ticket;
import de.dp_coding.zammadplugin.model.TimeAccountingEntry;
import de.dp_coding.zammadplugin.model.TimeAccountingRequest;
import de.dp_coding.zammadplugin.model.User;
import de.dp_coding.zammadplugin.model.Organization;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;
import java.util.Set;

/**
 * Retrofit interface for the Zammad API.
 */
public interface ZammadApi {
    /**
     * Get open tickets assigned to the current user with default query "state_id:4".
     */
    @GET("api/v1/tickets/search")
    default Call<List<Ticket>> getTicketsForCurrentUser(int userId) {
        return getTicketsForCurrentUser("(state_id:4 OR state_id:1 OR state_id:10) AND owner_id:" + userId);
    }

    /**
     * Get tickets assigned to the current user with a specific query.
     */
    @GET("api/v1/tickets/search")
    Call<List<Ticket>> getTicketsForCurrentUser(@Query("query") String query);

    /**
     * Get time accounting entries for a specific ticket.
     */
    @GET("api/v1/tickets/{ticketId}/time_accountings")
    Call<List<TimeAccountingEntry>> getTimeAccountingEntries(@Path("ticketId") int ticketId);

    /**
     * Create a new time accounting entry for a ticket.
     */
    @POST("api/v1/tickets/{ticketId}/time_accountings")
    Call<TimeAccountingEntry> createTimeAccountingEntry(
            @Path("ticketId") int ticketId,
            @Body TimeAccountingRequest request);

    /**
     * Get the current authenticated user.
     */
    @GET("api/v1/users/me")
    Call<User> getCurrentUser();

    /**
     * Get a user by ID.
     */
    @GET("api/v1/users/{userId}")
    Call<User> getUserById(@Path("userId") int userId);

    /**
     * Get tags for a specific ticket.
     */
    @GET("api/v1/tickets/{ticketId}/tags")
    Call<List<String>> getTicketTags(@Path("ticketId") int ticketId);

    /**
     * Get articles (comments/messages) for a specific ticket.
     */
    @GET("api/v1/ticket_articles/by_ticket/{ticketId}")
    Call<List<Article>> getTicketArticles(@Path("ticketId") int ticketId);

    /**
     * Get an organization by ID.
     */
    @GET("api/v1/organizations/{id}")
    Call<Organization> getOrganizationById(@Path("id") int id);
}
