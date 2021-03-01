#include <math.h>
#include <iostream>
#include <sstream>

#include <string>
#include <cstring>
#include <GL/glew.h>

#include <math.h>
#include <iostream>
#include <sstream>
#include <algorithm>
#include "l3DBillboard.h"

#include "controlers.h"
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

// GLUT is the toolkit to interface with the OS
#include <GL/freeglut.h>

#include "common.h"
#include "renderer.h"
#include "scene.h"
#include "TGA.h"
#include "textures.h"
#include "engine.h"
#include "material.h"
#include "trunk.h"

#define frand()			((float)rand()/RAND_MAX)
#define isqrt(x)        (int)((double)(x))
#define N_FLARES 6
#define NR_PARTICLES 500
// ----------------------------------------------------------
//
// GLOBAL VARIABLES
//
Scene *SCENE = nullptr;
HUD *Hud  = nullptr;

GLint tex_loc, tex_loc1, tex_loc2, tex_cube_loc, shadow_loc, fogActivated_loc, skyboxActivated_loc;
GLint texMode_uniformId;
GLint model_uniformId;

float xFlare = WinX / 2;
float yFlare = WinY / 2;

float flaresColor[4] = { 1.0f, 1.0f, 1.0f, 1.0f };
float flaresPos[N_FLARES] = { 0.0f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f };
float flaresScale[N_FLARES] = { 1.0f, 0.25f, 0.5f, 0.5f, 0.25f, 0.75f };


SceneQuad *HUDTest = nullptr;
std::vector<SceneObject*> to_draw;
std::vector<SceneObject*> paused_object;
std::vector<SceneObject*> billboards_objects;
std::vector<SceneObject*> lens_flare_objects;
std::vector<SceneObject*> game_over_object;
std::vector<SceneObject*> number_objects;
std::vector<SceneObject*> particles_objects;
SceneCube *SKYBOX=nullptr;

bool FOG_ACTIVATED = false;
bool LENS_FLARE_ACTIVATED = true;
bool SNOW_ACTIVATED = false;
typedef struct {
	float	life;		// vida
	float	fade;		// fade
	float	r, g, b;    // color
	GLfloat x, y, z;    // posicao
	GLfloat vx, vy, vz; // velocidade 
	GLfloat ax, ay, az; // aceleracao
} Particle;

Particle particula[NR_PARTICLES];


inline double clamp(const double x, const double min, const double max) {
	return (x < min ? min : (x > max ? max : x));
}

inline int clampi(const int x, const int min, const int max) {
	return (x < min ? min : (x > max ? max : x));
}


static void drawMesh(Texture * tex, MyMesh *currentObjectMesh) {
	// send matices to OGL
	computeDerivedMatrix(PROJ_VIEW_MODEL);
	glUniformMatrix4fv(vm_uniformId, 1, GL_FALSE, mCompMatrix[VIEW_MODEL]);
	glUniformMatrix4fv(pvm_uniformId, 1, GL_FALSE, mCompMatrix[PROJ_VIEW_MODEL]);
	computeNormalMatrix3x3();
	glUniformMatrix3fv(normal_uniformId, 1, GL_FALSE, mNormal3x3);
    

	// Render mesh
	glBindVertexArray(currentObjectMesh->vao);
	
	if (!shader.isProgramValid()) {
		printf("Program Not Valid!\n");
		exit(1);	
	}


	glDrawElements(currentObjectMesh->type, currentObjectMesh->numIndexes, GL_UNSIGNED_INT, 0);
	glBindVertexArray(0);
}

Scene * Renderer::getScene() {
    return SCENE;
}

void Renderer::setScene(Scene *scene) {
    SCENE = scene;
}

void Renderer::setHUD(HUD *hud) {
    Hud = hud;
}
// ------------------------------------------------------------
//
// Render stufff
//


void sendTexture(SceneObject * obj) {
        Texture * tex = obj->getTexture();
        if(tex->mode != NO_TEXTURE) {
            if(tex->mode == MULTITEXTURING || tex->mode == BUMP ) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, Texture::getTexture(tex->texture1)); 
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, Texture::getTexture(tex->texture2)); 
                glUniform1i(tex_loc, 0);
                glUniform1i(tex_loc1, 1);

            } else {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, Texture::getTexture(tex->texture1)); 
                glUniform1i(tex_loc, 0);
            }
        }
        
        texMode_uniformId = glGetUniformLocation(shader.getProgramIndex(), "texMode");
        glUniform1i(texMode_uniformId, tex->mode);
}

