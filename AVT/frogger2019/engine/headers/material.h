#ifndef __MATERIALH__
#define __MATERIALH__

typedef struct Color {
    float r, g, b, a;
} Color;

typedef struct BasicMaterial {
    Color amb, diff, spec, emissive;
    float shininess;
    int texcount;
} BasicMaterial;

extern BasicMaterial defaultMat;
extern BasicMaterial roadMat;
extern BasicMaterial curbMat;

extern BasicMaterial waterMat;
extern BasicMaterial grassMat;

extern BasicMaterial wheelMat;
extern BasicMaterial carMat;
extern BasicMaterial busMat;
extern BasicMaterial trunkMat;

extern BasicMaterial turtleBodyMat;
extern BasicMaterial tortoiseShelMat;
extern BasicMaterial turtleBodyMat2;

extern BasicMaterial lampMat;
extern BasicMaterial blackMaterial;

#endif