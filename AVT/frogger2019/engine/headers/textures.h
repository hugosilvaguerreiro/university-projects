#ifndef __TEXTURESH__
#define __TEXTURESH__

#include <string>
#include <GL/glew.h>
#include <GL/freeglut.h>

enum AVAILABLE_TEXTURES { 
    STONE,
    CHECKER,
    WOOD,
    WATER,
    ROAD,
    GRAVEL,
    GRASS,
    WALKWAY,
    TURTLE_SHELL,
    TRUNK_TEXT,
    PAUSED,
    GAME_OVER,
    SYMBOL_0,SYMBOL_1,SYMBOL_2,SYMBOL_3,SYMBOL_4,SYMBOL_5,
    SYMBOL_6,SYMBOL_7,SYMBOL_8,SYMBOL_9,POINTS,LIVES,
    FROG_TEX, FLARE_1, FLARE_2, FLARE_3, TURTLE_SHELL_DETAILED, 
    TURTLE_BODY_DETAILED, BILLBOARD,BILLBOARD2, POLICE_CAR_TEX, 
    CARTOON_CAR_TEX,CARTOON_INTERIOR_TEX, ROAD_HD, PAVEMENT_HD, PAVEMENT_NORMAL, WATER_HD,
    RIVER_STONES_HD, SKY, PARTICLE, SKY_BOX_FRONT, SKY_BOX_UP, SKY_BOX_DOWN,SKY_BOX_RIGHT,SKY_BOX_LEFT
};

enum TEXTURE_MODE {
    NO_TEXTURE,
    DEFAULT,
    MULTITEXTURING,
    NO_LIGHT,
    MODULATED,
    BUMP
};

//GLuint TextureArray[3];

class Texture { 
    public:
        TEXTURE_MODE mode;
        AVAILABLE_TEXTURES texture1;
        AVAILABLE_TEXTURES texture2; //for multitexturing
        Texture();
        Texture(TEXTURE_MODE mode, AVAILABLE_TEXTURES texture1);
        Texture(TEXTURE_MODE mode, AVAILABLE_TEXTURES texture1, AVAILABLE_TEXTURES texture2);
        static GLuint getTexture(AVAILABLE_TEXTURES texture);
        static void registerTextures();
};

#endif