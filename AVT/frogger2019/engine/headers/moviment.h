#ifndef __MOVIMENTH__
#define __MOVIMENTH__

#include "scene_object.h"

#include "player.h"
#include "trunk.h"
#include <chrono>
#include <ctime>
#include <vector>


class Moviment {
    public:
        static std::vector<SceneObject*> objects;
        static std::chrono::high_resolution_clock::time_point lastMoment;
        static void putObject(SceneObject *object);
        static void setOnTrunk(bool val, SceneObject *trunk);
        static void setOnTrunk(bool val);
        static bool isOnTrunk();
        static void setOnTurtle(bool val, SceneObject *trunk);
        static void setOnTurtle(bool val);
        static bool isOnTurtle();
        static void pause();
        static void resume();
        static void restart();
        static void putPlayer(Player *player);
        static std::vector<SceneObject*> getObjects();
        static void moveObjects(int value);
        static void checkMovementColision();
        static void reset();
};

#endif