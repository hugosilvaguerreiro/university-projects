#ifndef __RENDERERH__
#define __RENDERERH__

#include "scene.h"
void renderBillboards();
void renderSkyBox();
void renderFlare2();
void buildParticles();
class Renderer {

    public:
        static void renderLensFlare();
        static void setScene(Scene *scene);
        static void setHUD(HUD *hud);
        static Scene * getScene();
        static void togglePause();
        static void toggleFog();
        static void toggleSnow();
        static void toggleLensFlare();
        static void renderScene();
        static void renderHUD();
        static void changeSize(int w, int h);
        static void drawDebugAxis(GLdouble axis_size);
        GLuint setupShaders();
};
#endif
