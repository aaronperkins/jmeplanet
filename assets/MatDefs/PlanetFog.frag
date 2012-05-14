uniform vec2 g_FrustumNearFar;
uniform sampler2D m_Texture;
uniform sampler2D m_DepthTexture;
varying vec2 texCoord;

uniform vec4 m_FogColor;
uniform float m_FogDensity;
uniform float m_FogDistance;

void main() {
       vec4 texVal = texture2D(m_Texture, texCoord);      
       float fogVal = texture2D(m_DepthTexture,texCoord).r;

       float depth = fogVal;
       float fogFactor = exp( -(depth - m_FogDistance) * m_FogDensity);

       fogFactor = clamp(fogFactor, 0.0, 1.0);

       /*
       float debug = fogFactor;
       gl_FragColor = vec4(debug, 0, 0, 1);
       if (debug > 1)
            gl_FragColor = vec4(0, debug, 0, 1);
       if (debug < 0)
            gl_FragColor = vec4(0, 0, debug, 1);
       */

       gl_FragColor = mix(m_FogColor,texVal,fogFactor);

}