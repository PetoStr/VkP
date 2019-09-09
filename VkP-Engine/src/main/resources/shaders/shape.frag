#version 450

layout(location = 0) in vec2 frag_uv;

layout(location = 0) out vec4 out_col;

layout(set = 0, binding = 0) uniform sampler2D text;

void main(void)
{
	out_col = texture(text, frag_uv);
}
