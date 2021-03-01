from flask import Flask

app = Flask(__name__, instance_relative_config=True)

import p2server.views
import p2server.models

def create_app(test_config=None):
    return app