void sendMaterial(MyMesh *currentObjectMesh) {
    // send the material
	GLint loc;
    loc = glGetUniformLocation(shader.getProgramIndex(), "mat.ambient");
    glUniform4fv(loc, 1, currentObjectMesh->mat.ambient);
    loc = glGetUniformLocation(shader.getProgramIndex(), "mat.diffuse");
    glUniform4fv(loc, 1, currentObjectMesh->mat.diffuse);
    loc = glGetUniformLocation(shader.getProgramIndex(), "mat.specular");
    glUniform4fv(loc, 1, currentObjectMesh->mat.specular);
    loc = glGetUniformLocation(shader.getProgramIndex(), "mat.shininess");
    glUniform1f(loc, currentObjectMesh->mat.shininess);

    if(FOG_ACTIVATED)
        glUniform1i(fogActivated_loc, 1);
    else
        glUniform1i(fogActivated_loc, 0);
     
}

void renderSceneObject(SceneObject *obj, int blend) {
            MyMesh *currentObjectMesh = obj->getMesh();

            sendTexture(obj);
            sendMaterial(currentObjectMesh);

            pushMatrix(MODEL);

            Position *p = obj->getPosition();
            translate(MODEL, p->x , p->y, p->z);

            Scale *s = obj->getScale();
            scale(MODEL, s->x , s->y, s->z);

            std::vector<Rotation> *rotations; 
            rotations = obj->getRotations();
            for(auto rot : *rotations) {
                if(rot.angle > 0.0f) rotate(MODEL, rot.angle, rot.x , rot.y, rot.z);                        
            }

            Rotation *r = obj->getRotation();
            if(r->angle > 0.0f) rotate(MODEL, r->angle, r->x , r->y, r->z);

            if(blend) glEnable(GL_BLEND);
            if(blend == 1) glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            if(blend == 2) glBlendFunc(GL_DST_COLOR, GL_ZERO);
            
            drawMesh(obj->getTexture(), currentObjectMesh);
            popMatrix(MODEL);
            glBindTexture(GL_TEXTURE_2D, 0);
            if(blend) glDisable(GL_BLEND);
}

void Renderer::toggleFog() {
    FOG_ACTIVATED = !FOG_ACTIVATED;
}

void drawObjects(bool reflection, bool shadow, int blend) {
    // Iterate through the scene and render each object
    auto objects = SCENE->getObjects();
    for(std::vector<SceneObject*>::iterator it = objects->begin(); it != objects->end(); ++it) {
        SceneObject * obj = *it;
         if (SceneCompositeObject* sco = dynamic_cast<SceneCompositeObject*>(obj)) {
            for(std::vector<SceneObject*>::iterator it = sco->objects.begin(); it != sco->objects.end(); ++it) {
                obj = *it;
                MyMesh *currentObjectMesh = obj->getMesh();

                pushMatrix(MODEL);

                Position *p = sco->getPosition();
                translate(MODEL, p->x , p->y, p->z);

                Scale *s = sco->getScale();
                scale(MODEL, s->x , s->y, s->z);

                std::vector<Rotation> *rotations; 
                rotations = sco->getRotations();
                for(auto rot : *rotations) {
                    if(rot.angle > 0.0f) rotate(MODEL, rot.angle, rot.x , rot.y, rot.z);                        
                }

                Rotation *r = sco->getRotation();
                if(r->angle > 0.0f) rotate(MODEL, r->angle, r->x , r->y, r->z);
                renderSceneObject(obj, blend);
                    
                popMatrix(MODEL);

            }
            
        } else {
            if(reflection) {
                if(obj->reflect) {
                    renderSceneObject(obj, blend);
                }
            } else if(shadow) {
                if(obj->castShadow) {
                    renderSceneObject(obj, blend);
                }
            } else renderSceneObject(obj, blend);
        }
    }
}


void iterate(int value)
{
	int i;
	float h;

	/* M�todo de Euler de integra��o de eq. diferenciais ordin�rias
	h representa o step de tempo; dv/dt = a; dx/dt = v; e conhecem-se os valores iniciais de x e v */

	//h = 0.125f;
	h = 0.033;

		for (i = 0; i < NR_PARTICLES; i++)
		{
			particula[i].x += (h*particula[i].vx);
			particula[i].y += (h*particula[i].vy);
			particula[i].z += (h*particula[i].vz);
			particula[i].vx += (h*particula[i].ax);
			particula[i].vy += (h*particula[i].ay);
			particula[i].vz += (h*particula[i].az);
			particula[i].life -= particula[i].fade;
		}
		glutPostRedisplay();
        if(SNOW_ACTIVATED)
		    glutTimerFunc(33, iterate, 0);
}

void Renderer::toggleSnow() {
    SNOW_ACTIVATED = !SNOW_ACTIVATED;
    glutTimerFunc(0, iterate, 0);

}

