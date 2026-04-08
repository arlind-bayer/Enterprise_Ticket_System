package ticketsystem;

public enum RequestStatus {
    NEW,
    ASSIGNED,
    IN_PROGRESS,
    WAITING_FOR_INFORMATION,
    RESOLVED,
    CLOSED;

    public boolean canTransitionTo(RequestStatus next) {
        if (next == null) {
            return false;
        }

        if (this == next) {
            return true;
        }

        switch (this) {
            case NEW:
                return next == ASSIGNED;
            case ASSIGNED:
                return next == IN_PROGRESS || next == WAITING_FOR_INFORMATION || next == RESOLVED;
            case IN_PROGRESS:
                return next == WAITING_FOR_INFORMATION || next == RESOLVED;
            case WAITING_FOR_INFORMATION:
                return next == IN_PROGRESS || next == RESOLVED;
            case RESOLVED:
                return next == CLOSED || next == IN_PROGRESS;
            case CLOSED:
                return false;
            default:
                return false;
        }
    }
}
