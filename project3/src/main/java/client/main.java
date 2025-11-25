package client;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class main {
    private static final int SERVER_PORT = 5000; 
    private static final String SERVER_IP = "127.0.0.1"; 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        File dataDir = new File("data");
        File[] branchDir = dataDir.listFiles(file ->
                file.isDirectory() && !file.getName().equals("branch_weekly_sales.txt"));
        
        String branchCode = "";
        while (true) {
            System.out.print("Enter the  branch code that you would like to send (branch code must be exactly how it is spelled in the weekly sales report): ");
            branchCode = scanner.nextLine();

            boolean match = false;

            for (File currentFolder : branchDir) {
                 if (branchCode.equals(currentFolder.getName())) {
                    match = true;
                    break;
                }
            }

            if (match) {
                break;
            } else {
                System.out.println("Branch code not found. Please try again.");
            }
        }
        
        scanner.close();

        start_data_transfer(branchCode);

    }

    public static void start_data_transfer(String branchCode) {
        try {
        File branchFolder = new File("data/" + branchCode);

        File[] files = branchFolder.listFiles(file -> file.isFile() && file.getName().equals("branch_weekly_sales.txt"));
        File inputFile = files[0];
        String conversion = lib.encode_to_base64(inputFile.getPath());

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
        
                System.out.println("Connected to server");
                log("Connected to server");

                out.println("bcode~" +branchCode);
                String response = in.readLine();
                if (!"OK".equals(response)) {
                    throw new Exception("Server didn't recognize branch code");
                }

                out.println("~" + conversion + "~");

                response = in.readLine();
                if ("OK".equals(response)) {
                    log("OK");
                    System.out.println("File transfer was a success.");
                    log("File transfer was a success.");
                }
            }
    } catch (Exception ex) {
        System.out.println(ex.getMessage());
        log("Error: " + ex.getMessage());
    }
}

    public static void log(String text) {
        try (FileWriter logWriter = new FileWriter("log.txt", true)) {
            logWriter.write(text + "\n");
        } catch (IOException e) {
            System.err.println("Logging failed: " + e.getMessage());
        }
    }
}