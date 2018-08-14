#version 330 core

out vec4 colour;
uniform sampler2D tex;
in vec2 texCoordsPass;

const float width = 0.5;
const float edge = 0.1;

void main(void)
{
	colour = texture(tex, texCoordsPass);
	
	float dis = 1.0 - colour.a;
	float alpha = 1.0 - smoothstep(width, width + edge, dis);
	
		
	colour = vec4(1.0, 1.0, 1.0, alpha);
	
}