int dead_num_particles = 0;
void renderParticles(void) {
    if(!SNOW_ACTIVATED) return;

	float particle_color[4];
    //Texture *tex = particles_objects.at(0)->getTexture();
    sendTexture(particles_objects.at(0));
	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	glDepthMask(GL_FALSE);
	glDisable(GL_CULL_FACE);

	for (int i = 0; i < NR_PARTICLES; i++)
	{
        if (particula[i].life > 0.0f) /* s� desenha as que ainda est�o vivas */
			{

				/* A vida da part�cula representa o canal alpha da cor. Como o blend est� activo a cor final � a soma da cor rgb do fragmento multiplicada pelo
				alpha com a cor do pixel destino */

				particle_color[0] = particula[i].r;
				particle_color[1] = particula[i].g;
				particle_color[2] = particula[i].b;
				particle_color[3] = particula[i].life;

				// send the material - diffuse color modulated with texture
				GLuint loc = glGetUniformLocation(shader.getProgramIndex(), "mat.diffuse");
				glUniform4fv(loc, 1, particle_color);

				pushMatrix(MODEL);
				translate(MODEL, particula[i].x, particula[i].y, particula[i].z);

				// send matrices to OGL
				computeDerivedMatrix(PROJ_VIEW_MODEL);
				glUniformMatrix4fv(vm_uniformId, 1, GL_FALSE, mCompMatrix[VIEW_MODEL]);
				glUniformMatrix4fv(pvm_uniformId, 1, GL_FALSE, mCompMatrix[PROJ_VIEW_MODEL]);
				computeNormalMatrix3x3();
				glUniformMatrix3fv(normal_uniformId, 1, GL_FALSE, mNormal3x3);

				glBindVertexArray(particles_objects.at(0)->getMesh()->vao);
				glDrawElements(particles_objects.at(0)->getMesh()->type, particles_objects.at(0)->getMesh()->numIndexes, GL_UNSIGNED_INT, 0);
				popMatrix(MODEL);
			}
            else {
                int max = 20;
                int min = -20;
                particula[i].x = rand()%(max-min + 1) + min;
		        particula[i].y = rand()%(max-min + 1) + min;
		        particula[i].z = rand()%(10 + 1);
                particula[i].life = frand();
                

            }
	}
    glDepthMask(GL_TRUE); //make depth buffer again writeable
    if (dead_num_particles == NR_PARTICLES) {
        //fireworks = 0;
        dead_num_particles = 0;
        //buildParticles();
    }

	glBindTexture(GL_TEXTURE_2D, 0);
	glDisable(GL_BLEND);
	glDepthMask(GL_TRUE);
	glEnable(GL_CULL_FACE);
}



void Renderer::renderScene(void) {
    int index;
	FrameCount++;
    glClearStencil(0);
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
	// load identity matrices
	loadIdentity(VIEW);
	loadIdentity(MODEL);
	// set the camera using a function similar to gluLookAt
	Cam.lookat();
	// use our shader
	glUseProgram(shader.getProgramIndex());
    glEnable(GL_DEPTH_TEST);

    float res[4];
	float mat[16];
	GLfloat plane[4] = { 0,0,1,0 };
    GLfloat shadowLight[4] = { 10,-10,20,0 };
    
    //renderSkyBox();
    if(SCENE->getReflected()->size() != 0) {

        // Fill stencil buffer with Ground shape;
        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_NEVER, 0x1, 0x1);
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE);

        renderSceneObject(SCENE->getReflected()->front(), 0);

        // Now we only draw on the stencil
        glStencilFunc(GL_EQUAL, 0x1, 0x1);
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);

        index = 0;
        for(std::vector<Light*>::iterator it = SCENE->getLights()->begin(); it != SCENE->getLights()->end(); ++it) {
            Light * light = *it;
            light->position[3] *= -1.f;
            light->render(index++);
        }
        
        pushMatrix(MODEL);
		scale(MODEL, 1.0f, 1.0f, -1.0f);
		glCullFace(GL_FRONT);
		drawObjects(true, false, 0);
		glCullFace(GL_BACK);
		popMatrix(MODEL);

        index = 0;
        for(std::vector<Light*>::iterator it = SCENE->getLights()->begin(); it != SCENE->getLights()->end(); ++it) {
            Light * light = *it;
            light->position[3] *= -1.f;
            light->render(index++);
        }

        glDisable(GL_STENCIL_TEST);
    }


    renderSceneObject(SCENE->getReflected()->front(), 1);
    renderSceneObject(SCENE->getShadow()->front(), 1);

    glUniform1i(shadow_loc, 1);  //Render with constant color

    shadow_matrix(mat, plane, shadowLight);

    glEnable(GL_STENCIL_TEST);
    glClearStencil(1);
    glClear(GL_STENCIL_BUFFER_BIT);
    glDisable(GL_DEPTH_TEST); //To force the shadow geometry to be rendered even if behind the floor

    //Dark the color stored in color buffer
    //glBlendFunc(GL_DST_COLOR, GL_ZERO);
    glStencilOp(GL_KEEP, GL_KEEP, GL_ZERO);
    
    pushMatrix(MODEL);
    multMatrix(MODEL, mat);
    drawObjects(false, true, 2);
    popMatrix(MODEL);

    glUniform1i(shadow_loc, 0);
    glEnable(GL_DEPTH_TEST);
    glDisable(GL_STENCIL_TEST);

    drawObjects(false, false, 1);
    renderLensFlare();
    renderBillboards();
    renderParticles();
    glDisable(GL_DEPTH_TEST);
    if(Hud != nullptr) {
        Renderer::renderHUD();   
    }
    
    glEnable(GL_DEPTH_TEST);
    //
    glDisable(GL_BLEND);
    popMatrix(PROJECTION);
    glDepthMask(GL_TRUE);
    glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    glutSwapBuffers();
}

