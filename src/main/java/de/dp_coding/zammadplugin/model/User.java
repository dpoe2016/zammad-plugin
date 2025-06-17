package de.dp_coding.zammadplugin.model;

import java.util.Objects;

/**
 * Represents a user from the Zammad ticketing system.
 */
public class User {
    private final int id;
    private final String login;
    private final String firstname;
    private final String lastname;
    private final String email;
    private final String image;
    private final String created_at;
    private final String updated_at;

    public User(int id, String login, String firstname, String lastname, String email, 
                String image, String created_at, String updated_at) {
        this.id = id;
        this.login = login;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.image = image;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public String getImage() {
        return image;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    /**
     * Returns the full name of the user (firstname + lastname).
     */
    public String getFullName() {
        return firstname + " " + lastname;
    }

    @Override
    public String toString() {
        return getFullName() + " (" + email + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id &&
                Objects.equals(login, user.login) &&
                Objects.equals(firstname, user.firstname) &&
                Objects.equals(lastname, user.lastname) &&
                Objects.equals(email, user.email) &&
                Objects.equals(image, user.image) &&
                Objects.equals(created_at, user.created_at) &&
                Objects.equals(updated_at, user.updated_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login, firstname, lastname, email, image, created_at, updated_at);
    }
}