package NPC;

import ray.ai.behaviortrees.BTCondition;

public class OneSecPassed extends BTCondition{
	private NPCcontroller npcC;
	private NPC npc;
	private long lastUpdateTime;

	public OneSecPassed(NPCcontroller c, NPC n, boolean toNegate)  { 
		  super(toNegate);    
		  npcC = c;    
		  npc = n;    
		  lastUpdateTime = System.nanoTime();  
	}

	@Override
	protected boolean check() {
		 float elapsedMilliSecs = (System.nanoTime()-lastUpdateTime)/(1000000.0f);    
		 if ((elapsedMilliSecs >= 1000.0f) && (npc.getSize()==2.0)) { 
			 lastUpdateTime = System.nanoTime();     
			 npcC.setNearFlag(false);      
			 return true;    
		 }    
		 else return false;
		 } 
	}   
