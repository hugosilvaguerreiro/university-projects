#version 330

uniform mat4 MVPMatrix;
uniform mat4 MVMatrix;
uniform mat3 NormalMatrix;

uniform mat4 m_Model;   //por causa do cubo para a skybox
uniform int render_skybox =0;

in vec4 VertexColor;
in vec3 VertexNormal;
in vec4 VertexPosition;
in vec4 texCoord;

out Data {
	vec3 Normal;
	vec3 Eye;
	vec4 Position;
	vec2 tex_coord;
    //vec3 skyboxTexCoord;
} DataOut;

void main () {
    //if (render_skybox == 1) {
        //DataOut.skyboxTexCoord = vec3(m_Model * VertexPosition);	//Transforma��o de modela��o do cubo unit�rio 
//	    DataOut.skyboxTexCoord.x = 1 - DataOut.skyboxTexCoord.x; //Texturas mapeadas no interior logo negar a coordenada x
  //  }


	DataOut.Normal = normalize(NormalMatrix * VertexNormal);
	DataOut.Position = MVMatrix * VertexPosition;
	DataOut.Eye = vec3(-DataOut.Position);
	DataOut.tex_coord = texCoord.st;
    DataOut.tex_coord.t = 1.0 - DataOut.tex_coord.t;
	gl_Position = MVPMatrix * VertexPosition;
	
}