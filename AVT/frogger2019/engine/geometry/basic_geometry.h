
#ifndef __BASICGEOMETRYH__
#define __BASICGEOMETRYH__
#include "common.h"
struct customMesh { 
	int n_vertices;
	int n_indices;
	float ka[4];
	float kd[4];
	float ks[4]; 
	float shin;
};

#define MAX_TEXTURES 4

extern GLuint VboId[2];

typedef struct Material{
	float diffuse[4];
	float ambient[4];
	float specular[4];
	float emissive[4];
	float shininess;
	int texCount;
}Material;
// A model can be made of many meshes. Each is stored  in the following structure
typedef struct MyMesh {
		GLuint vao;
		GLuint texUnits[MAX_TEXTURES];
		GLuint texTypes[MAX_TEXTURES];
		float transform[16];
		int numIndexes;
		unsigned int type;
		struct Material mat;
}MyMesh;

void createCube(MyMesh *obj_mesh);
void createQuad(MyMesh *obj_mesh, float size_x, float size_y);
void createSphere(MyMesh *obj_mesh, float radius, int divisions);
void createTorus(MyMesh *obj_mesh, float innerRadius, float outerRadius, int rings, int sides);
void createCylinder(MyMesh *obj_mesh, float height, float radius, int sides);
void createCone(MyMesh *obj_mesh, float height, float baseRadius, int sides);
void createPawn(MyMesh *obj_mesh);
void computeVAO(MyMesh *obj_mesh, int numP, float *p, float *pfloatoints, int sides, float smoothCos);
void create (float *p, int numP, int sides, int closed, float smoothCos);
int revSmoothNormal2(float *p, float *nx, float *ny, float smoothCos, int beginEnd);
float *circularProfile(float minAngle, float maxAngle, float radius, int divisions, float transX= 0.0f, float transY = 0.0f);
void drawAxes(GLdouble length);

#endif