#include "engine.h"
#include "controlers.h"
#include "car.h"
#include "frog.h"
#include "turtle.h"
#include "bus.h"
#include "game.h"
#include "trunk.h"
 
 
 
 
int main(int argc, char **argv) {

    Engine engine = Engine();
    engine.initializeEngine(argc, argv);
    
    Scene *scene = new Scene();

    BasicMaterial testMat = {{0.5f, 0.5f, 0.5f, 1.0f}, {0.1f, 0.1f, 0.1f, 1.0f}, {0.2f, 0.2f, 0.2f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 1.f, 0};
    SceneQuad *road = new SceneQuad(testMat, {0, -5, 0}, 40, 12);
     
    road->setTexture(Texture(DEFAULT, ROAD_HD));
    scene->putShadow(road);


    SceneQuad *water = new SceneQuad(waterMat, {0, 6, 0.f}, 40, 10);
     
    water->getBoundingBox()->objectType = RIVER;
    water->setTexture(Texture(MULTITEXTURING, WATER_HD, RIVER_STONES_HD));
    scene->putReflected(water);
    

    BasicMaterial pavementMat = {{0.3f, 0.3f, 0.3f, 1.0f}, {0.1f, 0.1f, 0.1f, 1.0f}, {0.2f, 0.2f, 0.2f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 1.f, 0};
     

    

    for(int i=-1; i < 2; i++) {
        SceneCube *curb1 = new SceneCube(pavementMat, {-15.f, 11.f*i, 0.f});
        curb1->setScale(30.f,1.f,0.2f);
        if (i == 0) {
            curb1->setPosition({-15.f, -0.5f, 0.f}) ;
            curb1->setScale(30,1.5f, 0.2f);
        }
            
        curb1->setTexture(Texture(BUMP, PAVEMENT_HD, PAVEMENT_NORMAL));
        curb1->getBoundingBox()->objectType = PAVEWALK;
        scene->putObject(curb1);
    }    

     
    BasicMaterial tunnelMat = {{0.1f, 0.1f, 0.1f, 1.0f}, {0.1f, 0.1f, 0.1f, 1.0f}, {0.2f, 0.2f, 0.2f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 1.f, 0};
    for(int i=0; i < 2; i++) {
        for(int j=0; j < 2; j++) {
            SceneCube *curb1 = new SceneCube(tunnelMat, {-25.f+40.f*i, 1.f-11.f*j, 2.f});
            curb1->setScale(10.f, 10.f, 1.f);
            scene->putObject(curb1);
        }
    }
    for(int i=0; i < 2; i++) {
        for(int j=-1; j < 2; j++) {
            SceneCube *curb1 = new SceneCube(tunnelMat, {-25.f+40.f*i, 11.f*j, 0.f});
            curb1->setScale(10.f, 1.f, 2.f);
            scene->putObject(curb1);
        }
    }


    DirectionalLight *dirLight = new DirectionalLight(true, {1.f, 1.f, 1.f, 1.f}, {1.f, 1.f, 1.f, 1.f}, {5.f, -5.f, 20.f, 1.f});
    scene->putLight(dirLight);


     
    for(int i = -1; i <= 1; i++) {
        for(int j = 0; j <= 1;j++) {
            PointLight *point = new PointLight(true, {1.f, 1.f, 1.f, 1.f}, {1.f, 1.f, 1.f, 1.f}, {10.f * i, -7.f*j, 2.f, 1.f}, {1.f, 0.2f, 0.1f});
            scene->putLight(point);
            SceneCylinder *cy = new SceneCylinder(roadMat, {10.f * i, -11.f*j + 0.5f, 1.f}, 2.f, 0.1f, 12);
            cy->setRotation(90.0f, 1.f, 0.f, 0.f);
            scene->putObject(cy);
            SceneSphere *sph = new SceneSphere(lampMat, {10.f * i, -11.f*j + 0.5f, 2.3f}, 0.3f, 12);
            scene->putObject(sph);
        }
    }

    SpotLight *spot = new SpotLight(true, {1.f, 1.f, 1.f, 1.f}, {1.f, 1.f, 1.f, 1.f}, {-5.f, -5.f, 1.f, 1.f}, {1.f, 0.2f, 0.1f}, {0.f, 90.f, 0.f, 1.f}, 0.5f, 0.3f);
    scene->putLight(spot);

     
    engine.setScene(scene);


    Frog *frog = new Frog({-1.f, -10.5f, 0.f});
    FrogPlayer *player = new FrogPlayer(frog);
    player->set_light(spot);
    Controlers::setPlayer(player);  
    scene->putObject((SceneCompositeObject*) frog);


    HUD *hud = new HUD(player);

     
    int FACTOR = 50;
    SceneQuad *skybox_front = new SceneQuad(curbMat, {0, FACTOR, 1}, FACTOR*2, FACTOR*2);
    skybox_front->setTexture(Texture(NO_LIGHT, SKY_BOX_FRONT));
    skybox_front->setRotation(90,1,0,0);
    skybox_front->castShadow = false;
    scene->putObject(skybox_front);

     
    SceneQuad *skybox_left = new SceneQuad(curbMat, {-FACTOR, 0, 1}, FACTOR*2, FACTOR*2);
    skybox_left->setTexture(Texture(NO_LIGHT, SKY_BOX_LEFT));
    skybox_left->setRotation(90,0,1,0);
    skybox_left->castShadow = false;
     
    scene->putObject(skybox_left);

     
    SceneQuad *skybox_right = new SceneQuad(curbMat, {FACTOR, 0, 1}, FACTOR*2, FACTOR*2);
    skybox_right->setTexture(Texture(NO_LIGHT, SKY_BOX_RIGHT));
    skybox_right->setRotation(270,0,1,0);
    skybox_right->castShadow = false;
    scene->putObject(skybox_right);

     
    SceneQuad *skybox_down = new SceneQuad(curbMat, {0, 0, -FACTOR+10}, FACTOR*2, FACTOR*2);
    skybox_down->setTexture(Texture(NO_LIGHT, SKY_BOX_DOWN));
    skybox_down->castShadow = false;
    scene->putObject(skybox_down);

     
    SceneQuad *skybox_up = new SceneQuad(curbMat, {0, 0, FACTOR+10}, FACTOR*2, FACTOR*2);
    skybox_up->setTexture(Texture(NO_LIGHT, SKY_BOX_UP));
    skybox_up->castShadow = false;
    skybox_up->setRotation(180.f, 1.f, 0.f, 0.f);
    scene->putObject(skybox_up);

    engine.setHUD(hud);

     
    engine.start();
    return 0;
}


