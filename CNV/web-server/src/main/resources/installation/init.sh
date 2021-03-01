#!/bin/sh

export AWS_REGION= >REPLACE< &&\
export AWS_ACCESS_KEY_ID= >REPLACE< && \
export AWS_SECRET_ACCESS_KEY= >REPLACE< &&\
cd /home/ec2-user  && \
java -classpath web-server.jar -noverify pt.ulisboa.tecnico.cnv.server.WebServer
