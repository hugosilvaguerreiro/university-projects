#include "camera.h"
#include "common.h"
#include "AVTmathLib.h"

#define ORTHO_FACTOR 80

int MODE=0;

Camera::Camera(CameraType type, int w, int h, float camX, float camY, float camZ) {
    this->type = type;
    this->w = w;
    this->h = h;
    this->camX = camX;
    this->camY = camY;
    this->camZ = camZ;
}

int Camera::getMode() {
    return this->currentMode;
}

void Camera::setMode(int mode) {
    this->currentMode = mode;
}

void Camera::setOrtho(){
    loadIdentity(PROJECTION);
    ortho(-this->w / ORTHO_FACTOR, this->w / ORTHO_FACTOR, -this->h / ORTHO_FACTOR, this->h / ORTHO_FACTOR, -100.0f, 100.0f);
    this->type = ORTHO;
}


void Camera::setPerspective(){
    float ratio = (1.0f * w) / h;
    loadIdentity(PROJECTION);
    perspective(53.13f, ratio, 0.1f, 1000.0f);
    this->type = PERSPECTIVE;
}


void Camera::resizeWindow(int w, int h) {
    this->w = w;
    this->h = h;
    float ratio = (1.0f * w) / h;
     
    switch (this->type) {
        case PERSPECTIVE:
            loadIdentity(PROJECTION);
            perspective(53.13f, ratio, 0.1f, 1000.0f);
            break;
        case ORTHO:
            loadIdentity(PROJECTION);
            ortho(-w / ORTHO_FACTOR, w / ORTHO_FACTOR, -h / ORTHO_FACTOR, h / ORTHO_FACTOR, -100.0f, 100.0f);
            break;
        default:
            break;
    }
}

void Camera::lookat() {
    lookAt(camX, camY, camZ, targetX, targetY, targetZ , upX, upY, upZ);
}

void Camera::setPos(float x, float y, float z) {
    camX = x;
    camY = y;
    camZ = z;
}

void Camera::setTarget(float x, float y, float z) {
    targetX = x;
    targetY = y;
    targetZ = z;
}

void Camera::setUp(float x, float y, float z) {
    upX = x;
    upY = y;
    upZ = z;
}