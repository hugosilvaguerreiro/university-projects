
#include "textures.h"
#include "TGA.h"
#include <vector>
#include <string>

GLuint TextureArray[46];
GLuint SKYBOXTEXTURE;

Texture::Texture() {
    this->mode = NO_TEXTURE;
}

Texture::Texture(TEXTURE_MODE mode, AVAILABLE_TEXTURES texture1) {
    this->texture1 = texture1;
    this->mode = mode;
}

Texture::Texture(TEXTURE_MODE mode, AVAILABLE_TEXTURES texture1, AVAILABLE_TEXTURES texture2) {
    this->texture1 = texture1;
    this->texture2 = texture2;
    this->mode = mode;
}

void Texture::registerTextures() {
    glGenTextures(46, TextureArray); //If you're registering a new texture, make it in the same order as the enum
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/grass.tga", GRASS);
	TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/stone.tga", STONE);
	TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/checker.tga", CHECKER);
	TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/lightwood.tga", WOOD);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/water.tga", WATER);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/road.tga", ROAD);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/stone.tga", WALKWAY);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/road2.tga", GRAVEL);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/paused.tga", PAUSED);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/game_over.tga", GAME_OVER);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/turtle.tga", TURTLE_SHELL);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/trunk.tga", TRUNK_TEXT);

    //numbers
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/0.tga", SYMBOL_0);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/1.tga", SYMBOL_1);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/2.tga", SYMBOL_2);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/3.tga", SYMBOL_3);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/4.tga", SYMBOL_4);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/5.tga", SYMBOL_5);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/6.tga", SYMBOL_6);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/7.tga", SYMBOL_7);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/8.tga", SYMBOL_8);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/9.tga", SYMBOL_9);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/font/points.tga", POINTS);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/frog.tga", LIVES);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/frogtex.tga", FROG_TEX);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/Flare1.tga", FLARE_1);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/Flare2.tga", FLARE_2);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/Flare3.tga", FLARE_3);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/ShellTop_Base_Color.tga", TURTLE_SHELL_DETAILED);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/Turtle_Base_Color.tga", TURTLE_BODY_DETAILED);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/police_car.tga", POLICE_CAR_TEX);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/cartoon_body_orange.tga", CARTOON_CAR_TEX);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/cartoon_interior.tga", CARTOON_INTERIOR_TEX);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/road_hd.tga", ROAD_HD);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/stone_hd.tga", PAVEMENT_HD);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/stone_normal.tga", PAVEMENT_NORMAL);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/water2.tga", WATER_HD);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/river_stones.tga", RIVER_STONES_HD);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/stop2.tga", BILLBOARD);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/stop.tga", BILLBOARD2);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/particle.tga", PARTICLE);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/sky_front.tga", SKY_BOX_FRONT);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/sky_up.tga", SKY_BOX_UP);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/sky_down.tga", SKY_BOX_DOWN);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/sky_left.tga", SKY_BOX_LEFT);
    TGA_Texture(TextureArray, (char*)"engine/core/renderer/tex/sky_right.tga", SKY_BOX_RIGHT);

    std::vector<std::string> faces= {
        "engine/core/renderer/tex/right.tga",
        "engine/core/renderer/tex/left.tga",
        "engine/core/renderer/tex/top.tga",
        "engine/core/renderer/tex/bottom.tga",
        "engine/core/renderer/tex/front.tga",
        "engine/core/renderer/tex/back.tga"
    };
    loadCubemap(TextureArray, SKY, faces);
    

}

GLuint Texture::getTexture(AVAILABLE_TEXTURES texture) {
    return TextureArray[texture];
}