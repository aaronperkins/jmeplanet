package jmeplanet.test;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.material.Material;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;


import jmeplanet.Planet;
import jmeplanet.PlanetController;
import jmeplanet.FractalDataSource;
 
public class App extends SimpleApplication {
    
    Planet planet;
    Material planetMaterial;

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
        
        // Only show severe errors in log
        java.util.logging.Logger.getLogger("com.jme3").setLevel(java.util.logging.Level.SEVERE);
        
        // Togle mouse cursor
        inputManager.addMapping("TOGGLE_CURSOR", 
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "TOGGLE_CURSOR"); 
        inputManager.setCursorVisible(true);
        // Toggle wireframe
        inputManager.addMapping("TOGGLE_WIREFRAME", 
            new KeyTrigger(KeyInput.KEY_1));
        inputManager.addListener(actionListener, "TOGGLE_WIREFRAME"); 
        
        // Setup camera
        this.getCamera().setFrustumFar(200000f);
        this.getCamera().setLocation(new Vector3f(0f, 0f, 20000f));
        
        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);
        
        // Create height data source
        FractalDataSource dataSource = new FractalDataSource(13354);
        dataSource.setHeightScale(250f);

         // Terrain material
        planetMaterial = new Material(this.assetManager, "MatDefs/PlanetTerrain.j3md");
        
        // base color ( underwater )
        planetMaterial.setColor("baseColor", new ColorRGBA(0.1f,0.3f,0.8f,1.0f));
         // shore texture
        Texture dirt = this.assetManager.loadTexture("Textures/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region1ColorMap", dirt);
        planetMaterial.setVector3("region1", new Vector3f(0, 50, 0));
        // grass texture
        Texture grass = this.assetManager.loadTexture("Textures/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region2ColorMap", grass);
        planetMaterial.setVector3("region2", new Vector3f(40, 220, 0));
        // rock texture
        Texture rock = this.assetManager.loadTexture("Textures/rock.jpg");
        rock.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region3ColorMap", rock);
        planetMaterial.setVector3("region3", new Vector3f(210, 340, 0));
        // snow
        Texture snow = this.assetManager.loadTexture("Textures/snow.jpg");
        snow.setWrap(WrapMode.Repeat);
        planetMaterial.setTexture("region4ColorMap", snow);
        planetMaterial.setVector3("region4", new Vector3f(235, 375, 0));
        
        //planetMaterial = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //planetMaterial.setBoolean("VertexColor", true);
        //planetMaterial = new Material(this.assetManager, "Common/MatDefs/Light/Lighting.j3md");
        //planetMaterial.setBoolean("UseVertexColor", true);

        // add planet
        planet = new Planet("Planet",  6353f , planetMaterial, dataSource);
        rootNode.attachChild(planet);
        planet.setLocalTranslation(0f, 0f, 0f);
        PlanetController planetController = new PlanetController(planet, this.getCamera());
        planet.addControl(planetController);
 
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        //planet.rotate(0, 0.005f*tpf, 0); 
        
        //simple collision detection
        Vector3f cameraLocation = this.getCamera().getLocation();        
        Vector3f planetToCamera = cameraLocation.subtract(planet.getLocalTranslation());
        float cameraHeight = planetToCamera.length();
        float r = planet.getRadius();
        float hs = planet.getHeightScale();
        float minHeight = (r + hs / 2 + 1f);
        this.getFlyByCamera().setMoveSpeed(FastMath.clamp(cameraHeight - minHeight, 25, 2000));
        if (cameraHeight < minHeight) {
            //this.getCamera().setLocation(planet.getLocalTranslation().add(planetToCamera.normalize().mult(minHeight)));
        }
        
    }
    
    private ActionListener actionListener = new ActionListener(){
        public void onAction(String name, boolean pressed, float tpf){
                        
            if (name.equals("TOGGLE_CURSOR") && !pressed) {
                if (inputManager.isCursorVisible()) {
                    inputManager.setCursorVisible(false);
                } else {
                    inputManager.setCursorVisible(true);
                }
            }
            
            if (name.equals("TOGGLE_WIREFRAME") && !pressed) {
                    planet.toogleWireframe();
            }
            
        }
    };
    
}