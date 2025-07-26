package de.dp_coding.zammadplugin.api;

import com.google.gson.GsonBuilder;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import de.dp_coding.zammadplugin.exception.ApiException;
import de.dp_coding.zammadplugin.exception.ConfigurationException;
import de.dp_coding.zammadplugin.exception.FeatureNotEnabledException;
import de.dp_coding.zammadplugin.exception.ZammadException;
import de.dp_coding.zammadplugin.model.Article;
import de.dp_coding.zammadplugin.model.Ticket;
import de.dp_coding.zammadplugin.model.TimeAccountingEntry;
import de.dp_coding.zammadplugin.model.TimeAccountingRequest;
import de.dp_coding.zammadplugin.model.User;
import de.dp_coding.zammadplugin.model.Organization;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

/**
 * Service for communicating with the Zammad API.
 */
@Service
public final class ZammadService {
    private static final Logger LOG = Logger.getInstance(ZammadService.class);

    private ZammadApi zammadApi;
    private final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    // Cache for user information to avoid unnecessary API calls
    private final Map<Integer, User> userCache = new HashMap<>();
    // Cache for ticket tags to avoid unnecessary API calls
    private final Map<Integer, List<String>> tagCache = new HashMap<>();
    // Cache for ticket articles to avoid unnecessary API calls
    private final Map<Integer, List<Article>> articleCache = new HashMap<>();

    private static final String ZAMMAD_URL_KEY = "de.dp_coding.zammadplugin.zammadUrl";
    private static final String ZAMMAD_TOKEN_KEY = "de.dp_coding.zammadplugin.zammadToken";

    public static ZammadService getInstance() {
        return ApplicationManager.getApplication().getService(ZammadService.class);
    }

    /**
     * Initialize the Zammad API client with the provided URL and token.
     */
    public void initialize(String zammadUrl, String apiToken) {
        // Save settings
        propertiesComponent.setValue(ZAMMAD_URL_KEY, zammadUrl);
        propertiesComponent.setValue(ZAMMAD_TOKEN_KEY, apiToken);

        // Create API client
        createApiClient(zammadUrl, apiToken);
    }

    /**
     * Get the Zammad URL from settings.
     */
    public String getZammadUrl() {
        return propertiesComponent.getValue(ZAMMAD_URL_KEY, "");
    }

    /**
     * Get the API token from settings.
     */
    public String getApiToken() {
        return propertiesComponent.getValue(ZAMMAD_TOKEN_KEY, "");
    }

    /**
     * Check if the service is configured with URL and token.
     */
    public boolean isConfigured() {
        String url = getZammadUrl();
        String token = getApiToken();
        return url != null && !url.isEmpty() && token != null && !token.isEmpty();
    }

