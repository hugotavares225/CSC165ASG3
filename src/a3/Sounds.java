package a3;

import ray.audio.*;
import ray.rage.Engine;
import ray.rage.scene.SceneManager;
import ray.rage.scene.SceneNode;
import ray.rml.Vector3;
import ray.rml.Vector3f;

public class Sounds {
	
	  IAudioManager audioMgr;  
	  Sound windSound, shotSound;
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
		resource1 = audioMgr.createAudioResource("wind.wav",AudioResourceType.AUDIO_SAMPLE);      
		windSound = new Sound(resource1,SoundType.SOUND_MUSIC, 100, true);      
		windSound.initialize(audioMgr);       
		windSound.setMaxDistance(10.0f);    
		windSound.setMinDistance(0.5f);    
		windSound.setRollOff(5.0f); 
		
		resource2 = audioMgr.createAudioResource("shot.wav",AudioResourceType.AUDIO_SAMPLE);      
		shotSound = new Sound(resource2,SoundType.SOUND_EFFECT, 100, false);      
		shotSound.initialize(audioMgr);       
		shotSound.setMaxDistance(10.0f);    
		shotSound.setMinDistance(0.5f);    
		shotSound.setRollOff(5.0f);      
		
		setEarParameters(sm);   
     
		} 
	
	
	public void playWindSound() {
		windSound.play();
	}
	
	public void stopWindSound() {
		windSound.stop();
	}
	
	public void playShotSound(SceneNode n) {
		shotSound.setLocation(n.getWorldPosition());
		shotSound.play();
	}
	
	public void stopShotSound() {
		shotSound.stop();
	}
}
