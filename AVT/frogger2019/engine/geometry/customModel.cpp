
#include <GL/glew.h>

 
#include <GL/freeglut.h>
#include "customModel.h"
#include "common.h"
#include "basic_geometry.h"
#include "VertexAttrDef.h"
#include "OBJ_Loader.h"
#include <stdlib.h>
#include <algorithm>

void loadModel(char *fname, ModelDetails *myModel)
{
    FILE *fp;
    int read;
    GLfloat x = 0.f;
    GLfloat y = 0.f;
    GLfloat z = 0.f;
    char *ch;

    fp=fopen(fname,"r");
    if (!fp)  {
        printf("can't open file %s\n", fname);
        exit(1);
    }

    while(!(feof(fp))) {
        read=fscanf(fp,"%s %f %f %f",ch,&x,&y,&z);
        if(ch[0] =='v') {
            if(ch[1] == 'n')  {

            } else if(ch[1] == 't') {
                if(read == 3) {

                } else {

                }
            } else if(ch[1] == 'h'){}
            else if(ch[1] == 'p') {}
             else {
            }
        } else if(ch[0]=='f') {

        }
    }

    fclose(fp);
}


void createModel(MyMesh *obj_mesh, ModelDetails* myModel) {
    float* vertices = &myModel->vertexPoints[0];
    float* normals = &myModel->vertexNormals[0];
    float* texCoords = &myModel->textureIndexes[0];
    int* faceIndex = &myModel->faceIndexes[0];

     
    obj_mesh->numIndexes = myModel->nrIndexes;
     

	glGenVertexArrays(1, &(obj_mesh->vao));
	glBindVertexArray(obj_mesh->vao);

	glGenBuffers(2, VboId);
	glBindBuffer(GL_ARRAY_BUFFER, VboId[0]);

	glBufferData(GL_ARRAY_BUFFER, sizeof(vertices)+sizeof(normals)+sizeof(texCoords),NULL,GL_STATIC_DRAW);
	glBufferSubData(GL_ARRAY_BUFFER, 0, sizeof(vertices), vertices);
	glBufferSubData(GL_ARRAY_BUFFER, sizeof(vertices), sizeof(normals), normals);
	glBufferSubData(GL_ARRAY_BUFFER, sizeof(vertices)+ sizeof(normals), sizeof(texCoords), texCoords);

	glEnableVertexAttribArray(VERTEX_COORD_ATTRIB);
	glVertexAttribPointer(VERTEX_COORD_ATTRIB, 4, GL_FLOAT, 0, 0, 0);
	glEnableVertexAttribArray(NORMAL_ATTRIB);
	glVertexAttribPointer(NORMAL_ATTRIB, 4, GL_FLOAT, 0, 0, (void *)sizeof(vertices));
	glEnableVertexAttribArray(TEXTURE_COORD_ATTRIB);
	glVertexAttribPointer(TEXTURE_COORD_ATTRIB, 4, GL_FLOAT, 0, 0, (void *)(sizeof(vertices)+ sizeof(normals)));

	 
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VboId[1]);
	glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(unsigned int) * obj_mesh->numIndexes, faceIndex , GL_STATIC_DRAW);

	 
	glBindVertexArray(0);

	obj_mesh->type = GL_TRIANGLES;
}


void loadModel() {
	objl::Loader Loader;

	 
	bool loadout = Loader.LoadFile("engine/geometry/models/Bmw.obj");
    if (loadout) {
		 
        objl::Mesh myMesh = Loader.LoadedMeshes.at(0);
        glBegin(GL_TRIANGLES);
        glPointSize(2.0);
         
        glPushMatrix();
        glBegin(GL_POINTS);
        glColor3f(1.f, 0.f, 1.f);
        for(auto vertex : myMesh.Vertices) {
            glVertex3f(vertex.Position.X, vertex.Position.Y, vertex.Position.Z);
        }
        glEnd();
        glPopMatrix();
        glEndList();

    } else {
        std::cout << "Couldnt load the file\n";
    }
}

objl::Loader loader;


void createTeaPot( MyMesh *mesh , int v_index, int i_index,float *custom_vertices, float *custom_uvs, 
                    float *custom_normals, int *custom_indices, customMesh *custom_mesh ) {
     
    
     
     

     
     



	std::vector<float> tp_vertices = std::vector<float>();
	std::vector<float> tp_normals= std::vector<float>();
	std::vector<float> tp_textures= std::vector<float>();
	std::vector<int> tp_indices= std::vector<int>();
  


		for (int j = v_index; j < v_index + custom_mesh->n_vertices * 4; j++)
		{
			tp_vertices.push_back(custom_vertices[j]);
			tp_normals.push_back(custom_normals[j]);
			tp_textures.push_back(custom_uvs[j]);
		}

		for (int j = i_index; j < i_index + custom_mesh->n_vertices; j++)
		{
			tp_indices.push_back(custom_indices[j]);
		}
        mesh->numIndexes = tp_indices.size();

        glGenVertexArrays(1, &(mesh->vao));
        glBindVertexArray(mesh->vao);

        glGenBuffers(2, VboId);
        glBindBuffer(GL_ARRAY_BUFFER, VboId[0]);
        glBufferData(GL_ARRAY_BUFFER, tp_vertices.size()*sizeof(float) + tp_normals.size()*sizeof(float) + tp_textures.size() *sizeof(float), NULL, GL_STATIC_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, tp_vertices.size() *sizeof(float), &tp_vertices[0]);
        glBufferSubData(GL_ARRAY_BUFFER, tp_vertices.size() *sizeof(float), tp_normals.size() *sizeof(float), &tp_normals[0]);
        glBufferSubData(GL_ARRAY_BUFFER, tp_vertices.size() *sizeof(float) + tp_normals.size() *sizeof(float), tp_textures.size() *sizeof(float), &tp_textures[0]);

        glEnableVertexAttribArray(VERTEX_COORD_ATTRIB);
        glVertexAttribPointer(VERTEX_COORD_ATTRIB, sizeof(float), GL_FLOAT, 0, 0, 0);
        glEnableVertexAttribArray(NORMAL_ATTRIB);
        glVertexAttribPointer(NORMAL_ATTRIB, sizeof(float), GL_FLOAT, 0, 0, (void *)(tp_vertices.size() *sizeof(float)));
        glEnableVertexAttribArray(TEXTURE_COORD_ATTRIB);
        glVertexAttribPointer(TEXTURE_COORD_ATTRIB, sizeof(float), GL_FLOAT, 0, 0, (void *)(tp_vertices.size() *sizeof(float) + tp_normals.size() *sizeof(float)));


         
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, VboId[1]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(unsigned int) * mesh->numIndexes, &tp_indices[0], GL_STATIC_DRAW);

         
        glBindVertexArray(0);

         
        mesh->type = GL_TRIANGLES;

         

         

}