void drawMesh2(MyMesh *mesh) {
    
    computeDerivedMatrix(PROJ_VIEW_MODEL);
	glUniformMatrix4fv(vm_uniformId, 1, GL_FALSE, mCompMatrix[VIEW_MODEL]);
	glUniformMatrix4fv(pvm_uniformId, 1, GL_FALSE, mCompMatrix[PROJ_VIEW_MODEL]);
	computeNormalMatrix3x3();
	glUniformMatrix3fv(normal_uniformId, 1, GL_FALSE, mNormal3x3);

	glBindVertexArray(mesh->vao);
	if (!shader.isProgramValid()) {
		printf("Program Not Valid!\n");
		exit(1);
	}
	glDrawElements(mesh->type, mesh->numIndexes, GL_UNSIGNED_INT, 0);
	glBindVertexArray(0);
}

void draw_hud_scene_object(SceneObject* object) {
        MyMesh *mesh = object->getMesh();
        GLint loc;

        loc = glGetUniformLocation(shader.getProgramIndex(), "mat.ambient");
        glUniform4fv(loc, 1, mesh->mat.ambient);
        loc = glGetUniformLocation(shader.getProgramIndex(), "mat.diffuse");
        glUniform4fv(loc, 1, mesh->mat.diffuse);
        loc = glGetUniformLocation(shader.getProgramIndex(), "mat.specular");
        glUniform4fv(loc, 1, mesh->mat.specular);
        loc = glGetUniformLocation(shader.getProgramIndex(), "mat.shininess");
        glUniform1f(loc, mesh->mat.shininess);


        pushMatrix(MODEL);
        Position *p = object->getPosition();
        translate(MODEL, p->x , p->y, p->z);

        Scale *s = object->getScale();
        scale(MODEL, s->x , s->y, s->z);

        auto rotations = object->getRotations();
        for(auto rot : *rotations) {
                if(rot.angle > 0.0f) rotate(MODEL, rot.angle, rot.x , rot.y, rot.z);                        
        }
        Rotation *r = object->getRotation();
        if(r->angle > 0.0f) rotate(MODEL, r->angle, r->x , r->y, r->z);

        glDepthMask(GL_FALSE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


        Texture * tex = object->getTexture();
        if(tex->mode != NO_TEXTURE) {
            if(tex->mode == MULTITEXTURING) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, Texture::getTexture(tex->texture1)); 
                glActiveTexture(GL_TEXTURE1);
                glBindTexture(GL_TEXTURE_2D, Texture::getTexture(tex->texture2)); 
                glUniform1i(tex_loc, 0);
                glUniform1i(tex_loc1, 1);

            } else {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, Texture::getTexture(tex->texture1)); 
                glUniform1i(tex_loc, 0);
            }
        }
        
        texMode_uniformId = glGetUniformLocation(shader.getProgramIndex(), "texMode");
        glUniform1i(texMode_uniformId, tex->mode);

    drawMesh2(mesh);
    glBindTexture(GL_TEXTURE_2D, 0);
    glDisable(GL_BLEND);
    glDepthMask(GL_TRUE);
    popMatrix(MODEL);
}


void Renderer::renderHUD(void) {
    std::vector<int> digits_to_draw = std::vector<int>();

    if(Engine::isPaused() && !ENGINE_GAME_OVER) {
        to_draw.push_back(paused_object.at(0));
    } else if(ENGINE_GAME_OVER) {
        to_draw.push_back(game_over_object.at(0));
    } else {
        int points = Hud->player->getPoints();
        int location = 0;
        do {
            int digit = points % 10;
            points /= 10;
            digits_to_draw.push_back(digit);
            
        } while (points > 0);
        to_draw.push_back(number_objects.at(10));
        
    }

    pushMatrix(PROJECTION);
    /*if(to_draw.empty() && digits_to_draw.empty()) {
        return;
    }*/
    
	loadIdentity(PROJECTION);
    glOrtho(0, WinX, WinY, 0, -1, 1);

	loadIdentity(VIEW);
	loadIdentity(MODEL);
    for(std::vector<SceneObject*>::iterator it = to_draw.begin(); it != to_draw.end(); ++it) {
        draw_hud_scene_object(*it);
    }
    
    Position defaultNumberPosition = {-0.7, -0.85, 0};
    for(int i = digits_to_draw.size()-1; i >= 0; i--) {
        int digit = digits_to_draw.at(i);
        SceneObject* number =  number_objects.at(digit);
        number->getPosition()->x = defaultNumberPosition.x;
        number->getPosition()->y = defaultNumberPosition.y;
        number->getPosition()->z = defaultNumberPosition.z;
        defaultNumberPosition.x += 0.05;
        draw_hud_scene_object(number);
    }
    Position defaultLivesPosition = {0.4, -0.85, 0};
    for(int i=0 ; i < Hud->player->getLifes(); i++) {
        SceneObject* number =  number_objects.at(11);
        number->getPosition()->x = defaultLivesPosition.x;
        number->getPosition()->y = defaultLivesPosition.y;
        number->getPosition()->z = defaultLivesPosition.z;
        defaultLivesPosition.x += 0.11;
        draw_hud_scene_object(number);
    }

    while(!to_draw.empty()) {
        to_draw.pop_back();
    }
}


