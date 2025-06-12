package de.dp_coding.zammadplugin.api

import com.google.gson.GsonBuilder
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import de.dp_coding.zammadplugin.model.Ticket
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Service for communicating with the Zammad API.
 */
@Service
class ZammadService {
    private var zammadApi: ZammadApi? = null
    private val propertiesComponent = PropertiesComponent.getInstance()

    companion object {
        private const val ZAMMAD_URL_KEY = "de.dp_coding.zammadplugin.zammadUrl"
        private const val ZAMMAD_TOKEN_KEY = "de.dp_coding.zammadplugin.zammadToken"

        fun getInstance(): ZammadService = ApplicationManager.getApplication().service()
    }

    /**
     * Initialize the Zammad API client with the provided URL and token.
     */
    fun initialize(zammadUrl: String, apiToken: String) {
        // Save settings
        propertiesComponent.setValue(ZAMMAD_URL_KEY, zammadUrl)
        propertiesComponent.setValue(ZAMMAD_TOKEN_KEY, apiToken)

        // Create API client
        createApiClient(zammadUrl, apiToken)
    }

    /**
     * Get the Zammad URL from settings.
     */
    fun getZammadUrl(): String {
        return propertiesComponent.getValue(ZAMMAD_URL_KEY, "")
    }

    /**
     * Get the API token from settings.
     */
    fun getApiToken(): String {
        return propertiesComponent.getValue(ZAMMAD_TOKEN_KEY, "")
    }

    /**
     * Check if the service is configured with URL and token.
     */
    fun isConfigured(): Boolean {
        val url = getZammadUrl()
        val token = getApiToken()
        return url.isNotEmpty() && token.isNotEmpty()
    }

    /**
     * Get tickets assigned to the current user.
     */
    fun getTicketsForCurrentUser(): List<Ticket> {
        if (!isConfigured()) {
            throw IllegalStateException("Zammad service is not configured. Please set the Zammad URL and API token.")
        }

        if (zammadApi == null) {
            createApiClient(getZammadUrl(), getApiToken())
        }

        // Call the Zammad API to get tickets for the current user
        val call = zammadApi?.getTicketsForCurrentUser()?: throw IllegalStateException("Zammad API client is not initialized.")
        val response = call.execute()

        if (!response.isSuccessful) {
            throw IllegalStateException("Failed to fetch tickets: ${response.errorBody()?.string()}")
        }

        return response.body() ?: emptyList()
    }

    private fun createApiClient(zammadUrl: String, apiToken: String) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Token token=$apiToken")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(zammadUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        zammadApi = retrofit.create(ZammadApi::class.java)
    }
}
