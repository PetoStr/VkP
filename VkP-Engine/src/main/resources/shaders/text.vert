#version 450

layout(location = 0) in vec2 in_pos;
layout(location = 1) in vec2 in_uv;

out gl_PerVertex {
	vec4 gl_Position;
};

layout(location = 0) out vec2 out_uv;

void main(void)
{
	gl_Position = vec4(in_pos, 0.99, 1.0);
	out_uv = in_uv;
}
