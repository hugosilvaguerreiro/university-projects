#include "material.h"


BasicMaterial defaultMat = {{0.2f, 0.15f, 0.1f, 1.0f}, {0.8f, 0.6f, 0.4f, 1.0f}, {0.8f, 0.8f, 0.8f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 100.f, 1};
BasicMaterial roadMat = {{0.1f, 0.1f, 0.1f, 1.0f}, {0.1f, 0.1f, 0.1f, 1.0f}, {0.2f, 0.2f, 0.2f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 10.f, 0};
BasicMaterial curbMat = {{0.3f, 0.3f, 0.3f, 1.0f}, {0.5f, 0.5f, 0.5f, 1.0f}, {0.2f, 0.2f, 0.2f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 30.f, 0};


BasicMaterial waterMat = {{0.4f, 0.73f, 0.9f, 0.5f}, {0.2f, 0.53f, 0.65f, 0.5f}, {0.9f, 0.9f, 0.9f, 0.5f}, {0.0f, 0.0f, 0.0f, 1.0f}, 100.f, 0};
BasicMaterial grassMat = {{0.0f, 0.2f, 0.0f, 0.5f}, {0.0f, 0.2f, 0.0f, 0.5f}, {0.9f, 0.9f, 0.9f, 0.5f}, {0.0f, 0.0f, 0.0f, 0.5f}, 100.f, 0};


BasicMaterial wheelMat = {{0.05f, 0.05f, 0.05f, 1.0f}, {0.05f, 0.05f, 0.05f, 1.0f}, {0.6f, 0.6f, 0.6f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 5.f, 0};

BasicMaterial carMat = {{0.6f, 0.1f, 0.1f, 1.0f}, {0.6f, 0.1f, 0.1f, 1.0f}, {0.6f, 0.6f, 0.6f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 100.f, 0};
BasicMaterial busMat = {{0.7f, 0.4f, 0.2f, 1.0f}, {0.7f, 0.4f, 0.2f, 1.0f}, {0.7f, 0.4f, 0.2f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 100.f, 0};

BasicMaterial trunkMat = {{0.2f, 0.1f, 0.07f, 1.0f}, {0.2f, 0.1f, 0.07f, 1.0f}, {0.2f, 0.1f, 0.07f, 1.0f}, {0.5f, 0.25f, 0.07f, 1.0f}, 100.f, 0};

BasicMaterial turtleBodyMat = {{0.0f, 0.2f, 0.0f, 1.0f}, {0.0f, 0.2f, 0.0f, 1.0f}, {0.9f, 0.9f, 0.9f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 100.f, 0};
BasicMaterial turtleBodyMat2 = {{0.0f, 0.15f, 0.0f, 1.0f}, {0.0f, 0.15f, 0.0f, 1.0f}, {0.5f, 0.5f, 0.5f, 1.0f}, {0.0f, 0.0f, 0.0f, 1.0f}, 100.f, 0};
BasicMaterial tortoiseShelMat = {{0.2f, 0.1f, 0.07f, 1.0f}, {0.2f, 0.1f, 0.07f, 1.0f}, {0.2f, 0.1f, 0.07f, 1.0f}, {0.5f, 0.25f, 0.07f, 1.0f}, 100.f, 0};

BasicMaterial lampMat = {{0.5f, 0.5f, 0.05f, 0.5f}, {0.5f, 0.5f, 0.05f, 0.5f}, {0.8f, 0.8f, 0.8f, 0.5f}, {0.5f, 0.5f, 0.05f, 0.5f}, 100.f, 0};
BasicMaterial blackMaterial = {{0.f, 0.f, 0.f, 1.0f}, {0.f, 0.f, 0.f, 1.0f}, {0.f, 0.f, 0.f, 1.0f}, {0.f, 0.f, 0.f, 1.0f}, 100.f, 0};
