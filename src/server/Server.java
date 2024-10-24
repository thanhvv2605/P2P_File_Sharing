package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private static final int PORT = 3000;
    private static final int BUFFER_SIZE = 4096;

    public static void main(String[] args) {
        Selector selector;
        ServerSocketChannel serverChannel;
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        try {
            // Mở selector và ServerSocketChannel
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();

            // Cấu hình non-blocking cho ServerSocketChannel
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(PORT));

            // Đăng ký ServerSocketChannel với selector để lắng nghe kết nối mới
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server is running...waiting for connections on port " + PORT);

            while (true) {
                // Chờ các sự kiện từ selector
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    if (key.isAcceptable()) {
                        // Xử lý khi có kết nối mới
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = server.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("New client connected: " + clientChannel.getRemoteAddress());
                    } else if (key.isReadable()) {
                        // Xử lý khi có dữ liệu gửi đến từ khách hàng
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        int bytesRead = clientChannel.read(buffer);

                        if (bytesRead > 0) {
                            buffer.flip();
                            String message = new String(buffer.array(), 0, bytesRead);
                            System.out.println("Message received: " + message);

                            // Gửi lại cho tất cả các khách hàng khác
                            broadcastMessage(selector, clientChannel, message);
                            buffer.clear();
                        } else if (bytesRead == -1) {
                            // Ngắt kết nối nếu đọc không thành công
                            System.out.println("Client disconnected: " + clientChannel.getRemoteAddress());
                            clientChannel.close();
                        }
                    }

                    // Xóa key đã xử lý
                    keyIterator.remove();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hàm broadcast tin nhắn tới tất cả các clients khác
    private static void broadcastMessage(Selector selector, SocketChannel sender, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());

        // Duyệt qua tất cả các keys (các kết nối)
        for (SelectionKey key : selector.keys()) {
            if (key.channel() instanceof SocketChannel && key.isValid()) {
                SocketChannel clientChannel = (SocketChannel) key.channel();

                // Không gửi lại tin nhắn cho chính máy khách gửi đi
                if (clientChannel != sender) {
                    clientChannel.write(buffer);
                    buffer.rewind(); // Reset lại buffer để gửi cho client tiếp theo
                }
            }
        }
    }
}