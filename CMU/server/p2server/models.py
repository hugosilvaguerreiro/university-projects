import pathlib
import os, binascii
from collections import defaultdict
from p2server import app
from flask import Flask
from flask_sqlalchemy import SQLAlchemy
from werkzeug.security import generate_password_hash, check_password_hash

app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = True
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///p2photo.db'
db = SQLAlchemy(app)

#if you change a model you have to rebuild the database
class User(db.Model):
    __tablename__ = 'user'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    s_hash = db.Column(db.String(100), nullable=False)
    public_key = db.Column(db.String(100), nullable=False)

    def __repr__(self):
        return f'<User {self.id}:{self.username}>'

    def __str__(self):
        return f'<User {self.id}:{self.username}, {self.s_hash}>'

    def to_dict(self):
        return {"id": self.id, "username": self.username, "s_hash": self.s_hash, "pub": self.public_key}

    def check_password(self, password):
        return check_password_hash(self.s_hash, password)

class Session(db.Model):
    __tablename__ = 'session'
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), primary_key=True, nullable=False, index=True)
    cookie = db.Column(db.String(64))

    def __repr__(self):
        return f'<Session {self.user_id}>'

    def __str__(self):
        return f'<Session {self.user_id}: {self.cookie}>'

    def to_dict(self):
        return {"user_id": self.user_id, "cookie": self.cookie}

class Album(db.Model):
    __tablename__ = 'album'
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(64), nullable=False)

    def __repr__(self):
        return f'<Album {self.id}>'

    def __str__(self):
        return f'<Album {self.id}: {self.name}>'

    def to_dict(self):
        return {"id": self.id, "name": self.name}

class User_has_Album(db.Model):
    __tablename__ = 'user_has_album'
    id = db.Column(db.Integer, primary_key=True)
    user = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    album = db.Column(db.Integer, db.ForeignKey('album.id'), nullable=False)
    slice_url = db.Column(db.String(64), unique=True)
    secret = db.Column(db.String(100), nullable=False)

    def __repr__(self):
        return f'<User_has_Album {self.id}>'

    def __str__(self):
        return f'<User_has_Album {self.id}: {self.user} has {self.album} at {self.slice_url}>'

    def to_dict(self):
        return {"id": self.id, "user": self.user, "album": self.album, "slice_url": self.slice_url, "secret": self.secret}
        
def create_user(username, password, public_key):
    user = User(username=username, s_hash=generate_password_hash(password), public_key=public_key)
    db.session.add(user)
    db.session.commit()
    create_session(user)
    return user

def check_cookie(user_id, cookie):
    c = Session.query.get(user_id)
    return c is not None and c.cookie is not None and c.cookie == cookie

def search_likeuser(query):
    return User.query.filter(User.username.like(f"%{query}%")).all()

def create_session(user):
    session = Session(user_id=user.id, cookie=None)
    db.session.add(session)
    db.session.commit()
    return session

def login_session(user_id):
    random_cookie = binascii.hexlify(os.urandom(32)).decode("utf-8")
    session = Session.query.get(user_id)
    session.cookie = random_cookie
    db.session.commit()
    return session

def logout_session(user_id):
    session = Session.query.get(user_id)
    session.cookie = None
    db.session.commit()
    return session

def create_album(user_id, name, secret):
    album = Album(name=name)
    db.session.add(album)
    db.session.commit()
    create_user_has_album(user_id, album.id, secret)
    return album

def create_user_has_album(user_id, album_id, secret):
    uha = User_has_Album(user=user_id, album=album_id, secret=secret, slice_url=None)
    db.session.add(uha)
    db.session.commit()
    return uha

def get_user_albums(user_id):
    user_albums = {uha.album: {"name": album.name, "id":uha.album, "secret":uha.secret, "slices": []} for uha, album in 
        db.session.query(User_has_Album, Album).join(Album).filter(User_has_Album.user==user_id).all()}
    all_slices = db.session.query(User_has_Album, Album).join(Album).filter(User_has_Album.album.in_(user_albums.keys())).all()
    for uha, album in all_slices:
        user_albums[album.id]["slices"].append({"user": uha.user, "slice_url": uha.slice_url})
    return list(user_albums.values())

def set_slice_url(user_id, album_id, url):
    uha = User_has_Album.query.filter((User_has_Album.user == user_id) & (User_has_Album.album == album_id)).first()
    uha.slice_url = url
    db.session.commit()
    return uha

def check_user_has_album(user_id, album_id):
    return User_has_Album.query.filter((User_has_Album.user == user_id) & (User_has_Album.album == album_id)).first() is not None

def check_user_exists(user_id):
    return User.query.filter(User.id == user_id).first() is not None

#keep this at the end of the file to rebuild the db file when you delete it
db_file = pathlib.Path("./p2photo.db")
if not db_file.is_file():
    db.create_all()