    /**
     * Get tickets assigned to the current user.
     *
     * @return List of tickets assigned to the current user
     * @throws ConfigurationException If the service is not configured
     * @throws ApiException If there is an error communicating with the API
     * @throws ZammadException If there is another error
     */
    public List<Ticket> getTicketsForCurrentUser() throws ZammadException {
        if (!isConfigured()) {
            LOG.warn("Zammad service is not configured");
            throw new ConfigurationException("Zammad service is not configured. Please set the Zammad URL and API token.");
        }

        if (zammadApi == null) {
            createApiClient(getZammadUrl(), getApiToken());
        }

        // Call the Zammad API to get tickets for the current user
        if (zammadApi == null) {
            LOG.warn("Zammad API client is not initialized");
            throw new ConfigurationException("Zammad API client is not initialized.");
        }

        try {
            LOG.info("Fetching current user");
            retrofit2.Call<User> currentUserCall = zammadApi.getCurrentUser();
            retrofit2.Response<User> currentUserResponse = currentUserCall.execute();

            if (!currentUserResponse.isSuccessful()) {
                String errorBody = currentUserResponse.errorBody() != null ? currentUserResponse.errorBody().string() : "Unknown error";
                LOG.warn("Failed to fetch current user: " + errorBody);
                throw new ApiException("Failed to fetch current user: " + errorBody, currentUserResponse.code());
            }

            User user = currentUserResponse.body();
            if (user == null) {
                LOG.warn("Current user response body is null");
                throw new ApiException("Failed to fetch current user: Response body is null");
            }

            LOG.info("Fetching tickets for user ID: " + user.getId());
            retrofit2.Call<List<Ticket>> call = zammadApi.getTicketsForCurrentUser(user.getId());
            retrofit2.Response<List<Ticket>> response = call.execute();

            if (!response.isSuccessful()) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                LOG.warn("Failed to fetch tickets: " + errorBody);
                throw new ApiException("Failed to fetch tickets: " + errorBody, response.code());
            }

            List<Ticket> body = response.body();
            if (body != null) {
                for (Ticket ticket : body) {
                    Integer orgId = ticket.getOrganizationId();
                    if (orgId != null) {
                        try {
                            retrofit2.Call<Organization> orgCall = zammadApi.getOrganizationById(orgId);
                            retrofit2.Response<Organization> orgResponse = orgCall.execute();
                            if (orgResponse.isSuccessful() && orgResponse.body() != null) {
                                ticket.setOrganizationName(orgResponse.body().getName());
                            } else {
                                ticket.setOrganizationName(null);
                            }
                        } catch (Exception e) {
                            ticket.setOrganizationName(null);
                        }
                    }
                }
            }
            LOG.info("Fetched " + (body != null ? body.size() : 0) + " tickets");
            return body != null ? body : Collections.emptyList();
        } catch (IOException e) {
            LOG.warn("IO error while fetching tickets", e);
            throw new ApiException("Network error while fetching tickets", e);
        }
    }

    /**
     * Get time accounting entries for a specific ticket.
     *
     * @param ticketId The ID of the ticket to get time entries for
     * @return List of time accounting entries for the ticket
     * @throws ConfigurationException If the service is not configured
     * @throws FeatureNotEnabledException If time accounting is not enabled in the Zammad instance
     * @throws ApiException If there is an error communicating with the API
     * @throws ZammadException If there is another error
     */
    public List<TimeAccountingEntry> getTimeAccountingEntries(int ticketId) throws ZammadException {
        if (!isConfigured()) {
            LOG.warn("Zammad service is not configured");
            throw new ConfigurationException("Zammad service is not configured. Please set the Zammad URL and API token.");
        }

        if (zammadApi == null) {
            createApiClient(getZammadUrl(), getApiToken());
        }

        // Call the Zammad API to get time accounting entries for the ticket
        if (zammadApi == null) {
            LOG.warn("Zammad API client is not initialized");
            throw new ConfigurationException("Zammad API client is not initialized.");
        }

        try {
            LOG.info("Fetching time accounting entries for ticket ID: " + ticketId);
            retrofit2.Call<List<TimeAccountingEntry>> call = zammadApi.getTimeAccountingEntries(ticketId);
            retrofit2.Response<List<TimeAccountingEntry>> response = call.execute();

            if (!response.isSuccessful()) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";

                // Check for the specific "Time Accounting is not enabled" error
                if (response.code() == 403 && errorBody.contains("Time Accounting is not enabled")) {
                    LOG.warn("Time Accounting is not enabled in the Zammad instance");
                    throw new FeatureNotEnabledException("Time Accounting", 
                        "Time Accounting is not enabled in your Zammad instance. " +
                        "Please contact your Zammad administrator to enable this feature.");
                }

                LOG.warn("Failed to fetch time accounting entries: " + errorBody);
                throw new ApiException("Failed to fetch time accounting entries: " + errorBody, response.code());
            }

            List<TimeAccountingEntry> body = response.body();
            LOG.info("Fetched " + (body != null ? body.size() : 0) + " time accounting entries");
            return body != null ? body : Collections.emptyList();
        } catch (IOException e) {
            LOG.warn("IO error while fetching time accounting entries", e);
            throw new ApiException("Network error while fetching time accounting entries", e);
        }
    }

    /**
     * Create a new time accounting entry for a ticket.
     *
     * @param ticketId The ID of the ticket to create a time entry for
     * @param time The time to record in the format "HH:MM:SS"
     * @return The created time accounting entry
     * @throws ConfigurationException If the service is not configured
     * @throws FeatureNotEnabledException If time accounting is not enabled in the Zammad instance
     * @throws ApiException If there is an error communicating with the API
     * @throws ZammadException If there is another error
     */
    public TimeAccountingEntry createTimeAccountingEntry(int ticketId, String time) throws ZammadException {
        if (!isConfigured()) {
            LOG.warn("Zammad service is not configured");
            throw new ConfigurationException("Zammad service is not configured. Please set the Zammad URL and API token.");
        }

        if (zammadApi == null) {
            createApiClient(getZammadUrl(), getApiToken());
        }

        // Call the Zammad API to create a time accounting entry
        if (zammadApi == null) {
            LOG.warn("Zammad API client is not initialized");
            throw new ConfigurationException("Zammad API client is not initialized.");
        }

        try {
            LOG.info("Creating time accounting entry for ticket ID: " + ticketId + " with time: " + time);
            TimeAccountingRequest request = new TimeAccountingRequest(ticketId, time);
            retrofit2.Call<TimeAccountingEntry> call = zammadApi.createTimeAccountingEntry(ticketId, request);
            retrofit2.Response<TimeAccountingEntry> response = call.execute();

            if (!response.isSuccessful()) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";

                // Check for the specific "Time Accounting is not enabled" error
                if (response.code() == 403 && errorBody.contains("Time Accounting is not enabled")) {
                    LOG.warn("Time Accounting is not enabled in the Zammad instance");
                    throw new FeatureNotEnabledException("Time Accounting", 
                        "Time Accounting is not enabled in your Zammad instance. " +
                        "Please contact your Zammad administrator to enable this feature.");
                }

                LOG.warn("Failed to create time accounting entry: " + errorBody);
                throw new ApiException("Failed to create time accounting entry: " + errorBody, response.code());
            }

            TimeAccountingEntry entry = response.body();
            if (entry == null) {
                LOG.warn("Time accounting entry response body is null");
                throw new ApiException("Failed to create time accounting entry: Response body is null");
            }

            LOG.info("Created time accounting entry with ID: " + entry.getId());
            return entry;
        } catch (IOException e) {
            LOG.warn("IO error while creating time accounting entry", e);
            throw new ApiException("Network error while creating time accounting entry", e);
        }
    }

    /**
     * Get the current authenticated user.
     *
     * @return The current user
     * @throws ConfigurationException If the service is not configured
     * @throws ApiException If there is an error communicating with the API
     * @throws ZammadException If there is another error
     */
    public User getCurrentUser() throws ZammadException {
        if (!isConfigured()) {
            LOG.warn("Zammad service is not configured");
            throw new ConfigurationException("Zammad service is not configured. Please set the Zammad URL and API token.");
        }

        if (zammadApi == null) {
            createApiClient(getZammadUrl(), getApiToken());
        }

        // Call the Zammad API to get the current user
        if (zammadApi == null) {
            LOG.warn("Zammad API client is not initialized");
            throw new ConfigurationException("Zammad API client is not initialized.");
        }

        try {
            LOG.info("Fetching current user");
            retrofit2.Call<User> call = zammadApi.getCurrentUser();
            retrofit2.Response<User> response = call.execute();

            if (!response.isSuccessful()) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                LOG.warn("Failed to fetch current user: " + errorBody);
                throw new ApiException("Failed to fetch current user: " + errorBody, response.code());
            }

            User user = response.body();
            if (user == null) {
                LOG.warn("Current user response body is null");
                throw new ApiException("Failed to fetch current user: Response body is null");
            }

            LOG.info("Fetched current user with ID: " + user.getId());
            return user;
        } catch (IOException e) {
            LOG.warn("IO error while fetching current user", e);
            throw new ApiException("Network error while fetching current user", e);
        }
    }

    /**
     * Get a user by ID.
     *
     * @param userId The ID of the user to get
     * @return The user with the specified ID
     * @throws ConfigurationException If the service is not configured
     * @throws ApiException If there is an error communicating with the API
     * @throws ZammadException If there is another error
     */
    public User getUserById(int userId) throws ZammadException {
        // Check cache first
        User cachedUser = userCache.get(userId);
        if (cachedUser != null) {
            LOG.info("Using cached user for ID: " + userId);
            return cachedUser;
        }

        if (!isConfigured()) {
            LOG.warn("Zammad service is not configured");
            throw new ConfigurationException("Zammad service is not configured. Please set the Zammad URL and API token.");
        }

        if (zammadApi == null) {
            createApiClient(getZammadUrl(), getApiToken());
        }

        // Call the Zammad API to get the user by ID
        if (zammadApi == null) {
            LOG.warn("Zammad API client is not initialized");
            throw new ConfigurationException("Zammad API client is not initialized.");
        }

        try {
            LOG.info("Fetching user with ID: " + userId);
            retrofit2.Call<User> call = zammadApi.getUserById(userId);
            retrofit2.Response<User> response = call.execute();

            if (!response.isSuccessful()) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                LOG.warn("Failed to fetch user: " + errorBody);
                throw new ApiException("Failed to fetch user: " + errorBody, response.code());
            }

            User user = response.body();
            if (user == null) {
                LOG.warn("User response body is null for ID: " + userId);
                throw new ApiException("Failed to fetch user: Response body is null");
            }

            // Cache the user for future requests
            LOG.info("Caching user with ID: " + userId);
            userCache.put(userId, user);

            return user;
        } catch (IOException e) {
            LOG.warn("IO error while fetching user with ID: " + userId, e);
            throw new ApiException("Network error while fetching user", e);
        }
    }

    /**
     * Clear the user cache.
     * This can be useful in scenarios where you want to force a refresh of user data.
     */
    public void clearUserCache() {
        userCache.clear();
    }

    /**
     * Get tags for a specific ticket.
     * This method uses a cache to avoid unnecessary API calls.
     *
     * @param ticketId The ID of the ticket to get tags for
     * @return List of tags for the ticket
     * @throws ConfigurationException If the service is not configured
     * @throws ApiException If there is an error communicating with the API
     * @throws ZammadException If there is another error
     */
    public List<String> getTicketTags(int ticketId) throws ZammadException {
        // Check cache first
        List<String> cachedTags = tagCache.get(ticketId);
        if (cachedTags != null) {
            LOG.info("Using cached tags for ticket ID: " + ticketId);
            return cachedTags;
        }

        if (!isConfigured()) {
            LOG.warn("Zammad service is not configured");
            throw new ConfigurationException("Zammad service is not configured. Please set the Zammad URL and API token.");
        }

        if (zammadApi == null) {
            createApiClient(getZammadUrl(), getApiToken());
        }

        // Call the Zammad API to get the tags for the ticket
        if (zammadApi == null) {
            LOG.warn("Zammad API client is not initialized");
            throw new ConfigurationException("Zammad API client is not initialized.");
        }

        try {
            LOG.info("Fetching tags for ticket ID: " + ticketId);
            retrofit2.Call<List<String>> call = zammadApi.getTicketTags(ticketId);
            retrofit2.Response<List<String>> response = call.execute();

            if (!response.isSuccessful()) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                LOG.warn("Failed to fetch ticket tags: " + errorBody);
                throw new ApiException("Failed to fetch ticket tags: " + errorBody, response.code());
            }

            List<String> tags = response.body();

            // Cache the tags for future requests
            if (tags != null) {
                LOG.info("Caching tags for ticket ID: " + ticketId + ", count: " + tags.size());
                tagCache.put(ticketId, tags);
            } else {
                LOG.warn("Ticket tags response body is null for ticket ID: " + ticketId);
                tags = Collections.emptyList();
            }

            return tags;
        } catch (IOException e) {
            LOG.warn("IO error while fetching tags for ticket ID: " + ticketId, e);
            throw new ApiException("Network error while fetching ticket tags", e);
        }
    }

    /**
     * Clear the tag cache.
     * This can be useful in scenarios where you want to force a refresh of tag data.
     */
    public void clearTagCache() {
        tagCache.clear();
    }

    /**
     * Get articles (comments/messages) for a specific ticket.
     * This method uses a cache to avoid unnecessary API calls.
     *
     * @param ticketId The ID of the ticket to get articles for
     * @return List of articles for the ticket
     * @throws ConfigurationException If the service is not configured
     * @throws ApiException If there is an error communicating with the API
     * @throws ZammadException If there is another error
     */
    public List<Article> getTicketArticles(int ticketId) throws ZammadException {
        // Check cache first
        List<Article> cachedArticles = articleCache.get(ticketId);
        if (cachedArticles != null) {
            LOG.info("Using cached articles for ticket ID: " + ticketId);
            return cachedArticles;
        }

        if (!isConfigured()) {
            LOG.warn("Zammad service is not configured");
            throw new ConfigurationException("Zammad service is not configured. Please set the Zammad URL and API token.");
        }

        if (zammadApi == null) {
            createApiClient(getZammadUrl(), getApiToken());
        }

        // Call the Zammad API to get the articles for the ticket
        if (zammadApi == null) {
            LOG.warn("Zammad API client is not initialized");
            throw new ConfigurationException("Zammad API client is not initialized.");
        }

        try {
            LOG.info("Fetching articles for ticket ID: " + ticketId);
            retrofit2.Call<List<Article>> call = zammadApi.getTicketArticles(ticketId);
            retrofit2.Response<List<Article>> response = call.execute();

            if (!response.isSuccessful()) {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                LOG.warn("Failed to fetch ticket articles: " + errorBody);
                throw new ApiException("Failed to fetch ticket articles: " + errorBody, response.code());
            }

            List<Article> articles = response.body();

            // Cache the articles for future requests
            if (articles != null) {
                LOG.info("Caching articles for ticket ID: " + ticketId + ", count: " + articles.size());
                articleCache.put(ticketId, articles);
            } else {
                LOG.warn("Ticket articles response body is null for ticket ID: " + ticketId);
                articles = Collections.emptyList();
            }

            return articles;
        } catch (IOException e) {
            LOG.warn("IO error while fetching articles for ticket ID: " + ticketId, e);
            throw new ApiException("Network error while fetching ticket articles", e);
        }
    }

    /**
     * Clear the article cache.
     * This can be useful in scenarios where you want to force a refresh of article data.
     */
    public void clearArticleCache() {
        articleCache.clear();
    }

    private void createApiClient(String zammadUrl, String apiToken) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(chain -> {
                okhttp3.Request request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + apiToken)
                    .build();
                return chain.proceed(request);
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

        com.google.gson.Gson gson = new GsonBuilder()
            .setLenient()
            .create();

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(zammadUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

        zammadApi = retrofit.create(ZammadApi.class);
    }
}
