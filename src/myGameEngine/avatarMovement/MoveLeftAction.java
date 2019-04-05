package myGameEngine.avatarMovement;

import Network.GameClient;
import net.java.games.input.Event;
import ray.input.action.AbstractInputAction;
import ray.rage.scene.Node;

public class MoveLeftAction extends AbstractInputAction {
	private Node avN;
	private GameClient gameClient;
	public MoveLeftAction(Node n, GameClient gc) { 
		avN = n;
		gameClient = gc;
	}
	
	public void performAction(float time, Event e) { 
		
		avN.moveLeft(0.1f);
		gameClient.sendMoveMessages(avN.getWorldPosition());
	}
}
