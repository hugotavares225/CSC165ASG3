package Network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
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
	private Vector<GhostAvatar> ghostAvatars;
	
	public GameClient(InetAddress remAddr, int remPort,
			  ProtocolType pType, MyGame game)  throws IOException {
		super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
	}
	
@Override
protected void processPacket(Object o) {
	String message = (String) o;
	String[] msgTokens = message.split(",");
	
	if(msgTokens.length > 0) {
		/// format: join, success or failure
		if(msgTokens[0].compareTo("join") == 0) { 	// receive "join"
			if(msgTokens[1].compareTo("success") == 0) {
				game.setIsConnected(true);
				sendCreateMessages(game.getPlayerPosition());
			}
			if(msgTokens[1].compareTo("failure") == 0) {
				game.setIsConnected(false);
			}
		}
		
		// format: bye, remoteId
		if(msgTokens[0].compareTo("bye") == 0) {	// receive "bye"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			removeGhostAvatar(ghostID);
		}
			
		// format: create, remoteId, x,y,z 
		if(msgTokens[0].compareTo("create") == 0) {	// receive "create"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			createGhostAvatar(ghostID, ghostPosition);
		}
			
		// format: create, remoteId, x,y,z 
		if (msgTokens[0].compareTo("sdm") == 0)  { // receive "details-message"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			createGhostAvatar(ghostID, ghostPosition);
		}
			
		// format: move, remoteId, x,y,z
		if(msgTokens[0].compareTo("move") == 0) {	// receive "move"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			moveGhostAvatar(ghostID, ghostPosition);
		}
			
		if (msgTokens[0].compareTo("dsfr") == 0)	// receive "wants-details"
		{ // format: wantRequest,requestorId
			UUID ghostID = UUID.fromString(msgTokens[1]);
			sendDetailsForMessages(ghostID, game.getPlayerPosition());
		}
	}
} // void processPacket
	
	public void sendJoinMessages() {		// format: join,localid
		try {
			sendPacket(new String("join," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendCreateMessages(Vector3 pos) {	// format: create, localid, x,y,z
		try {
			String message = new String("create," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendByeMessages() {	// format: create, localid, x,y,z
		try { 
			String message = new String("bye," + id.toString()); 
			sendPacket(message); 
		}  catch (IOException e) { 
			e.printStackTrace();
		}
	}

	public void sendMoveMessages(Vector3 pos) {
		try {
			String message = new String("move," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void sendDetailsForMessages(UUID remoteID, Vector3 pos) {
		try {
			String message = new String("dsfr," + id.toString() + "," + remoteID.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** PRIVATE HELPER METHOD **/
	private void createGhostAvatar(UUID ghostID, Vector3 pos) {
		try {
			GhostAvatar gAvatar = new GhostAvatar(ghostID, pos);
			ghostAvatars.add(gAvatar);
			game.addGhostAvatarToGameWorld(gAvatar, pos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void moveGhostAvatar(UUID ghostID, Vector3 pos) {
		try {
			Iterator<GhostAvatar> itr = ghostAvatars.iterator();
			while(itr.hasNext()) {
				GhostAvatar gAvatar = itr.next();
				if(gAvatar.getID().equals(ghostID)) {
					gAvatar.setPosition(pos);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void removeGhostAvatar(UUID ghostID) {
		try {
			Iterator<GhostAvatar> itr = ghostAvatars.iterator();
			while(itr.hasNext()) {
				GhostAvatar gAvatar = itr.next();
				if(gAvatar.getID().equals(ghostID)) {
					game.removeGhostAvatarFromGameWorld(gAvatar);
					ghostAvatars.remove(gAvatar);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
