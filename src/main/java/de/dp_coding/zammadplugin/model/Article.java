package de.dp_coding.zammadplugin.model;

import java.util.Objects;

/**
 * Represents an article (comment/message) from a Zammad ticket.
 */
public class Article {
    private final int id;
    private final int ticketId;
    private final String type;
    private final String body;
    private final String subject;
    private final String contentType;
    private final String internalNote;
    private final String createdAt;
    private final String updatedAt;
    private final int createdById;
    private final String from;

    public Article(int id, int ticketId, String type, String body, String subject, 
                  String contentType, String internalNote, String createdAt, 
                  String updatedAt, int createdById, String from) {
        this.id = id;
        this.ticketId = ticketId;
        this.type = type;
        this.body = body;
        this.subject = subject;
        this.contentType = contentType;
        this.internalNote = internalNote;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdById = createdById;
        this.from = from;
    }

    public int getId() {
        return id;
    }

    public int getTicketId() {
        return ticketId;
    }

    public String getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public String getSubject() {
        return subject;
    }

    public String getContentType() {
        return contentType;
    }

    public String getInternalNote() {
        return internalNote;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public int getCreatedById() {
        return createdById;
    }

    public String getFrom() {
        return from;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return id == article.id &&
                ticketId == article.ticketId &&
                createdById == article.createdById &&
                Objects.equals(type, article.type) &&
                Objects.equals(body, article.body) &&
                Objects.equals(subject, article.subject) &&
                Objects.equals(contentType, article.contentType) &&
                Objects.equals(internalNote, article.internalNote) &&
                Objects.equals(createdAt, article.createdAt) &&
                Objects.equals(updatedAt, article.updatedAt) &&
                Objects.equals(from, article.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ticketId, type, body, subject, contentType, internalNote, createdAt, updatedAt, createdById, from);
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", ticketId=" + ticketId +
                ", type='" + type + '\'' +
                ", subject='" + subject + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}