uniform float m_tilingFactor;
uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform mat3 g_NormalMatrix;

uniform float m_terrainSize;

attribute vec2 inTexCoord;
attribute vec3 inNormal;
attribute vec3 inPosition;

varying vec3 normal;
varying vec4 position;
varying vec2 texCoord1;

void main()
{
 	normal = normalize(inNormal);
 	position = g_WorldMatrix * vec4(inPosition, 0.0);
        texCoord1 = inTexCoord;
        gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1);
}


