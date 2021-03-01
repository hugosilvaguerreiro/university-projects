#include "player.h"
#include <iostream>

Player::Player(SceneObject *player_obj) {
    this->obj = player_obj;
    this->points = 0;
    this->lifes = 5;
    this->initialPosition = *this->obj->getPosition();
}

void Player::moveObjectToPosition(Position pos) {}

SceneObject *Player::getObject() {
    return this->obj;
}

int Player::getPoints() {
    return this->points;
}

void Player::setPoints(int points) {
    this->points = points;
}

int Player::getLifes() {
    return this->lifes;
}
void Player::setLifes(int lifes) {
    this->lifes = lifes;
}


void Player::move_left() {
    Position *p = this->obj->getPosition();
    
}

void Player::move_right() {
    Position *p = this->obj->getPosition();
   
}

void Player::move_front() {
    Position *p = this->obj->getPosition();
   
}

void Player::move_back() {
    Position *p = this->obj->getPosition();
    
}

Position Player::getInitialPosition() {
    return this->initialPosition;
}
