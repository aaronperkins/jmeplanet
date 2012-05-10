uniform mat4 g_WorldViewProjectionMatrix;
#ifdef LOGARITHIMIC_DEPTH_BUFFER
uniform vec2 g_FrustumNearFar;
#endif

attribute vec3 inPosition;

void main() { 
    gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);

    #ifdef LOGARITHIMIC_DEPTH_BUFFER
    const float C = 1.0;
    gl_Position.z = (2*log(C*gl_Position.z + 1) / log(C*g_FrustumNearFar.y + 1) - 1) * gl_Position.w;
    #endif

}
