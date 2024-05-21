package main;

import org.lwjgl.util.vector.*;

import java.nio.FloatBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.Window;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.webgl.WebGLBuffer;
import org.teavm.jso.webgl.WebGLFramebuffer;
import org.teavm.jso.webgl.WebGLProgram;
import org.teavm.jso.webgl.WebGLRenderbuffer;
import org.teavm.jso.webgl.WebGLRenderingContext;
import org.teavm.jso.webgl.WebGLTexture;
import org.teavm.jso.webgl.WebGLUniformLocation;

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

    public static HTMLDocument document = null;
	public static HTMLElement parent = null;
	public static HTMLCanvasElement canvas = null;
	public static CanvasRenderingContext2D canvasContext = null;
	public static HTMLCanvasElement canvasBack = null;
	public static WebGL2RenderingContext webgl = null;
	public static Window window = null;
	
	private static int width = 0;
	private static int height = 0;
	
	public static void main(String args[]) throws LWJGLException{
		window = Window.current();
		document = window.getDocument();
		parent = document.getBody();
		
		String s = parent.getAttribute("style");
		parent.setAttribute("style", (s == null ? "" : s)+"overflow-x:hidden;overflow-y:hidden;");
		
		canvas = (HTMLCanvasElement)document.createElement("canvas");
		width = parent.getClientWidth();
		height = parent.getClientHeight();
		canvas.setWidth(width);
		canvas.setHeight(height);
		canvasContext = (CanvasRenderingContext2D) canvas.getContext("2d");
		parent.appendChild(canvas);
		canvasBack = (HTMLCanvasElement)document.createElement("canvas");
		canvasBack.setWidth(width);
		canvasBack.setHeight(height);
		webgl = (WebGL2RenderingContext) canvasBack.getContext("webgl2", WebGLConfig());
		if(webgl == null) {
			throw new LWJGLException("WebGL 2.0 is not supported in your browser :(");
		}
		setCurrentContext(webgl);
		
		webgl.getExtension("EXT_texture_filter_anisotropic");
	}
	
	@JSBody(params = { }, script = "return {antialias: false, depth: true, powerPreference: \"high-performance\", desynchronized: false, preserveDrawingBuffer: false, premultipliedAlpha: false, alpha: false};")
	public static native JSObject WebGLConfig();
	
	@JSBody(params = { "obj" }, script = "window.currentContext = obj;")
	private static native int setCurrentContext(JSObject obj);

    public static class GLEnums {
        public static final int GL_ACCUM = 256;
		public static final int GL_LOAD = 257;
		public static final int GL_RETURN = 258;
		public static final int GL_MULT = 259;
		public static final int GL_ADD = 260;
		public static final int GL_NEVER = 512;
		public static final int GL_LESS = 513;
		public static final int GL_EQUAL = 514;
		public static final int GL_LEQUAL = 515;
		public static final int GL_GREATER = 516;
		public static final int GL_NOTEQUAL = 517;
		public static final int GL_GEQUAL = 518;
		public static final int GL_ALWAYS = 519;
		public static final int GL_CURRENT_BIT = 1;
		public static final int GL_POINT_BIT = 2;
		public static final int GL_LINE_BIT = 4;
		public static final int GL_POLYGON_BIT = 8;
		public static final int GL_POLYGON_STIPPLE_BIT = 16;
		public static final int GL_PIXEL_MODE_BIT = 32;
		public static final int GL_LIGHTING_BIT = 64;
		public static final int GL_FOG_BIT = 128;
		public static final int GL_DEPTH_BUFFER_BIT = 256;
		public static final int GL_ACCUM_BUFFER_BIT = 512;
		public static final int GL_STENCIL_BUFFER_BIT = 1024;
		public static final int GL_VIEWPORT_BIT = 2048;
		public static final int GL_TRANSFORM_BIT = 4096;
		public static final int GL_ENABLE_BIT = 8192;
		public static final int GL_COLOR_BUFFER_BIT = 16384;
		public static final int GL_HINT_BIT = 32768;
		public static final int GL_EVAL_BIT = 65536;
		public static final int GL_LIST_BIT = 131072;
		public static final int GL_TEXTURE_BIT = 262144;
		public static final int GL_SCISSOR_BIT = 524288;
		public static final int GL_ALL_ATTRIB_BITS = 1048575;
		public static final int GL_POINTS = 0;
		public static final int GL_LINES = 1;
		public static final int GL_LINE_LOOP = 2;
		public static final int GL_LINE_STRIP = 3;
		public static final int GL_TRIANGLES = 4;
		public static final int GL_TRIANGLE_STRIP = 5;
		public static final int GL_TRIANGLE_FAN = 6;
		public static final int GL_QUADS = 7;
		public static final int GL_QUAD_STRIP = 8;
		public static final int GL_POLYGON = 9;
		public static final int GL_ZERO = 0;
		public static final int GL_ONE = 1;
		public static final int GL_SRC_COLOR = 768;
		public static final int GL_ONE_MINUS_SRC_COLOR = 769;
		public static final int GL_SRC_ALPHA = 770;
		public static final int GL_ONE_MINUS_SRC_ALPHA = 771;
		public static final int GL_DST_ALPHA = 772;
		public static final int GL_ONE_MINUS_DST_ALPHA = 773;
		public static final int GL_DST_COLOR = 774;
		public static final int GL_ONE_MINUS_DST_COLOR = 775;
		public static final int GL_SRC_ALPHA_SATURATE = 776;
		public static final int GL_CONSTANT_COLOR = 32769;
		public static final int GL_ONE_MINUS_CONSTANT_COLOR = 32770;
		public static final int GL_CONSTANT_ALPHA = 32771;
		public static final int GL_ONE_MINUS_CONSTANT_ALPHA = 32772;
		public static final int GL_TRUE = 1;
		public static final int GL_FALSE = 0;
		public static final int GL_CLIP_PLANE0 = 12288;
		public static final int GL_CLIP_PLANE1 = 12289;
		public static final int GL_CLIP_PLANE2 = 12290;
		public static final int GL_CLIP_PLANE3 = 12291;
		public static final int GL_CLIP_PLANE4 = 12292;
		public static final int GL_CLIP_PLANE5 = 12293;
		public static final int GL_BYTE = 5120;
		public static final int GL_UNSIGNED_BYTE = 5121;
		public static final int GL_SHORT = 5122;
		public static final int GL_UNSIGNED_SHORT = 5123;
		public static final int GL_INT = 5124;
		public static final int GL_UNSIGNED_INT = 5125;
		public static final int GL_FLOAT = 5126;
		public static final int GL_2_BYTES = 5127;
		public static final int GL_3_BYTES = 5128;
		public static final int GL_4_BYTES = 5129;
		public static final int GL_DOUBLE = 5130;
		public static final int GL_NONE = 0;
		public static final int GL_FRONT_LEFT = 1024;
		public static final int GL_FRONT_RIGHT = 1025;
		public static final int GL_BACK_LEFT = 1026;
		public static final int GL_BACK_RIGHT = 1027;
		public static final int GL_FRONT = 1028;
		public static final int GL_BACK = 1029;
		public static final int GL_LEFT = 1030;
		public static final int GL_RIGHT = 1031;
		public static final int GL_FRONT_AND_BACK = 1032;
		public static final int GL_AUX0 = 1033;
		public static final int GL_AUX1 = 1034;
		public static final int GL_AUX2 = 1035;
		public static final int GL_AUX3 = 1036;
		public static final int GL_NO_ERROR = 0;
		public static final int GL_INVALID_ENUM = 1280;
		public static final int GL_INVALID_VALUE = 1281;
		public static final int GL_INVALID_OPERATION = 1282;
		public static final int GL_STACK_OVERFLOW = 1283;
		public static final int GL_STACK_UNDERFLOW = 1284;
		public static final int GL_OUT_OF_MEMORY = 1285;
		public static final int GL_2D = 1536;
		public static final int GL_3D = 1537;
		public static final int GL_3D_COLOR = 1538;
		public static final int GL_3D_COLOR_TEXTURE = 1539;
		public static final int GL_4D_COLOR_TEXTURE = 1540;
		public static final int GL_PASS_THROUGH_TOKEN = 1792;
		public static final int GL_POINT_TOKEN = 1793;
		public static final int GL_LINE_TOKEN = 1794;
		public static final int GL_POLYGON_TOKEN = 1795;
		public static final int GL_BITMAP_TOKEN = 1796;
		public static final int GL_DRAW_PIXEL_TOKEN = 1797;
		public static final int GL_COPY_PIXEL_TOKEN = 1798;
		public static final int GL_LINE_RESET_TOKEN = 1799;
		public static final int GL_EXP = 2048;
		public static final int GL_EXP2 = 2049;
		public static final int GL_CW = 2304;
		public static final int GL_CCW = 2305;
		public static final int GL_COEFF = 2560;
		public static final int GL_ORDER = 2561;
		public static final int GL_DOMAIN = 2562;
		public static final int GL_CURRENT_COLOR = 2816;
		public static final int GL_CURRENT_INDEX = 2817;
		public static final int GL_CURRENT_NORMAL = 2818;
		public static final int GL_CURRENT_TEXTURE_COORDS = 2819;
		public static final int GL_CURRENT_RASTER_COLOR = 2820;
		public static final int GL_CURRENT_RASTER_INDEX = 2821;
		public static final int GL_CURRENT_RASTER_TEXTURE_COORDS = 2822;
		public static final int GL_CURRENT_RASTER_POSITION = 2823;
		public static final int GL_CURRENT_RASTER_POSITION_VALID = 2824;
		public static final int GL_CURRENT_RASTER_DISTANCE = 2825;
		public static final int GL_POINT_SMOOTH = 2832;
		public static final int GL_POINT_SIZE = 2833;
		public static final int GL_POINT_SIZE_RANGE = 2834;
		public static final int GL_POINT_SIZE_GRANULARITY = 2835;
		public static final int GL_LINE_SMOOTH = 2848;
		public static final int GL_LINE_WIDTH = 2849;
		public static final int GL_LINE_WIDTH_RANGE = 2850;
		public static final int GL_LINE_WIDTH_GRANULARITY = 2851;
		public static final int GL_LINE_STIPPLE = 2852;
		public static final int GL_LINE_STIPPLE_PATTERN = 2853;
		public static final int GL_LINE_STIPPLE_REPEAT = 2854;
		public static final int GL_LIST_MODE = 2864;
		public static final int GL_MAX_LIST_NESTING = 2865;
		public static final int GL_LIST_BASE = 2866;
		public static final int GL_LIST_INDEX = 2867;
		public static final int GL_POLYGON_MODE = 2880;
		public static final int GL_POLYGON_SMOOTH = 2881;
		public static final int GL_POLYGON_STIPPLE = 2882;
		public static final int GL_EDGE_FLAG = 2883;
		public static final int GL_CULL_FACE = 2884;
		public static final int GL_CULL_FACE_MODE = 2885;
		public static final int GL_FRONT_FACE = 2886;
		public static final int GL_LIGHTING = 2896;
		public static final int GL_LIGHT_MODEL_LOCAL_VIEWER = 2897;
		public static final int GL_LIGHT_MODEL_TWO_SIDE = 2898;
		public static final int GL_LIGHT_MODEL_AMBIENT = 2899;
		public static final int GL_SHADE_MODEL = 2900;
		public static final int GL_COLOR_MATERIAL_FACE = 2901;
		public static final int GL_COLOR_MATERIAL_PARAMETER = 2902;
		public static final int GL_COLOR_MATERIAL = 2903;
		public static final int GL_FOG = 2912;
		public static final int GL_FOG_INDEX = 2913;
		public static final int GL_FOG_DENSITY = 2914;
		public static final int GL_FOG_START = 2915;
		public static final int GL_FOG_END = 2916;
		public static final int GL_FOG_MODE = 2917;
		public static final int GL_FOG_COLOR = 2918;
		public static final int GL_DEPTH_RANGE = 2928;
		public static final int GL_DEPTH_TEST = 2929;
		public static final int GL_DEPTH_WRITEMASK = 2930;
		public static final int GL_DEPTH_CLEAR_VALUE = 2931;
		public static final int GL_DEPTH_FUNC = 2932;
		public static final int GL_ACCUM_CLEAR_VALUE = 2944;
		public static final int GL_STENCIL_TEST = 2960;
		public static final int GL_STENCIL_CLEAR_VALUE = 2961;
		public static final int GL_STENCIL_FUNC = 2962;
		public static final int GL_STENCIL_VALUE_MASK = 2963;
		public static final int GL_STENCIL_FAIL = 2964;
		public static final int GL_STENCIL_PASS_DEPTH_FAIL = 2965;
		public static final int GL_STENCIL_PASS_DEPTH_PASS = 2966;
		public static final int GL_STENCIL_REF = 2967;
		public static final int GL_STENCIL_WRITEMASK = 2968;
		public static final int GL_MATRIX_MODE = 2976;
		public static final int GL_NORMALIZE = 2977;
		public static final int GL_VIEWPORT = 2978;
		public static final int GL_MODELVIEW_STACK_DEPTH = 2979;
		public static final int GL_PROJECTION_STACK_DEPTH = 2980;
		public static final int GL_TEXTURE_STACK_DEPTH = 2981;
		public static final int GL_MODELVIEW_MATRIX = 2982;
		public static final int GL_PROJECTION_MATRIX = 2983;
		public static final int GL_TEXTURE_MATRIX = 2984;
		public static final int GL_ATTRIB_STACK_DEPTH = 2992;
		public static final int GL_CLIENT_ATTRIB_STACK_DEPTH = 2993;
		public static final int GL_ALPHA_TEST = 3008;
		public static final int GL_ALPHA_TEST_FUNC = 3009;
		public static final int GL_ALPHA_TEST_REF = 3010;
		public static final int GL_DITHER = 3024;
		public static final int GL_BLEND_DST = 3040;
		public static final int GL_BLEND_SRC = 3041;
		public static final int GL_BLEND = 3042;
		public static final int GL_LOGIC_OP_MODE = 3056;
		public static final int GL_INDEX_LOGIC_OP = 3057;
		public static final int GL_COLOR_LOGIC_OP = 3058;
		public static final int GL_AUX_BUFFERS = 3072;
		public static final int GL_DRAW_BUFFER = 3073;
		public static final int GL_READ_BUFFER = 3074;
		public static final int GL_SCISSOR_BOX = 3088;
		public static final int GL_SCISSOR_TEST = 3089;
		public static final int GL_INDEX_CLEAR_VALUE = 3104;
		public static final int GL_INDEX_WRITEMASK = 3105;
		public static final int GL_COLOR_CLEAR_VALUE = 3106;
		public static final int GL_COLOR_WRITEMASK = 3107;
		public static final int GL_INDEX_MODE = 3120;
		public static final int GL_RGBA_MODE = 3121;
		public static final int GL_DOUBLEBUFFER = 3122;
		public static final int GL_STEREO = 3123;
		public static final int GL_RENDER_MODE = 3136;
		public static final int GL_PERSPECTIVE_CORRECTION_HINT = 3152;
		public static final int GL_POINT_SMOOTH_HINT = 3153;
		public static final int GL_LINE_SMOOTH_HINT = 3154;
		public static final int GL_POLYGON_SMOOTH_HINT = 3155;
		public static final int GL_FOG_HINT = 3156;
		public static final int GL_TEXTURE_GEN_S = 3168;
		public static final int GL_TEXTURE_GEN_T = 3169;
		public static final int GL_TEXTURE_GEN_R = 3170;
		public static final int GL_TEXTURE_GEN_Q = 3171;
		public static final int GL_PIXEL_MAP_I_TO_I = 3184;
		public static final int GL_PIXEL_MAP_S_TO_S = 3185;
		public static final int GL_PIXEL_MAP_I_TO_R = 3186;
		public static final int GL_PIXEL_MAP_I_TO_G = 3187;
		public static final int GL_PIXEL_MAP_I_TO_B = 3188;
		public static final int GL_PIXEL_MAP_I_TO_A = 3189;
		public static final int GL_PIXEL_MAP_R_TO_R = 3190;
		public static final int GL_PIXEL_MAP_G_TO_G = 3191;
		public static final int GL_PIXEL_MAP_B_TO_B = 3192;
		public static final int GL_PIXEL_MAP_A_TO_A = 3193;
		public static final int GL_PIXEL_MAP_I_TO_I_SIZE = 3248;
		public static final int GL_PIXEL_MAP_S_TO_S_SIZE = 3249;
		public static final int GL_PIXEL_MAP_I_TO_R_SIZE = 3250;
		public static final int GL_PIXEL_MAP_I_TO_G_SIZE = 3251;
		public static final int GL_PIXEL_MAP_I_TO_B_SIZE = 3252;
		public static final int GL_PIXEL_MAP_I_TO_A_SIZE = 3253;
		public static final int GL_PIXEL_MAP_R_TO_R_SIZE = 3254;
		public static final int GL_PIXEL_MAP_G_TO_G_SIZE = 3255;
		public static final int GL_PIXEL_MAP_B_TO_B_SIZE = 3256;
		public static final int GL_PIXEL_MAP_A_TO_A_SIZE = 3257;
		public static final int GL_UNPACK_SWAP_BYTES = 3312;
		public static final int GL_UNPACK_LSB_FIRST = 3313;
		public static final int GL_UNPACK_ROW_LENGTH = 3314;
		public static final int GL_UNPACK_SKIP_ROWS = 3315;
		public static final int GL_UNPACK_SKIP_PIXELS = 3316;
		public static final int GL_UNPACK_ALIGNMENT = 3317;
		public static final int GL_PACK_SWAP_BYTES = 3328;
		public static final int GL_PACK_LSB_FIRST = 3329;
		public static final int GL_PACK_ROW_LENGTH = 3330;
		public static final int GL_PACK_SKIP_ROWS = 3331;
		public static final int GL_PACK_SKIP_PIXELS = 3332;
		public static final int GL_PACK_ALIGNMENT = 3333;
		public static final int GL_MAP_COLOR = 3344;
		public static final int GL_MAP_STENCIL = 3345;
		public static final int GL_INDEX_SHIFT = 3346;
		public static final int GL_INDEX_OFFSET = 3347;
		public static final int GL_RED_SCALE = 3348;
		public static final int GL_RED_BIAS = 3349;
		public static final int GL_ZOOM_X = 3350;
		public static final int GL_ZOOM_Y = 3351;
		public static final int GL_GREEN_SCALE = 3352;
		public static final int GL_GREEN_BIAS = 3353;
		public static final int GL_BLUE_SCALE = 3354;
		public static final int GL_BLUE_BIAS = 3355;
		public static final int GL_ALPHA_SCALE = 3356;
		public static final int GL_ALPHA_BIAS = 3357;
		public static final int GL_DEPTH_SCALE = 3358;
		public static final int GL_DEPTH_BIAS = 3359;
		public static final int GL_MAX_EVAL_ORDER = 3376;
		public static final int GL_MAX_LIGHTS = 3377;
		public static final int GL_MAX_CLIP_PLANES = 3378;
		public static final int GL_MAX_TEXTURE_SIZE = 3379;
		public static final int GL_MAX_PIXEL_MAP_TABLE = 3380;
		public static final int GL_MAX_ATTRIB_STACK_DEPTH = 3381;
		public static final int GL_MAX_MODELVIEW_STACK_DEPTH = 3382;
		public static final int GL_MAX_NAME_STACK_DEPTH = 3383;
		public static final int GL_MAX_PROJECTION_STACK_DEPTH = 3384;
		public static final int GL_MAX_TEXTURE_STACK_DEPTH = 3385;
		public static final int GL_MAX_VIEWPORT_DIMS = 3386;
		public static final int GL_MAX_CLIENT_ATTRIB_STACK_DEPTH = 3387;
		public static final int GL_SUBPIXEL_BITS = 3408;
		public static final int GL_INDEX_BITS = 3409;
		public static final int GL_RED_BITS = 3410;
		public static final int GL_GREEN_BITS = 3411;
		public static final int GL_BLUE_BITS = 3412;
		public static final int GL_ALPHA_BITS = 3413;
		public static final int GL_DEPTH_BITS = 3414;
		public static final int GL_STENCIL_BITS = 3415;
		public static final int GL_ACCUM_RED_BITS = 3416;
		public static final int GL_ACCUM_GREEN_BITS = 3417;
		public static final int GL_ACCUM_BLUE_BITS = 3418;
		public static final int GL_ACCUM_ALPHA_BITS = 3419;
		public static final int GL_NAME_STACK_DEPTH = 3440;
		public static final int GL_AUTO_NORMAL = 3456;
		public static final int GL_MAP1_COLOR_4 = 3472;
		public static final int GL_MAP1_INDEX = 3473;
		public static final int GL_MAP1_NORMAL = 3474;
		public static final int GL_MAP1_TEXTURE_COORD_1 = 3475;
		public static final int GL_MAP1_TEXTURE_COORD_2 = 3476;
		public static final int GL_MAP1_TEXTURE_COORD_3 = 3477;
		public static final int GL_MAP1_TEXTURE_COORD_4 = 3478;
		public static final int GL_MAP1_VERTEX_3 = 3479;
		public static final int GL_MAP1_VERTEX_4 = 3480;
		public static final int GL_MAP2_COLOR_4 = 3504;
		public static final int GL_MAP2_INDEX = 3505;
		public static final int GL_MAP2_NORMAL = 3506;
		public static final int GL_MAP2_TEXTURE_COORD_1 = 3507;
		public static final int GL_MAP2_TEXTURE_COORD_2 = 3508;
		public static final int GL_MAP2_TEXTURE_COORD_3 = 3509;
		public static final int GL_MAP2_TEXTURE_COORD_4 = 3510;
		public static final int GL_MAP2_VERTEX_3 = 3511;
		public static final int GL_MAP2_VERTEX_4 = 3512;
		public static final int GL_MAP1_GRID_DOMAIN = 3536;
		public static final int GL_MAP1_GRID_SEGMENTS = 3537;
		public static final int GL_MAP2_GRID_DOMAIN = 3538;
		public static final int GL_MAP2_GRID_SEGMENTS = 3539;
		public static final int GL_TEXTURE_1D = 3552;
		public static final int GL_TEXTURE_2D = 3553;
		public static final int GL_FEEDBACK_BUFFER_POINTER = 3568;
		public static final int GL_FEEDBACK_BUFFER_SIZE = 3569;
		public static final int GL_FEEDBACK_BUFFER_TYPE = 3570;
		public static final int GL_SELECTION_BUFFER_POINTER = 3571;
		public static final int GL_SELECTION_BUFFER_SIZE = 3572;
		public static final int GL_TEXTURE_WIDTH = 4096;
		public static final int GL_TEXTURE_HEIGHT = 4097;
		public static final int GL_TEXTURE_INTERNAL_FORMAT = 4099;
		public static final int GL_TEXTURE_BORDER_COLOR = 4100;
		public static final int GL_TEXTURE_BORDER = 4101;
		public static final int GL_DONT_CARE = 4352;
		public static final int GL_FASTEST = 4353;
		public static final int GL_NICEST = 4354;
		public static final int GL_LIGHT0 = 16384;
		public static final int GL_LIGHT1 = 16385;
		public static final int GL_LIGHT2 = 16386;
		public static final int GL_LIGHT3 = 16387;
		public static final int GL_LIGHT4 = 16388;
		public static final int GL_LIGHT5 = 16389;
		public static final int GL_LIGHT6 = 16390;
		public static final int GL_LIGHT7 = 16391;
		public static final int GL_AMBIENT = 4608;
		public static final int GL_DIFFUSE = 4609;
		public static final int GL_SPECULAR = 4610;
		public static final int GL_POSITION = 4611;
		public static final int GL_SPOT_DIRECTION = 4612;
		public static final int GL_SPOT_EXPONENT = 4613;
		public static final int GL_SPOT_CUTOFF = 4614;
		public static final int GL_CONSTANT_ATTENUATION = 4615;
		public static final int GL_LINEAR_ATTENUATION = 4616;
		public static final int GL_QUADRATIC_ATTENUATION = 4617;
		public static final int GL_COMPILE = 4864;
		public static final int GL_COMPILE_AND_EXECUTE = 4865;
		public static final int GL_CLEAR = 5376;
		public static final int GL_AND = 5377;
		public static final int GL_AND_REVERSE = 5378;
		public static final int GL_COPY = 5379;
		public static final int GL_AND_INVERTED = 5380;
		public static final int GL_NOOP = 5381;
		public static final int GL_XOR = 5382;
		public static final int GL_OR = 5383;
		public static final int GL_NOR = 5384;
		public static final int GL_EQUIV = 5385;
		public static final int GL_INVERT = 5386;
		public static final int GL_OR_REVERSE = 5387;
		public static final int GL_COPY_INVERTED = 5388;
		public static final int GL_OR_INVERTED = 5389;
		public static final int GL_NAND = 5390;
		public static final int GL_SET = 5391;
		public static final int GL_EMISSION = 5632;
		public static final int GL_SHININESS = 5633;
		public static final int GL_AMBIENT_AND_DIFFUSE = 5634;
		public static final int GL_COLOR_INDEXES = 5635;
		public static final int GL_MODELVIEW = 5888;
		public static final int GL_PROJECTION = 5889;
		public static final int GL_TEXTURE = 5890;
		public static final int GL_COLOR = 6144;
		public static final int GL_DEPTH = 6145;
		public static final int GL_STENCIL = 6146;
		public static final int GL_COLOR_INDEX = 6400;
		public static final int GL_STENCIL_INDEX = 6401;
		public static final int GL_DEPTH_COMPONENT = 6402;
		public static final int GL_RED = 6403;
		public static final int GL_GREEN = 6404;
		public static final int GL_BLUE = 6405;
		public static final int GL_ALPHA = 6406;
		public static final int GL_RGB = 6407;
		public static final int GL_RGBA = 6408;
		public static final int GL_LUMINANCE = 6409;
		public static final int GL_LUMINANCE_ALPHA = 6410;
		public static final int GL_BITMAP = 6656;
		public static final int GL_POINT = 6912;
		public static final int GL_LINE = 6913;
		public static final int GL_FILL = 6914;
		public static final int GL_RENDER = 7168;
		public static final int GL_FEEDBACK = 7169;
		public static final int GL_SELECT = 7170;
		public static final int GL_FLAT = 7424;
		public static final int GL_SMOOTH = 7425;
		public static final int GL_KEEP = 7680;
		public static final int GL_REPLACE = 7681;
		public static final int GL_INCR = 7682;
		public static final int GL_DECR = 7683;
		public static final int GL_VENDOR = 7936;
		public static final int GL_RENDERER = 7937;
		public static final int GL_VERSION = 7938;
		public static final int GL_EXTENSIONS = 7939;
		public static final int GL_S = 8192;
		public static final int GL_T = 8193;
		public static final int GL_R = 8194;
		public static final int GL_Q = 8195;
		public static final int GL_MODULATE = 8448;
		public static final int GL_DECAL = 8449;
		public static final int GL_TEXTURE_ENV_MODE = 8704;
		public static final int GL_TEXTURE_ENV_COLOR = 8705;
		public static final int GL_TEXTURE_ENV = 8960;
		public static final int GL_EYE_LINEAR = 9216;
		public static final int GL_OBJECT_LINEAR = 9217;
		public static final int GL_SPHERE_MAP = 9218;
		public static final int GL_TEXTURE_GEN_MODE = 9472;
		public static final int GL_OBJECT_PLANE = 9473;
		public static final int GL_EYE_PLANE = 9474;
		public static final int GL_NEAREST = 9728;
		public static final int GL_LINEAR = 9729;
		public static final int GL_NEAREST_MIPMAP_NEAREST = 9984;
		public static final int GL_LINEAR_MIPMAP_NEAREST = 9985;
		public static final int GL_NEAREST_MIPMAP_LINEAR = 9986;
		public static final int GL_LINEAR_MIPMAP_LINEAR = 9987;
		public static final int GL_TEXTURE_MAG_FILTER = 10240;
		public static final int GL_TEXTURE_MIN_FILTER = 10241;
		public static final int GL_TEXTURE_WRAP_S = 10242;
		public static final int GL_TEXTURE_WRAP_T = 10243;
		public static final int GL_CLAMP = 10496;
		public static final int GL_REPEAT = 10497;
		public static final int GL_CLIENT_PIXEL_STORE_BIT = 1;
		public static final int GL_CLIENT_VERTEX_ARRAY_BIT = 2;
		public static final int GL_ALL_CLIENT_ATTRIB_BITS = -1;
		public static final int GL_POLYGON_OFFSET_FACTOR = 32824;
		public static final int GL_POLYGON_OFFSET_UNITS = 10752;
		public static final int GL_POLYGON_OFFSET_POINT = 10753;
		public static final int GL_POLYGON_OFFSET_LINE = 10754;
		public static final int GL_POLYGON_OFFSET_FILL = 32823;
		public static final int GL_ALPHA4 = 32827;
		public static final int GL_ALPHA8 = 32828;
		public static final int GL_ALPHA12 = 32829;
		public static final int GL_ALPHA16 = 32830;
		public static final int GL_LUMINANCE4 = 32831;
		public static final int GL_LUMINANCE8 = 32832;
		public static final int GL_LUMINANCE12 = 32833;
		public static final int GL_LUMINANCE16 = 32834;
		public static final int GL_LUMINANCE4_ALPHA4 = 32835;
		public static final int GL_LUMINANCE6_ALPHA2 = 32836;
		public static final int GL_LUMINANCE8_ALPHA8 = 32837;
		public static final int GL_LUMINANCE12_ALPHA4 = 32838;
		public static final int GL_LUMINANCE12_ALPHA12 = 32839;
		public static final int GL_LUMINANCE16_ALPHA16 = 32840;
		public static final int GL_INTENSITY = 32841;
		public static final int GL_INTENSITY4 = 32842;
		public static final int GL_INTENSITY8 = 32843;
		public static final int GL_INTENSITY12 = 32844;
		public static final int GL_INTENSITY16 = 32845;
		public static final int GL_R3_G3_B2 = 10768;
		public static final int GL_RGB4 = 32847;
		public static final int GL_RGB5 = 32848;
		public static final int GL_RGB8 = 32849;
		public static final int GL_RGB10 = 32850;
		public static final int GL_RGB12 = 32851;
		public static final int GL_RGB16 = 32852;
		public static final int GL_RGBA2 = 32853;
		public static final int GL_RGBA4 = 32854;
		public static final int GL_RGB5_A1 = 32855;
		public static final int GL_RGBA8 = 32856;
		public static final int GL_RGB10_A2 = 32857;
		public static final int GL_RGBA12 = 32858;
		public static final int GL_RGBA16 = 32859;
		public static final int GL_TEXTURE_RED_SIZE = 32860;
		public static final int GL_TEXTURE_GREEN_SIZE = 32861;
		public static final int GL_TEXTURE_BLUE_SIZE = 32862;
		public static final int GL_TEXTURE_ALPHA_SIZE = 32863;
		public static final int GL_TEXTURE_LUMINANCE_SIZE = 32864;
		public static final int GL_TEXTURE_INTENSITY_SIZE = 32865;
		public static final int GL_PROXY_TEXTURE_1D = 32867;
		public static final int GL_PROXY_TEXTURE_2D = 32868;
		public static final int GL_TEXTURE_PRIORITY = 32870;
		public static final int GL_TEXTURE_RESIDENT = 32871;
		public static final int GL_TEXTURE_BINDING_1D = 32872;
		public static final int GL_TEXTURE_BINDING_2D = 32873;
		public static final int GL_VERTEX_ARRAY = 32884;
		public static final int GL_NORMAL_ARRAY = 32885;
		public static final int GL_COLOR_ARRAY = 32886;
		public static final int GL_INDEX_ARRAY = 32887;
		public static final int GL_TEXTURE_COORD_ARRAY = 32888;
		public static final int GL_EDGE_FLAG_ARRAY = 32889;
		public static final int GL_VERTEX_ARRAY_SIZE = 32890;
		public static final int GL_VERTEX_ARRAY_TYPE = 32891;
		public static final int GL_VERTEX_ARRAY_STRIDE = 32892;
		public static final int GL_NORMAL_ARRAY_TYPE = 32894;
		public static final int GL_NORMAL_ARRAY_STRIDE = 32895;
		public static final int GL_COLOR_ARRAY_SIZE = 32897;
		public static final int GL_COLOR_ARRAY_TYPE = 32898;
		public static final int GL_COLOR_ARRAY_STRIDE = 32899;
		public static final int GL_INDEX_ARRAY_TYPE = 32901;
		public static final int GL_INDEX_ARRAY_STRIDE = 32902;
		public static final int GL_TEXTURE_COORD_ARRAY_SIZE = 32904;
		public static final int GL_TEXTURE_COORD_ARRAY_TYPE = 32905;
		public static final int GL_TEXTURE_COORD_ARRAY_STRIDE = 32906;
		public static final int GL_EDGE_FLAG_ARRAY_STRIDE = 32908;
		public static final int GL_VERTEX_ARRAY_POINTER = 32910;
		public static final int GL_NORMAL_ARRAY_POINTER = 32911;
		public static final int GL_COLOR_ARRAY_POINTER = 32912;
		public static final int GL_INDEX_ARRAY_POINTER = 32913;
		public static final int GL_TEXTURE_COORD_ARRAY_POINTER = 32914;
		public static final int GL_EDGE_FLAG_ARRAY_POINTER = 32915;
		public static final int GL_V2F = 10784;
		public static final int GL_V3F = 10785;
		public static final int GL_C4UB_V2F = 10786;
		public static final int GL_C4UB_V3F = 10787;
		public static final int GL_C3F_V3F = 10788;
		public static final int GL_N3F_V3F = 10789;
		public static final int GL_C4F_N3F_V3F = 10790;
		public static final int GL_T2F_V3F = 10791;
		public static final int GL_T4F_V4F = 10792;
		public static final int GL_T2F_C4UB_V3F = 10793;
		public static final int GL_T2F_C3F_V3F = 10794;
		public static final int GL_T2F_N3F_V3F = 10795;
		public static final int GL_T2F_C4F_N3F_V3F = 10796;
		public static final int GL_T4F_C4F_N3F_V4F = 10797;
		public static final int GL_LOGIC_OP = 3057;
		public static final int GL_TEXTURE_COMPONENTS = 4099;
		public static final int GL_TEXTURE_BINDING_3D = 32874;
		public static final int GL_PACK_SKIP_IMAGES = 32875;
		public static final int GL_PACK_IMAGE_HEIGHT = 32876;
		public static final int GL_UNPACK_SKIP_IMAGES = 32877;
		public static final int GL_UNPACK_IMAGE_HEIGHT = 32878;
		public static final int GL_TEXTURE_3D = 32879;
		public static final int GL_PROXY_TEXTURE_3D = 32880;
		public static final int GL_TEXTURE_DEPTH = 32881;
		public static final int GL_TEXTURE_WRAP_R = 32882;
		public static final int GL_MAX_3D_TEXTURE_SIZE = 32883;
		public static final int GL_BGR = 32992;
		public static final int GL_BGRA = 32993;
		public static final int GL_UNSIGNED_BYTE_3_3_2 = 32818;
		public static final int GL_UNSIGNED_BYTE_2_3_3_REV = 33634;
		public static final int GL_UNSIGNED_SHORT_5_6_5 = 33635;
		public static final int GL_UNSIGNED_SHORT_5_6_5_REV = 33636;
		public static final int GL_UNSIGNED_SHORT_4_4_4_4 = 32819;
		public static final int GL_UNSIGNED_SHORT_4_4_4_4_REV = 33637;
		public static final int GL_UNSIGNED_SHORT_5_5_5_1 = 32820;
		public static final int GL_UNSIGNED_SHORT_1_5_5_5_REV = 33638;
		public static final int GL_UNSIGNED_INT_8_8_8_8 = 32821;
		public static final int GL_UNSIGNED_INT_8_8_8_8_REV = 33639;
		public static final int GL_UNSIGNED_INT_10_10_10_2 = 32822;
		public static final int GL_UNSIGNED_INT_2_10_10_10_REV = 33640;
		public static final int GL_RESCALE_NORMAL = 32826;
		public static final int GL_LIGHT_MODEL_COLOR_CONTROL = 33272;
		public static final int GL_SINGLE_COLOR = 33273;
		public static final int GL_SEPARATE_SPECULAR_COLOR = 33274;
		public static final int GL_CLAMP_TO_EDGE = 33071;
		public static final int GL_TEXTURE_MIN_LOD = 33082;
		public static final int GL_TEXTURE_MAX_LOD = 33083;
		public static final int GL_TEXTURE_BASE_LEVEL = 33084;
		public static final int GL_TEXTURE_MAX_LEVEL = 33085;
		public static final int GL_MAX_ELEMENTS_VERTICES = 33000;
		public static final int GL_MAX_ELEMENTS_INDICES = 33001;
		public static final int GL_ALIASED_POINT_SIZE_RANGE = 33901;
		public static final int GL_ALIASED_LINE_WIDTH_RANGE = 33902;
		public static final int GL_SMOOTH_POINT_SIZE_RANGE = 2834;
		public static final int GL_SMOOTH_POINT_SIZE_GRANULARITY = 2835;
		public static final int GL_SMOOTH_LINE_WIDTH_RANGE = 2850;
		public static final int GL_SMOOTH_LINE_WIDTH_GRANULARITY = 2851;
		public static final int GL_TEXTURE0 = 33984;
		public static final int GL_TEXTURE1 = 33985;
		public static final int GL_TEXTURE2 = 33986;
		public static final int GL_TEXTURE3 = 33987;
		public static final int GL_TEXTURE4 = 33988;
		public static final int GL_TEXTURE5 = 33989;
		public static final int GL_TEXTURE6 = 33990;
		public static final int GL_TEXTURE7 = 33991;
		public static final int GL_TEXTURE8 = 33992;
		public static final int GL_TEXTURE9 = 33993;
		public static final int GL_TEXTURE10 = 33994;
		public static final int GL_TEXTURE11 = 33995;
		public static final int GL_TEXTURE12 = 33996;
		public static final int GL_TEXTURE13 = 33997;
		public static final int GL_TEXTURE14 = 33998;
		public static final int GL_TEXTURE15 = 33999;
		public static final int GL_TEXTURE16 = 34000;
		public static final int GL_TEXTURE17 = 34001;
		public static final int GL_TEXTURE18 = 34002;
		public static final int GL_TEXTURE19 = 34003;
		public static final int GL_TEXTURE20 = 34004;
		public static final int GL_TEXTURE21 = 34005;
		public static final int GL_TEXTURE22 = 34006;
		public static final int GL_TEXTURE23 = 34007;
		public static final int GL_TEXTURE24 = 34008;
		public static final int GL_TEXTURE25 = 34009;
		public static final int GL_TEXTURE26 = 34010;
		public static final int GL_TEXTURE27 = 34011;
		public static final int GL_TEXTURE28 = 34012;
		public static final int GL_TEXTURE29 = 34013;
		public static final int GL_TEXTURE30 = 34014;
		public static final int GL_TEXTURE31 = 34015;
		public static final int GL_ACTIVE_TEXTURE = 34016;
		public static final int GL_CLIENT_ACTIVE_TEXTURE = 34017;
		public static final int GL_MAX_TEXTURE_UNITS = 34018;
		public static final int GL_NORMAL_MAP = 34065;
		public static final int GL_REFLECTION_MAP = 34066;
		public static final int GL_TEXTURE_CUBE_MAP = 34067;
		public static final int GL_TEXTURE_BINDING_CUBE_MAP = 34068;
		public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_X = 34069;
		public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 34070;
		public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 34071;
		public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 34072;
		public static final int GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 34073;
		public static final int GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 34074;
		public static final int GL_PROXY_TEXTURE_CUBE_MAP = 34075;
		public static final int GL_MAX_CUBE_MAP_TEXTURE_SIZE = 34076;
		public static final int GL_COMPRESSED_ALPHA = 34025;
		public static final int GL_COMPRESSED_LUMINANCE = 34026;
		public static final int GL_COMPRESSED_LUMINANCE_ALPHA = 34027;
		public static final int GL_COMPRESSED_INTENSITY = 34028;
		public static final int GL_COMPRESSED_RGB = 34029;
		public static final int GL_COMPRESSED_RGBA = 34030;
		public static final int GL_TEXTURE_COMPRESSION_HINT = 34031;
		public static final int GL_TEXTURE_COMPRESSED_IMAGE_SIZE = 34464;
		public static final int GL_TEXTURE_COMPRESSED = 34465;
		public static final int GL_NUM_COMPRESSED_TEXTURE_FORMATS = 34466;
		public static final int GL_COMPRESSED_TEXTURE_FORMATS = 34467;
		public static final int GL_MULTISAMPLE = 32925;
		public static final int GL_SAMPLE_ALPHA_TO_COVERAGE = 32926;
		public static final int GL_SAMPLE_ALPHA_TO_ONE = 32927;
		public static final int GL_SAMPLE_COVERAGE = 32928;
		public static final int GL_SAMPLE_BUFFERS = 32936;
		public static final int GL_SAMPLES = 32937;
		public static final int GL_SAMPLE_COVERAGE_VALUE = 32938;
		public static final int GL_SAMPLE_COVERAGE_INVERT = 32939;
		public static final int GL_MULTISAMPLE_BIT = 536870912;
		public static final int GL_TRANSPOSE_MODELVIEW_MATRIX = 34019;
		public static final int GL_TRANSPOSE_PROJECTION_MATRIX = 34020;
		public static final int GL_TRANSPOSE_TEXTURE_MATRIX = 34021;
		public static final int GL_TRANSPOSE_COLOR_MATRIX = 34022;
		public static final int GL_COMBINE = 34160;
		public static final int GL_COMBINE_RGB = 34161;
		public static final int GL_COMBINE_ALPHA = 34162;
		public static final int GL_SOURCE0_RGB = 34176;
		public static final int GL_SOURCE1_RGB = 34177;
		public static final int GL_SOURCE2_RGB = 34178;
		public static final int GL_SOURCE0_ALPHA = 34184;
		public static final int GL_SOURCE1_ALPHA = 34185;
		public static final int GL_SOURCE2_ALPHA = 34186;
		public static final int GL_OPERAND0_RGB = 34192;
		public static final int GL_OPERAND1_RGB = 34193;
		public static final int GL_OPERAND2_RGB = 34194;
		public static final int GL_OPERAND0_ALPHA = 34200;
		public static final int GL_OPERAND1_ALPHA = 34201;
		public static final int GL_OPERAND2_ALPHA = 34202;
		public static final int GL_RGB_SCALE = 34163;
		public static final int GL_ADD_SIGNED = 34164;
		public static final int GL_INTERPOLATE = 34165;
		public static final int GL_SUBTRACT = 34023;
		public static final int GL_CONSTANT = 34166;
		public static final int GL_PRIMARY_COLOR = 34167;
		public static final int GL_PREVIOUS = 34168;
		public static final int GL_DOT3_RGB = 34478;
		public static final int GL_DOT3_RGBA = 34479;
		public static final int GL_CLAMP_TO_BORDER = 33069;
		public static final int GL_ARRAY_BUFFER = 34962;
		public static final int GL_ELEMENT_ARRAY_BUFFER = 34963;
		public static final int GL_ARRAY_BUFFER_BINDING = 34964;
		public static final int GL_ELEMENT_ARRAY_BUFFER_BINDING = 34965;
		public static final int GL_VERTEX_ARRAY_BUFFER_BINDING = 34966;
		public static final int GL_NORMAL_ARRAY_BUFFER_BINDING = 34967;
		public static final int GL_COLOR_ARRAY_BUFFER_BINDING = 34968;
		public static final int GL_INDEX_ARRAY_BUFFER_BINDING = 34969;
		public static final int GL_TEXTURE_COORD_ARRAY_BUFFER_BINDING = 34970;
		public static final int GL_EDGE_FLAG_ARRAY_BUFFER_BINDING = 34971;
		public static final int GL_SECONDARY_COLOR_ARRAY_BUFFER_BINDING = 34972;
		public static final int GL_FOG_COORDINATE_ARRAY_BUFFER_BINDING = 34973;
		public static final int GL_WEIGHT_ARRAY_BUFFER_BINDING = 34974;
		public static final int GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = 34975;
		public static final int GL_STREAM_DRAW = 35040;
		public static final int GL_STREAM_READ = 35041;
		public static final int GL_STREAM_COPY = 35042;
		public static final int GL_STATIC_DRAW = 35044;
		public static final int GL_STATIC_READ = 35045;
		public static final int GL_STATIC_COPY = 35046;
		public static final int GL_DYNAMIC_DRAW = 35048;
		public static final int GL_DYNAMIC_READ = 35049;
		public static final int GL_DYNAMIC_COPY = 35050;
		public static final int GL_READ_ONLY = 35000;
		public static final int GL_WRITE_ONLY = 35001;
		public static final int GL_READ_WRITE = 35002;
		public static final int GL_BUFFER_SIZE = 34660;
		public static final int GL_BUFFER_USAGE = 34661;
		public static final int GL_BUFFER_ACCESS = 35003;
		public static final int GL_BUFFER_MAPPED = 35004;
		public static final int GL_BUFFER_MAP_POINTER = 35005;
		public static final int GL_FOG_COORD_SRC = 33872;
		public static final int GL_FOG_COORD = 33873;
		public static final int GL_CURRENT_FOG_COORD = 33875;
		public static final int GL_FOG_COORD_ARRAY_TYPE = 33876;
		public static final int GL_FOG_COORD_ARRAY_STRIDE = 33877;
		public static final int GL_FOG_COORD_ARRAY_POINTER = 33878;
		public static final int GL_FOG_COORD_ARRAY = 33879;
		public static final int GL_FOG_COORD_ARRAY_BUFFER_BINDING = 34973;
		public static final int GL_SRC0_RGB = 34176;
		public static final int GL_SRC1_RGB = 34177;
		public static final int GL_SRC2_RGB = 34178;
		public static final int GL_SRC0_ALPHA = 34184;
		public static final int GL_SRC1_ALPHA = 34185;
		public static final int GL_SRC2_ALPHA = 34186;
		public static final int GL_SAMPLES_PASSED = 35092;
		public static final int GL_QUERY_COUNTER_BITS = 34916;
		public static final int GL_CURRENT_QUERY = 34917;
		public static final int GL_QUERY_RESULT = 34918;
		public static final int GL_QUERY_RESULT_AVAILABLE = 34919;
		public static final int GL_SHADING_LANGUAGE_VERSION = 35724;
		public static final int GL_CURRENT_PROGRAM = 35725;
		public static final int GL_SHADER_TYPE = 35663;
		public static final int GL_DELETE_STATUS = 35712;
		public static final int GL_COMPILE_STATUS = 35713;
		public static final int GL_LINK_STATUS = 35714;
		public static final int GL_VALIDATE_STATUS = 35715;
		public static final int GL_INFO_LOG_LENGTH = 35716;
		public static final int GL_ATTACHED_SHADERS = 35717;
		public static final int GL_ACTIVE_UNIFORMS = 35718;
		public static final int GL_ACTIVE_UNIFORM_MAX_LENGTH = 35719;
		public static final int GL_ACTIVE_ATTRIBUTES = 35721;
		public static final int GL_ACTIVE_ATTRIBUTE_MAX_LENGTH = 35722;
		public static final int GL_SHADER_SOURCE_LENGTH = 35720;
		public static final int GL_SHADER_OBJECT = 35656;
		public static final int GL_FLOAT_VEC2 = 35664;
		public static final int GL_FLOAT_VEC3 = 35665;
		public static final int GL_FLOAT_VEC4 = 35666;
		public static final int GL_INT_VEC2 = 35667;
		public static final int GL_INT_VEC3 = 35668;
		public static final int GL_INT_VEC4 = 35669;
		public static final int GL_BOOL = 35670;
		public static final int GL_BOOL_VEC2 = 35671;
		public static final int GL_BOOL_VEC3 = 35672;
		public static final int GL_BOOL_VEC4 = 35673;
		public static final int GL_FLOAT_MAT2 = 35674;
		public static final int GL_FLOAT_MAT3 = 35675;
		public static final int GL_FLOAT_MAT4 = 35676;
		public static final int GL_SAMPLER_1D = 35677;
		public static final int GL_SAMPLER_2D = 35678;
		public static final int GL_SAMPLER_3D = 35679;
		public static final int GL_SAMPLER_CUBE = 35680;
		public static final int GL_SAMPLER_1D_SHADOW = 35681;
		public static final int GL_SAMPLER_2D_SHADOW = 35682;
		public static final int GL_VERTEX_SHADER = 35633;
		public static final int GL_MAX_VERTEX_UNIFORM_COMPONENTS = 35658;
		public static final int GL_MAX_VARYING_FLOATS = 35659;
		public static final int GL_MAX_VERTEX_ATTRIBS = 34921;
		public static final int GL_MAX_TEXTURE_IMAGE_UNITS = 34930;
		public static final int GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS = 35660;
		public static final int GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS = 35661;
		public static final int GL_MAX_TEXTURE_COORDS = 34929;
		public static final int GL_VERTEX_PROGRAM_POINT_SIZE = 34370;
		public static final int GL_VERTEX_PROGRAM_TWO_SIDE = 34371;
		public static final int GL_VERTEX_ATTRIB_ARRAY_ENABLED = 34338;
		public static final int GL_VERTEX_ATTRIB_ARRAY_SIZE = 34339;
		public static final int GL_VERTEX_ATTRIB_ARRAY_STRIDE = 34340;
		public static final int GL_VERTEX_ATTRIB_ARRAY_TYPE = 34341;
		public static final int GL_VERTEX_ATTRIB_ARRAY_NORMALIZED = 34922;
		public static final int GL_CURRENT_VERTEX_ATTRIB = 34342;
		public static final int GL_VERTEX_ATTRIB_ARRAY_POINTER = 34373;
		public static final int GL_FRAGMENT_SHADER = 35632;
		public static final int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS = 35657;
		public static final int GL_FRAGMENT_SHADER_DERIVATIVE_HINT = 35723;
		public static final int GL_MAX_DRAW_BUFFERS = 34852;
		public static final int GL_DRAW_BUFFER0 = 34853;
		public static final int GL_DRAW_BUFFER1 = 34854;
		public static final int GL_DRAW_BUFFER2 = 34855;
		public static final int GL_DRAW_BUFFER3 = 34856;
		public static final int GL_DRAW_BUFFER4 = 34857;
		public static final int GL_DRAW_BUFFER5 = 34858;
		public static final int GL_DRAW_BUFFER6 = 34859;
		public static final int GL_DRAW_BUFFER7 = 34860;
		public static final int GL_DRAW_BUFFER8 = 34861;
		public static final int GL_DRAW_BUFFER9 = 34862;
		public static final int GL_DRAW_BUFFER10 = 34863;
		public static final int GL_DRAW_BUFFER11 = 34864;
		public static final int GL_DRAW_BUFFER12 = 34865;
		public static final int GL_DRAW_BUFFER13 = 34866;
		public static final int GL_DRAW_BUFFER14 = 34867;
		public static final int GL_DRAW_BUFFER15 = 34868;
		public static final int GL_POINT_SPRITE = 34913;
		public static final int GL_COORD_REPLACE = 34914;
		public static final int GL_POINT_SPRITE_COORD_ORIGIN = 36000;
		public static final int GL_LOWER_LEFT = 36001;
		public static final int GL_UPPER_LEFT = 36002;
		public static final int GL_STENCIL_BACK_FUNC = 34816;
		public static final int GL_STENCIL_BACK_FAIL = 34817;
		public static final int GL_STENCIL_BACK_PASS_DEPTH_FAIL = 34818;
		public static final int GL_STENCIL_BACK_PASS_DEPTH_PASS = 34819;
		public static final int GL_STENCIL_BACK_REF = 36003;
		public static final int GL_STENCIL_BACK_VALUE_MASK = 36004;
		public static final int GL_STENCIL_BACK_WRITEMASK = 36005;
		public static final int GL_BLEND_EQUATION_RGB = 32777;
		public static final int GL_BLEND_EQUATION_ALPHA = 34877;
		public static final int GL_TEXTURE_MAX_ANISOTROPY = 34046;
		public static final int GL_CONTEXT_LOST_WEBGL = -100;
    }

	public interface WebGL2RenderingContext extends WebGLRenderingContext {
		
	    int TEXTURE_MAX_LEVEL              = 0x0000813D;
	    int TEXTURE_MAX_ANISOTROPY_EXT     = 0x000084FE;
	    int UNSIGNED_INT_24_8              = 0x000084FA;
		int ANY_SAMPLES_PASSED             = 0x00008D6A; 
		int QUERY_RESULT                   = 0x00008866;
		int QUERY_RESULT_AVAILABLE         = 0x00008867;
		int DEPTH24_STENCIL8               = 0x000088F0;
		int DEPTH_COMPONENT32F             = 0x00008CAC;
		int READ_FRAMEBUFFER               = 0x00008CA8;
		int DRAW_FRAMEBUFFER               = 0x00008CA9;
		int RGB8                           = 0x00008051;
		int RGBA8                          = 0x00008058;
		
		WebGLQuery createQuery();

		void beginQuery(int p1, WebGLQuery obj);

		void endQuery(int p1);

		void deleteQuery(WebGLQuery obj);

		int getQueryParameter(WebGLQuery obj, int p2);

		WebGLVertexArray createVertexArray();

		void deleteVertexArray(WebGLVertexArray obj);  

		void bindVertexArray(WebGLVertexArray obj); 
		
		void renderbufferStorageMultisample(int p1, int p2, int p3, int p4, int p5);
		
		void blitFramebuffer(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8, int p9, int p10);
		
		void drawBuffers(int[] p1);
	    
	}
	
	public interface WebGLQuery extends JSObject {
	}
	
	public interface WebGLVertexArray extends JSObject {
	}
	
	public static final class TextureGL { 
		public final WebGLTexture obj;
		public int w = -1;
		public int h = -1;
		public boolean nearest = true;
		public boolean anisotropic = false;
		public TextureGL(WebGLTexture obj) { 
			this.obj = obj; 
		} 
	} 
	public static final class BufferGL { 
		public final WebGLBuffer obj; 
		public BufferGL(WebGLBuffer obj) { 
			this.obj = obj; 
		} 
	} 
	public static final class ShaderGL { 
		public final org.teavm.jso.webgl.WebGLShader obj; 
		public ShaderGL(org.teavm.jso.webgl.WebGLShader webGLShader) { 
			this.obj = webGLShader; 
		} 
	}
	private static int progId = 0;
	public static final class ProgramGL { 
		public final WebGLProgram obj; 
		public final int hashcode; 
		public ProgramGL(WebGLProgram obj) { 
			this.obj = obj; 
			this.hashcode = ++progId;
		} 
	} 
	public static final class UniformGL { 
		public final WebGLUniformLocation obj; 
		public UniformGL(WebGLUniformLocation obj) { 
			this.obj = obj; 
		} 
	} 
	public static final class BufferArrayGL { 
		public final WebGLVertexArray obj; 
		public boolean isQuadBufferBound; 
		public BufferArrayGL(WebGLVertexArray obj) { 
			this.obj = obj; 
			this.isQuadBufferBound = false; 
		} 
	} 
	public static final class FramebufferGL { 
		public final WebGLFramebuffer obj; 
		public FramebufferGL(WebGLFramebuffer obj) { 
			this.obj = obj; 
		} 
	} 
	public static final class RenderbufferGL { 
		public final WebGLRenderbuffer obj; 
		public RenderbufferGL(WebGLRenderbuffer obj) { 
			this.obj = obj; 
		} 
	} 
	public static final class QueryGL { 
		public final WebGLQuery obj; 
		public QueryGL(WebGLQuery obj) { 
			this.obj = obj; 
		} 
	}
	
	// credit to lax1dude for the original code
	// (changes have been made for it to work with matrix classes)
	public static class WebGLShader {

		private static final WebGLShader[] instances = new WebGLShader[128];
		
		private static String shader = null;

		public static void refreshCoreGL() {
			for (int i = 0; i < instances.length; ++i) {
				if (instances[i] != null) {
					GL11.glDeleteProgram(instances[i].globject);
					instances[i] = null;
				}
			}
			shader = null;
		}

		public static final int COLOR = 1;
		public static final int NORMAL = 2;
		public static final int TEXTURE0 = 4;
		public static final int LIGHTING = 8;
		public static final int FOG = 16;
		public static final int ALPHATEST = 32;
		public static final int UNIT0 = 64;

		public static WebGLShader instance(int i) {
			WebGLShader s = instances[i];
			if (s == null) {
				boolean CC_a_color = false;
				boolean CC_a_normal = false;
				boolean CC_a_texture0 = false;
				boolean CC_lighting = false;
				boolean CC_fog = false;
				boolean CC_alphatest = false;
				boolean CC_unit0 = false;
				if ((i & COLOR) == COLOR) {
					CC_a_color = true;
				}
				if ((i & NORMAL) == NORMAL) {
					CC_a_normal = true;
				}
				if ((i & TEXTURE0) == TEXTURE0) {
					CC_a_texture0 = true;
				}
				if ((i & LIGHTING) == LIGHTING) {
					CC_lighting = true;
				}
				if ((i & FOG) == FOG) {
					CC_fog = true;
				}
				if ((i & ALPHATEST) == ALPHATEST) {
					CC_alphatest = true;
				}
				if ((i & UNIT0) == UNIT0) {
					CC_unit0 = true;
				}
				s = new WebGLShader(i, CC_a_color, CC_a_normal, CC_a_texture0, CC_lighting, CC_fog, CC_alphatest, CC_unit0);
				instances[i] = s;
			}
			return s;
		}

		private final boolean enable_color;
		private final boolean enable_normal;
		private final boolean enable_texture0;
		private final boolean enable_lighting;
		private final boolean enable_fog;
		private final boolean enable_alphatest;
		private final boolean enable_unit0;
		private final ProgramGL globject;

		private UniformGL u_matrix_m = null;
		private UniformGL u_matrix_p = null;
		private UniformGL u_matrix_t = null;

		private UniformGL u_fogColor = null;
		private UniformGL u_fogMode = null;
		private UniformGL u_fogStart = null;
		private UniformGL u_fogEnd = null;
		private UniformGL u_fogDensity = null;
		private UniformGL u_fogPremultiply = null;

		private UniformGL u_colorUniform = null;
		private UniformGL u_normalUniform = null;

		private UniformGL u_alphaTestF = null;

		private UniformGL u_texCoordV0 = null;

		private UniformGL u_light0Pos = null;
		private UniformGL u_light1Pos = null;

		private final int a_position;
		private final int a_texture0;
		private final int a_color;
		private final int a_normal;

		public final BufferArrayGL genericArray;
		public final BufferGL genericBuffer;
		public boolean bufferIsInitialized = false;

		private WebGLShader(int j, boolean CC_a_color, boolean CC_a_normal, boolean CC_a_texture0,
				boolean CC_lighting, boolean CC_fog, boolean CC_alphatest, boolean CC_unit0) {
			enable_color = CC_a_color;
			enable_normal = CC_a_normal;
			enable_texture0 = CC_a_texture0;
			enable_lighting = CC_lighting;
			enable_fog = CC_fog;
			enable_alphatest = CC_alphatest;
			enable_unit0 = CC_unit0;

			if (shader == null) {
				shader = new String(vertexFragmentShader);
			}

			String source = "";
			if (enable_color)
				source += "\n#define CC_a_color\n";
			if (enable_normal)
				source += "#define CC_a_normal\n";
			if (enable_texture0)
				source += "#define CC_a_texture0\n";
			if (enable_lighting)
				source += "#define CC_lighting\n";
			if (enable_fog)
				source += "#define CC_fog\n";
			if (enable_alphatest)
				source += "#define CC_alphatest\n";
			if (enable_unit0)
				source += "#define CC_unit0\n";
			source += shader;

			ShaderGL v = GL11.glCreateShader(GL11.GL_VERTEX_SHADER);
			GL11.glShaderSource(v, GL11.glGetShaderHeader() + "\n#define CC_VERT\n" + source);
			GL11.glCompileShader(v);

			if (!GL11.glGetShaderCompiled(v)) {
				System.err.println(("\n\n" + GL11.glGetShaderInfoLog(v)).replace("\n", "\n[main.Main.vertexFragmentShader][CC_VERT] "));
				throw new RuntimeException("broken shader source");
			}

			ShaderGL f = GL11.glCreateShader(GL11.GL_FRAGMENT_SHADER);
			GL11.glShaderSource(f, GL11.glGetShaderHeader() + "\n#define CC_FRAG\n" + source);
			GL11.glCompileShader(f);

			if (!GL11.glGetShaderCompiled(f)) {
				System.err.println(("\n\n" + GL11.glGetShaderInfoLog(f)).replace("\n", "\n[main.Main.vertexFragmentShader][CC_FRAG] "));
				throw new RuntimeException("broken shader source");
			}

			globject = GL11.glCreateProgram();
			GL11.glAttachShader(globject, v);
			GL11.glAttachShader(globject, f);

			int i = 0;
			a_position = i++;
			GL11.glBindAttributeLocation(globject, a_position, "a_position");

			if (enable_texture0) {
				a_texture0 = i++;
				GL11.glBindAttributeLocation(globject, a_texture0, "a_texture0");
			} else {
				a_texture0 = -1;
			}
			if (enable_color) {
				a_color = i++;
				GL11.glBindAttributeLocation(globject, a_color, "a_color");
			} else {
				a_color = -1;
			}
			if (enable_normal) {
				a_normal = i++;
				GL11.glBindAttributeLocation(globject, a_normal, "a_normal");
			} else {
				a_normal = -1;
			}

			GL11.glLinkProgram(globject);

			GL11.glDetachShader(globject, v);
			GL11.glDetachShader(globject, f);
			GL11.glDeleteShader(v);
			GL11.glDeleteShader(f);

			if (!GL11.glGetProgramLinked(globject)) {
				System.err.println(("\n\n" + GL11.glGetProgramInfoLog(globject)).replace("\n", "\n[LINKER] "));
				throw new RuntimeException("broken shader source");
			}

			GL11.glUseProgram(globject);

			u_matrix_m = GL11.glGetUniformLocation(globject, "matrix_m");
			u_matrix_p = GL11.glGetUniformLocation(globject, "matrix_p");
			u_matrix_t = GL11.glGetUniformLocation(globject, "matrix_t");

			u_colorUniform = GL11.glGetUniformLocation(globject, "colorUniform");

			if (enable_lighting) {
				u_normalUniform = GL11.glGetUniformLocation(globject, "normalUniform");
				u_light0Pos = GL11.glGetUniformLocation(globject, "light0Pos");
				u_light1Pos = GL11.glGetUniformLocation(globject, "light1Pos");
			}

			if (enable_fog) {
				u_fogColor = GL11.glGetUniformLocation(globject, "fogColor");
				u_fogMode = GL11.glGetUniformLocation(globject, "fogMode");
				u_fogStart = GL11.glGetUniformLocation(globject, "fogStart");
				u_fogEnd = GL11.glGetUniformLocation(globject, "fogEnd");
				u_fogDensity = GL11.glGetUniformLocation(globject, "fogDensity");
				u_fogPremultiply = GL11.glGetUniformLocation(globject, "fogPremultiply");
			}

			if (enable_alphatest) {
				u_alphaTestF = GL11.glGetUniformLocation(globject, "alphaTestF");
			}

			GL11.glUniform1i(GL11.glGetUniformLocation(globject, "tex0"), 0);
			u_texCoordV0 = GL11.glGetUniformLocation(globject, "texCoordV0");

			genericArray = GL11.glCreateVertexArray();
			genericBuffer = GL11.glCreateBuffer();
			GL11.glBindVertexArray(genericArray);
			GL11.glBindBuffer(GL11.GL_ARRAY_BUFFER, genericBuffer);
			setupArrayForProgram();

		}

		public void setupArrayForProgram() {
			GL11.glEnableVertexAttribArray(a_position);
			GL11.glVertexAttribPointer(a_position, 3, GL11.GL_FLOAT, false, 28, 0);
			if (enable_texture0) {
				GL11.glEnableVertexAttribArray(a_texture0);
				GL11.glVertexAttribPointer(a_texture0, 2, GL11.GL_FLOAT, false, 28, 12);
			}
			if (enable_color) {
				GL11.glEnableVertexAttribArray(a_color);
				GL11.glVertexAttribPointer(a_color, 4, GL11.GL_UNSIGNED_BYTE, true, 28, 20);
			}
			if (enable_normal) {
				GL11.glEnableVertexAttribArray(a_normal);
				GL11.glVertexAttribPointer(a_normal, 4, GL11.GL_UNSIGNED_BYTE, true, 28, 24);
			}
		}

		public void use() {
			GL11.glUseProgram(globject);
		}

		public void unuse() {

		}

		private FloatBuffer modelBuffer = FloatBuffer.wrap(new float[16]);
		private FloatBuffer projectionBuffer = FloatBuffer.wrap(new float[16]);
		private FloatBuffer textureBuffer = FloatBuffer.wrap(new float[16]);

		private Matrix4f modelMatrix = (Matrix4f) new Matrix4f().setZero();
		private Matrix4f projectionMatrix = (Matrix4f) new Matrix4f().setZero();
		private Matrix4f textureMatrix = (Matrix4f) new Matrix4f().setZero();
		private Vector4f light0Pos = new Vector4f();
		private Vector4f light1Pos = new Vector4f();

		public void modelMatrix(Matrix4f mat) {
			if (!mat.equals(modelMatrix)) {
				modelMatrix.load(mat).store(modelBuffer);
				GL11.glUniformMat4fv(u_matrix_m, modelBuffer.array());
			}
		}

		public void projectionMatrix(Matrix4f mat) {
			if (!mat.equals(projectionMatrix)) {
				projectionMatrix.load(mat).store(projectionBuffer);
				GL11.glUniformMat4fv(u_matrix_p, projectionBuffer.array());
			}
		}

		public void textureMatrix(Matrix4f mat) {
			if (!mat.equals(textureMatrix)) {
				textureMatrix.load(mat).store(textureBuffer);
				GL11.glUniformMat4fv(u_matrix_t, textureBuffer.array());
			}
		}

		public void lightPositions(Vector4f pos0, Vector4f pos1) {
			if (!pos0.equals(light0Pos) || !pos1.equals(light1Pos)) {
				light0Pos.set(pos0);
				light1Pos.set(pos1);
				GL11.glUniform3f(u_light0Pos, light0Pos.x, light0Pos.y, light0Pos.z);
				GL11.glUniform3f(u_light1Pos, light1Pos.x, light1Pos.y, light1Pos.z);
			}
		}

		private int fogMode = 0;

		public void fogMode(int mode) {
			if (fogMode != mode) {
				fogMode = mode;
				GL11.glUniform1i(u_fogMode, mode % 2);
				GL11.glUniform1f(u_fogPremultiply, mode / 2);
			}
		}

		private float fogColorR = 0.0f;
		private float fogColorG = 0.0f;
		private float fogColorB = 0.0f;
		private float fogColorA = 0.0f;

		public void fogColor(float r, float g, float b, float a) {
			if (fogColorR != r || fogColorG != g || fogColorB != b || fogColorA != a) {
				fogColorR = r;
				fogColorG = g;
				fogColorB = b;
				fogColorA = a;
				GL11.glUniform4f(u_fogColor, fogColorR, fogColorG, fogColorB, fogColorA);
			}
		}

		private float fogStart = 0.0f;
		private float fogEnd = 0.0f;

		public void fogStartEnd(float s, float e) {
			if (fogStart != s || fogEnd != e) {
				fogStart = s;
				fogEnd = e;
				GL11.glUniform1f(u_fogStart, fogStart);
				GL11.glUniform1f(u_fogEnd, fogEnd);
			}
		}

		private float fogDensity = 0.0f;

		public void fogDensity(float d) {
			if (fogDensity != d) {
				fogDensity = d;
				GL11.glUniform1f(u_fogDensity, fogDensity);
			}
		}

		private float alphaTestValue = 0.0f;

		public void alphaTest(float limit) {
			if (alphaTestValue != limit) {
				alphaTestValue = limit;
				GL11.glUniform1f(u_alphaTestF, alphaTestValue);
			}
		}

		private float tex0x = 0.0f;
		private float tex0y = 0.0f;

		public void tex0Coords(float x, float y) {
			if (tex0x != x || tex0y != y) {
				tex0x = x;
				tex0y = y;
				GL11.glUniform2f(u_texCoordV0, tex0x, tex0y);
			}
		}

		private float colorUniformR = 0.0f;
		private float colorUniformG = 0.0f;
		private float colorUniformB = 0.0f;
		private float colorUniformA = 0.0f;

		public void color(float r, float g, float b, float a) {
			if (colorUniformR != r || colorUniformG != g || colorUniformB != b || colorUniformA != a) {
				colorUniformR = r;
				colorUniformG = g;
				colorUniformB = b;
				colorUniformA = a;
				GL11.glUniform4f(u_colorUniform, colorUniformR, colorUniformG, colorUniformB, colorUniformA);
			}
		}

		private float normalUniformX = 0.0f;
		private float normalUniformY = 0.0f;
		private float normalUniformZ = 0.0f;

		public void normal(float x, float y, float z) {
			if (normalUniformX != x || normalUniformY != y || normalUniformZ != z) {
				normalUniformX = x;
				normalUniformY = y;
				normalUniformZ = z;
				GL11.glUniform3f(u_normalUniform, normalUniformX, normalUniformY, normalUniformZ);
			}
		}

	}
}
