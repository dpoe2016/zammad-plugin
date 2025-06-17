package de.dp_coding.zammadplugin.api;

/**
 * Cache for ticket tags to avoid unnecessary API calls.
 * This is a very simple implementation that caches tags for a single ticket.
 */
public class TicketTagCache {
    private static TicketTagCache instance;
    
    // Cache for a single ticket
    private int cachedTicketId = -1;
    
    private TicketTagCache() {
        // Private constructor to enforce singleton pattern
    }
    
    public static synchronized TicketTagCache getInstance() {
        if (instance == null) {
            instance = new TicketTagCache();
        }
        return instance;
    }
    
    /**
     * Check if a ticket is in the cache.
     * 
     * @param ticketId The ID of the ticket to check
     * @return true if the ticket is in the cache, false otherwise
     */
    public synchronized boolean isInCache(int ticketId) {
        return cachedTicketId == ticketId;
    }
    
    /**
     * Put a ticket in the cache.
     * 
     * @param ticketId The ID of the ticket to cache
     */
    public synchronized void putInCache(int ticketId) {
        cachedTicketId = ticketId;
    }
    
    /**
     * Clear the cache.
     */
    public synchronized void clearCache() {
        cachedTicketId = -1;
    }
}