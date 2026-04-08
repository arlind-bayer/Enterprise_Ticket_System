package ticketsystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class RequestService {
    private final DataStore dataStore;
    private final List<ServiceRequest> requests;
    private int nextId;

    public RequestService(DataStore dataStore) {
        this.dataStore = dataStore;
        this.requests = dataStore.loadRequests();
        this.nextId = requests.stream().map(ServiceRequest::getId).max(Integer::compareTo).orElse(0) + 1;
    }

    public ServiceRequest createRequest(
            String requester,
            String title,
            String description,
            RequestCategory category,
            Priority priority
    ) {
        requireText(requester, "Requester");
        requireText(title, "Title");
        requireText(description, "Description");

        ServiceRequest request = new ServiceRequest(
                nextId++,
                title.trim(),
                description.trim(),
                requester.trim(),
                category,
                priority
        );
        request.addComment(requester, "Request created");

        requests.add(request);
        save();
        return request;
    }

    public List<ServiceRequest> getVisibleRequests(Role role, String username) {
        List<ServiceRequest> visible = new ArrayList<>();

        for (ServiceRequest request : requests) {
            if (isVisibleToRole(request, role, username)) {
                visible.add(request);
            }
        }

        visible.sort(Comparator.comparing(ServiceRequest::getId));
        return visible;
    }

    public List<ServiceRequest> searchVisibleRequests(Role role, String username, String query) {
        requireText(query, "Search query");

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        List<ServiceRequest> results = new ArrayList<>();

        for (ServiceRequest request : getVisibleRequests(role, username)) {
            if (request.matchesQuery(normalizedQuery)) {
                results.add(request);
            }
        }

        return results;
    }

    public ServiceRequest getVisibleRequestById(int requestId, Role role, String username) {
        ServiceRequest request = findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request with ID " + requestId + " was not found."));

        if (!isVisibleToRole(request, role, username)) {
            throw new IllegalArgumentException("You do not have access to this request.");
        }

        return request;
    }

    public void assignRequest(int requestId, String agentName, String actorName, Role actorRole) {
        if (actorRole == Role.EMPLOYEE) {
            throw new IllegalArgumentException("Employees cannot assign requests.");
        }
        requireText(agentName, "Agent name");

        ServiceRequest request = findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request with ID " + requestId + " was not found."));

        if (request.getStatus() == RequestStatus.CLOSED) {
            throw new IllegalArgumentException("Closed requests cannot be assigned.");
        }

        request.assignAgent(agentName.trim(), actorName);
        if (request.getStatus() == RequestStatus.NEW) {
            request.setStatus(RequestStatus.ASSIGNED, actorName);
        }

        save();
    }

    public void updateStatus(int requestId, RequestStatus nextStatus, String actorName, Role actorRole) {
        ServiceRequest request = findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request with ID " + requestId + " was not found."));

        if (actorRole == Role.EMPLOYEE) {
            updateStatusAsEmployee(request, nextStatus, actorName);
            save();
            return;
        }

        RequestStatus current = request.getStatus();
        if (!current.canTransitionTo(nextStatus)) {
            throw new IllegalArgumentException("Invalid transition: " + current + " -> " + nextStatus);
        }

        if (actorRole == Role.SERVICE_AGENT) {
            String assigned = request.getAssignedAgent();
            if (assigned == null || !assigned.equalsIgnoreCase(actorName)) {
                throw new IllegalArgumentException("Service agent can only update requests assigned to themselves.");
            }
        }

        request.setStatus(nextStatus, actorName);
        save();
    }

    public void addComment(int requestId, String actorName, Role actorRole, String comment) {
        requireText(comment, "Comment");

        ServiceRequest request = findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request with ID " + requestId + " was not found."));

        if (actorRole == Role.EMPLOYEE && !request.getRequester().equalsIgnoreCase(actorName)) {
            throw new IllegalArgumentException("Employees can only comment on their own requests.");
        }

        request.addComment(actorName, comment.trim());
        save();
    }

    public Map<RequestStatus, Integer> getStatusSummary() {
        Map<RequestStatus, Integer> summary = new EnumMap<>(RequestStatus.class);
        for (RequestStatus status : RequestStatus.values()) {
            summary.put(status, 0);
        }

        for (ServiceRequest request : requests) {
            summary.put(request.getStatus(), summary.get(request.getStatus()) + 1);
        }

        return summary;
    }

    public Map<String, Integer> getAgentWorkloadSummary() {
        Map<String, Integer> workload = new LinkedHashMap<>();

        for (ServiceRequest request : requests) {
            String agent = request.getAssignedAgent();
            if (agent == null || request.getStatus() == RequestStatus.CLOSED) {
                continue;
            }
            workload.put(agent, workload.getOrDefault(agent, 0) + 1);
        }

        return workload;
    }

    public void seedDemoDataIfEmpty() {
        if (!requests.isEmpty()) {
            return;
        }

        ServiceRequest first = createRequest(
                "Alice",
                "Laptop cannot connect to VPN",
                "I get timeout errors when trying to connect from home.",
                RequestCategory.IT_SUPPORT,
                Priority.HIGH
        );

        ServiceRequest second = createRequest(
                "Bob",
                "Need employment certificate",
                "Please prepare a signed employment certificate.",
                RequestCategory.HR_DOCUMENT,
                Priority.MEDIUM
        );

        ServiceRequest third = createRequest(
                "Carla",
                "Meeting room projector is broken",
                "Projector in room C12 does not turn on.",
                RequestCategory.FACILITY,
                Priority.HIGH
        );

        assignRequest(first.getId(), "AgentJohn", "ManagerMaria", Role.MANAGER);
        assignRequest(third.getId(), "AgentJohn", "ManagerMaria", Role.MANAGER);
        updateStatus(first.getId(), RequestStatus.IN_PROGRESS, "AgentJohn", Role.SERVICE_AGENT);
        updateStatus(first.getId(), RequestStatus.RESOLVED, "AgentJohn", Role.SERVICE_AGENT);

        assignRequest(second.getId(), "AgentNora", "ManagerMaria", Role.MANAGER);
        updateStatus(second.getId(), RequestStatus.IN_PROGRESS, "AgentNora", Role.SERVICE_AGENT);
    }

    private Optional<ServiceRequest> findById(int requestId) {
        return requests.stream().filter(r -> r.getId() == requestId).findFirst();
    }

    private boolean isVisibleToRole(ServiceRequest request, Role role, String username) {
        if (role == Role.MANAGER || role == Role.SERVICE_AGENT) {
            return true;
        }

        return request.getRequester().equalsIgnoreCase(username);
    }

    private void updateStatusAsEmployee(ServiceRequest request, RequestStatus nextStatus, String actorName) {
        if (!request.getRequester().equalsIgnoreCase(actorName)) {
            throw new IllegalArgumentException("Employees can only update their own requests.");
        }

        if (request.getStatus() == RequestStatus.RESOLVED && nextStatus == RequestStatus.CLOSED) {
            request.setStatus(nextStatus, actorName);
            return;
        }

        throw new IllegalArgumentException("Employees may only close a resolved request (RESOLVED -> CLOSED).");
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty.");
        }
    }

    private void save() {
        dataStore.saveRequests(requests);
    }
}
