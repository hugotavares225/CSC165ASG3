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
import ray.rage.rendersystem.Renderable.Primitive;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Matrix3;
import ray.rml.Matrix3f;
import ray.rml.Vector3;
import ray.rml.Vector3f;
public class GameClient extends GameConnectionClient {
	private MyGame game;
	private UUID id;
	private Vector<GhostAvatar> ghostAvatars;
	private Vector<GhostProjectile> ghostProjectiles;
	
	public GameClient(InetAddress remAddr, int remPort,
			  ProtocolType pType, MyGame game)  throws IOException {
		super(remAddr, remPort, pType);
		this.game = game;
		this.id = UUID.randomUUID();
		this.ghostAvatars = new Vector<GhostAvatar>();
		this.ghostProjectiles = new Vector<GhostProjectile>();
		
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
		
		//RECEIVE BYE MESSAGE FOR AVATAR
		if(msgTokens[0].compareTo("bye") == 0) {	// receive "bye"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			removeGhostAvatar(ghostID);
		}
		
		//RECEIVE BYE MESSAGE FOR PROJECTILE
		if(msgTokens[0].compareTo("byeP") == 0) {	// receive "bye"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			removeGhostProjectile(ghostID);
		}
			
		//RECEIVE CREATE MESSAGE FOR AVATAR
		if(msgTokens[0].compareTo("create") == 0) {	// receive "create"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			createGhostAvatar(ghostID, ghostPosition);

		}
		
		//RECEIVE CREATE MESSAGE FOR PROJECTILE
		if(msgTokens[0].compareTo("createP") == 0) {	// receive "create"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			createGhostProjectile(ghostID, ghostPosition);
		}
			
		//RECEIVE SDM for AVATAR 
		if (msgTokens[0].compareTo("sdm") == 0)  { // receive "details-message"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			createGhostAvatar(ghostID, ghostPosition);
		}
		
		//RECEIVE SDM for PROJECTILE 
		if (msgTokens[0].compareTo("sdmP") == 0)  { // receive "details-message"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			createGhostProjectile(ghostID, ghostPosition);
		}
			
		//RECEIVE MOVE MESSAGE FOR AVATAR
		if(msgTokens[0].compareTo("move") == 0) {	// receive "move"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			moveGhostAvatar(ghostID, ghostPosition);
		}
		
		//RECEIVE MOVE MESSAGE FOR PROJECTILE
		if(msgTokens[0].compareTo("moveP") == 0) {	// receive "move"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostPosition = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			moveGhostProjectile(ghostID, ghostPosition);
		}
		
		//RECEIVE ROTATE MESSAGE FOR AVATAR
		if(msgTokens[0].compareTo("rotate") == 0) {	// receive "rotate"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			float matrix[] = {Float.parseFloat(msgTokens[2]),
							Float.parseFloat(msgTokens[3]),
							Float.parseFloat(msgTokens[4]),
							Float.parseFloat(msgTokens[5]),
							Float.parseFloat(msgTokens[6]),
							Float.parseFloat(msgTokens[7]),
							Float.parseFloat(msgTokens[8]),
							Float.parseFloat(msgTokens[9]),
							Float.parseFloat(msgTokens[10])};
			
			Matrix3 ghostRotation = Matrix3f.createFrom(matrix);
			rotateGhostAvatar(ghostID, ghostRotation);
		}
		

		
		//RECEIVE ROTATE MESSAGE FOR PROJECTILE
		if(msgTokens[0].compareTo("rotateP") == 0) {	// receive "rotate"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			float matrix[] = {Float.parseFloat(msgTokens[2]),
							Float.parseFloat(msgTokens[3]),
							Float.parseFloat(msgTokens[4]),
							Float.parseFloat(msgTokens[5]),
							Float.parseFloat(msgTokens[6]),
							Float.parseFloat(msgTokens[7]),
							Float.parseFloat(msgTokens[8]),
							Float.parseFloat(msgTokens[9]),
							Float.parseFloat(msgTokens[10])};
			
