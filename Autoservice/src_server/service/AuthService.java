package service;

import model.User;
import model.UserRole;
import repository.UserRepository;
import security.PasswordHasher;

public class AuthService {
    private final UserRepository users;

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public User register(String username, String password, UserRole role) {
        if (role != UserRole.CLIENT && role != UserRole.MECHANIC) {
            throw new ServiceException("Doar CLIENT si MECHANIC isi pot crea cont.");
        }
        if (username == null || username.isBlank()) throw new ServiceException("Username invalid.");
        if (password == null || password.length() < 4) throw new ServiceException("Parola prea scurta (min 4).");

        try {
            if (users.existsUsername(username)) throw new ServiceException("Username deja folosit.");
            return users.insert(username.trim(), PasswordHasher.sha256(password), role);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public User login(String username, String password) {
        try {
            User u = users.findByUsername(username);
            if (u == null) throw new ServiceException("User inexistent.");
            if (!u.getPasswordHash().equals(PasswordHasher.sha256(password))) throw new ServiceException("Parola gresita.");
            return u;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }
}