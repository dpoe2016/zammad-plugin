package de.dp_coding.zammadplugin.api;

import com.google.gson.GsonBuilder;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import de.dp_coding.zammadplugin.model.Ticket;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for communicating with the Zammad API.
 */
@Service
public final class ZammadService {
    private ZammadApi zammadApi;
    private final PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();

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
     */
    public List<Ticket> getTicketsForCurrentUser() throws IOException {
        if (!isConfigured()) {
            throw new IllegalStateException("Zammad service is not configured. Please set the Zammad URL and API token.");
        }

        if (zammadApi == null) {
            createApiClient(getZammadUrl(), getApiToken());
        }

        // Call the Zammad API to get tickets for the current user
        if (zammadApi == null) {
            throw new IllegalStateException("Zammad API client is not initialized.");
        }
        
        retrofit2.Call<List<Ticket>> call = zammadApi.getTicketsForCurrentUser();
        retrofit2.Response<List<Ticket>> response = call.execute();

        if (!response.isSuccessful()) {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            throw new IllegalStateException("Failed to fetch tickets: " + errorBody);
        }

        List<Ticket> body = response.body();
        return body != null ? body : Collections.emptyList();
    }

    private void createApiClient(String zammadUrl, String apiToken) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(chain -> {
                okhttp3.Request request = chain.request().newBuilder()
                    .addHeader("Authorization", "Token token=" + apiToken)
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