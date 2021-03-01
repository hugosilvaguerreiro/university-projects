#ifndef __ENGINEH__
#define __ENGINEH__

#include <math.h>
#include <iostream>
#include <sstream>

#include <cstring>

// include GLEW to access OpenGL 3.3 functions
#include <GL/glew.h>


// GLUT is the toolkit to interface with the OS
#include <GL/freeglut.h>

// Use Very Simple Libs
#include "VSShaderLib.h"
#include "AVTmathLib.h"
#include "VertexAttrDef.h"
#include "basic_geometry.h"


#include "common.h"
#include "renderer.h"
#include "moviment.h"

extern bool ENGINE_PAUSED;
extern bool ENGINE_GAME_OVER;
class Engine {
    Renderer renderer;    
   private:
    void initializeGlut(int argc, char **argv);
    void registerCallbacks();
    void init();

   public:
    Engine();
    int initializeEngine(int argc, char **argv);
    void setScene(Scene *scene);
    void setHUD(HUD *hud);
    void activateDebugAxis(GLdouble axis_size);
    static void checkCollisions(int value);
    static void togglePause();
    static bool isPaused();
    static void gameOver();
    int start();
    
};

bool checkColision(SceneObject*obj1, SceneObject* obj2);

#endif