package ticketsystem;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLogEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final LocalDateTime timestamp;
    private final String author;
    private final String message;

    public ActivityLogEntry(LocalDateTime timestamp, String author, String message) {
        this.timestamp = timestamp;
        this.author = author;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "[" + timestamp.format(FORMATTER) + "] " + author + ": " + message;
    }
}
