package myGameEngine.avatarMovement;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import Network.GameClient;
import net.java.games.input.Event;
public class MoveForwardAction extends AbstractInputAction {
	private Node avN;
	private GameClient gameClient;
	public MoveForwardAction(Node n, GameClient gc) { 
		avN = n;
		gameClient = gc;
	}
	
	public void performAction(float time, Event e) { 
		avN.moveForward(0.01f);
		gameClient.sendMoveMessages(avN.getWorldPosition());
	}
}
