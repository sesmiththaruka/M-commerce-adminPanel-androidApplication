package lk.jiat.xpectadmin.dto;

public class EventStatusDTO {
    private String eventUniqueId;
    private String status;

    public EventStatusDTO() {
    }

    public EventStatusDTO(String eventUniqueId, String status) {
        this.eventUniqueId = eventUniqueId;
        this.status = status;
    }

    public String getEventUniqueId() {
        return eventUniqueId;
    }

    public void setEventUniqueId(String eventUniqueId) {
        this.eventUniqueId = eventUniqueId;
    }

    public String isStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