/*
    int flarePos[2];
    int m_viewport[4];
    glGetIntegerv(GL_VIEWPORT, m_viewport);

    if(!project(eyeLightPos, lightScreenPos, m_viewport))
        printf("Error in getting projected light in screen\n");  //Calculate the window Coordinates of the light position: the projected position of light on viewport
    flarePos[0] = clampi((int)lightScreenPos[0], m_viewport[0], m_viewport[0] + m_viewport[2] - 1);
    flarePos[1] = clampi((int)lightScreenPos[1], m_viewport[1], m_viewport[1] + m_viewport[3] - 1);
    
*/
void Renderer::toggleLensFlare() {
    if (Renderer::getScene()->POINT_LIGHTS_ON)
        LENS_FLARE_ACTIVATED = !LENS_FLARE_ACTIVATED;
    if (!Renderer::getScene()->POINT_LIGHTS_ON && LENS_FLARE_ACTIVATED)
        LENS_FLARE_ACTIVATED = false;
}

void Renderer::renderLensFlare(void) {
    Frog * frog = ((Frog*)Controlers::getPlayer()->getObject());
    if (Cam.getMode() != 3 && Cam.getMode() != 2) return;
    if (Cam.getMode() == 3 && frog->nrRow < 4) return;

    if(!LENS_FLARE_ACTIVATED) return;
    computeDerivedMatrix(PROJ_VIEW_MODEL);
    

    float eyeLightPos[4];
    

    float lightPos[4] = {0.0f, 1.f, 1.5f, 0.9f};
    float lightScreenPos[3];
    int m_viewport[4];

	glGetIntegerv(GL_VIEWPORT, m_viewport);
    multMatrixPoint(VIEW, lightPos, eyeLightPos);
    glUniform4fv(lPos_uniformId, 1, eyeLightPos); //sending the position of point light in eye coordinates
    
    project(eyeLightPos, lightScreenPos, m_viewport);

    
    xFlare = (int)lightScreenPos[0];
    yFlare = (int)lightScreenPos[1];

    
    GLint loc;

	glDepthMask(GL_FALSE);
	glDisable(GL_CULL_FACE);
	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE);

	WinX = glutGet(GLUT_WINDOW_WIDTH);
	WinY = glutGet(GLUT_WINDOW_HEIGHT);
	
    //int cx = WinX / 2;
	//int cy = WinY / 2;
    int screenMaxCoordX = m_viewport[0] + m_viewport[2] - 1;
	int screenMaxCoordY = m_viewport[1] + m_viewport[3] - 1;
    int cx = m_viewport[0] + (int)(0.5f * (float)m_viewport[2]) - 1;
	int cy = m_viewport[1] + (int)(0.5f * (float)m_viewport[3]) - 1;

	float maxflaredist = sqrt(cx*cx + cy * cy);
	float flaredist = sqrt((xFlare - cx)*(xFlare - cx) + (yFlare - cy)*(yFlare - cy));

    float scaleDistance = (maxflaredist - flaredist) / maxflaredist;
	int flaremaxsize = (int)(WinX * 0.2);
	int flarescale = (int)(WinX * 0.2);
	
    int dx = clampi(cx + (cx - xFlare), m_viewport[0], screenMaxCoordX);
    int dy = clampi(cy + (cy - yFlare), m_viewport[1], screenMaxCoordY);

    for (int i =0; i < N_FLARES; i++) {
    
            // Position is interpolated along line between start and destination.
            sendTexture(lens_flare_objects.at(i));

            int px = (int)((1.0f - flaresPos[i])*xFlare + flaresPos[i]*dx);
		    int py = (int)((1.0f - flaresPos[i])*yFlare + flaresPos[i]*dy);
		    px = clampi(px, m_viewport[0], screenMaxCoordX);
		    py = clampi(py, m_viewport[1], screenMaxCoordY);

            // Piece size are 0 to 1; flare size is proportion of
            // screen width; scale by flaredist/maxflaredist.
            int width = (int)(scaleDistance*flarescale*flaresPos[i]);

            // Width gets clamped, to allows the off-axis flares
            // to keep a good size without letting the elements get
            // too big when centered.
            if (width > flaremaxsize)
                width = flaremaxsize;

            // Flare elements are square (round) so we'll use same value for width and height
            int height = width;
            int alpha = flaredist / maxflaredist;
            if (width < 1) continue;

		    height = (int)((float)m_viewport[3] / (float)m_viewport[2] * (float)width);

            flaresColor[3] = alpha+0.3f;

            pushMatrix(MODEL);
            pushMatrix(PROJECTION);
            pushMatrix(VIEW);

            loadIdentity(VIEW);
            loadIdentity(PROJECTION);
            ortho(0, WinX, 0, WinY, 0, 1);
            translate(MODEL, (float)(px - width * 0.0f), (float)(py - height * 0.0f), 0.0f);
			scale(MODEL, (float)width, (float)height, 1);
            drawMesh(nullptr, lens_flare_objects.at(i)->getMesh());

            popMatrix(MODEL);
            popMatrix(VIEW);
            popMatrix(PROJECTION);
	}

	glBindTexture(GL_TEXTURE_2D, 0);
	glDisable(GL_BLEND);
	glDepthMask(GL_TRUE);
	glEnable(GL_CULL_FACE);

}


