#version 450

layout(location = 0) in vec4 in_pos;
layout(location = 1) in vec2 in_uv;

out gl_PerVertex {
	vec4 gl_Position;
};

layout(location = 0) out vec2 frag_uv;

layout (std140, push_constant) uniform push_constants {
	mat4 mp_matrix;
} push_consts;

void main(void)
{
	gl_Position = push_consts.mp_matrix * in_pos;
	frag_uv = in_uv;
}
