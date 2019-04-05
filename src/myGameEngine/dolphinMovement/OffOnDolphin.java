package myGameEngine.dolphinMovement;

import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Camera;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;
//athena.ecs.csus.edu/~rdp135/rdr/rdp.php
/*
 * Toggle between getting ON and OFF the dolphin. When OFF the dolphin moves the 
 * camera axes and position near the dolphin
 */

public class OffOnDolphin extends AbstractInputAction {
	private Camera camera;
	private SceneNode dolphinN;
	private boolean rideDolphin = false;
	
	public OffOnDolphin (Camera c, SceneNode n) {
		camera = c;
		dolphinN = n;
	}

	@Override
	public void performAction(float arg0, Event arg1) {
		Vector3 dolphinPosition = dolphinN.getLocalPosition();
		//on dolphin
		if (!rideDolphin) {
			camera.setMode('n');
			rideDolphin = true;			
		}
		
		//off dolphin
		else {
			camera.setMode('c'); 
			rideDolphin = false;	
			camera.setPo((Vector3f) dolphinPosition); //Once off the	
		}
	}
	
	public boolean getDolphinStatus() {
		return rideDolphin;
	}
}
