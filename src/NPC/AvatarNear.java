package NPC;

import Network.GameServerUDP;
import ray.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition {
	
	private NPCcontroller npcC;
	private GameServerUDP server;
	private NPC npc;
	
	public AvatarNear(GameServerUDP s, NPCcontroller c, NPC n, boolean toNegate) { 
		  super(toNegate);    
		  server = s;    
		  npcC = c;    
		  npc = n;  
	  }  
	
	protected boolean check() { 
		//server.sendCheckForAvatarNear();    
		return npcC.getNearFlag();
	} 
}
