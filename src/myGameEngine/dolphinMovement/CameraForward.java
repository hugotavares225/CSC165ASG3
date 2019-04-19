package myGameEngine.dolphinMovement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

/*
 * Moves camera forward using D Key or X-Axis input from controller*/
 

public class CameraForward extends AbstractInputAction {
	
	private Camera camera;
	private SceneNode node;
	
	public CameraForward(Camera c, SceneNode n) { 
		camera = c;
		node = n;
	}

	public void performAction(float t, Event e) {
		float speed = 50.0f;
		if (camera.getMode() == 'c') {
			Vector3f n = camera.getFd();
			Vector3f p = camera.getPo();
			Vector3f p1 = (Vector3f) Vector3f.createFrom(speed*n.x(), speed*n.y(), speed*n.z());
			Vector3f p2 = (Vector3f) p.add((Vector3)p1);
			camera.setPo((Vector3f)Vector3f.createFrom(p2.x(),p2.y(),p2.z()));
		}
		else if (camera.getMode() == 'n')
			node.moveForward(speed);
	}
}
