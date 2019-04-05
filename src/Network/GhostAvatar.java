package Network;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;

public class GhostAvatar {

	private UUID id;
	private Vector3 position;
	private SceneNode node;
	private Entity entity;
	
	public GhostAvatar(UUID id, Vector3 position) { 
		this.id = id;
		this.position = position;
		
	}
	/*Set and Return Ghost Avatar ID*/
	public UUID getId() {
		return id;
	}
	public void setId(UUID id) {
		this.id = id;
	}
	
	/*Set and Return Ghost Avatar position*/
	public Vector3 getPosition() {
		return position;
	}
	public void setPosition(Vector3 position) {
		this.position = position;
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
