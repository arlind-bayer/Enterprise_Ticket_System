package ticketsystem;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TicketSystemApp {
    private final RequestService requestService;
    private final Scanner scanner;

    public TicketSystemApp() {
        this.requestService = new RequestService(new DataStore());
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        TicketSystemApp app = new TicketSystemApp();
        app.start();
    }

    public void start() {
        requestService.seedDemoDataIfEmpty();

        System.out.println("===============================================");
        System.out.println(" Enterprise Service Request Management System ");
        System.out.println("===============================================");

        boolean running = true;
        while (running) {
            Role role = selectRole();
            if (role == null) {
                running = false;
                continue;
            }

            String username = readNonEmptyLine("Enter your name: ");
            runRoleMenu(role, username);
        }

        System.out.println("Goodbye.");
    }

    private Role selectRole() {
        while (true) {
            System.out.println("\nSelect role:");
            System.out.println("1. Employee");
            System.out.println("2. Service Agent");
            System.out.println("3. Manager");
            System.out.println("0. Exit");

            int choice = readInt("Choose option: ");
            switch (choice) {
                case 1:
                    return Role.EMPLOYEE;
                case 2:
                    return Role.SERVICE_AGENT;
                case 3:
                    return Role.MANAGER;
                case 0:
                    return null;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private void runRoleMenu(Role role, String username) {
        System.out.println("\nLogged in as " + role + " (" + username + ")");

        boolean loggedIn = true;
        while (loggedIn) {
            printRoleMenu(role);
            int choice = readInt("Choose option: ");

            try {
                switch (role) {
                    case EMPLOYEE:
                        loggedIn = handleEmployeeMenuChoice(choice, username);
                        break;
                    case SERVICE_AGENT:
                        loggedIn = handleAgentMenuChoice(choice, username);
                        break;
                    case MANAGER:
                        loggedIn = handleManagerMenuChoice(choice, username);
                        break;
                    default:
                        loggedIn = false;
                }
            } catch (IllegalArgumentException ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (RuntimeException ex) {
                System.out.println("Unexpected error: " + ex.getMessage());
            }
        }
    }

    private void printRoleMenu(Role role) {
        System.out.println("\n------------- MENU -------------");

        if (role == Role.EMPLOYEE) {
            System.out.println("1. Create request");
            System.out.println("2. View my requests");
            System.out.println("3. Search my requests");
            System.out.println("4. View request details");
            System.out.println("5. Add comment to my request");
            System.out.println("6. Close my resolved request");
            System.out.println("0. Logout");
            return;
        }

        if (role == Role.SERVICE_AGENT) {
            System.out.println("1. View all requests");
            System.out.println("2. Search requests");
            System.out.println("3. View request details");
            System.out.println("4. Assign request to myself");
            System.out.println("5. Update status of assigned request");
            System.out.println("6. Add comment");
            System.out.println("0. Logout");
            return;
        }

        System.out.println("1. View all requests");
        System.out.println("2. Search requests");
        System.out.println("3. View request details");
        System.out.println("4. Assign request to an agent");
        System.out.println("5. Update status");
        System.out.println("6. Add comment");
        System.out.println("7. View dashboard summary");
        System.out.println("0. Logout");
    }

    private boolean handleEmployeeMenuChoice(int choice, String username) {
        switch (choice) {
            case 1:
                createRequest(username);
                return true;
            case 2:
                listRequests(Role.EMPLOYEE, username);
                return true;
            case 3:
                searchRequests(Role.EMPLOYEE, username);
                return true;
            case 4:
                showRequestDetails(Role.EMPLOYEE, username);
                return true;
            case 5:
                addComment(Role.EMPLOYEE, username);
                return true;
            case 6:
                closeResolvedRequest(username);
                return true;
            case 0:
                return false;
            default:
                System.out.println("Invalid option. Please try again.");
                return true;
        }
    }

    private boolean handleAgentMenuChoice(int choice, String username) {
        switch (choice) {
            case 1:
                listRequests(Role.SERVICE_AGENT, username);
                return true;
            case 2:
                searchRequests(Role.SERVICE_AGENT, username);
                return true;
            case 3:
                showRequestDetails(Role.SERVICE_AGENT, username);
                return true;
            case 4:
                assignRequestToSelf(username);
                return true;
            case 5:
                updateRequestStatus(Role.SERVICE_AGENT, username);
                return true;
            case 6:
                addComment(Role.SERVICE_AGENT, username);
                return true;
            case 0:
                return false;
            default:
                System.out.println("Invalid option. Please try again.");
                return true;
        }
    }

    private boolean handleManagerMenuChoice(int choice, String username) {
        switch (choice) {
            case 1:
                listRequests(Role.MANAGER, username);
                return true;
            case 2:
                searchRequests(Role.MANAGER, username);
                return true;
            case 3:
                showRequestDetails(Role.MANAGER, username);
                return true;
            case 4:
                assignRequestToAgent(username);
                return true;
            case 5:
                updateRequestStatus(Role.MANAGER, username);
                return true;
            case 6:
                addComment(Role.MANAGER, username);
                return true;
            case 7:
                showDashboardSummary();
                return true;
            case 0:
                return false;
            default:
                System.out.println("Invalid option. Please try again.");
                return true;
        }
    }

    private void createRequest(String username) {
        String title = readNonEmptyLine("Title: ");
        String description = readNonEmptyLine("Description: ");
        RequestCategory category = chooseCategory();
        Priority priority = choosePriority();

        ServiceRequest request = requestService.createRequest(username, title, description, category, priority);
        System.out.println("Request created with ID: " + request.getId());
    }

    private void listRequests(Role role, String username) {
        List<ServiceRequest> requests = requestService.getVisibleRequests(role, username);
        printRequestList(requests);
    }

    private void searchRequests(Role role, String username) {
        String query = readNonEmptyLine("Search query: ");
        List<ServiceRequest> results = requestService.searchVisibleRequests(role, username, query);
        printRequestList(results);
    }

    private void showRequestDetails(Role role, String username) {
        int requestId = readInt("Request ID: ");
        ServiceRequest request = requestService.getVisibleRequestById(requestId, role, username);
        System.out.println("\n" + request.toDetailText());
    }

    private void addComment(Role role, String username) {
        int requestId = readInt("Request ID: ");
        String comment = readNonEmptyLine("Comment: ");
        requestService.addComment(requestId, username, role, comment);
        System.out.println("Comment added.");
    }

    private void closeResolvedRequest(String username) {
        int requestId = readInt("Request ID to close: ");
        requestService.updateStatus(requestId, RequestStatus.CLOSED, username, Role.EMPLOYEE);
        System.out.println("Request closed.");
    }

    private void assignRequestToSelf(String username) {
        int requestId = readInt("Request ID to assign to yourself: ");
        requestService.assignRequest(requestId, username, username, Role.SERVICE_AGENT);
        System.out.println("Request assigned to " + username + ".");
    }

    private void assignRequestToAgent(String managerName) {
        int requestId = readInt("Request ID: ");
        String agentName = readNonEmptyLine("Agent name: ");
        requestService.assignRequest(requestId, agentName, managerName, Role.MANAGER);
        System.out.println("Request assigned to " + agentName + ".");
    }

    private void updateRequestStatus(Role role, String username) {
        int requestId = readInt("Request ID: ");
        RequestStatus nextStatus = chooseStatus();
        requestService.updateStatus(requestId, nextStatus, username, role);
        System.out.println("Status updated to " + nextStatus + ".");
    }

    private void showDashboardSummary() {
        System.out.println("\nStatus distribution:");
        Map<RequestStatus, Integer> statusSummary = requestService.getStatusSummary();
        for (Map.Entry<RequestStatus, Integer> entry : statusSummary.entrySet()) {
            System.out.println("- " + entry.getKey() + ": " + entry.getValue());
        }

        System.out.println("\nAgent workload (active assignments):");
        Map<String, Integer> workload = requestService.getAgentWorkloadSummary();
        if (workload.isEmpty()) {
            System.out.println("- No assigned requests.");
            return;
        }

        for (Map.Entry<String, Integer> entry : workload.entrySet()) {
            System.out.println("- " + entry.getKey() + ": " + entry.getValue());
        }
    }

    private void printRequestList(List<ServiceRequest> requests) {
        if (requests.isEmpty()) {
            System.out.println("No requests found.");
            return;
        }

        System.out.println("\nRequests:");
        for (ServiceRequest request : requests) {
            System.out.println(request.toSummaryLine());
        }
    }

    private RequestCategory chooseCategory() {
        RequestCategory[] values = RequestCategory.values();
        while (true) {
            System.out.println("Select category:");
            for (int i = 0; i < values.length; i++) {
                System.out.println((i + 1) + ". " + values[i]);
            }
            int choice = readInt("Category option: ");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            System.out.println("Invalid option. Please try again.");
        }
    }

    private Priority choosePriority() {
        Priority[] values = Priority.values();
        while (true) {
            System.out.println("Select priority:");
            for (int i = 0; i < values.length; i++) {
                System.out.println((i + 1) + ". " + values[i]);
            }
            int choice = readInt("Priority option: ");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            System.out.println("Invalid option. Please try again.");
        }
    }

    private RequestStatus chooseStatus() {
        RequestStatus[] values = RequestStatus.values();
        while (true) {
            System.out.println("Select status:");
            for (int i = 0; i < values.length; i++) {
                System.out.println((i + 1) + ". " + values[i]);
            }
            int choice = readInt("Status option: ");
            if (choice >= 1 && choice <= values.length) {
                return values[choice - 1];
            }
            System.out.println("Invalid option. Please try again.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
            System.out.println("Input must not be empty.");
        }
    }
}
