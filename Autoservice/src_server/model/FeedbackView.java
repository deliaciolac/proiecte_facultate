package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FeedbackView implements Serializable {
    private long appointmentId;
    private long clientId;
    private String clientUsername;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(long appointmentId) { this.appointmentId = appointmentId; }

    public long getClientId() { return clientId; }
    public void setClientId(long clientId) { this.clientId = clientId; }

    public String getClientUsername() { return clientUsername; }
    public void setClientUsername(String clientUsername) { this.clientUsername = clientUsername; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}