import urllib.request

url = "http://localhost:8001/climb?w=%d&h=%d&x0=%d&x1=%d&y0=%d&y1=%d&xS=%d&yS=%d&s=%s&i=datasets/%s"

img = "RANDOM_HILL_512x512_2019-02-27_09-46-42.dat"
(w, h) = (512, 512)
(x1, y1) = (512,512)

(x0, y0) = (0,0) # (0, 150, 300)
strategy = "BFS" # BFS DFS ASTAR
(xS, yS) = (0, 0) # (350, 400, 450)

valuesS = [(350,)*2, (400,)*2, (450,)*2]
inits0 = [(0,)*2, (150,)*2, (300,)*2]
strategies = ["BFS","DFS","ASTAR"]

def url_link():
	return url % (w, h, x0, x1, y0, y1, xS, yS, strategy, img)

for v in valuesS:
	for i in inits0:
		for s in strategies:
			(x0,y0) = i
			(xS,yS) = v
			strategy = s
			contents = urllib.request.urlopen( url_link()).read()

