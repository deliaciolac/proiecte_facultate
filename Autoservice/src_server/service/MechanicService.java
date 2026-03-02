package service;

import model.*;
import repository.AppointmentRepository;
import repository.PartRepository;
import repository.UsedPartRepository;

import java.util.List;

public class MechanicService {
    private final AppointmentRepository appointments;
    private final UsedPartRepository usedParts;
    private final PartRepository parts;

    public MechanicService(AppointmentRepository appointments, UsedPartRepository usedParts, PartRepository parts) {
        this.appointments = appointments;
        this.usedParts = usedParts;
        this.parts = parts;
    }

    public List<Appointment> listAssigned(long mechanicId) {
        try {
            return appointments.listAssignedToMechanic(mechanicId);
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public List<Part> listParts() {
        try {
            return parts.listAll();
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }

    public Appointment updateStatus(long mechanicId, long appointmentId, WorkStatus status) {
        try {
            Appointment a = appointments.findById(appointmentId);
            if (a == null) throw new ServiceException("Programare inexistenta.");
            if (a.getMechanicId() == null || a.getMechanicId() != mechanicId) throw new ServiceException("Nu e programarea ta.");

            // pastram durata/notes existente
            appointments.updateMechanicWork(appointmentId, status, a.getDurationMinutes(), a.getMechanicNotes());
            a.setStatus(status);
            return a;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }


    public Appointment addPartsAndDuration(long mechanicId, long appointmentId, int durationMinutes, String notes, List<UsedPart> partsUsed) {
        try {
            Appointment a = appointments.findById(appointmentId);
            if (a == null) throw new ServiceException("Programare inexistenta.");
            if (a.getMechanicId() == null || a.getMechanicId() != mechanicId) throw new ServiceException("Nu e programarea ta.");
            if (a.getStatus() == WorkStatus.COMPLETED) {
                throw new ServiceException("Lucrarea este deja finalizata. Nu mai poti adauga piese/durata.");
            }


            if (partsUsed != null) {
                for (UsedPart up : partsUsed) {
                    up.setAppointmentId(appointmentId);

                    if (up.getQuantity() <= 0) throw new ServiceException("Cantitate invalida pentru piesa: " + up.getPartName());

                    if (up.getPartId() != null) {
                        int updated = parts.decreaseStock(up.getPartId(), up.getQuantity());
                        if (updated == 0) throw new ServiceException("Stoc insuficient pentru partId=" + up.getPartId());
                    }
                    usedParts.insert(up);
                }
            }

            String newNotes = notes == null ? "" : notes;
            appointments.updateMechanicWork(appointmentId, a.getStatus(), durationMinutes, newNotes);

            a.setDurationMinutes(durationMinutes);
            a.setMechanicNotes(newNotes);
            return a;

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Eroare DB: " + e.getMessage());
        }
    }
}