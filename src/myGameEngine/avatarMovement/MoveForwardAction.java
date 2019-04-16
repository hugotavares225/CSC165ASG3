package myGameEngine.avatarMovement;

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import Network.GameClient;
import a3.MyGame;
import net.java.games.input.Event;
public class MoveForwardAction extends AbstractInputAction {
	private Node avN;
	private GameClient gameClient;
	private MyGame myGame;
	
	public MoveForwardAction(Node n, GameClient gc, MyGame g) { 
		avN = n;
		gameClient = gc;
		myGame = g;
	}
	
	public void performAction(float time, Event e) { 
		avN.moveForward(0.1f);
		gameClient.sendMoveMessages(avN.getWorldPosition());
		myGame.updateVerticalPosition();
	}
}
