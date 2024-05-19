package main;

public class Main {

    // copyright (c) 2020-2023 lax1dude
    private static String vertexFragmentShader =
    "precision highp int;\n" +
    "precision highp sampler2D;\n" +
    "precision highp float;\n" +
    "\n" +
    "uniform mat4 matrix_m;\n" +
    "uniform mat4 matrix_p;\n" +
    "uniform mat4 matrix_t;\n" +
    "\n" +
    "#ifdef CC_VERT\n" +
    "\n" +
    "in vec3 a_position;\n" +
    "#ifdef CC_a_texture0\n" +
    "in vec2 a_texture0;\n" +
    "#endif\n" +
    "#ifdef CC_a_color\n" +
    "in vec4 a_color;\n" +
    "#endif\n" +
    "#ifdef CC_a_normal\n" +
    "in vec4 a_normal;\n" +
    "#endif\n" +
    "\n" +
    "#ifdef CC_fog\n" +
    "out vec4 v_position;\n" +
    "#endif\n" +
    "#ifdef CC_a_color\n" +
    "out vec4 v_color;\n" +
    "#endif\n" +
    "#ifdef CC_a_normal\n" +
    "out vec4 v_normal;\n" +
    "#endif\n" +
    "#ifdef CC_a_texture0\n" +
    "out vec2 v_texture0;\n" +
    "#endif\n" +
    "\n" +
    "void main(){\n" +
    "\tvec4 pos = matrix_m * vec4(a_position, 1.0);\n" +
    "#ifdef CC_fog\n" +
    "\tv_position = pos;\n" +
    "#endif\n" +
    "#ifdef CC_a_color\n" +
    "\tv_color = a_color;\n" +
    "#endif\n" +
    "#ifdef CC_a_normal\n" +
    "\tv_normal = a_normal;\n" +
    "#endif\n" +
    "#ifdef CC_a_texture0\n" +
    "\tv_texture0 = a_texture0;\n" +
    "#endif\n" +
    "\tgl_Position = matrix_p * pos;\n" +
    "}\n" +
    "\n" +
    "#endif\n" +
    "\n" +
    "#ifdef CC_FRAG\n" +
    "\n" +
    "#ifdef CC_unit0\n" +
    "uniform sampler2D tex0;\n" +
    "#ifndef CC_a_texture0\n" +
    "uniform vec2 texCoordV0;\n" +
    "#endif\n" +
    "#endif\n" +
    "#ifdef CC_lighting\n" +
    "uniform vec3 light0Pos;\n" +
    "uniform vec3 light1Pos;\n" +
    "uniform vec3 normalUniform;\n" +
    "#endif\n" +
    "#ifdef CC_fog\n" +
    "uniform vec4 fogColor;\n" +
    "uniform int fogMode;\n" +
    "uniform float fogStart;\n" +
    "uniform float fogEnd;\n" +
    "uniform float fogDensity;\n" +
    "uniform float fogPremultiply;\n" +
    "#endif\n" +
    "uniform vec4 colorUniform;\n" +
    "#ifdef CC_alphatest\n" +
    "uniform float alphaTestF;\n" +
    "#endif\n" +
    "\n" +
    "#ifdef CC_fog\n" +
    "in vec4 v_position;\n" +
    "#endif\n" +
    "#ifdef CC_a_color\n" +
    "in vec4 v_color;\n" +
    "#endif\n" +
    "#ifdef CC_a_normal\n" +
    "in vec4 v_normal;\n" +
    "#endif\n" +
    "#ifdef CC_a_texture0\n" +
    "in vec2 v_texture0;\n" +
    "#endif\n" +
    "\n" +
    "out vec4 fragColor;\n" +
    "\n" +
    "void main(){\n" +
    "#ifdef CC_a_color\n" +
    "\tvec4 color = colorUniform * v_color;\n" +
    "#else\n" +
    "\tvec4 color = colorUniform;\n" +
    "#endif\n" +
    "\t\n" +
    "#ifdef CC_unit0\n" +
    "#ifdef CC_a_texture0\n" +
    "\tcolor *= texture(tex0, (matrix_t * vec4(v_texture0, 0.0, 1.0)).xy).rgba;\n" +
    "#else\n" +
    "\tcolor *= texture(tex0, (matrix_t * vec4(texCoordV0, 0.0, 1.0)).xy).rgba;\n" +
    "#endif\n" +
    "#endif\n" +
    "\n" +
    "#ifdef CC_alphatest\n" +
    "\tif(color.a < alphaTestF){\n" +
    "\t\tdiscard;\n" +
    "\t}\n" +
    "#endif\n" +
    "\n" +
    "#ifdef CC_lighting\n" +
    "#ifdef CC_a_normal\n" +
    "\tvec3 normal = ((v_normal.xyz - 0.5) * 2.0);\n" +
    "#else\n" +
    "\tvec3 normal = normalUniform;\n" +
    "#endif\n" +
    "\tnormal = normalize(mat3(matrix_m) * normal);\n" +
    "\tfloat ins = max(dot(normal, -light0Pos), 0.0) + max(dot(normal, -light1Pos), 0.0);\n" +
    "\tcolor.rgb *= min((0.4 + ins * 0.6), 1.0);\n" +
    "#endif\n" +
    "\t\n" +
    "#ifdef CC_fog\n" +
    "\tfloat dist = sqrt(dot(v_position, v_position));\n" +
    "\tfloat i = (fogMode == 1) ? clamp((dist - fogStart) / (fogEnd - fogStart), 0.0, 1.0) : clamp(1.0 - pow(2.718, -(fogDensity * dist)), 0.0, 1.0);\n" +
    "\tcolor.rgb = mix(color.rgb, fogColor.xyz, i * fogColor.a);\n" +
    "#endif\n" +
    "\t\n" +
    "\tfragColor = color;\n" +
    "}\n" +
    "\n" +
    "#endif\n" +
    "";

    public static void main(String args[]) {

    }
}