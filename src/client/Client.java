package client;
import java.io.*;
import java.net.Socket;

public class Client {

    private static final int MAXLINE = 4096; // Kích thước tối đa của dòng nhận và gửi
    private static final int SERV_PORT = 3000; // Cổng server kết nối

    public static void main(String[] args) {
//        if (args.length != 1) {
//            System.err.println("Usage: java Client <Server IP>");
//            return;
//        }
//
//        String serverIP = args[0];
        String serverIP = "127.0.0.1";
        Socket socket = null;
        BufferedReader userInput = null;
        PrintWriter outToServer = null;
        BufferedReader inFromServer = null;

        try {
            // Tạo socket để kết nối tới server
            socket = new Socket(serverIP, SERV_PORT);
            System.out.println("Connected to the server. You can now send and receive messages.");

            // Tạo luồng đọc từ bàn phím (đầu vào người dùng)
            userInput = new BufferedReader(new InputStreamReader(System.in));

            // Tạo luồng gửi dữ liệu tới server
            outToServer = new PrintWriter(socket.getOutputStream(), true);

            // Tạo luồng nhận dữ liệu từ server
            inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String sendLine;
            char[] recvBuffer = new char[MAXLINE];
            int bytesRead;

            // Vòng lặp để gửi và nhận dữ liệu
            while (true) {
                // Kiểm tra nếu có dữ liệu từ bàn phím
                if (userInput.ready()) {
                    sendLine = userInput.readLine(); // Đọc dữ liệu từ người dùng
                    outToServer.println(sendLine); // Gửi tới server
                }

                // Kiểm tra nếu có dữ liệu từ server
                if (inFromServer.ready()) {
                    bytesRead = inFromServer.read(recvBuffer, 0, MAXLINE);
                    if (bytesRead > 0) {
                        String recvLine = new String(recvBuffer, 0, bytesRead);
                        System.out.println("Message from server: " + recvLine);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Đảm bảo đóng các tài nguyên
            try {
                if (socket != null) socket.close();
                if (userInput != null) userInput.close();
                if (outToServer != null) outToServer.close();
                if (inFromServer != null) inFromServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}