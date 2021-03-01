import time

from sqlalchemy import Column, Integer, String, Text
from database import Base


class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True)
    name = Column(String(50), unique=True)
    email = Column(String(120), unique=True)

    def __init__(self, name=None, email=None):
        self.name = name
        self.email = email

    def __repr__(self):
        return '<User %r>' % self.name

    def __str__(self):
        return '{}: {}, {}'.format(self.id, self.name, self.email)


class Message(Base):
    __tablename__ = 'messages'
    id = Column(Integer, primary_key=True)
    guid = Column(String(60))
    uuid = Column(String(60))
    time = Column(Integer)
    key = Column(String(44))
    iv = Column(String(24))
    text = Column(Text)
    sig = Column(String(344))

    def __init__(self, guid=None,  uuid=None, key=None, iv=None, text=None, sig=None):
        self.guid = guid
        self.uuid = uuid
        self.key = key
        self.iv = iv
        self.text = text
        self.sig = sig
        self.time = int(time.time())

    def __repr__(self):
        return '<Message from %r>' % self.uuid

    def __str__(self):
        return '{}: {}, {}'.format(self.time, self.uuid, self.text)

    def get_as_dict(self):
        return {"uuid": self.uuid, "time": self.time, "key": self.key, "iv": self.iv, "text": self.text, "sig": self.sig}
