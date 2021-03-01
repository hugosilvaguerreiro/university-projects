import re
import logging
from flask import json, render_template, request, redirect
from p2server import app
from p2server.models import *

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(name)-12s %(levelname)-8s %(message)s',
                    datefmt='%m-%d %H:%M',
                    filename='/tmp/p2server.log')
logging.getLogger('werkzeug').setLevel(logging.ERROR)

verify_regex = re.compile(r"^[a-zA-Z0-9_]{5,}[a-zA-Z0-9_]*$")
url_regex = re.compile(r"^[:/a-zA-Z0-9-=_?.]+$")
cookie_regex = re.compile(r"^[a-fA-F0-9]{64,}$")
id_regex = re.compile(r"^[0-9]+$")
query_regex = re.compile(r"^[a-zA-Z0-9_]+$")
base64_regex = re.compile(r"^[A-Za-z0-9+/=]+$")

def log_request(req):
    s = f"{request.remote_addr}: {req.method} {req.path}"
    for k, v in req.form.items():
        s += f"\n{k}: {v}"
    logging.info(s)

@app.route("/")
def hello():
    log_request(request)
    return render_template('home.html')


@app.route("/listdb",  methods=['GET'])
def user_list():
    log_request(request)
    output = {"users":[x.to_dict() for x in User.query.all()],
              "sessions": [x.to_dict() for x in Session.query.all()],
              "albums": [x.to_dict() for x in Album.query.all()],
              "user_has_album": [x.to_dict() for x in User_has_Album.query.all()]}
    response = app.response_class(
        response=json.dumps(output),
        status=200,
        mimetype='application/json'
    )
    return response


@app.route('/newuser', methods=['GET', 'POST'])
def new_user():
    log_request(request)
    if request.method == 'POST':
        if verify_regex.match(request.form["usr"]) is None or \
           verify_regex.match(request.form["pwd"]) is None or \
           base64_regex.match(request.form["pub"]) is None:
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="format for user and pass must be [a-zA-Z0-9_]{5,}[a-zA-Z0-9_]*", status=422, mimetype='text/plain')
        create_user(request.form["usr"], request.form["pwd"], request.form["pub"])
        logging.info(f"{request.remote_addr}: ok")
        return app.response_class(response="ok", status=200, mimetype='text/plain')
    else:
        return render_template('new_user.html')

@app.route('/searchuser', methods=['GET', 'POST'])
def search_user():
    log_request(request)
    if request.method == 'POST':
        if query_regex.match(request.form["query"]) is None or \
           id_regex.match(request.form["usr_id"]) is None or \
           cookie_regex.match(request.form["cookie"]) is None:
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad args", status=422, mimetype='text/plain')
        if not check_cookie(int(request.form["usr_id"]), request.form["cookie"]):
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad auth", status=403, mimetype='text/plain')
        q = search_likeuser(request.form["query"])
        r = json.dumps([{"user":x.username, "id":x.id, "pub": x.public_key} for x in q])
        logging.info(f"{request.remote_addr}: response: \n{r}")
        return app.response_class(response=r, status=200, mimetype='application/json')
    else:
        return render_template('search_user.html')


@app.route('/login', methods=['GET', 'POST'])
def login_user():
    log_request(request)
    if request.method == 'POST':
        if verify_regex.match(request.form["usr"]) is None or \
           verify_regex.match(request.form["pwd"]) is None:
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="Format for user and pass must be [a-zA-Z0-9_]{5,}[a-zA-Z0-9_]*", status=422, mimetype='text/plain')
        user = User.query.filter(User.username == request.form["usr"]).all()
        if len(user) != 1 or not user[0].check_password(request.form["pwd"]):
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="Wrong Password", status=422, mimetype='text/plain')
        session = login_session(user[0].id)
        session_dict = session.to_dict()
        session_dict["username"] = request.form["usr"]
        r = json.dumps(session_dict)
        logging.info(f"{request.remote_addr}: response: \n{r}")
        return app.response_class(response=r, status=200, mimetype='application/json')
    else:
        return render_template('login_user.html')

