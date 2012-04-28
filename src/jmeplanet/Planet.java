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
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * Quad
 * 
 * Credits
 * This code has been adapted from OgrePlanet
 * Copyright (c) 2010 Anders Lingfors
 * https://bitbucket.org/lingfors/ogreplanet/
 */
public class Planet extends Node {
    
    protected Material material;
    protected float baseRadius = 50.0f;
    protected float scalingFactor = 1f;
    protected int quads = 32;
    protected int minDepth = 1;
    protected int maxDepth = 10;
    protected Quad[] surfaceSide = new Quad[6];
    protected HeightDataSource dataSource;
    
    protected boolean wireframeMode;
    
    public Planet(String name, float baseRadius, Material material, HeightDataSource dataSource) {
        super(name);
        this.material = material;
        this.baseRadius = baseRadius;
        this.dataSource = dataSource;
        
        Vector3f rightMin = new Vector3f(1.0f, 1.0f, 1.0f);
        Vector3f rightMax = new Vector3f(1.0f, -1.0f, -1.0f);
        surfaceSide[0] = new Quad(
                "SurfaceRight",
                this.material,
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
                this.material,
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
                this.material,
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
                this.material,
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
                this.material,
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
                this.material,
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
    
    public void setCameraPosition(Vector3f position) {
        
        int currentMaxDepth = this.minDepth;
        
        for (int i = 0; i < 6; i++) {
            if (surfaceSide[i] != null) {
                surfaceSide[i].setCameraPosition(position);
                currentMaxDepth = Math.max(currentMaxDepth, surfaceSide[i].getCurrentMaxDepth());                
            }
        }
        
        boolean skirting;
        if (currentMaxDepth == this.minDepth )
            skirting = false;
        else
            skirting = true;
        for (int i = 0; i < 6; i++) {
            if (surfaceSide[i] != null)
                surfaceSide[i].setSkirting(skirting);
        }

        
    }
    
    public float getRadius() {
        return this.baseRadius;
    }
    
    public float getHeightScale() {
        return dataSource.getHeightScale();
    }
    
    public void toogleWireframe() {
        
        if (this.wireframeMode)
            wireframeMode = false;
        else
            wireframeMode = true;
        
        for (int i = 0; i < 6; i++) {
            if (surfaceSide[i] != null)
                surfaceSide[i].setWireframe(wireframeMode);
        }
        
    }
     
}
