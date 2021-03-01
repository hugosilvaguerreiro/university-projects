#include "common.h"
#include "scene_object.h"
#include "engine.h"
#include "engine_utils.h"
#include "renderer.h"
#include "controlers.h"
#include "textures.h"
#include "game.h"
#include "bus.h"
#include "car.h"

#define CAPTION "Frogger"
#define LIMIT 8

int WindowHandle = 0;
int WinX = 640, WinY = 480;

unsigned int FrameCount = 0;

float road[4] = {-1.5, -4, -6.5, -9};
float riverTrunk[3] = {2, 6, 10};
float riverTurtle[2] = {4, 8};

Trunk *trunks[LIMIT];
Turtle2 *turtles[LIMIT];
CartoonCar *vehicles[LIMIT];

static int indexObjects = -1;

VSShaderLib shader;

struct MyMesh mesh[4];
int objId=0;  

GLint pvm_uniformId;
GLint vm_uniformId;
GLint normal_uniformId;
GLint lPos_uniformId;
	
 
Camera Cam(PERSPECTIVE, WinX, WinY);
Camera OldCam(PERSPECTIVE, WinX, WinY);

 
int startX, startY, tracking = 0;

 
float alpha = 39.0f, beta = 51.0f;
float r = 10.0f;

 
long myTime,timebase = 0,frame = 0;
char s[32];
float lightPos[4] = {20.0f, 10.0f, 30.0f, 1.0f};

bool ENGINE_PAUSED = false;
bool ENGINE_GAME_OVER = false;
SceneQuad *QUAD;

bool firstTime = true;
 
Engine::Engine() {
    this->renderer = Renderer();
}

 
void Engine::initializeGlut(int argc, char **argv) {
     
	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_DEPTH|GLUT_DOUBLE|GLUT_RGBA|GLUT_MULTISAMPLE|GLUT_STENCIL);

	glutInitContextVersion (3, 3);
	glutInitContextProfile (GLUT_CORE_PROFILE );
	glutInitContextFlags(GLUT_FORWARD_COMPATIBLE | GLUT_DEBUG);

	glutInitWindowPosition(100,100);
	glutInitWindowSize(WinX, WinY);
	WindowHandle = glutCreateWindow(CAPTION);
}

 
int Engine::initializeEngine(int argc, char **argv) {
    this->initializeGlut(argc, argv);
     
	glutDisplayFunc(Renderer::renderScene);
	glutReshapeFunc(Renderer::changeSize);

	glutTimerFunc(0, timer, 0);
    glutTimerFunc(0, Engine::checkCollisions, 0);  
    glutTimerFunc(0, Moviment::moveObjects, 0);
	glutIdleFunc(Renderer::renderScene);   

	 

     
	glutKeyboardFunc(Controlers::processKeys);
	glutMouseFunc(Controlers::processMouseButtons);
	glutMotionFunc(Controlers::processMouseMotion);
	glutMouseWheelFunc (Controlers::mouseWheel ) ;

     
	glutSetOption(GLUT_ACTION_ON_WINDOW_CLOSE, GLUT_ACTION_GLUTMAINLOOP_RETURNS);

     
	glewExperimental = GL_TRUE;
	glewInit();

	printf ("Vendor: %s\n", glGetString (GL_VENDOR));
	printf ("Renderer: %s\n", glGetString (GL_RENDERER));
	printf ("Version: %s\n", glGetString (GL_VERSION));
	printf ("GLSL: %s\n", glGetString (GL_SHADING_LANGUAGE_VERSION));

	if (!this->renderer.setupShaders())
		return(1);
	
	return 0;
}

void randomizeTrunk(bool new_objs){
    srand (time(NULL));
    if(new_objs) {
        for(int i = 0; i<LIMIT; i++){
            int index1 = rand() % 3;
            Trunk *trunk = new Trunk(trunkMat, {-20, riverTrunk[index1], 0.1f});
            trunk->reflect = false;
            trunks[i] = trunk;
        }
    }
}

void randomizeTurtle(bool new_objs){
    srand (time(NULL));
    if(new_objs) {
        for(int i = 0; i<LIMIT; i++){
            int index1 = rand() % 2;
            Turtle2 *turtle = new Turtle2(turtleBodyMat, {20, riverTurtle[index1], 0.2f});
             
            turtle->setScale(0.009, 0.011, 0.009);
            turtles[i] = turtle;
        }
    }

}