bool distance(std::vector<float> x, std::vector<float> y) {
	float cam[] = { Cam.camX, Cam.camY, Cam.camZ };
	float distX = (pow(x[0] - cam[0], 2) + pow(x[1] - cam[1], 2) + pow(x[2] - cam[2], 2));
	float distY = (pow(y[0] - cam[0], 2) + pow(y[1] - cam[1], 2) + pow(y[2] - cam[2], 2));
	return (distX > distY);
}

void renderSkyBox() {

    glActiveTexture(GL_TEXTURE4);
	glBindTexture(GL_TEXTURE_CUBE_MAP, Texture::getTexture(SKY));
    // Render Sky Box

	glUniform1i(texMode_uniformId, 5);
    glUniform1i(skyboxActivated_loc, 1);
    glUniform1i(tex_cube_loc, Texture::getTexture(SKY));

	//it won't write anything to the zbuffer; all subsequently drawn scenery to be in front of the sky box. 
	glDepthMask(GL_FALSE); 

	glFrontFace(GL_CW); // set clockwise vertex order to mean the front
	
	pushMatrix(MODEL);
	pushMatrix(VIEW);  //se quiser anular a transla��o
	
	//  Fica mais realista se n�o anular a transla��o da c�mara 
	// Cancel the translation movement of the camera - de acordo com o tutorial do Antons
	mMatrix[VIEW][12] = 0.0f;
	mMatrix[VIEW][13] = 0.0f;
	mMatrix[VIEW][14] = 0.0f;
	
	scale(MODEL, 100.0, 100.0f, 100.0f);
	translate(MODEL, -0.5f, -0.5f, -0.5f);

	// send matrices to OGL
	glUniformMatrix4fv(model_uniformId, 1, GL_FALSE, mMatrix[MODEL]); //Transforma��o de modela��o do cubo unit�rio para o "Big Cube"
	computeDerivedMatrix(PROJ_VIEW_MODEL);
	glUniformMatrix4fv(pvm_uniformId, 1, GL_FALSE, mCompMatrix[PROJ_VIEW_MODEL]);

	glBindVertexArray(SKYBOX->getMesh()->vao);
	glDrawElements(SKYBOX->getMesh()->type, SKYBOX->getMesh()->numIndexes, GL_UNSIGNED_INT, 0);
	glBindVertexArray(0);
	popMatrix(MODEL);
	popMatrix(VIEW);
	 
	glFrontFace(GL_CCW); // restore counter clockwise vertex order to mean the front
	glDepthMask(GL_TRUE);
}

