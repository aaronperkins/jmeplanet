package jmeplanet.test;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.Geometry;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.debug.Grid;

public class Utility {
    
    public static Node createGridAxis(int lines, int spacing, AssetManager assetManager) {
        Node grid = new Node("Grid Axis");
        
        float half_size = (lines * spacing) / 2.0f - (spacing / 2);
        
        Geometry xGrid = new Geometry();
        Material xMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        xMat.setColor("Color", ColorRGBA.Blue);
        xGrid.setMesh(new Grid(lines,lines,spacing));
        xGrid.setMaterial(xMat);
        grid.attachChild(xGrid);
        xGrid.setLocalTranslation(-half_size, 0, -half_size);
        
        Geometry yGrid = new Geometry();
        Material yMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        yMat.setColor("Color", ColorRGBA.Green);
        yGrid.setMesh(new Grid(lines,lines,spacing));
        yGrid.setMaterial(yMat);
        grid.attachChild(yGrid);
        yGrid.rotate(FastMath.HALF_PI, 0, 0);
        yGrid.setLocalTranslation(-half_size, half_size, 0);
        
        Geometry zGrid = new Geometry();
        Material zMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        zMat.setColor("Color", ColorRGBA.Red);
        zGrid.setMesh(new Grid(lines,lines,spacing));
        zGrid.setMaterial(zMat);
        grid.attachChild(zGrid);
        zGrid.rotate(0, 0, FastMath.HALF_PI);
        zGrid.setLocalTranslation(0, -half_size, -half_size);
        
        return grid;
    }
    
}
