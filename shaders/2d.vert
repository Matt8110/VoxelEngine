#version 330 core

layout (location = 0) in vec3 vertex;
layout (location = 1) in vec2 texCoord;

out vec2 texCoordPass;

void main()
{
	texCoordPass = texCoord;
	gl_Position = vec4(vertex, 1.0);
}