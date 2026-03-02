package model;

import java.io.Serializable;

public class UsedPart implements Serializable {
    private long id;
    private long appointmentId;
    private Long partId;
    private String partName;
    private int quantity;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(long appointmentId) { this.appointmentId = appointmentId; }

    public Long getPartId() { return partId; }
    public void setPartId(Long partId) { this.partId = partId; }

    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}