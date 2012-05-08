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

import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import com.jme3.shader.VarType;

/**
 * Quad
 * 
 * Credits
 * This code has been adapted from OgrePlanet
 * Copyright (c) 2010 Anders Lingfors
 * https://bitbucket.org/lingfors/ogreplanet/
 */
public class Planet extends Node {
    
    protected Material surfaceMaterial;
    protected Material oceanMaterial;
    protected float baseRadius = 50.0f;
    protected HeightDataSource dataSource;
    protected float scalingFactor = 1f;
    // Number of planer quads per patch. This value directly controls the 
    // complexity of the geometry generated.
    protected int quads = 32;
    // Minimal depth for spliting. The planet will start at this depth
    // no matter the distance from camera
    protected int minDepth = 1;
    // Max depth for splitting. The planet will only split down to this depth
    // no matter the distance from the camera
    protected int maxDepth = 10;
    protected Quad[] surfaceSide = new Quad[6];
    protected boolean wireframeMode;
    protected boolean oceanFloorCulling;
    protected Vector3f planetToCamera;
    protected Node oceanNode;
    
    /**
    * <code>Planet</code>
    * @param name Name of the node
    * @param baseRadius The radius of the planet
    * @param material The material applied to the planet
    * @param dataSource The <code>HeightDataSource</code> used for the terrain
    * @param quads Number of planer quads per patch.
    * @param minDepth Minimal depth for spliting.
    * @param maxDepth Max depth for splitting.
    * @return true if inside or intersecting camera frustum
    */
    public Planet(String name, float baseRadius, Material material, HeightDataSource dataSource, int quads, int minDepth, int maxDepth) {
        super(name);
        this.surfaceMaterial = material;
        this.baseRadius = baseRadius;
        this.dataSource = dataSource;
        this.quads = quads;
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        
         prepareSurface();
    }
    
    /**
    * <code>Planet</code>
    * @param name Name of the node
    * @param baseRadius The radius of the planet
    * @param material The material applied to the planet
    * @param dataSource The <code>HeightDataSource</code> used for the terrain
    */
    public Planet(String name, float baseRadius, Material material, HeightDataSource dataSource) {
        super(name);
        this.surfaceMaterial = material;
        this.baseRadius = baseRadius;
        this.dataSource = dataSource;
        
        prepareSurface();
    }
    
    public void createOcean(Material material) {
        this.oceanMaterial = material;
        
        if (oceanNode == null)
            prepareOcean();
    }
    
    public void setCameraPosition(Vector3f position) {
        
        // get vector between planet and camera
        this.planetToCamera = position.subtract(this.getLocalTranslation());
        
        // Update camera positions for all quads
        int currentMaxDepth = 0;
        for (int i = 0; i < 6; i++) {
            if (surfaceSide[i] != null) {
                surfaceSide[i].setCameraPosition(position);
                // get current max depth of quad for skirt toggling
                currentMaxDepth = Math.max(currentMaxDepth, surfaceSide[i].getCurrentMaxDepth());                
            }
        }
        
        boolean skirting;
        // Are we at minDepth?
        if (currentMaxDepth == this.minDepth ) {
            // Turn off skirting if entire sphere is at minDepth
            skirting = false;
            // swap ocean to sky bucket to avoid z-fighting
            if (oceanNode != null)
                oceanNode.setQueueBucket(queueBucket.Sky);
        } else {
            //otherwise turn on skirting
            skirting = true;
            // swap ocean to regular bucket
            if (oceanNode != null)
                oceanNode.setQueueBucket(queueBucket.Opaque);
        }
        // Go through and set skirting on all quads
        for (int i = 0; i < 6; i++) {
            if (surfaceSide[i] != null)
                surfaceSide[i].setSkirting(skirting);
        }
        
        // If we get close to the ocean floor, turn it on so we can see it.
        // It's normally turned off to avoid z-fighting issues
        if (oceanNode != null) {
            float distance = this.planetToCamera.length() - this.baseRadius;
            float floorThreshold = this.baseRadius / 100.0f;
            if (this.oceanFloorCulling)
                if (distance <= floorThreshold) {
                    this.oceanFloorCulling = false;
                    setOceanFloorCulling(this.oceanFloorCulling);
                }
            if (!this.oceanFloorCulling)
                if (distance > floorThreshold) {
                    this.oceanFloorCulling = true;
                    setOceanFloorCulling(this.oceanFloorCulling);
                }             
        }
    }
    
