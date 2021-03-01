
#include <math.h>
#include <iostream>
#include <sstream>

#include <string>
#include <cstring>
#include <GL/glew.h>


 
#include <GL/freeglut.h>

 

#include "controlers.h"
#include "common.h"
#include "player.h"
#include "renderer.h"
#include "engine.h"

 
 
 
 

Player *PLAYER = nullptr;

Player* Controlers::getPlayer() {
    return PLAYER;
}

void Controlers::setPlayer(Player* player) {
    PLAYER = player;
}

void Controlers::processKeys(unsigned char key, int xx, int yy)
{   if(!Engine::isPaused() && !ENGINE_GAME_OVER) {
        switch(key) {
            case '1':
                PLAYER->set_cam(nullptr);
                Cam.setPos(0.f, 0.f, 20.f);
                Cam.setTarget(0.f, 0.f, 0.f);
                Cam.setUp(0.f, 1.f, 0.f);
                Cam.setOrtho();
                Cam.setMode(1);
                break;
            case '2':
                PLAYER->set_cam(nullptr);
                Cam.setPos(-15.f, -15.f, 10.f);
                Cam.setTarget(0.f, 0.f, 0.f);
                Cam.setUp(1.f, 1.f, 1.f);
                Cam.setPerspective();
                Cam.setMode(2);
                break;
            case '3':
                PLAYER->set_cam(&Cam);
                Cam.setPerspective();
                Cam.setMode(3);
                break;
            case 27:
                glutLeaveMainLoop();
                break;
            case 'o': 
                PLAYER->move_left();
                break;
            case 'a': 
                PLAYER->move_back();
                break;
            case 'p': 
                PLAYER->move_right();
                break;
            case 'q': 
                PLAYER->move_front();
                break;
            case 'y':
                PLAYER->setPoints(PLAYER->getPoints() + 1);
                break;
            case 's':
                Engine::togglePause();
                break;
            case 'f':
                Renderer::toggleFog();
                break;
            case 'j':
                Renderer::toggleSnow();
                break;
            case 'l':
                Renderer::toggleLensFlare();
                break;
            case '-': 
                printf("Camera Spherical Coordinates (%f, %f, %f)\n", alpha, beta, r);
                break;
            case ',': glEnable(GL_MULTISAMPLE); break;
            case '.': glDisable(GL_MULTISAMPLE); break;
            case 'n': 
                Renderer::getScene()->toggleDirectionalLights();
                break;
            case 'c': 
                Renderer::getScene()->togglePointLights();
                break;
			case 'h': 
                Renderer::getScene()->toggleSpotLights();
                break;
    }
    } else {
        if(Engine::isPaused() || ENGINE_GAME_OVER) {
            switch (key) {
            case 's':
                Engine::togglePause();
                break;
            case 'r':
                ENGINE_GAME_OVER = false;
                ENGINE_PAUSED = false;
                PLAYER->setLifes(5);
                PLAYER->setPoints(0);
                Engine::gameOver();
                PLAYER->moveObjectToPosition(PLAYER->initialPosition);
                break;
            }
        }

	}
}

 
 
 
 
void Controlers::processMouseButtons(int button, int state, int xx, int yy)
{
	 
	if (state == GLUT_DOWN)  {
		startX = xx;
		startY = yy;
		if (button == GLUT_LEFT_BUTTON)
			tracking = 1;
		else if (button == GLUT_RIGHT_BUTTON)
			tracking = 2;
	}

	 
	else if (state == GLUT_UP) {
		if (tracking == 1) {
			alpha -= (xx - startX);
			beta += (yy - startY);
		}
		else if (tracking == 2) {
			r += (yy - startY) * 0.01f;
			if (r < 0.1f)
				r = 0.1f;
		}
		tracking = 0;
	}
}

 

void Controlers::processMouseMotion(int xx, int yy)
{
	if(PLAYER->get_cam() != nullptr) {

		int deltaX, deltaY;
		float alphaAux, betaAux;
		float rAux;

		deltaX =  - xx + startX;
		deltaY =    yy - startY;

		 
		if (tracking == 1) {


			alphaAux = alpha + deltaX;
			betaAux = beta + deltaY;

			if (betaAux > 85.0f)
				betaAux = 85.0f;
			else if (betaAux < -85.0f)
				betaAux = -85.0f;
			rAux = r;
		}
		 
		else if (tracking == 2) {

			alphaAux = alpha;
			betaAux = beta;
			rAux = r + (deltaY * 0.01f);
			if (rAux < 0.1f)
				rAux = 0.1f;
		}


		float camX = PLAYER->get_cam()->targetX - deltaX*0.001;
		float camY = PLAYER->get_cam()->targetY;
		float camZ = PLAYER->get_cam()->targetZ;
		PLAYER->get_cam()->setTarget(camX, camY, camZ);
		
	}

 
 
}


void Controlers::mouseWheel(int wheel, int direction, int x, int y) {

	r += direction * 0.1f;
	if (r < 0.1f)
		r = 0.1f;

	 
	 
	 

 
 
}