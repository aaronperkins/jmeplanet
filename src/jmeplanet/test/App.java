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


import jmeplanet.Planet;
import jmeplanet.PlanetController;
import jmeplanet.FractalDataSource;
 
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
        
        // Only show severe errors in log
        java.util.logging.Logger.getLogger("com.jme3").setLevel(java.util.logging.Level.SEVERE);
        
        // Release mouse on click
        inputManager.addMapping("MOUSE_BUTTON_LEFT", 
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
                new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "MOUSE_BUTTON_LEFT"); 
        inputManager.setCursorVisible(true);
        
        // Setup camera
        this.getCamera().setFrustumFar(20000f);
        this.getCamera().setLocation(new Vector3f(0f, 0f, 9000f));
        this.getFlyByCamera().setMoveSpeed(1000.0f);
        
        // Add sun
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);
        
        // Add planet
        FractalDataSource dataSource = new FractalDataSource();
        dataSource.setHeightScale(150f);
        
        //Material material = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");            
        //Material material = new Material(this.assetManager, "Common/MatDefs/Light/Lighting.j3md");
        //Material material = this.getAssetManager().loadMaterial("Materials/grass.j3m");
        //material.setColor("Color", ColorRGBA.White);
        //material.setBoolean("VertexColor", true);
        //material.setBoolean("UseVertexColor", true);
        //material.getAdditionalRenderState().setWireframe(true);
        
         // TERRAIN TEXTURE material
        Material material = new Material(this.assetManager, "MatDefs/HeightBasedTerrain.j3md");

        // Parameters to material:
        float grassScale = 64;
        float dirtScale = 16;
        float rockScale = 128;
        
         // DIRT texture
        Texture dirt = this.assetManager.loadTexture("Textures/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        material.setTexture("region1ColorMap", dirt);
        material.setVector3("region1", new Vector3f(0, 25, dirtScale));
        
        // GRASS texture
        Texture grass = this.assetManager.loadTexture("Textures/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        material.setTexture("region2ColorMap", grass);
        material.setVector3("region2", new Vector3f(15, 90, grassScale));

        // ROCK texture
        Texture rock = this.assetManager.loadTexture("Textures/rock.jpg");
        rock.setWrap(WrapMode.Repeat);
        material.setTexture("region3ColorMap", rock);
        material.setVector3("region3", new Vector3f(80, 200, rockScale));

        material.setTexture("region4ColorMap", rock);
        material.setVector3("region4", new Vector3f(80, 200, rockScale));


        
        planet = new Planet("Planet", 5000f, material, dataSource);
        PlanetController planetController = new PlanetController(planet, this.getCamera());
        planet.addControl(planetController);
        rootNode.attachChild(planet);
        
        planet.setLocalTranslation(0f, 0f, 0f);
         
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
        float minHeight = (r + hs + 1f);
        if (cameraHeight < minHeight) {
            this.getCamera().setLocation(planet.getLocalTranslation().add(planetToCamera.normalize().mult(minHeight)));
        }
        
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