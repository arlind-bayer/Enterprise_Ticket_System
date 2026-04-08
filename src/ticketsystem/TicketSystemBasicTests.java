package ticketsystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TicketSystemBasicTests {
    public static void main(String[] args) throws Exception {
        Path testFile = Paths.get("data", "test-requests.ser");
        Files.deleteIfExists(testFile);

        try {
            DataStore dataStore = new DataStore(testFile);
            RequestService service = new RequestService(dataStore);

            testCreateAndSearch(service);
            testVisibilityRules(service);
            testStatusRules(service);

            System.out.println("All basic tests passed.");
        } finally {
            Files.deleteIfExists(testFile);
        }
    }

    private static void testCreateAndSearch(RequestService service) {
        ServiceRequest req = service.createRequest(
                "TestEmployee",
                "Printer not working",
                "Printer on floor 2 is offline",
                RequestCategory.IT_SUPPORT,
                Priority.MEDIUM
        );

        if (req.getId() <= 0) {
            throw new RuntimeException("Request ID should be generated.");
        }

        List<ServiceRequest> results = service.searchVisibleRequests(Role.EMPLOYEE, "TestEmployee", "printer");
        if (results.isEmpty()) {
            throw new RuntimeException("Expected to find request by search.");
        }
    }

    private static void testVisibilityRules(RequestService service) {
        ServiceRequest ownRequest = service.createRequest(
                "TestEmployee",
                "Email access request",
                "Need mailbox access for shared inbox",
                RequestCategory.ACCESS_PERMISSION,
                Priority.MEDIUM
        );

        ServiceRequest otherRequest = service.createRequest(
                "AnotherEmployee",
                "Need ID card replacement",
                "Card was lost",
                RequestCategory.ACCESS_PERMISSION,
                Priority.HIGH
        );

        List<ServiceRequest> own = service.getVisibleRequests(Role.EMPLOYEE, "TestEmployee");
        List<ServiceRequest> other = service.getVisibleRequests(Role.EMPLOYEE, "AnotherEmployee");

        boolean ownCanSeeOwnRequest = own.stream().anyMatch(r -> r.getId() == ownRequest.getId());
        boolean ownCanSeeOtherRequest = own.stream().anyMatch(r -> r.getId() == otherRequest.getId());
        boolean otherCanSeeOwnRequest = other.stream().anyMatch(r -> r.getId() == ownRequest.getId());

        if (!ownCanSeeOwnRequest || ownCanSeeOtherRequest || otherCanSeeOwnRequest) {
            throw new RuntimeException("Employee visibility rules are not enforced correctly.");
        }

        List<ServiceRequest> managerView = service.getVisibleRequests(Role.MANAGER, "Boss");
        if (managerView.size() < 3) {
            throw new RuntimeException("Manager should see all requests.");
        }
    }

    private static void testStatusRules(RequestService service) {
        ServiceRequest req = service.createRequest(
                "FlowEmployee",
                "VPN access",
                "Need VPN for remote work",
                RequestCategory.ACCESS_PERMISSION,
                Priority.HIGH
        );

        int id = req.getId();
        service.assignRequest(id, "AgentOne", "ManagerOne", Role.MANAGER);
        service.updateStatus(id, RequestStatus.IN_PROGRESS, "AgentOne", Role.SERVICE_AGENT);
        service.updateStatus(id, RequestStatus.RESOLVED, "AgentOne", Role.SERVICE_AGENT);

        boolean blocked = false;
        try {
            service.updateStatus(id, RequestStatus.CLOSED, "OtherEmployee", Role.EMPLOYEE);
        } catch (IllegalArgumentException ex) {
            blocked = true;
        }

        if (!blocked) {
            throw new RuntimeException("Employee should not close someone else's request.");
        }

        service.updateStatus(id, RequestStatus.CLOSED, "FlowEmployee", Role.EMPLOYEE);
    }
}
