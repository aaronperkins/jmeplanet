uniform mat4 g_WorldViewProjectionMatrix;

#ifdef LOGARITHIMIC_DEPTH_BUFFER
varying vec4 positionProjectionSpace;
#endif

attribute vec4 inPosition;

void main() { 
    gl_Position = g_WorldViewProjectionMatrix * inPosition;

    #ifdef LOGARITHIMIC_DEPTH_BUFFER
        positionProjectionSpace = gl_Position;
    #endif
}