    public float getRadius() {
        return this.baseRadius;
    }
    
    public float getHeightScale() {
        return dataSource.getHeightScale();
    }
    
    public Vector3f getPlanetToCamera() {
        return this.planetToCamera;
    }
    
    public void toogleWireframe() {
        if (this.wireframeMode)
            wireframeMode = false;
        else
            wireframeMode = true;
        
        if (oceanNode != null)
            this.oceanMaterial.getAdditionalRenderState().setWireframe(wireframeMode);
        
        for (int i = 0; i < 6; i++) {
            if (surfaceSide[i] != null)
                surfaceSide[i].setWireframe(wireframeMode);
        }      
    }
    
    private void prepareSurface() {
        
        Vector3f rightMin = new Vector3f(1.0f, 1.0f, 1.0f);
        Vector3f rightMax = new Vector3f(1.0f, -1.0f, -1.0f);
        surfaceSide[0] = new Quad(
                "SurfaceRight",
                this.surfaceMaterial,
                this,
                rightMin,
                rightMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);
   
        Vector3f leftMin = new Vector3f(-1.0f, 1.0f, -1.0f);
        Vector3f leftMax = new Vector3f(-1.0f, -1.0f, 1.0f);
        surfaceSide[1] = new Quad(
                "SurfaceLeft",
                this.surfaceMaterial,
                this,
                leftMin,
                leftMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);

        Vector3f topMin = new Vector3f(-1.0f, 1.0f, -1.0f);
        Vector3f topMax = new Vector3f(1.0f, 1.0f, 1.0f);
        surfaceSide[2] = new Quad(
                "SurfaceTop",
                this.surfaceMaterial,
                this,
                topMin,
                topMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);

        Vector3f bottomMin = new Vector3f(-1.0f, -1.0f, 1.0f);
        Vector3f bottomMax = new Vector3f(1.0f, -1.0f, -1.0f);
        surfaceSide[3] = new Quad(
                "SurfaceBottom",
                this.surfaceMaterial,
                this,
                bottomMin,
                bottomMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);
      
        Vector3f backMin = new Vector3f(1.0f, 1.0f, -1.0f);
        Vector3f backMax = new Vector3f(-1.0f, -1.0f, -1.0f);
        surfaceSide[5] = new Quad(
                "SurfaceBack",
                this.surfaceMaterial,
                this,
                backMin,
                backMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);
        
        Vector3f frontMin = new Vector3f(-1.0f, 1.0f, 1.0f);
        Vector3f frontMax = new Vector3f(1.0f, -1.0f, 1.0f);
        surfaceSide[4] = new Quad(
                "SurfaceFront",
                this.surfaceMaterial,
                this,
                frontMin,
                frontMax,
                0f,
                FastMath.pow(2.0f, 20f),
                0f,
                FastMath.pow(2.0f, 20f),
                this.baseRadius,
                this.scalingFactor,
                this.dataSource,
                this.quads,
                0,
                this.minDepth,
                this.maxDepth,
                null,
                0);  
    }
    
    private void setOceanFloorCulling(boolean cull) {        
        if (this.surfaceMaterial.getMaterialDef().getMaterialParam("CullOceanFloor") != null) {
            for (int i = 0; i < 6; i++) {
                if (surfaceSide[i] != null)
                    surfaceSide[i].setMaterialParam("CullOceanFloor", VarType.Boolean, new Boolean(cull).toString());
            }
        }
    }
    
    private void prepareOcean() {        
        oceanNode = new Node("OceanNode");
        this.attachChild(oceanNode);
             
        Mesh sphere = new Sphere(100, 100, this.baseRadius + (this.baseRadius * 0.00025f), false, false);
        Geometry ocean = new Geometry("Ocean", sphere);
        
        ocean.setMaterial(this.oceanMaterial);
        ocean.rotate( FastMath.HALF_PI, 0, 0);
        
        oceanNode.attachChild(ocean);
    }
    
     
}
