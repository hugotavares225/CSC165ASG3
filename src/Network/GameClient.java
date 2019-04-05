package Network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.vecmath.Vector3d;

import a3.*;
import ray.networking.IGameConnection.ProtocolType;
import ray.networking.client.GameConnectionClient;
import ray.rml.Vector3;
import ray.rml.Vector3f;
public class GameClient extends GameConnectionClient {
	
	private MyGame game;
	private UUID id;
	private GhostAvatar ghostAvatar;
	//private Vector<GhostAvatar> ghostAvatars; 
	
	public GameClient(InetAddress remAddr, int remPort, 
			ProtocolType pType, MyGame ngame) throws IOException {
		super(remAddr, remPort, pType);
		game = ngame;
		id = UUID.randomUUID();
		
		System.out.println(id.toString());
		
		//ghostAvatars = new Vector<GhostAvatar>();	
	}
	
	/*
	 * Process Packet
	 */
	@Override
	protected void processPacket(Object msg) {
		String message = (String) msg;
		String[] msgTokens = message.split(",");
		
		if(msgTokens.length > 0) {
			if(msgTokens[0].compareTo("join") == 0) { //receive join
				System.out.println("Join Message has been received");
				
				//
				if(msgTokens[1].compareTo("success") == 0) { 
					game.setIsConnected(true);
					sendCreateMessage(game.getPlayerPosition());
					System.out.println("Client Connection Successfull");
				}
				
				if(msgTokens[1].compareTo("failure") == 0) {
					//game.setIsConnected(false);
					System.out.println("Client Connection Failed");
				}
			}
			
			if(msgTokens[0].compareTo("bye") == 0) { //receive bye
				// format: bye, remoteId
				UUID ghostID = UUID.fromString(msgTokens[1]);
				removeGhostAvatar(ghostID);
			}
			
			if ((msgTokens[0].compareTo("dsfr") == 0 ) // receive “dsfr”
				|| (msgTokens[0].compareTo("create")==0)) { // format: create, remoteId, x,y,z or dsfr, remoteId, x,y,z
				UUID ghostID = UUID.fromString(msgTokens[1]);
				Vector3 ghostPosition = Vector3f.createFrom(
						Float.parseFloat(msgTokens[2]),
						Float.parseFloat(msgTokens[3]),
						Float.parseFloat(msgTokens[4]));
				if (ghostAvatar == null)
						createGhostAvatar(ghostID, ghostPosition); 
			}	
			
			if(msgTokens[0].compareTo("wsds") == 0) { // rec. “create…”
				Vector3 pos = game.getPlayerPosition();
				UUID remID = UUID.fromString(msgTokens[1]);
				sendDetailsForMessage(remID, pos);
			}

			if(msgTokens[0].compareTo("move") == 0) { // rec. “move...”
				//format: move, remoteID, x,y,z
				UUID ghostID = UUID.fromString(msgTokens[1]);
				//extract ghost new x,y,z position from message
				Vector3 ghostPosition = Vector3f.createFrom(
						Float.parseFloat(msgTokens[2]),
						Float.parseFloat(msgTokens[3]),
						Float.parseFloat(msgTokens[4]));
				//then:
				//int rotateDegrees = Integer.parseInt(msgTokens[5]);
				moveGhostAvatar(ghostID, ghostPosition);
			}
		}
	}
	
	private void moveGhostAvatar(UUID ghostID, Vector3 ghostPosition) {
		// TODO Auto-generated method stub
		
	}

	public void sendJoinMessage() { // format: join, localId
		try { 
			sendPacket(new String("join," + id.toString()));
		} 
		catch (IOException e) { 
			e.printStackTrace();
		}
	} 
	
	public void sendCreateMessage(Vector3 pos) { // format: (create, localId, x,y,z)
		try { 
			String message = new String("create," + id.toString());
			message += "," + pos.x()+"," + pos.y() + "," + pos.z();
			sendPacket(message);
		}
		catch (IOException e) { 
			e.printStackTrace(); 
			} 
	}
	
	public void sendByeMessage() {
		try{
			sendPacket(new String("bye," + id.toString()));
			System.out.println("Client Disconnected");
		} 
		catch(IOException e) { 
			e.printStackTrace(); }
	}
	
	public void sendDetailsForMessage(UUID remId, Vector3 pos) {
		try{
			String message = new String("dsfr," + id.toString() + "," + remId.toString());
			message += "," + pos.x();
			message += "," + pos.y();
			message += "," + pos.z();
			sendPacket(message);
		} 
		catch(IOException e) { 
			e.printStackTrace(); 
		}	
	}
	
	public void sendMoveMessage(Vector3 pos) {	
		try { 
			String message = new String("move,"); 
			message += "," + pos.x(); 
			message += "," + pos.y(); 
			message += "," + pos.z();
			sendPacket(message); 
		} 
		catch (IOException e) { 
			e.printStackTrace();
		}
	}
	
	public void createGhostAvatar(UUID id, Vector3 pos) {
			ghostAvatar = new GhostAvatar(id, pos);
			try {
				game.addGhostAvatarToGameWorld(ghostAvatar);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
	}
	
	public void removeGhostAvatar(UUID id) {
			game.removeGhostAvatarFromGameWorld(ghostAvatar);
	}
}
