package jmeplanet;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;

public class PlanetController extends AbstractControl {

    protected Planet planet;
    protected Camera camera;
    
    public PlanetController() {
    }

    public PlanetController(Planet planet, Camera camera) {
        this.planet = planet;
        this.camera = camera;
    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {

    }

    @Override
    protected void controlUpdate(float tpf) {
        planet.setCameraPosition(camera.getLocation());
    }
    
    public Control cloneForSpatial(Spatial spatial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
