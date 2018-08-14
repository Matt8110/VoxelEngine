#version 330 core

out vec4 colour;

in vec3 colourPass;

void main()
{
	
	colour = vec4(colourPass, 1);
}
