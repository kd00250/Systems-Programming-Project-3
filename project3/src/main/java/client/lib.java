package client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class lib {
    
    public static String encode_to_base64(String message) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(message));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }
}