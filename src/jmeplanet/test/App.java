package jmeplanet.test;

import jmeplanet.*;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.material.Material;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;

import com.jme3.scene.Node;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.debug.*;
import jmeplanet.Planet;
import jmeplanet.PlanetController;
 
public class App extends SimpleApplication {
    
    Planet planet;

    public static void main(String[] args){
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1024,768);
        App app = new App();
        
        app.setSettings(settings);
        app.showSettings = false;
        app.start();
    }
 
    @Override
    public void simpleInitApp() {
        
        java.util.logging.Logger.getLogger("com.jme3").setLevel(java.util.logging.Level.SEVERE);
        
        // Release mouse on click
        inputManager.addMapping("MOUSE_BUTTON_LEFT", 
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "MOUSE_BUTTON_LEFT"); 
        inputManager.setCursorVisible(true);
        
        // Setup camera
        this.getCamera().setFrustumFar(12000f);
        this.getCamera().setLocation(new Vector3f(0f, 0f, 9000f));
        this.getFlyByCamera().setMoveSpeed(2000.0f);
        
        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);
        
        // Add planet
        planet = new Planet(this.getAssetManager(), 5000f);
        PlanetController planetController = new PlanetController(planet, this.getCamera());
        planet.addControl(planetController);
        rootNode.attachChild(planet);
        planet.setLocalTranslation(0f, 0f, 0f);
        
        // Add grid axis
        //rootNode.attachChild(createGridAxis(10,10));
         
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //planet.rotate(0, 0.005f*tpf, 0); 
        
        //easy collision detection
        Vector3f cameraLocation = this.getCamera().getLocation();        
        Vector3f planetToCamera = cameraLocation.subtract(planet.getLocalTranslation());
        float cameraHeight = planetToCamera.length();
        float r = planet.getRadius();
        if (cameraHeight < (r + 75f)) {
            this.getCamera().setLocation(planet.getLocalTranslation().add(planetToCamera.normalize().mult(r + 75f)));
        }
        
    }
    
    private Node createGridAxis(int lines, int spacing) {
        Node grid = new Node("Grid Axis");
        
        float half_size = (lines * spacing) / 2.0f - (spacing / 2);
        
        Geometry xGrid = new Geometry();
        Material xMat = new Material(this.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        xMat.setColor("Color", ColorRGBA.Blue);
        xGrid.setMesh(new Grid(lines,lines,spacing));
        xGrid.setMaterial(xMat);
        grid.attachChild(xGrid);
        xGrid.setLocalTranslation(-half_size, 0, -half_size);
        
        Geometry yGrid = new Geometry();
        Material yMat = new Material(this.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        yMat.setColor("Color", ColorRGBA.Green);
        yGrid.setMesh(new Grid(lines,lines,spacing));
        yGrid.setMaterial(yMat);
        grid.attachChild(yGrid);
        yGrid.rotate(FastMath.HALF_PI, 0, 0);
        yGrid.setLocalTranslation(-half_size, half_size, 0);
        
        Geometry zGrid = new Geometry();
        Material zMat = new Material(this.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        zMat.setColor("Color", ColorRGBA.Red);
        zGrid.setMesh(new Grid(lines,lines,spacing));
        zGrid.setMaterial(zMat);
        grid.attachChild(zGrid);
        zGrid.rotate(0, 0, FastMath.HALF_PI);
        zGrid.setLocalTranslation(0, -half_size, -half_size);
        
        return grid;
    }
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean pressed, float tpf){
                        
            if (name == "MOUSE_BUTTON_LEFT" && !pressed) {
                if (inputManager.isCursorVisible()) {
                    inputManager.setCursorVisible(false);
                } else {
                    inputManager.setCursorVisible(true);
                }
            }
            
        }
    };
    
}