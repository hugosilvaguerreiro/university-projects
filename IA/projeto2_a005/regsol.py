#Grupo 5, Hugo Silva Guerreiro, n 83475, Rodrigo Domingues Oliveira, n 83558
import numpy as np
from sklearn import datasets, tree, linear_model
from sklearn.kernel_ridge import KernelRidge
from sklearn.model_selection import cross_val_score
import timeit

def mytraining(X,Y):
    reg = KernelRidge(alpha=0.001, gamma= 0.10, kernel="rbf")
    reg.fit(X,Y)
    return reg

def mytraining2(X,Y):
    reg = linear_model.LinearRegression(fit_intercept=False)
    reg.fit(X,Y)
    return reg

def mytrainingaux(X,Y,par):
    reg = KernelRidge(alpha=par[0], gamma=par[1], kernel="rbf")
    reg.fit(X,Y)

    return reg

def myprediction(X,reg):

    Ypred = reg.predict(X)

    return Ypred
