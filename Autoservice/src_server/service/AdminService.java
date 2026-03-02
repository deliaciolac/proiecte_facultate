package service;

import model.User;
import model.UserRole;
import repository.UserRepository;

import java.util.List;

public class AdminService {

    private final UserRepository userRepo;

    public AdminService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    // LISTARE
    public List<User> listUsers() {
        try {
            return userRepo.listAll();
        } catch (Exception e) {
            throw new ServiceException("Eroare la listarea utilizatorilor.");
        }
    }

    // ADAUGARE MECANIC
    public User addMechanic(String username, String password) {
        try {
            if (userRepo.findByUsername(username) != null) {
                throw new ServiceException("Username deja existent.");
            }
            return userRepo.insert(username, password, UserRole.MECHANIC);
        } catch (Exception e) {
            throw new ServiceException("Eroare la adaugarea mecanicului.");
        }
    }

    // EDITARE MECANIC (username si parola)
    public void editMechanic(long id, String newUsername, String newPassword) {
        try {
            User u = userRepo.findById(id);
            if (u == null) {
                throw new ServiceException("Mecanic inexistent.");
            }
            if (u.getRole() != UserRole.MECHANIC) {
                throw new ServiceException("Utilizatorul nu este mecanic.");
            }

            if (newUsername != null && !newUsername.isBlank()) {
                userRepo.updateUsername(id, newUsername);
            }

            if (newPassword != null && !newPassword.isBlank()) {
                userRepo.updatePassword(id, newPassword);
            }
        } catch (Exception e) {
            throw new ServiceException("Eroare la editarea mecanicului.");
        }
    }

    // STERGERE MECANIC
    public void removeMechanic(long id) {
        try {
            User u = userRepo.findById(id);
            if (u == null) {
                throw new ServiceException("Mecanic inexistent.");
            }
            if (u.getRole() != UserRole.MECHANIC) {
                throw new ServiceException("Utilizatorul nu este mecanic.");
            }
            userRepo.delete(id);
        } catch (Exception e) {
            throw new ServiceException("Eroare la stergerea mecanicului.");
        }
    }

    // MODIFICARE CONT CLIENT - CONT MECANIC
    public void promoteClientToMechanic(long id) {
        try {
            userRepo.updateRole(id, UserRole.MECHANIC);
        } catch (Exception e) {
            throw new ServiceException("Eroare la promovarea clientului.");
        }
    }

    //MODIFICARE CONT CLIENT - CONT MANAGER
    public void promoteClientToManager(long id) {
        try {
            userRepo.updateRole(id, UserRole.MANAGER);
        } catch (Exception e) {
            throw new ServiceException("Eroare la promovarea clientului.");
        }
    }
}
