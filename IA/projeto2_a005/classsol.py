#Grupo 5, Hugo Silva Guerreiro, n 83475, Rodrigo Domingues Oliveira, n 83558
import numpy as np
from sklearn import neighbors, datasets, tree, linear_model, naive_bayes

from sklearn.externals import joblib
import timeit

from sklearn.model_selection import cross_val_score

def features(X):

    F = np.zeros((len(X),5))
    for x in range(0,len(X)):
        F[x,0] = len(X[x])
        F[x,1] = getNAccentuantion(X[x])
        F[x,2] = 'a' in X[x]

    return F

def getNAccentuantion(word):
    count = 0
    for c in word:
        if c in ['á','à','â','ã','é','ê', 'í', 'ó', 'ô', 'ú']:
            count += 1
    return count

def mytraining(f,Y):
    clf = tree.DecisionTreeClassifier()
    clf.fit(f,Y)
    return clf

def mytrainingaux(f,Y,par):

    return clf

def myprediction(f, clf):
    Ypred = clf.predict(f)

    return Ypred
