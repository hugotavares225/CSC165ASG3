package myGameEngine.avatarMovement;

import Network.GameClient;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;
import ray.rml.Angle;
import ray.rml.Degreef;

public class YawRightAction extends AbstractInputAction {
	private Node avN;
	private GameClient gameClient;
	
	public YawRightAction(Node n, GameClient gc) { 
		avN = n;
		gameClient = gc;
	}
	
	public void performAction(float time, Event e) { 
		Angle rotAmt = Degreef.createFrom(-3.0f);
		avN.yaw(rotAmt);
		gameClient.sendMoveMessages(avN.getWorldPosition());
	}

}
