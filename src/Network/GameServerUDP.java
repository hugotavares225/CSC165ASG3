package Network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import ray.networking.server.GameConnectionServer;
import ray.networking.server.IClientInfo;
public class GameServerUDP extends GameConnectionServer<UUID> {
	public GameServerUDP(int localPort) throws IOException
	{
		super(localPort, ProtocolType.UDP);
	}
 
	@Override
	public void processPacket(Object o, InetAddress senderIP, int sndPort) {
		String message = (String) o;
		String[] msgTokens = message.split(",");
		
		if(msgTokens.length > 0) {
			// case where server receives a JOIN message
			// format: join, localid
			if(msgTokens[0].compareTo("join") == 0) {
				try {
					IClientInfo ci;
					ci = getServerSocket().createClientInfo(senderIP, sndPort);
					UUID clientID = UUID.fromString(msgTokens[1]);
					addClient(ci, clientID);
					sendJoinedMessage(clientID, true);
					System.out.println("Client has joined the server.");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// case where server receives a BYE message
		// format: bye, localid
		if(msgTokens[0].compareTo("bye") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			sendByeMessages(clientID);
			removeClient(clientID);
		}
		
		//SERVER RECEIVES CREATE MESSAGE FOR AVATAR
		if(msgTokens[0].compareTo("create") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
			sendCreateMessages(clientID, pos);
			sendWantsDetailsMessages(clientID);
		}
		
		//SERVER RECEIVES CREATE MESSAGE FOR PROJECTILE
		if(msgTokens[0].compareTo("createP") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
			sendCreateMessagesP(clientID, pos);
			sendWantsDetailsMessagesP(clientID);
		}
		
		
		//SERVER RECEIVES DETAILS FOR MESSAGE FOR AVATAR
		if(msgTokens[0].compareTo("dsfr") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			UUID remoteID = UUID.fromString(msgTokens[2]);
			String[] pos = {msgTokens[3], msgTokens[4], msgTokens[5]};
			sendDetailsMessages(clientID, remoteID, pos);
		}
		
		//SERVER RECEIVES DETAILS FOR MESSAGE FOR PROJECTILE
		if(msgTokens[0].compareTo("dsfrP") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			UUID remoteID = UUID.fromString(msgTokens[2]);
			String[] pos = {msgTokens[3], msgTokens[4], msgTokens[5]};
			sendDetailsMessagesP(clientID, remoteID, pos);
		}
		
		//SERVER RECEIVES MOVE MESSAGE FOR AVATAR
		if(msgTokens[0].compareTo("move") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
			sendMoveMessages(clientID, pos);
		}
		
		//SERVER RECEIVES MOVE MESSAGE FOR PROJECTILE
		if(msgTokens[0].compareTo("moveP") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			String[] pos = {msgTokens[2], msgTokens[3], msgTokens[4]};
			sendMoveMessagesP(clientID, pos);
		}
		
		//SERVER RECEIVES ROTATE MESSAGE
		if(msgTokens[0].compareTo("rotate") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			String[] rot = {msgTokens[2], msgTokens[3], msgTokens[4],
					 msgTokens[5], msgTokens[6], msgTokens[7],
					 msgTokens[8], msgTokens[9], msgTokens[10]};
			sendRotateMessages(clientID, rot);
		}
		
		//SERVER RECEIVES ROTATE MESSAGE
		if(msgTokens[0].compareTo("rotateP") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			String[] rot = {msgTokens[2], msgTokens[3], msgTokens[4],
					 msgTokens[5], msgTokens[6], msgTokens[7],
					 msgTokens[8], msgTokens[9], msgTokens[10]};
			sendRotateMessagesP(clientID, rot);
		}
		
		//SERVER RECEIVES SCALE MESSAGE
		if(msgTokens[0].compareTo("scale") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			String[] scale = {msgTokens[2], msgTokens[3], msgTokens[4]};
			sendScaleMessages(clientID, scale);
		}
		
		//SERVER RECEIVES SCALE MESSAGE
		if(msgTokens[0].compareTo("scaleP") == 0) {
			UUID clientID = UUID.fromString(msgTokens[1]);
			String[] scale = {msgTokens[2], msgTokens[3], msgTokens[4]};
			sendScaleMessagesP(clientID, scale);
		}
	} 
	
	/** HELPER METHOD **/
	// format: join, success or join, failure
	public void sendJoinedMessage(UUID clientID, boolean success) {
		try {
			String message = new String("join,");
			if (success) 
				message += "success";
			else 
				message += "failure";
			sendPacket(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SERVER SEND CREATE MESSAGES FOR AVATAR
	public void sendCreateMessages(UUID clientID, String[] position) {
		try {
			String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SERVER SEND CREATE MESSAGES FOR PROJECTILE
	public void sendCreateMessagesP(UUID clientID, String[] position) {
		try {
			String message = new String("createP," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//SERVER SEND WANT DETAILS FOR MESSAGES FOR AVATAR
	public void sendWantsDetailsMessages(UUID clientID) {
		try {
			String message = new String("dsfr," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SERVER SEND WANT DETAILS FOR MESSAGES FOR PROJECTILE
	public void sendWantsDetailsMessagesP(UUID clientID) {
		try {
			String message = new String("dsfrP," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SERVER SEND DETAILS MESSAGES FOR AVATAR
	public void sendDetailsMessages(UUID clientID, UUID remoteID, String[] position) {
		try {
			String message = new String("sdm," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			sendPacket(message, remoteID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SERVER SEND DETAILS MESSAGES FOR PROJECTILE
	public void sendDetailsMessagesP(UUID clientID, UUID remoteID, String[] position) {
		try {
			String message = new String("sdmP," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			sendPacket(message, remoteID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	//SERVER SEND MOVE MESSAGES FOR AVATAR
	public void sendMoveMessages(UUID clientID, String[] position) {
		try {
			String message = new String("move," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SERVER SEND MOVE MESSAGES FOR PROJECTILE
	public void sendMoveMessagesP(UUID clientID, String[] position) {
		try {
			String message = new String("moveP," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*ROTATE MESSAGE AVATAR*/
	public void sendRotateMessages(UUID clientID, String[] rot) {
		try {
			String message = new String("rotate," + clientID.toString());
			message += "," + rot[0];
			message += "," + rot[1];
			message += "," + rot[2];
			message += "," + rot[3];
			message += "," + rot[4];
			message += "," + rot[5];
			message += "," + rot[6];
			message += "," + rot[7];
			message += "," + rot[8];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*ROTATE MESSAGE PROJECTILE*/
	public void sendRotateMessagesP(UUID clientID, String[] rot) {
		try {
			String message = new String("rotateP," + clientID.toString());
			message += "," + rot[0];
			message += "," + rot[1];
			message += "," + rot[2];
			message += "," + rot[3];
			message += "," + rot[4];
			message += "," + rot[5];
			message += "," + rot[6];
			message += "," + rot[7];
			message += "," + rot[8];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*SCALE MESSAGE AVATAR*/
	public void sendScaleMessages(UUID clientID, String[] scale) {
		try {
			String message = new String("scale," + clientID.toString());
			message += "," + scale[0];
			message += "," + scale[1];
			message += "," + scale[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*SCALE MESSAGE*/
	public void sendScaleMessagesP(UUID clientID, String[] scale) {
		try {
			String message = new String("scaleP," + clientID.toString());
			message += "," + scale[0];
			message += "," + scale[1];
			message += "," + scale[2];
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendByeMessages(UUID clientID) {
		try {
			String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

