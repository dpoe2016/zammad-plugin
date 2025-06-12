package de.dp_coding.zammadplugin.api

import de.dp_coding.zammadplugin.model.Ticket
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Zammad API.
 */
interface ZammadApi {
    /**
     * Get open tickets assigned to the current user.
     */
    @GET("api/v1/tickets/search")
    fun getTicketsForCurrentUser(
        @Query("query") query: String = "state_id:4"
    ): Call<List<Ticket>>
}
