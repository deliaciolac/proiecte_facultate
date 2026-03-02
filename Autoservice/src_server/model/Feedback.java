package model;

import java.io.Serializable;

public class Feedback implements Serializable {
    private long id;
    private long appointmentId;
    private long clientId;
    private int rating;
    private String comment;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(long appointmentId) { this.appointmentId = appointmentId; }

    public long getClientId() { return clientId; }
    public void setClientId(long clientId) { this.clientId = clientId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}