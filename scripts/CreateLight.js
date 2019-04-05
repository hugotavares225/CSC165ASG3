var JavaPackages = new JavaImporter(Packages.ray.rage.scene.SceneManager,
	Packages.ray.rage.scene.Light, Packages.ray.rage.scene.Light.Type,
	Packages.ray.rage.scene.Light.Type.POINT, Packages.java.awt.Color);

// creates a RAGE object - in this case a light
with (JavaPackages) { 
	var plight = sm.createLight("testLamp1", Light.Type.POINT);
	plight.setAmbient(new Color(.3, .3, .3));
	plight.setDiffuse(new Color(.7, .7, .7));
	plight.setSpecular(new Color(1.0, 1.0, 1.0));
	plight.setRange(100);
}

print("Create Light Script is working!");