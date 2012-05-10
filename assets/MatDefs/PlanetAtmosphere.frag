uniform sampler2D m_AtmosphereGradient;
uniform float m_Atmosphere_G;

varying vec4 oPosition;
varying vec2 oUV;
varying float oAlpha;
varying vec3 oCamToPos;
varying vec3 oLightDir;

void main() {

    const float fExposure = .75;
    float g = m_Atmosphere_G;
    float g2 = g * g;
 
    // atmosphere color
    vec4 diffuse = texture2D(m_AtmosphereGradient,oUV);
  
    // sun outer color - might could use atmosphere color
    vec4 diffuse2 = texture2D(m_AtmosphereGradient,vec2(min(0.5,oUV.x),1));
 
    // this is equivilant but faster than 
    float fCos = dot(normalize(oLightDir.xyz),normalize(oCamToPos));
    //float fCos = dot(oLightDir.xyz,oCamToPos) * inversesqrt( dot(oLightDir.xyz,oLightDir.xyz) * dot(oCamToPos,oCamToPos));
    float fCos2 = fCos * fCos;
 
    // apply alpha to atmosphere
    vec4 diffuseColor = diffuse * oAlpha;
     
    // sun glow color
    float fMiePhase = 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + fCos2) /(1.0 + g2 - 2.0*g*fCos);
    vec4 mieColor = diffuse2 * fMiePhase * oAlpha;
   
    // use exponential falloff because mie color is in high dynamic range
    // boost diffuse color near horizon because it gets desaturated by falloff
    gl_FragColor = 1.0 - exp((diffuseColor * (1.0 + oUV.y) + mieColor) * -fExposure);
    gl_FragColor.a = gl_FragColor.a * .95;
}

