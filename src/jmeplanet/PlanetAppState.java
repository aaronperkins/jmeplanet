/*
Copyright (c) 2012 Aaron Perkins

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package jmeplanet;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import java.util.ArrayList;
import java.util.List;

/**
 * PlanetAppState
 * 
 */
public class PlanetAppState extends AbstractAppState {
    
    protected Application app;
    protected List<Planet> planets;
    
    public PlanetAppState() {
        this.planets = new ArrayList<Planet>();    
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        
        this.app = app;    
    }
            
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
    
    @Override
    public void update(float tpf) {
        for (Planet planet: this.planets ) {
            planet.setCameraPosition(this.app.getCamera().getLocation());
        }
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
    }
    
    public void addPlanet(Planet planet) {
        this.planets.add(planet);
    }
    
    public List<Planet> getPlanets() {
        return this.planets;
    }
    
    public Planet getClosestPlanet() {
        Planet cPlanet = null;
        for (Planet planet: this.planets ) {
            if (cPlanet == null || cPlanet.getDistanceToCamera() > planet.getDistanceToCamera()) {
                cPlanet = planet;
            }
        }
        return cPlanet;
    }
  
}
