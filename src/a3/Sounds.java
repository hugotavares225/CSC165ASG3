package a3;

import ray.audio.*;
import ray.rage.Engine;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Sounds {
	
	  IAudioManager audioMgr;  
	  Sound drivingSound, shotSound;
	  MyGame game;
	  public Sounds (MyGame g) {
		  game = g;
	  }


	public void setEarParameters(SceneManager sm)  { 
		SceneNode vehicleNode = sm.getSceneNode("myvehicleNode");
		Vector3 avDir = vehicleNode.getWorldForwardAxis();    
		//  note - should get the camera's forward direction    
		//     - avatar direction plus azimuth     
		audioMgr.getEar().setLocation(vehicleNode.getWorldPosition());    
		audioMgr.getEar().setOrientation(avDir, Vector3f.createFrom(0,1,0));  
	}


	public void initAudio(SceneManager sm)  { 
		AudioResource resource1, resource2;    
		audioMgr = AudioManagerFactory.createAudioManager("ray.audio.joal.JOALAudioManager");    
		if (!audioMgr.initialize())     { 
			System.out.println("Audio Manager failed to initialize!");     
			return;    
			}     
		resource1 = audioMgr.createAudioResource("driving.wav",AudioResourceType.AUDIO_SAMPLE);      
		drivingSound = new Sound(resource1,SoundType.SOUND_EFFECT, 100, true);      
		drivingSound.initialize(audioMgr);       
		drivingSound.setMaxDistance(10.0f);    
		drivingSound.setMinDistance(0.5f);    
		drivingSound.setRollOff(5.0f); 
		
		resource2 = audioMgr.createAudioResource("shot.wav",AudioResourceType.AUDIO_SAMPLE);      
		shotSound = new Sound(resource2,SoundType.SOUND_EFFECT, 100, false);      
		shotSound.initialize(audioMgr);       
		shotSound.setMaxDistance(10.0f);    
		shotSound.setMinDistance(0.5f);    
		shotSound.setRollOff(5.0f);      
		
		setEarParameters(sm);   
     
		} 
	
	
	public void playDrivingSound() {
		drivingSound.play();
	}
	
	public void stopDrivingSound() {
		drivingSound.stop();
	}
	
	public void playShotSound(SceneNode n) {
		shotSound.setLocation(n.getWorldPosition());
		shotSound.play();
	}
	
	public void stopShotSound() {
		shotSound.stop();
	}
}
