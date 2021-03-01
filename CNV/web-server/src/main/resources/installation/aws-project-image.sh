#!/bin/sh
sudo yum update
sudo yum install java-1.7.0-openjdk-devel.x86_64
sudo mv hill-server.service /etc/systemd/system/hill-server.service
sudo mv target/web_server_jar/web-server.jar /home/ec2-user/
sudo systemctl enable hill-server
sudo systemctl start hill-server
sudo systemctl status hill-server