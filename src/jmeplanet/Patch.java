package jmeplanet;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import com.jme3.bounding.BoundingBox;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Patch {
       
    protected int quads;
    protected Vector3f min;
    protected Vector3f max;
    protected float texXMin;
    protected float texXMax;
    protected float texYMin;
    protected float texYMax;
    protected float baseRadius;
    protected float scalingFactor;
    protected HeightDataSource dataSource;
    protected int position;
    
    protected int padding = 2;
    protected Mesh mesh;
    protected BoundingBox aabb;
    protected Vector3f center;
   
    public Patch(
            int quads,
            Vector3f min,
            Vector3f max,
            float texXMin,
            float texXMax,
            float texYMin,
            float texYMax,
            float baseRadius,
            float scalingFactor,
            HeightDataSource dataSource,
            int position) {
        
        this.quads = quads;
        this.min = min;
        this.max = max;
        this.texXMin = texXMin;
        this.texXMax = texXMax;
        this.texYMin = texYMin;
        this.texYMax = texYMax;
        this.baseRadius = baseRadius;
        this.scalingFactor = scalingFactor;
        this.dataSource = dataSource;
        this.position = position;
    }
    
    public Mesh prepare() {
        
        int quadVertexCount = (this.quads + 1) * (this.quads + 1);
        int quadVertexCountPadded = (this.quads + 2*this.padding + 1) * (this.quads + 2*this.padding + 1);
        int skirtVertexCount = this.quads * 4;
        int totalVertexCount = quadVertexCount + skirtVertexCount;
        int verticesPerSide = this.quads + 1;
        int quadTriangles = (2 * quads * quads);
        int skirtTriangles = skirtVertexCount * 2;
        int totalTriangles = quadTriangles + skirtTriangles;

        // Calculate vertex positions, normals, etc
        Vector3f[] vertexPosition = new Vector3f[quadVertexCountPadded];
        float[] vertexColor = new float[4 * quadVertexCountPadded];
        Vector3f[] vertexNormal = new Vector3f[quadVertexCount];
        
        generateVertexPositions(vertexPosition, vertexColor);
        generateVertexNormals(vertexNormal, vertexPosition);
        
        // Create final buffers
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(3 * totalVertexCount);
        FloatBuffer colorBuffer = BufferUtils.createFloatBuffer((4 * totalVertexCount));
        FloatBuffer normalBuffer = BufferUtils.createFloatBuffer(3 * totalVertexCount);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(3 * totalTriangles);
        // Fill final buffers
        for (int y = 0; y < (this.quads + 1); y++)
        {
            for (int x = 0; x < (this.quads + 1); x++)
            {
                int vi = (this.quads + 2 * this.padding + 1) * (y + this.padding) + (x + this.padding);
                int ni = (this.quads + 1) * y + x;

                // Vertex pos
                vertexBuffer.put(vertexPosition[vi].x);
                vertexBuffer.put(vertexPosition[vi].y);
                vertexBuffer.put(vertexPosition[vi].z);
                // Vertex color
                colorBuffer.put(vertexColor[vi * 4 ]);
                colorBuffer.put(vertexColor[vi * 4 + 1]);
                colorBuffer.put(vertexColor[vi * 4 + 2]);
                colorBuffer.put(vertexColor[vi * 4 + 3]);
                // Vertex normal
                normalBuffer.put(vertexNormal[ni].x);
                normalBuffer.put(vertexNormal[ni].y);
                normalBuffer.put(vertexNormal[ni].z);
            }
        }
        
        // Get the patch's edge vertex indexes going clockwise
        int indexEdgeVertexIndex = 0;
        int[] edgeVertexIndex = new int[skirtVertexCount];
        for (int i = 0; i < verticesPerSide; i++)
            edgeVertexIndex[indexEdgeVertexIndex++] = i;
        for (int i = verticesPerSide + this.quads; i < quadVertexCount + 1; i+=verticesPerSide)
            edgeVertexIndex[indexEdgeVertexIndex++] = i;
        for (int i = quadVertexCount - 2; i >= verticesPerSide * quads; i--)
            edgeVertexIndex[indexEdgeVertexIndex++] = i;
        for (int i = verticesPerSide * quads - verticesPerSide; i > 0; i-=verticesPerSide)
            edgeVertexIndex[indexEdgeVertexIndex++] = i;
        
        // Add skirt to end of vertex buffer
        for (int i = 0; i < skirtVertexCount; i++) {
            // Make skirt 1/4th the height scale
            Vector3f v = new Vector3f(
                    vertexBuffer.get(3 * edgeVertexIndex[i]), 
                    vertexBuffer.get(3 * edgeVertexIndex[i] + 1),
                    vertexBuffer.get(3 * edgeVertexIndex[i] + 2));
            v.subtractLocal(this.center.normalize().mult(this.dataSource.getHeightScale() / 4));
            vertexBuffer.put(v.x);
            vertexBuffer.put(v.y);
            vertexBuffer.put(v.z);

            normalBuffer.put(normalBuffer.get(3 * edgeVertexIndex[i]));
            normalBuffer.put(normalBuffer.get(3 * edgeVertexIndex[i] + 1));
            normalBuffer.put(normalBuffer.get(3 * edgeVertexIndex[i] + 2));
            
            colorBuffer.put(colorBuffer.get(edgeVertexIndex[i] * 4));
            colorBuffer.put(colorBuffer.get(edgeVertexIndex[i] * 4 + 1));
            colorBuffer.put(colorBuffer.get(edgeVertexIndex[i] * 4 + 2));
            colorBuffer.put(colorBuffer.get(edgeVertexIndex[i] * 4 + 3));
        }
      
        generateIndices(indexBuffer, edgeVertexIndex);

        // Set mesh buffers
        mesh = new Mesh();
        mesh.setBuffer(Type.Position, 3, vertexBuffer);
        mesh.setBuffer(Type.Normal, 3, normalBuffer);
        mesh.setBuffer(Type.Color, 4, colorBuffer);
        mesh.setBuffer(Type.Index, 3, indexBuffer);  
        mesh.updateBound();
        
        return mesh; 
    }
        
    public boolean isPrepared() {
        return this.mesh != null;
    }
    
    public Mesh getMesh() {
        return this.mesh;
    }
    
    public Vector3f getCenter() {
        return this.center;
    }
    
    public BoundingBox getAABB() {
        return this.aabb;
    }
    
    protected void generateVertexPositions(Vector3f[] vertexPosition, float[] vertexColor) {
        
       // Use "texture coordinates"
        // _xPos is where U axis changes
        float xPos = 0;
        // _yPos is where V axis changes
        float yPos = 0;
        // _zPos is where the normal would be
        float zPos = 0;

        Vector2f startPos = new Vector2f();
        Vector2f endPos = new Vector2f();

        int a = 0;
        int b = 0;
        int c = 0;
        int side = 0;
        
        if (this.min.x == 1.0 && this.max.x == 1.0) {
            // Patch on right side
            // On right side, T coordinate lies in Z axis
            // U coordinate lies in Y axis
            // X axis is normal
            side = 0;
            startPos.x = this.min.z;
            startPos.y = this.min.y;
            endPos.x = this.max.z;
            endPos.y = this.max.y;
            a = -1;
            b = -1;
            c = 1;
        }
        if (this.min.x == -1.0 && this.max.x == -1.0) {
            // Patch on left side
            side = 1;
            startPos.x = this.min.z;
            startPos.y = this.min.y;
            endPos.x = this.max.z;
            endPos.y = this.max.y;
            a = 1;
            b = -1;
            c = -1;
        } else if (this.min.y == 1.0 && this.max.y == 1.0) {
            // Patch on top side
            side = 2;
            startPos.x = this.min.x;
            startPos.y = this.min.z;
            endPos.x = this.max.x;
            endPos.y = this.max.z;
            a = 1;
            b = 1;
            c = 1;
        } else if (this.min.y == -1.0 && this.max.y == -1.0) {
            // Patch on bottom side
            side = 3;
            startPos.x = this.min.x;
            startPos.y = this.min.z;
            endPos.x = this.max.x;
            endPos.y = this.max.z;
            a = 1;
            b = -1;
            c = -1;
        } else if (this.min.z == 1.0 && this.max.z == 1.0) {
            // Patch on front side
            side = 4;
            startPos.x = this.min.x;
            startPos.y = this.min.y;
            endPos.x = this.max.x;
            endPos.y = this.max.y;
            a = 1;
            b = -1;
            c = 1;
        } else if (this.min.z == -1.0 && this.max.z == -1.0) {
            // Patch on back side
            side = 5;
            startPos.x = this.min.x;
            startPos.y = this.min.y;
            endPos.x = this.max.x;
            endPos.y = this.max.y;
            a = -1;
            b = -1;
            c = -1;
        }

        // Calculate unit sphere positions
        Vector3f[] unitSpherePos = new Vector3f[(this.quads + 2*this.padding + 1) * (this.quads + 2*this.padding + 1)];
        float[] heightData = new float[(this.quads + 2*this.padding + 1) * (this.quads + 2*this.padding + 1)];
        Vector3f pos = new Vector3f();
        for (int y = 0-this.padding; y <= (this.quads + this.padding); y++) {
            for (int x = 0-this.padding; x <= (this.quads + this.padding); x++) {
                int index = (this.quads + 2*this.padding + 1) * (y + this.padding) + (x + this.padding);

                xPos = (startPos.x + (endPos.x - startPos.x) * (((float) x)/this.quads));
                yPos = (startPos.y + (endPos.y - startPos.y) * (((float) y)/this.quads));
                zPos = c;
                
                switch (side) {
                    case 0: pos.x = zPos; pos.y = yPos; pos.z = xPos; break;
                    case 1: pos.x = zPos; pos.y = yPos; pos.z = xPos; break;
                    case 2: pos.x = xPos; pos.y = zPos; pos.z = yPos; break;
                    case 3: pos.x = xPos; pos.y = zPos; pos.z = yPos; break;
                    case 4: pos.x = xPos; pos.y = yPos; pos.z = zPos; break;
                    case 5: pos.x = xPos; pos.y = yPos; pos.z = zPos; break;
                }
              
                // normalize the position making it curved
                unitSpherePos[index] = pos.normalize();
                
                // get height data for given index
                heightData[index] = this.dataSource.getValue(unitSpherePos[index]);                    
            }
        }
              
        // Now calculate vertex positions (with padding) in planet space
        Vector3f minBounds = new Vector3f(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        Vector3f maxBounds = new Vector3f(-Integer.MAX_VALUE, -Integer.MAX_VALUE, -Integer.MAX_VALUE);
        for (int y = 0; y < (this.quads + 2*this.padding + 1); y++) {
            for (int x = 0; x < (this.quads + 2*this.padding + 1); x++) {
                
                int index = (this.quads + 2*this.padding + 1) * y + x;
 
                vertexPosition[index] = unitSpherePos[index].mult(baseRadius + heightData[index] * scalingFactor);

                minBounds.x = Math.min(minBounds.x, vertexPosition[index].x);
                minBounds.y = Math.min(minBounds.y, vertexPosition[index].y);
                minBounds.z = Math.min(minBounds.z, vertexPosition[index].z);
                maxBounds.x = Math.max(maxBounds.x, vertexPosition[index].x);
                maxBounds.y = Math.max(maxBounds.y, vertexPosition[index].y);
                maxBounds.z = Math.max(maxBounds.z, vertexPosition[index].z);
                
                // Set vertex colors
                if( heightData[index] <= 0f ) {
                    vertexColor[index * 4 ]=(0.0f);
                    vertexColor[index * 4 +1]=(0.4f);
                    vertexColor[index * 4 +2]=(0.8f);
                    vertexColor[index * 4 +3]=(1.0f); // Ocean
                } else if( heightData[index] <= 15f ) {
                    vertexColor[index * 4 ]=(0.83f);
                    vertexColor[index * 4 +1]=(0.72f);
                    vertexColor[index * 4 +2]=(0.34f);
                    vertexColor[index * 4 +3]=(1.0f); // Sand
                } else if( heightData[index] <= 125f ) {
                    vertexColor[index * 4 ]=(0.2f);
                    vertexColor[index * 4 +1]=(0.6f);
                    vertexColor[index * 4 +2]=(0.1f);
                    vertexColor[index * 4 +3]=(1.0f); // Grass
                } else { 
                    vertexColor[index * 4 ]=(0.5f);
                    vertexColor[index * 4 +1]=(0.5f);
                    vertexColor[index * 4 +2]=(0.5f);
                    vertexColor[index * 4 +3]=(1.0f); // Mountains
                }
                
            }
        }
        
        // Transform vertex positions to object space (i.e. centered around origin)
        this.aabb = new BoundingBox(minBounds, maxBounds);
        this.center = aabb.getCenter();
        minBounds = minBounds.subtract(this.center);
        maxBounds = maxBounds.subtract(this.center);
        for (int y = 0; y < (this.quads + 2*this.padding + 1); y++)
        {
                for (int x = 0; x < (this.quads + 2*this.padding + 1); x++)
                {
                        int index = (this.quads + 2*this.padding + 1) * y + x;
                        vertexPosition[index] = vertexPosition[index].subtract(center);
                }
        }
        
    }
    
    protected void generateVertexNormals(Vector3f[] vertexNormal, Vector3f[] vertexPosition) {
       
        // Calculate vertex normals
        for (int y = 0; y < (this.quads + 1); y++)
        {
            for (int x = 0; x < (this.quads + 1); x++)
            {
                int index = (this.quads + 1) * y + x;

                // 6-connected, with triangle-area correction
                int pIndex = (this.quads + 2*this.padding + 1) * (y + this.padding) + (x + this.padding);
                int pNextXIndex = (this.quads + 2*this.padding + 1) * (y + this.padding) + (x + this.padding + 1);
                int pNextYIndex = (this.quads + 2*this.padding + 1) * (y + this.padding + 1) + (x + this.padding);
                int pPrevXIndex = (this.quads + 2*this.padding + 1) * (y + this.padding) + (x + this.padding - 1);
                int pPrevYIndex = (this.quads + 2*this.padding + 1) * (y + this.padding - 1) + (x + this.padding);
                int pNextXPrevYIndex = (this.quads + 2*this.padding + 1) * (y + this.padding - 1) + (x + this.padding + 1);
                int pPrevXNextYIndex = (this.quads + 2*this.padding + 1) * (y + this.padding + 1) + (x + this.padding - 1);

                Vector3f thisVertex = vertexPosition[pIndex];
                Vector3f nextXVertex = vertexPosition[pNextXIndex];
                Vector3f nextYVertex = vertexPosition[pNextYIndex];
                Vector3f prevXVertex = vertexPosition[pPrevXIndex];
                Vector3f prevYVertex = vertexPosition[pPrevYIndex];
                Vector3f nextXPrevYVertex = vertexPosition[pNextXPrevYIndex];
                Vector3f prevXNextYVertex = vertexPosition[pPrevXNextYIndex];

                Vector3f n1 = nextXVertex.subtract(thisVertex).cross(nextXPrevYVertex.subtract(thisVertex));
                Vector3f n2 = nextXPrevYVertex.subtract(thisVertex).cross(prevYVertex.subtract(thisVertex));
                Vector3f n3 = prevYVertex.subtract(thisVertex).cross(prevXVertex.subtract(thisVertex));
                Vector3f n4 = prevXVertex.subtract(thisVertex).cross(prevXNextYVertex.subtract(thisVertex));
                Vector3f n5 = prevXNextYVertex.subtract(thisVertex).cross(nextYVertex.subtract(thisVertex));
                Vector3f n6 = nextYVertex.subtract(thisVertex).cross(nextXVertex.subtract(thisVertex));

                vertexNormal[index] = (n1.add(n2).add(n3).add(n4).add(n5).add(n6)).normalize();
            }
        }
        
    }
    
    protected void generateIndices(IntBuffer indexBuffer, int[] edgeVertexIndex) {
        
        for (int y = 0; y < quads; y++) {
            for (int x = 0; x < quads; x++) {
                indexBuffer.put(y * (quads + 1) + x);
                indexBuffer.put((y + 1) * (quads + 1) + x);
                indexBuffer.put(y * (quads + 1) + x + 1);
                indexBuffer.put((y + 1) * (quads + 1) + x);
                indexBuffer.put((y + 1) * (quads + 1) + x + 1);
                indexBuffer.put(y * (quads + 1) + x + 1);
            }
        }  

        int skirtOffset = (this.quads + 1) * (this.quads + 1);
        for (int y = 0; y < 1; y++) {
            for (int x = 0; x < edgeVertexIndex.length; x++) {
                if (x != edgeVertexIndex.length - 1) {
                    indexBuffer.put(edgeVertexIndex[x]);
                    indexBuffer.put(edgeVertexIndex[x + 1]);
                    indexBuffer.put(x + skirtOffset);                
                    indexBuffer.put(x + skirtOffset);
                    indexBuffer.put(edgeVertexIndex[x + 1]);       
                    indexBuffer.put(x + 1 + skirtOffset);
                } else {
                    indexBuffer.put(edgeVertexIndex[x]);
                    indexBuffer.put(edgeVertexIndex[0]);
                    indexBuffer.put(x + skirtOffset);                
                    indexBuffer.put(x + skirtOffset);
                    indexBuffer.put(edgeVertexIndex[0]);       
                    indexBuffer.put(skirtOffset);
                }
                
            }
        }
        
    }
        
}
