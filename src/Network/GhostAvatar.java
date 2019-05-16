package Network;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Matrix3;
import ray.rml.Vector3;

public class GhostAvatar {

	private UUID id;
	private Vector3 position;
	private Matrix3 rotation;
	private SceneNode node;
	private Entity entity;
	private int health = 100;
	private static int count =0;
	
	public GhostAvatar(UUID id, Vector3 position) { 
		this.id = id;
		this.position = position;
		count++;

		
	}

	public int getGhostAvatarCount() {
		return count;
	}
	
	/*Set and Return Ghost Avatar ID*/
	public UUID getID() {
		return id;
	}
	
	/*Set and Return Ghost Avatar position*/
	public Vector3 getPosition() {
		return position;
	}
	
	public int getHealth() {
		return health;
	}
	
	public void decreaseHealth(int h) {
		health -= h;
	}
	
	public void setPosition(Vector3 position) {
		node.setLocalPosition(position);
	}
	
	/*Get and set Rotation*/
	public Matrix3 getRotation() {
		return node.getLocalRotation();
	}
	
	public void setRotation(Matrix3 rotation) {
		node.setLocalRotation(rotation);
	}
	
	//SET SCALE
	public void setScale(Vector3 scale) {
		node.setLocalScale(scale);
	}
	
	
	/*Set and Return Ghost Avatar node*/
	public SceneNode getNode() {
		return node;
	}
	public void setNode(SceneNode node) {
		this.node = node;
	}
	
	/*Set and Return Ghost Avatar entity*/
	public Entity getEntity() {
		return entity;
	}
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	
	

}