			Matrix3 ghostRotation = Matrix3f.createFrom(matrix);
			rotateGhostProjectile(ghostID, ghostRotation);
		}
		
		//RECEIVE SCALE MESSAGE FOR AVATAR
		if(msgTokens[0].compareTo("scale") == 0) {	// receive "scale"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostScale = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			scaleGhostAvatar(ghostID, ghostScale);
		}
		
		//RECEIVE SCALE MESSAGE FOR PROJECTILE
		if(msgTokens[0].compareTo("scaleP") == 0) {	// receive "scale"
			UUID ghostID = UUID.fromString(msgTokens[1]);
			Vector3 ghostScale = Vector3f.createFrom(
					Float.parseFloat(msgTokens[2]), 
					Float.parseFloat(msgTokens[3]), 
					Float.parseFloat(msgTokens[4]));
			scaleGhostProjectile(ghostID, ghostScale);
		}
		
		//RECEIVE WANTS DETAILS FOR AVATAR	
		if (msgTokens[0].compareTo("dsfr") == 0)	// receive "wants-details"
		{ // format: wantRequest,requestorId
			UUID ghostID = UUID.fromString(msgTokens[1]);
			sendDetailsForMessages(ghostID, game.getPlayerPosition());
		}
		
		//RECEIVE WANTS DETAILS FOR PROJECTILE	
		if (msgTokens[0].compareTo("dsfrP") == 0)	// receive "wants-details"
		{ // format: wantRequest,requestorId
			UUID ghostID = UUID.fromString(msgTokens[1]);
			sendDetailsForMessagesP(ghostID, game.getPlayerPosition());
		}
	}
} 
	

	public void sendJoinMessages() {		// format: join,localid
		try {
			sendPacket(new String("join," + id.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	//SEND CREATE MESSAGES FOR AVATAR
	public void sendCreateMessages(Vector3 pos) {	// format: create, localid, x,y,z
		try {
			String message = new String("create," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SEND CREATE MESSAGES FOR PROJECTILE
	public void sendCreateMessagesP(Vector3 pos) {	// format: create, localid, x,y,z
		try {
			String message = new String("createP," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	//SEND BYE MESSAGES FOR AVATAR
	public void sendByeMessages() {	// format: create, localid, x,y,z
		try { 
			String message = new String("bye," + id.toString()); 
			sendPacket(message); 
		}  catch (IOException e) { 
			e.printStackTrace();
		}
	}
	
	//SEND BYE MESSAGES FOR PROJECTILE
	public void sendByeMessagesP() {	// format: create, localid, x,y,z
		try { 
			String message = new String("byeP," + id.toString()); 
			sendPacket(message); 
		}  catch (IOException e) { 
			e.printStackTrace();
		}
	}

	
	//SEND MOVE MESSAGES FOR AVATAR
	public void sendMoveMessages(Vector3 pos) {
		try {
			String message = new String("move," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	//SEND MOVE MESSAGES FOR PROJECTILE
	public void sendMoveMessagesP(Vector3 pos) {
		try {
			String message = new String("moveP," + id.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//ROTATE MESSAGE FOR AVATAR
	public void sendRotateMessages(Matrix3 rot) {
		try {
			String message = new String("rotate," + id.toString());
			message += "," + rot.value(0, 0) + "," + rot.value(1, 0) + "," + rot.value(2, 0) + "," +
							rot.value(0, 1) + "," + rot.value(1, 1) + "," + rot.value(2, 1) + "," +
					        rot.value(0, 2) + "," + rot.value(1, 2) + "," + rot.value(2, 2);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//ROTATE MESSAGE FOR PROJECTILE
	public void sendRotateMessagesP(Matrix3 rot) {
		try {
			String message = new String("rotateP," + id.toString());
			message += "," + rot.value(0, 0) + "," + rot.value(1, 0) + "," + rot.value(2, 0) + "," +
							rot.value(0, 1) + "," + rot.value(1, 1) + "," + rot.value(2, 1) + "," +
					        rot.value(0, 2) + "," + rot.value(1, 2) + "," + rot.value(2, 2);
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SEND SCALE MESSAGES FOR AVATAR
	public void sendScaleMessages(Vector3 scale) {
		try {
			String message = new String("scale," + id.toString());
			message += "," + scale.x() + "," + scale.y() + "," + scale.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	//SEND SCALE MESSAGES FOR PROJECTILE
	public void sendScaleMessagesP(Vector3 scale) {
		try {
			String message = new String("scaleP," + id.toString());
			message += "," + scale.x() + "," + scale.y() + "," + scale.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	//SEND DETAILS FOR MESSAGE FOR AVATAR
	public void sendDetailsForMessages(UUID remoteID, Vector3 pos) {
		try {
			String message = new String("dsfr," + id.toString() + "," + remoteID.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//SEND DETAILS FOR MESSAGE FOR PROJECTILE
	public void sendDetailsForMessagesP(UUID remoteID, Vector3 pos) {
		try {
			String message = new String("dsfrP," + id.toString() + "," + remoteID.toString());
			message += "," + pos.x() + "," + pos.y() + "," + pos.z();
			sendPacket(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** PRIVATE HELPER METHOD **/
	//CREATE GHOST AVATR
	private void createGhostAvatar(UUID ghostID, Vector3 pos) {
		try {
			GhostAvatar gAvatar = new GhostAvatar(ghostID, pos);
			ghostAvatars.add(gAvatar);
			game.addGhostAvatarToGameWorld(gAvatar, pos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//CREATE GHOST PROJECTILE
	private void createGhostProjectile(UUID ghostID, Vector3 pos) {
		try {
			GhostProjectile gAvatar = new GhostProjectile(ghostID, pos);
			ghostProjectiles.add(gAvatar);
			game.addGhostProjectileToGameWorld(gAvatar, pos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* MOVE GHOST AVATAR */
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
	
	/* MOVE GHOST PROJECTILE */
	private void moveGhostProjectile(UUID ghostID, Vector3 pos) {
		try {
			Iterator<GhostProjectile> itr = ghostProjectiles.iterator();
			while(itr.hasNext()) {
				GhostProjectile gAvatar = itr.next();
				if(gAvatar.getID().equals(ghostID)) {
					gAvatar.setPosition(pos);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* ROTATE GHOST AVATAR */
	private void rotateGhostAvatar(UUID ghostID, Matrix3 rotate) {
		try {
			Iterator<GhostAvatar> itr = ghostAvatars.iterator();
			while(itr.hasNext()) {
				GhostAvatar gAvatar = itr.next();
				if(gAvatar.getID().equals(ghostID)) {
					gAvatar.setRotation(rotate);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/* ROTATE GHOST PROJECTILE */
	private void rotateGhostProjectile(UUID ghostID, Matrix3 rotate) {
		try {
			Iterator<GhostProjectile> itr = ghostProjectiles.iterator();
			while(itr.hasNext()) {
				GhostProjectile gAvatar = itr.next();
				if(gAvatar.getID().equals(ghostID)) {
					gAvatar.setRotation(rotate);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*SCALE GHOST AVATAR*/
	private void scaleGhostAvatar(UUID ghostID, Vector3 scale) {
		try {
			Iterator<GhostAvatar> itr = ghostAvatars.iterator();
			while(itr.hasNext()) {
				GhostAvatar gAvatar = itr.next();
				if(gAvatar.getID().equals(ghostID)) {
					gAvatar.setScale(scale);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*Get Ghost Avatar's location*/
	public List<Vector3> getGhostLocation(UUID ghostID) {
		List<Vector3> ghostLocs = new ArrayList<>(); 
		try {
			Iterator<GhostAvatar> itr = ghostAvatars.iterator();
			while (itr.hasNext()) {
				GhostAvatar gAvatar = itr.next();	
				if(gAvatar.getID().equals(ghostID)) {
					ghostLocs.add(gAvatar.getPosition());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return ghostLocs;
	}
	
	
	/*Get Avatar Object Details*/
	public List<Vector3> getAvatarVehicle(UUID ghostID, Vector3 scale) {
		List<Vector3> ghostLocs = new ArrayList<>(); 
		try {
			Iterator<GhostAvatar> itr = ghostAvatars.iterator();
			while (itr.hasNext()) {
				GhostAvatar gAvatar = itr.next();	
				if(gAvatar.getID().equals(ghostID)) {
					ghostLocs.add(gAvatar.getPosition());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return ghostLocs;
	}
	
	/*SCALE GHOST PROJECTILE*/
	private void scaleGhostProjectile(UUID ghostID, Vector3 scale) {
		try {
			Iterator<GhostProjectile> itr = ghostProjectiles.iterator();
			while(itr.hasNext()) {
				GhostProjectile gAvatar = itr.next();
				if(gAvatar.getID().equals(ghostID)) {
					gAvatar.setScale(scale);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/*REMOVE GHOST AVATAR*/
	private void removeGhostAvatar(UUID ghostID) {
		try {
			Iterator<GhostAvatar> itr = ghostAvatars.iterator();
			if (itr != null) {
				while(itr.hasNext()) {
					GhostAvatar gAvatar = itr.next();
					if(gAvatar.getID().equals(ghostID)) {
						game.removeGhostAvatarFromGameWorld(gAvatar);
						ghostAvatars.remove(gAvatar);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*REMOVE GHOST PROJECTILE*/
	private void removeGhostProjectile(UUID ghostID) {
		try {
			Iterator<GhostProjectile> itr = ghostProjectiles.iterator();
			if (itr != null) {
				while(itr.hasNext()) {
					GhostProjectile gAvatar = itr.next();
					if(gAvatar.getID().equals(ghostID)) {
						game.removeGhostProjectileFromGameWorld(gAvatar);
						ghostProjectiles.remove(gAvatar);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
