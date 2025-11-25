package client;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.Base64;
import java.io.IOException;

public class testLib {
    @Test
    public void testEncodeToBase64() throws IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(Paths.get("data/ALBNM/branch_weekly_sales.txt"));
        String expected = Base64.getEncoder().encodeToString(bytes);
        String result = lib.encode_to_base64("data/ALBNM/branch_weekly_sales.txt");

        assertEquals(expected, result);
        System.out.println(result);
    }
}