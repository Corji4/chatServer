package main;

import java.util.Objects;

public class User {

    private String login;
    private String password;

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login) &&
                Objects.equals(password, user.password);
    }

    @Override
    public String toString() {
        return login + " " + password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }
}
