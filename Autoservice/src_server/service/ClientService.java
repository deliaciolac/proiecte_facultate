package service;

import model.*;
import repository.AppointmentRepository;
import repository.FeedbackRepository;
import repository.PriceRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientService {
    private final AppointmentRepository appointments;
    private final PriceRepository prices;
    private final FeedbackRepository feedback;

    public ClientService(AppointmentRepository appointments, PriceRepository prices, FeedbackRepository feedback) {
        this.appointments = appointments;
        this.prices = prices;
        this.feedback = feedback;
    }

    public Appointment createAppointment(long clientId, ServiceType type, LocalDateTime dt, String vehicleInfo, String notes) {
        if (dt == null) throw new ServiceException("Data/ora invalida.");
        if (type == null) throw new ServiceException("Tip serviciu invalid.");

        try {
            double estimate = prices.getBasePrice(type);
            Appointment a = new Appointment();
            a.setClientId(clientId);
            a.setMechanicId(null);
            a.setServiceType(type);
            a.setScheduledAt(dt);
            a.setVehicleInfo(vehicleInfo == null ? "" : vehicleInfo);
            a.setClientNotes(notes == null ? "" : notes);
            a.setEstimatedCost(estimate);
            a.setStatus(WorkStatus.PENDING);
            a.setDurationMinutes(0);
            a.setMechanicNotes("");
            a.setCompletionNotified(false);
            return appointments.insert(a);
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public List<Appointment> listAppointments(long clientId) {
        try {
            return appointments.listByClient(clientId);
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public Appointment addNotes(long clientId, long appointmentId, String extra) {
        try {
            Appointment a = appointments.findById(appointmentId);
            if (a == null) throw new ServiceException("Programare inexistenta.");
            if (a.getClientId() != clientId) throw new ServiceException("Nu e programarea ta.");

            String merged = (a.getClientNotes() == null ? "" : a.getClientNotes());
            merged = (merged + "\n" + (extra == null ? "" : extra)).trim();

            appointments.updateClientNotes(appointmentId, merged);
            a.setClientNotes(merged);
            return a;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public List<String> getNotifications(long clientId) {
        try {
            List<Appointment> list = appointments.listByClient(clientId);
            List<String> out = new ArrayList<>();
            for (Appointment a : list) {
                if (a.getStatus() == WorkStatus.COMPLETED && !a.isCompletionNotified()) {
                    out.add("Programarea " + a.getId() + " este FINALIZATA.");
                    appointments.markCompletionNotified(a.getId());
                }
            }
            return out;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public Feedback submitFeedback(long clientId, long appointmentId, int rating, String comment) {
        if (rating < 1 || rating > 5) throw new ServiceException("Rating trebuie 1..5.");

        try {
            Appointment a = appointments.findById(appointmentId);
            if (a == null) throw new ServiceException("Programare inexistenta.");
            if (a.getClientId() != clientId) throw new ServiceException("Nu e programarea ta.");
            if (a.getStatus() != WorkStatus.COMPLETED) throw new ServiceException("Feedback doar dupa COMPLETED.");

            Feedback existing = feedback.findByAppointmentId(appointmentId);
            if (existing != null) throw new ServiceException("Feedback deja trimis pentru aceasta programare.");

            return feedback.insert(appointmentId, clientId, rating, comment == null ? "" : comment);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }
}