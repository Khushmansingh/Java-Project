package model;

import exception.InvalidInputException;
import util.PasswordHasher;

public class Admin {
    private final String username;
    private final String passwordHash;

    public Admin(String username, String passwordHash) {
        this.username = Person.requireText(username, "Username");
        this.passwordHash = Person.requireText(passwordHash, "Password hash");
    }

    public String getUsername() { return username; }
    public boolean verifyPassword(char[] candidate, PasswordHasher passwordHasher) { return passwordHasher.verify(candidate, passwordHash); }
}
