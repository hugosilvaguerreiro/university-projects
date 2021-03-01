from flask import Flask, redirect, render_template, request, Response
from database import db_session, init_db
from models import Message
import json

app = Flask(__name__)


@app.teardown_appcontext
def shutdown_session(exception=None):
    db_session.remove()


@app.route('/post', methods=['GET', 'POST'])
def post():
    if request.method == 'POST':
        m = Message(request.form["guid"], request.form["uuid"], request.form["key"],
                    request.form["iv"], request.form["text"], request.form["sig"])
        db_session.add(m)
        db_session.commit()
        return "OK"
    else:
        return render_template("new.html")


@app.route('/<guid>')
def get(guid):
    msgs = Message.query.filter(Message.guid == guid)
    msgs = [msg.get_as_dict() for msg in msgs]
    return render_template("home.html", data=json.dumps(msgs, indent=4))

@app.route('/<guid>.json')
def getjson(guid):
    last = request.args.get('last')
    if last is not None:
        msgs = Message.query.filter(Message.guid == guid)[-int(last):]
    else:
        msgs = Message.query.filter(Message.guid == guid)
    msgs = [msg.get_as_dict() for msg in msgs]
    return Response(json.dumps(msgs), mimetype="application/json")


@app.route('/')
def meme():
    return render_template("home.html")


if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)
