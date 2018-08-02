#version 330 core

in vec2 texCoordPass;
out vec4 colour;
uniform sampler2D tex;

void main()
{
	vec4 texColour = texture(tex, texCoordPass);
	
	if (texColour.a < 0.5)
		discard;
	
	colour = texColour;
	
}