void randomizeVehicle(bool new_objs) {

    if(new_objs) {
        for(int i=0; i<LIMIT; i++) {
            int index1 = rand() % 2;
            int index2 = rand() % 4;
            

            float x = -20;
            if(index2 == 1 || index2 == 3) {
                x = 20;
            }
            CartoonCar *car = new CartoonCar(curbMat, {x, road[index2], 0});
            car->setScale(0.005, 0.005, 0.005);

            if(x < 0)  {
                car->setRotation(180, 0, 0, 1);

            }
            vehicles[i] = car;
                


            /*if(index1 == 0){            
                 
                Car2 *car = new Car2(carMat, {x, road[index2], 0});
                car->setScale(0.035, 0.035, 0.035);
                 
                vehicles[i] = car; 
            }
            else{
                 
                 
                Car2 *car = new Car2(carMat, {x, road[index2]+0.5, 0});
                car->setScale(0.035, 0.035, 0.035);
                car->setRotation(180, 0, 0, 1);
                vehicles[i] = car; 
            }*/
        }
    }

}

void startFabric(bool new_objs) {
    srand (time(NULL));
    randomizeTrunk(new_objs);   
    randomizeTurtle(new_objs);
    randomizeVehicle(new_objs);
}

void animateEverything(int value) {
    if(indexObjects < LIMIT-1) {
        indexObjects++;
        Renderer::getScene()->putObject((SceneCompositeObject*)vehicles[indexObjects]);
        Moviment::putObject(vehicles[indexObjects]);

        Renderer::getScene()->putObject(trunks[indexObjects]);
        Moviment::putObject(trunks[indexObjects]);

        Renderer::getScene()->putObject(turtles[indexObjects]);
        Moviment::putObject(turtles[indexObjects]);

        if(firstTime){
            Moviment::checkMovementColision();
            firstTime = false;
        }
        glutTimerFunc(1100, animateEverything, 0); 
    }
}

int Engine::start() {
    Moviment::putPlayer(Controlers::getPlayer());

	this->init();   
    startFabric(true);
    glutTimerFunc(0, animateEverything, 0); 

	glutMainLoop();

	return(0);  
}

void Engine::togglePause() {
    if (ENGINE_PAUSED) {
        ENGINE_PAUSED = false;
        Moviment::resume();
    } else {
        ENGINE_PAUSED = true;
        Moviment::pause();
    }
}

bool Engine::isPaused() {
    return ENGINE_PAUSED;
}

void Engine::gameOver() {
    Moviment::restart();

    Moviment::resume();
}

void Engine::setScene(Scene *scene) {
     
    SceneQuad *quad = new SceneQuad(defaultMat, {-1000, -1000, -1000}, 50, 25);
    quad->setTexture(Texture(NO_LIGHT, AVAILABLE_TEXTURES::PAUSED));
    scene->putObject(quad);
    QUAD = quad;

    Renderer::setScene(scene);
}

void Engine::setHUD(HUD *hud) {
     
    Renderer::setHUD(hud);
}

 
 
 
 

void Engine::init() {
	 
	 
	 
	 

	 
	glEnable(GL_DEPTH_TEST);
	glEnable(GL_CULL_FACE);
	glEnable(GL_MULTISAMPLE);
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

}


bool checkColision(SceneObject*obj1, SceneObject* obj2) {
    BoundingBox *bound1 = obj1->getBoundingBox();
    BoundingBox *bound2 = obj2->getBoundingBox();

     
     
    if(bound1->objectType != bound2->objectType){
        if ((bound1->position.x < (bound2->position.x+bound2->length)) &&
            ((bound1->position.x+bound1->length) > bound2->position.x ) &&
            (bound1->position.y > (bound2->position.y-bound2->width)) &&
            ((bound1->position.y-bound1->width) < bound2->position.y)) {
                return true;
            }
    }
    return false;
}

void Engine::checkCollisions(int value) {
    SceneObject* player_obj = Controlers::getPlayer()->obj;
    
    std::vector<SceneObject*> colisions = std::vector<SceneObject*>();
    auto objects = Renderer::getScene()->getObjects();
    bool colision;
    for(std::vector<SceneObject*>::iterator it = objects->begin(); it != objects->end(); ++it) {
        colision = checkColision(player_obj, *it);
        if(colision) {
            player_obj->preColision(*it);
            colisions.push_back(*it);
        }
    }
    colision = checkColision(player_obj, Renderer::getScene()->getReflected()->front());
    if(colision) {
        player_obj->preColision(Renderer::getScene()->getReflected()->front());
        colisions.push_back(Renderer::getScene()->getReflected()->front());
    }
    for(std::vector<SceneObject*>::iterator it = colisions.begin(); it != colisions.end(); ++it) {
        player_obj->onColision(*it);
    }
    player_obj->afterColisions();
    glutTimerFunc(1000/60, Engine::checkCollisions, 0);  
}

void Engine::activateDebugAxis(double axis_size) {
    Renderer::drawDebugAxis(GLdouble(axis_size));
}