@app.route('/logout', methods=['GET', 'POST'])
def logout_user():
    log_request(request)
    if request.method == 'POST':
        if id_regex.match(request.form["usr_id"]) is None or \
           cookie_regex.match(request.form["cookie"]) is None:
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad args", status=422, mimetype='text/plain')
        if not check_cookie(int(request.form["usr_id"]), request.form["cookie"]):
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad auth", status=403, mimetype='text/plain')
        logout_session(int(request.form["usr_id"]))
        logging.info(f"{request.remote_addr}: ok")
        return app.response_class(response="ok", status=200, mimetype='text/plain')
    else:
        return render_template('logout_user.html')

@app.route('/newalbum', methods=['GET', 'POST'])
def new_album():
    log_request(request)
    if request.method == 'POST':
        if verify_regex.match(request.form["name"]) is None or \
           id_regex.match(request.form["usr_id"]) is None or \
           base64_regex.match(request.form["secret"]) is None or \
           cookie_regex.match(request.form["cookie"]) is None:
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad args", status=422, mimetype='text/plain')
        if not check_cookie(int(request.form["usr_id"]), request.form["cookie"]):
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad auth", status=403, mimetype='text/plain')
        alb = create_album(int(request.form["usr_id"]), request.form["name"], request.form["secret"])
        r = json.dumps(alb.to_dict())
        logging.info(f"{request.remote_addr}: response: \n{r}")
        return app.response_class(response=r, status=200, mimetype='application/json')
    else:
        return render_template('new_album.html')

@app.route('/getalbums', methods=['GET', 'POST'])
def get_albums():
    log_request(request)
    if request.method == 'POST':
        if id_regex.match(request.form["usr_id"]) is None or \
           cookie_regex.match(request.form["cookie"]) is None:
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad args", status=422, mimetype='text/plain')
        if not check_cookie(int(request.form["usr_id"]), request.form["cookie"]):
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad auth", status=403, mimetype='text/plain')
        q = get_user_albums(int(request.form["usr_id"]))
        r = json.dumps(q)
        logging.info(f"{request.remote_addr}: response: \n{r}")
        return app.response_class(response=r, status=200, mimetype='application/json')
    else:
        return render_template('get_albums.html')

@app.route('/addusertoalbum', methods=['GET', 'POST'])
def add_user_to_album():
    log_request(request)
    if request.method == 'POST':
        if id_regex.match(request.form["alb_id"]) is None or \
           id_regex.match(request.form["n_usr_id"]) is None or \
           id_regex.match(request.form["usr_id"]) is None or \
           cookie_regex.match(request.form["cookie"]) is None or \
           base64_regex.match(request.form["secret"]) is None or \
           not check_user_exists(int(request.form["n_usr_id"])) or \
           check_user_has_album(int(request.form["n_usr_id"]), int(request.form["alb_id"])):
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad args", status=422, mimetype='text/plain')
        if not check_cookie(int(request.form["usr_id"]), request.form["cookie"]) or \
           not check_user_has_album(int(request.form["usr_id"]), int(request.form["alb_id"])):
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad auth", status=403, mimetype='text/plain')
        create_user_has_album(int(request.form["n_usr_id"]), int(request.form["alb_id"]), request.form["secret"])
        logging.info(f"{request.remote_addr}: ok")
        return app.response_class(response="ok", status=200, mimetype='text/plain')
    else:
        return render_template('add_user_album.html')

@app.route('/setsliceurl', methods=['GET', 'POST'])
def set_slice():
    log_request(request)
    if request.method == 'POST':
        if id_regex.match(request.form["alb_id"]) is None or \
           url_regex.match(request.form["url"]) is None or \
           id_regex.match(request.form["usr_id"]) is None or \
           cookie_regex.match(request.form["cookie"]) is None:
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad args", status=422, mimetype='text/plain')
        if not check_cookie(int(request.form["usr_id"]), request.form["cookie"]) or \
           not check_user_has_album(int(request.form["usr_id"]), int(request.form["alb_id"])):
            logging.info(f"{request.remote_addr}: ERROR")
            return app.response_class(response="bad auth", status=403, mimetype='text/plain')
        set_slice_url(int(request.form["usr_id"]), int(request.form["alb_id"]), request.form["url"])
        logging.info(f"{request.remote_addr}: ok")
        return app.response_class(response="ok", status=200, mimetype='text/plain')
    else:
        return render_template('set_slice_url.html')
