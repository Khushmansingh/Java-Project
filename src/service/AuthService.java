package service;

import dao.AdminDAO;
import exception.AuthenticationException;
import model.Admin;
import util.PasswordHasher;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final AdminDAO adminDAO;
    private final PasswordHasher passwordHasher;
    private int failedAttempts;

    public AuthService(AdminDAO adminDAO, PasswordHasher passwordHasher) { this.adminDAO = adminDAO; this.passwordHasher = passwordHasher; }

    public boolean login(String username, char[] password) throws SQLException {
        if (failedAttempts >= 3) throw new AuthenticationException("Login locked after three failed attempts.");
        Optional<Admin> admin = adminDAO.findByUsername(username);
        boolean accepted = admin.isPresent() && admin.get().verifyPassword(password, passwordHasher);
        if (!accepted) failedAttempts++;
        return accepted;
    }

    public int getRemainingAttempts() { return 3 - failedAttempts; }
}
