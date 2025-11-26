package com.example;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implements a multithreaded TCP server
 */
public class Main {
    private static final int PORT = 5000;
    private static final String DATA_DIR = "data";
    private static final String LOG_FILE = "server_log.txt";

    /**
     * Creates the data directory and begins listening for client
     *
     */
    public static void main(String[] args) {
        File dataFolder = new File(DATA_DIR);
        if (!dataFolder.exists()) {
            boolean created = dataFolder.mkdir();
            log("Server: Created 'data' directory: " + created);
        }
        start_listening();
    }

    /**
     * Starts the ServerSocket and listens for client connections
     */
    public static void start_listening() {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            log("Server: Listening on port " + PORT + "...");
            while (true) {
                Socket clientSocket = serverSocket.accept();

                log("Server: New client connected: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            log("Server Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * logging
     * writes to both System.out and server_log.txt 
     */
    public static synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + message;

        System.out.println(logEntry);

        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logEntry);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Implements runnable to handle client communication in a separate thread
     */
    private static class ClientHandler implements Runnable {
        private final Socket socket;

        /**
         * Constructor
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * executes when thread starts
         * handshake file reception saving process
         */
        @Override
        public void run() {
            String clientIP = socket.getInetAddress().getHostAddress();
            String rawFileContent = null;
            String branchCode = null;

            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                //Reads the branch code
                String branchMsg = in.readLine();
                if (branchMsg != null) {
                    Main.log("Thread: Received branch message from " + clientIP + ": " + branchMsg);

                    if (branchMsg.startsWith("bcode~")) {
                        branchCode = branchMsg.split("~")[1];
                        Main.log("Thread: Parsed Branch Code: " + branchCode);

                        File branchDir = new File(DATA_DIR + File.separator + branchCode);
                        if (!branchDir.exists()) {
                            branchDir.mkdirs();
                        }
                    }

                    out.println("OK");
                }

                //Reads BASE64 content
                rawFileContent = in.readLine();
                Main.log("Thread: Received raw file content length: " + (rawFileContent != null ? rawFileContent.length() : "null"));

                out.println("OK");

                //Closes the connection
                socket.close();
                Main.log("Thread: Connection closed with " + clientIP);

            } catch (IOException e) {
                Main.log("Thread Error (" + clientIP + "): " + e.getMessage());
            }

            //processes the data
            if (rawFileContent != null && branchCode != null) {
                processAndSaveFile(branchCode, rawFileContent);
            }
        }

        /**
         *helper method to decode Base64 
         */
        private void processAndSaveFile(String branchCode, String rawData) {
            try {
                if (rawData.startsWith("~") && rawData.endsWith("~")) {
                    String cleanBase64 = rawData.substring(1, rawData.length() - 1);

                    String decodedText = Lib.decode_from_base64(cleanBase64); 
                    
                    Main.log("Thread: Decoded content successfully.");
                    Main.log("Thread: Content Preview: " + (decodedText.length() > 50 ? decodedText.substring(0, 50) + "..." : decodedText));

                    File outputFile = new File(DATA_DIR + File.separator + branchCode, "branch_weekly_sales.txt");
                    try (PrintWriter writer = new PrintWriter(outputFile)) {
                        writer.print(decodedText);
                    }

                    Main.log("Thread: File saved successfully at: " + outputFile.getPath());
                } else {
                    Main.log("Thread Error: Received data was not wrapped in '~'. Cannot process.");
                }
            } catch (Exception e) {
                Main.log("Thread Error processing file: " + e.getMessage());
            }
        }
    }
}