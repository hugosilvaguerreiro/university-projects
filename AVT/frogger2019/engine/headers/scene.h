#ifndef __SCENEH__
#define __SCENEH__

#include "scene_object.h"
#include "car.h"
#include "bus.h"
#include "frog.h"
#include "light.h"
#include "trunk.h"
#include "turtle.h"
#include <map>
#include "player.h"


class Scene {
    private: 
        std::vector<SceneObject*> *objects;
        std::vector<Light*> *lights;
        std::vector<SceneObject*> *reflectedPlanes;
        std::vector<SceneObject*> *shadowPlanes;
        
    public:
        bool POINT_LIGHTS_ON = true;
        Scene();
        void putObject(SceneObject *object);
        void putObject(SceneCompositeObject *object);
        void putObject(Car *car);
        void putObject(Bus *bus);
        void putObject(Turtle *turtle);
        void putObject(Frog *object);
        void putLight(Light *light);
        void putObject(Trunk *trunk);
        void putReflected(SceneObject *quad);
        void putShadow(SceneObject *quad);

    //    void removeObject(SceneObject *object);
        std::vector<SceneObject*>* getObjects();
        std::vector<Light*>* getLights();
        std::vector<SceneObject*>* getReflected();
        std::vector<SceneObject*>* getShadow();
        

        void toggleDirectionalLights();
        void togglePointLights();
        void toggleSpotLights();
};


class HUD : public Scene {
    private: 
        std::vector<SceneObject*> *objects;
        std::vector<Light*> *lights;
        
    public:
        Player *player;
        HUD(Player * player);
        /*void putObject(SceneObject *object);
        void putObject(Car *car);
        void putObject(Frog *object);
        void putLight(Light *light);
        //SceneObject removeObject(int identifier);
        std::vector<SceneObject*>* getObjects();
        std::vector<Light*>* getLights();

        void toggleDirectionalLights();
        void togglePointLights();
        void toggleSpotLights();*/
};


#endif