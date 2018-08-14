#version 330 core

layout (location = 0) in vec3 vertex_modelSpace;
layout (location = 1) in vec3 colour;

uniform mat4 projectionMatrix;
uniform mat4 transformationMatrix;
uniform mat4 viewMatrix;

out vec3 colourPass;

void main()
{
	colourPass = colour;
	gl_Position = projectionMatrix * viewMatrix * transformationMatrix * vec4(vertex_modelSpace, 1.0);
}
