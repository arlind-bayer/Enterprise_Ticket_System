package ticketsystem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private final Path filePath;

    public DataStore() {
        this(Paths.get("data", "requests.ser"));
    }

    public DataStore(Path filePath) {
        this.filePath = filePath;
    }

    @SuppressWarnings("unchecked")
    public List<ServiceRequest> loadRequests() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            return (List<ServiceRequest>) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Warning: Could not read existing data. Starting with empty list.");
            return new ArrayList<>();
        }
    }

    public void saveRequests(List<ServiceRequest> requests) {
        try {
            Path parent = filePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
                output.writeObject(requests);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not save requests: " + e.getMessage(), e);
        }
    }
}
