package ticketsystem;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final int id;
    private final String title;
    private final String description;
    private final String requester;
    private final RequestCategory category;
    private final Priority priority;
    private final LocalDateTime createdAt;

    private RequestStatus status;
    private String assignedAgent;
    private LocalDateTime updatedAt;
    private final List<ActivityLogEntry> activityLog;

    public ServiceRequest(
            int id,
            String title,
            String description,
            String requester,
            RequestCategory category,
            Priority priority
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.requester = requester;
        this.category = category;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.status = RequestStatus.NEW;
        this.activityLog = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRequester() {
        return requester;
    }

    public RequestCategory getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getAssignedAgent() {
        return assignedAgent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<ActivityLogEntry> getActivityLog() {
        return Collections.unmodifiableList(activityLog);
    }

    public void assignAgent(String agentName, String actor) {
        this.assignedAgent = agentName;
        this.updatedAt = LocalDateTime.now();
        addLog(actor, "Assigned to " + agentName);
    }

    public void setStatus(RequestStatus status, String actor) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        addLog(actor, "Status changed to " + status);
    }

    public void addComment(String actor, String comment) {
        this.updatedAt = LocalDateTime.now();
        addLog(actor, "Comment: " + comment);
    }

    public boolean matchesQuery(String query) {
        String normalized = query.toLowerCase();
        return String.valueOf(id).contains(normalized)
                || title.toLowerCase().contains(normalized)
                || description.toLowerCase().contains(normalized)
                || requester.toLowerCase().contains(normalized)
                || category.name().toLowerCase().contains(normalized)
                || priority.name().toLowerCase().contains(normalized)
                || status.name().toLowerCase().contains(normalized)
                || (assignedAgent != null && assignedAgent.toLowerCase().contains(normalized));
    }

    public String toSummaryLine() {
        return String.format(
                "#%d | %s | %s | %s | requester=%s | agent=%s",
                id,
                status,
                category,
                priority,
                requester,
                assignedAgent == null ? "-" : assignedAgent
        );
    }

    public String toDetailText() {
        StringBuilder builder = new StringBuilder();
        builder.append("Request #").append(id).append("\n");
        builder.append("Title: ").append(title).append("\n");
        builder.append("Description: ").append(description).append("\n");
        builder.append("Requester: ").append(requester).append("\n");
        builder.append("Category: ").append(category).append("\n");
        builder.append("Priority: ").append(priority).append("\n");
        builder.append("Status: ").append(status).append("\n");
        builder.append("Assigned Agent: ").append(assignedAgent == null ? "-" : assignedAgent).append("\n");
        builder.append("Created At: ").append(createdAt.format(FORMATTER)).append("\n");
        builder.append("Updated At: ").append(updatedAt.format(FORMATTER)).append("\n");
        builder.append("Activity Log:\n");

        if (activityLog.isEmpty()) {
            builder.append("  (No activity yet)\n");
        } else {
            for (ActivityLogEntry entry : activityLog) {
                builder.append("  - ").append(entry).append("\n");
            }
        }

        return builder.toString();
    }

    private void addLog(String actor, String message) {
        activityLog.add(new ActivityLogEntry(LocalDateTime.now(), actor, message));
    }
}
