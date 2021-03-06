package Network;

import java.util.UUID;

import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Matrix3;
import ray.rml.Vector3;

public class GhostProjectile {
	private UUID id;
	private Vector3 position;
	private Matrix3 rotation;
	private SceneNode node;
	private Entity entity;
	
	public GhostProjectile(UUID id, Vector3 position) { 
		this.id = id;
		this.position = position;

		
	}
	/*Set and Return Ghost Projectile ID*/
	public UUID getID() {
		return id;
	}
	
	/*Set and Return Ghost Projectile position*/
	public Vector3 getPosition() {
		return position;
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
	
	
	/*Set and Return Ghost Projectile node*/
	public SceneNode getNode() {
		return node;
	}
	public void setNode(SceneNode node) {
		this.node = node;
	}
	
	/*Set and Return Ghost Projectile entity*/
	public Entity getEntity() {
		return entity;
	}
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
}