void renderBillboards() {
	
	//objId = treeMeshID;
    
	float cam[] = { Cam.camX, Cam.camY, Cam.camZ };

	glDepthMask(GL_FALSE);
	glEnable(GL_BLEND);
	glDisable(GL_CULL_FACE);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	glUniform1i(texMode_uniformId, 4);
	glActiveTexture(GL_TEXTURE0);
	glBindTexture(GL_TEXTURE_2D, Texture::getTexture(WOOD));
	glUniform1i(tex_loc, 0);

	std::vector<std::vector<float>> billboards(0);


	//std::sort(billboards.begin(), billboards.end(), distance);
    for (auto billboard : billboards_objects) {

            sendTexture(billboard);
            sendMaterial(billboard->getMesh());

            pushMatrix(MODEL);

            Position *p = billboard->getPosition();
            translate(MODEL, p->x , p->y, p->z);

            Scale *s = billboard->getScale();
            scale(MODEL, s->x , s->y, s->z);
            
            auto rotations = billboard->getRotations();
            for(auto rot : *rotations) {
                    if(rot.angle > 0.0f) rotate(MODEL, rot.angle, rot.x , rot.y, rot.z);                        
            }
            Rotation *r = billboard->getRotation();
            if(r->angle > 0.0f) rotate(MODEL, r->angle, r->x , r->y, r->z);

            float p2[] = {p->x, p->y, p->z};
            
            l3dBillboardCylindricalBegin(cam, p2);

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            
            drawMesh(billboard->getTexture(), billboard->getMesh());
            popMatrix(MODEL);
            glBindTexture(GL_TEXTURE_2D, 0);

    }

	glBindTexture(GL_TEXTURE_2D, 0);
	glDisable(GL_BLEND);
	glDepthMask(GL_TRUE);
	glEnable(GL_CULL_FACE);
}



void refresh(int value)
{
	glutPostRedisplay();
	glutTimerFunc(1000/60, refresh, 0);
}

// ------------------------------------------------------------
//
// Reshape Callback Function
//

void Renderer::changeSize(int w, int h) {
    WinX = w;
    WinY = h;
	// Prevent a divide by zero, when window is too short
	if(h == 0)
		h = 1;
	// set the viewport to be the entire window
	glViewport(0, 0, w, h);
	Cam.resizeWindow(w, h);
}

void buildLensFlare() {
    lens_flare_objects = std::vector<SceneObject*>();

    for(int i=0; i< N_FLARES; i++) {
        if (i == 0 || i == N_FLARES - 1){ // large halos
            SceneQuad *halo = new SceneQuad(defaultMat, {1, 1, 1}, 3,3);
            halo->setTexture(Texture(DEFAULT, FLARE_1));
            lens_flare_objects.push_back(halo);
        }
        else if (i == 1 || i == N_FLARES - 2) { // small circles
            SceneQuad *halo = new SceneQuad(defaultMat, {1, 1, 1}, 3,3);
            halo->setTexture(Texture(DEFAULT, FLARE_2));
            lens_flare_objects.push_back(halo);
        }
        else { // hexagons
            SceneQuad *halo = new SceneQuad(defaultMat, {0, 0, 2}, 3,3);
            halo->setTexture(Texture(NO_LIGHT, FLARE_3));
            lens_flare_objects.push_back(halo);
        }
    }

}


void buildParticles() {    
    if(!particles_objects.size() > 0) {
        for (int i=0; i < 1; i++) {
            SceneQuad *particle = new SceneQuad(defaultMat, {0,0,3}, 1,1);
            particle->setTexture(Texture(MODULATED, PARTICLE));
            particles_objects.push_back(particle);
        }
    }

    GLfloat v, theta, phi;
	int i;

	for (i = 0; i < NR_PARTICLES; i++)
	{
        
		v = 0.8*frand() + 0.2;
		phi = frand()*M_PI;
		theta = 2.0*frand()*M_PI;
        int max = 20;
        int min = -20;
		particula[i].x = rand()%(max-min + 1) + min;
		particula[i].y = rand()%(max-min + 1) + min;
		particula[i].z = rand()%(10 + 1);
		particula[i].vx = v * cos(theta) * sin(phi);
		particula[i].vy = v * cos(phi);
		particula[i].vz = v * sin(theta) * sin(phi);
		particula[i].ax = 0.1f; /* simular um pouco de vento */
		particula[i].ay = -0.15f; /* simular a aceleracao da gravidade */
		particula[i].az = 0.0f;

		/* tom amarelado que vai ser multiplicado pela textura que varia entre branco e preto */
		particula[i].r = 1.f;//0.882f;
		particula[i].g = 1.f;//0.552f;
		particula[i].b = 1.f;//0.211f;

		//particula[i].life = 1.0f;		/* vida inicial */
        particula[i].life = frand();
		particula[i].fade = 0.005f;	    /* step de decr�scimo da vida para cada itera��o */
	}
    glutTimerFunc(0, iterate, 0);

}

void buildSkyBox() {
    SceneCube *skybox = new SceneCube(curbMat, {-20, -20, -20.f});
    skybox->setScale(100,100,100);
    skybox->setTexture(Texture(NO_LIGHT,SKY));
    SKYBOX = skybox;
}

void buildBillboards() {
    billboards_objects = std::vector<SceneObject*>();
    //BasicMaterial hudmat = {{0.5f, 0.5f, 0.5f, 1.0f}, {0.1f, 0.1f, 0.1f, 1.0f}, {0.2f, 0.2f, 0.2f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 1.f, 0};

    SceneQuad *tree2 = new SceneQuad(defaultMat, {-5, 0, 1.2}, 1.5,2);
    tree2->setTexture(Texture(DEFAULT, BILLBOARD));
    tree2->setRotation(90, 1,0,0);
    billboards_objects.push_back(tree2);

    SceneQuad *tree3 = new SceneQuad(defaultMat, {5, 0, 1}, 1.5,2);
    tree3->setTexture(Texture(DEFAULT, BILLBOARD2));
    tree3->setRotation(90, 1,0,0);
    billboards_objects.push_back(tree3);
}


