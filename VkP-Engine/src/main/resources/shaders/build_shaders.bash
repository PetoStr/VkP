#!/bin/bash

for f in $(find . -regex '.*\.\(vert\|frag\)'); do
	glslangValidator -V $f -o $f.spv
done

