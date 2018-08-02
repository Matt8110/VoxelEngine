#version 330 core

layout (location = 0) in vec3 vertex_modelSpace;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 texCoord;

uniform mat4 projectionMatrix;
uniform mat4 transformationMatrix;
uniform mat4 viewMatrix;

const float density = 0.01;
const float gradient = 10;

out float visibility;

out vec2 texCoordPass;

void main()
{
	

	texCoordPass = texCoord;
	
	vec4 positionRelativeToCam = viewMatrix * transformationMatrix * vec4(vertex_modelSpace, 1.0);
	float dis = length(positionRelativeToCam.xyz);
	visibility = exp(-pow(dis * density, gradient));
	visibility = clamp(visibility, 0.0, 1.0);
	
	gl_Position = projectionMatrix * positionRelativeToCam;
}