void buildHUD() {
    to_draw = std::vector<SceneObject*>();
    paused_object = std::vector<SceneObject*>();
    BasicMaterial hudmat = {{0.5f, 0.5f, 0.5f, 1.0f}, {0.1f, 0.1f, 0.1f, 1.0f}, {0.2f, 0.2f, 0.2f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 1.f, 0};
    SceneQuad *pausedScreen = new SceneQuad(defaultMat, {0, 0, 0}, 2,2);
    pausedScreen->setTexture(Texture(NO_LIGHT, PAUSED));
    paused_object.push_back(pausedScreen);

    game_over_object = std::vector<SceneObject*>();
    SceneQuad *gameOverScreen = new SceneQuad(defaultMat, {0, 0, 0}, 2,2);
    gameOverScreen->setTexture(Texture(NO_LIGHT, GAME_OVER));
    game_over_object.push_back(gameOverScreen);
    
    
    number_objects = std::vector<SceneObject*>();
    for(int digit=SYMBOL_0; digit  <= SYMBOL_9; digit++) {
        SceneQuad *number = new SceneQuad(defaultMat, {1, -1, 0}, 0.05, 0.1);
        number->setTexture(Texture(NO_LIGHT, AVAILABLE_TEXTURES(digit)));
        number_objects.push_back(number);
    }

    SceneQuad *points = new SceneQuad(defaultMat, {-0.85, -0.85, 0}, 0.2, 0.1);
    points->setTexture(Texture(NO_LIGHT, POINTS));
    number_objects.push_back(points);

    SceneQuad *black = new SceneQuad(blackMaterial, {0, 0, 0}, 0.1, 0.1);
    black->setTexture(Texture(NO_LIGHT, LIVES));
    number_objects.push_back(black);
}

// --------------------------------------------------------
//
// Shader Stuff
//
GLuint Renderer::setupShaders() {

	// Shader for models
	shader.init();
	shader.loadShader(VSShaderLib::VERTEX_SHADER, "engine/core/renderer/shaders/pointlight.vert");
	shader.loadShader(VSShaderLib::FRAGMENT_SHADER, "engine/core/renderer/shaders/pointlight.frag");

	// set semantics for the shader variables
    texMode_uniformId = glGetUniformLocation(shader.getProgramIndex(), "texMode"); // different modes of texturing
	glBindFragDataLocation(shader.getProgramIndex(), 0,"colorOut");
	glBindAttribLocation(shader.getProgramIndex(), VERTEX_COORD_ATTRIB, "VertexPosition");
	glBindAttribLocation(shader.getProgramIndex(), NORMAL_ATTRIB, "VertexNormal");
	//glBindAttribLocation(shader.getProgramIndex(), TEXTURE_COORD_ATTRIB, "texCoord");

	glLinkProgram(shader.getProgramIndex());

	pvm_uniformId = glGetUniformLocation(shader.getProgramIndex(), "MVPMatrix");
	vm_uniformId = glGetUniformLocation(shader.getProgramIndex(), "MVMatrix");
	normal_uniformId = glGetUniformLocation(shader.getProgramIndex(), "NormalMatrix");
	//lPos_uniformId = glGetUniformLocation(shader.getProgramIndex(), "l_pos");
    model_uniformId = glGetUniformLocation(shader.getProgramIndex(), "m_Model");
    tex_loc = glGetUniformLocation(shader.getProgramIndex(), "texmap");
	tex_loc1 = glGetUniformLocation(shader.getProgramIndex(), "texmap1");
	tex_loc2 = glGetUniformLocation(shader.getProgramIndex(), "texmap2");
    tex_cube_loc = glGetUniformLocation(shader.getProgramIndex(), "cubeMap");
    shadow_loc = glGetUniformLocation(shader.getProgramIndex(), "shadowMode");
    fogActivated_loc = glGetUniformLocation(shader.getProgramIndex(), "fogActivated");
    skyboxActivated_loc = glGetUniformLocation(shader.getProgramIndex(), "render_skybox");
    


	
	printf("InfoLog for Per Fragment Phong Lightning Shader\n%s\n\n", shader.getAllInfoLogs().c_str());
    Texture::registerTextures();

    buildHUD();
    buildBillboards();
    buildSkyBox();
    buildLensFlare();
    buildParticles();

	return(shader.isProgramLinked());
}

void Renderer::drawDebugAxis(GLdouble axis_size) { 
    drawAxes(axis_size);
}