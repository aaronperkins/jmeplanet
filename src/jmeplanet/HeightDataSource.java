/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmeplanet;

import com.jme3.math.Vector3f;

/**
 *
 * @author aaron
 */
public interface HeightDataSource {
       
    public float getValue(Vector3f position);
    
}
