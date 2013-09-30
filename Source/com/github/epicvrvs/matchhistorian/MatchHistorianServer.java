package com.github.epicvrvs.matchhistorian;

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

public class MatchHistorianServer extends WebSocketServer {
	private MatchHistorian historian;
	
	public MatchHistorianServer(MatchHistorian historian, int port) throws UnknownHostException {
		super(new InetSocketAddress(port));
		this.historian = historian;
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
	
	@Override
	public void onError(WebSocket socket, Exception exception) {
		exception.printStackTrace();
	}
}
