package matchHistorian;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class TestServer extends WebSocketServer {

	public TestServer(int port) throws UnknownHostException {
		super(new InetSocketAddress(port));
	}
	
	void print(WebSocket socket, String message) {
		System.out.println("[" + socket.getRemoteSocketAddress().getAddress().getHostAddress() + "] " + message);
	}

	@Override
	public void onOpen(WebSocket socket, ClientHandshake handshake) {
		print(socket, "Connected");
	}

	@Override
	public void onClose(WebSocket socket, int code, String reason, boolean remote) {
		print(socket, "Disconnected");
	}

	@Override
	public void onMessage(WebSocket socket, String message) {
		print(socket, "Message: " + message);
	}

	public static void runTest() {
		try {
			final int port = 1031;
			TestServer server = new TestServer(port);
			server.start();
			System.out.println("Running server on port " + port);
		}
		catch(Exception exception) {
			exception.printStackTrace();
		}
	}
	
	@Override
	public void onError(WebSocket socket, Exception exception) {
		exception.printStackTrace();
	}
}
