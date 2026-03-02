package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Appointment implements Serializable {
    private long id;
    private long clientId;
    private Long mechanicId;
    private ServiceType serviceType;
    private LocalDateTime scheduledAt;

    private String vehicleInfo;
    private String clientNotes;

    private double estimatedCost;
    private WorkStatus status;

    private int durationMinutes;
    private String mechanicNotes;

    private boolean completionNotified;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClientId() { return clientId; }
    public void setClientId(long clientId) { this.clientId = clientId; }

    public Long getMechanicId() { return mechanicId; }
    public void setMechanicId(Long mechanicId) { this.mechanicId = mechanicId; }

    public ServiceType getServiceType() { return serviceType; }
    public void setServiceType(ServiceType serviceType) { this.serviceType = serviceType; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }

    public String getClientNotes() { return clientNotes; }
    public void setClientNotes(String clientNotes) { this.clientNotes = clientNotes; }

    public double getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(double estimatedCost) { this.estimatedCost = estimatedCost; }

    public WorkStatus getStatus() { return status; }
    public void setStatus(WorkStatus status) { this.status = status; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getMechanicNotes() { return mechanicNotes; }
    public void setMechanicNotes(String mechanicNotes) { this.mechanicNotes = mechanicNotes; }

    public boolean isCompletionNotified() { return completionNotified; }
    public void setCompletionNotified(boolean completionNotified) { this.completionNotified = completionNotified; }
}