#version 330 core

out vec4 colour;
in vec2 texCoordPass;

in float visibility;

vec3 skyColour = vec3(0.4, 0.4, 1.0);

uniform sampler2D texSample; 

void main()
{
	vec4 tex = texture(texSample, texCoordPass);

	colour = tex;
	colour = mix(vec4(skyColour, 1.0), colour, visibility);
}
