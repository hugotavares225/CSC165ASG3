package NPC;

import myGameEngine.avatarMovement.MoveForwardAction;
import ray.rage.scene.Entity;
import ray.rage.scene.SceneNode;
import ray.rml.Matrix3;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class NPC {
	float locX, locY, locZ;
	//private int id;
	private Vector3 position;
	private SceneNode node;
	private Entity entity;
	public NPC(Vector3 position) // constructor
	{ 
		this.position = position;
	}
	
	public void setPosition(Vector3 position)
	{ 
		node.setLocalPosition(position);
	}
	
	public void getPosition(Vector3 position) { 
		node.getLocalPosition();
	}
	
	
	public void updateLocation() {
		node.moveForward(1.0f);
		locX=node.getLocalPosition().x();
		locY=node.getLocalPosition().y();
		locZ=node.getLocalPosition().z();
		node.setLocalPosition(Vector3f.createFrom(locX,locY,locZ));
	}
		
	public double getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void setPos(Vector3 add) {
		locX=add.x();
		locY=add.y();
		locZ=add.z();
		
	}
	
	public void getBig() {
		

	}
	public void randomizeLocation(int nextInt, int nextInt2, int nextInt3) {
		
	}
	public void getSmall() {
		// TODO Auto-generated method stub
		
	};
	
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
