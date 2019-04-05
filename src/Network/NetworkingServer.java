package Network;

import java.io.IOException;

import ray.networking.IGameConnection.ProtocolType;

public class NetworkingServer {
	private GameServerUDP thisUDPServer;
	//private GameServerTCP thisTCPserver;
	
	public NetworkingServer(int serverPort, ProtocolType protocol) {
		//if (protocol.toUpperCase().compareTo("TCP") == 0) {
		//thisTCPServer = new GameServerTCP(serverPort);
//}
//else {
		System.out.println("HI");
		//thisUDPServer = new GameServerUDP(serverPort, protocol);
//} 
	}
}
