# Compiladores 2018 - GR8

GR8 is an imperative unstructured language. One of the objectives of the language is to allow easy reading, so it is strongly restrictive regarding the appearance and position of the code in the lines (indentation).

## Final Grade
17.81

## Authors

* **Rodrigo Oliveira** - [rodrigdoliveira](https://github.com/rodrigdoliveira)
* **Hugo Guerreiro** - [hugosilvaguerreiro](https://github.com/hugosilvaguerreiro)

## Friendly Reminder

We are not responsible for your actions. We highly recommend you to not copy and paste any of this code on your own project, if you do you better be prepared to be "eliminado por cópia". Use this project to help you think and to solve your problem in a unique way.

## GR8 Program Example


    small function g uses small i
    define small function f on small i as
      small n (initially 9)
      if i equals 0 then
        assign 0 to n
      else
        assign i plus use i minus 1 for g to n
      return n
    define small function g on small i as
      small n (initially 9)
      if i equals 0 then
        assign 0 to n
      else
        assign i plus use i minus 1 for f to n
      return n
    define public small function covfefe as
      post use 3 for f
      return 0