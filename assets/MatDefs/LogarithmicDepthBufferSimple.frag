uniform vec4 m_Color;

#ifdef LOGARITHIMIC_DEPTH_BUFFER
uniform vec2 g_FrustumNearFar;
varying vec4 positionProjectionSpace;
#endif

varying vec4 vertexPosClip;

void main() {
    gl_FragColor = m_Color;

    #ifdef LOGARITHIMIC_DEPTH_BUFFER
        const float C = 1.0;
        const float offset = 1.0;
        gl_FragDepth = (log(C * positionProjectionSpace.z + offset) / log(C * g_FrustumNearFar.y + offset));
    #endif
}

