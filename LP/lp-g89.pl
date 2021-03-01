%  Logica para programacao

% grupo 89
% Hugo Rafael Silva Guerreiro   83475
% Rodrigo Domingues Oliveira    83558


%-----------------------------------------------------------------------------------------------------------------
%                                       PREDICADOS PRINCIPAIS
%-----------------------------------------------------------------------------------------------------------------

%-----------------------------------------------------------------------------------------------------------------
%                                       movs_possiveis
%-----------------------------------------------------------------------------------------------------------------
movs_possiveis(Lab, (L, C), Movs, Poss):- 	seleciona_celula(Lab, (L, C), Celula),
									 		subtract([d,e,b,c], Celula, Dir_sem_parede), %elimina as paredes e devolve os sitios sem paredes
									       	movs_possiveis_aux((L, C), Dir_sem_parede, Poss, Movs), !.

movs_possiveis_aux(_, [], [], _).

movs_possiveis_aux((L, C), [P|R], Poss, Movs):- 	lista_possiveis(P, Poss1, (L, C), Movs), %adiciona os movimentos possiveis para cada direcao
										    		movs_possiveis_aux((L, C), R, Poss2, Movs),
										    		append(Poss2, Poss1, Poss).

%-----------------------------------------------------------------------------------------------------------------
%                                       distancia
%-----------------------------------------------------------------------------------------------------------------


distancia((L1, C1), (L2, C2), Dist) :- Dist is abs(L1 - L2) + abs(C1 - C2).

distancia_aux((_, L1, C1), I, X):-	distancia((L1, C1), I, X).


%-----------------------------------------------------------------------------------------------------------------
%                                       ordena_poss
%-----------------------------------------------------------------------------------------------------------------

ordena_poss([],[],_,_).
%baseado no selection sort, elimina o menor da lista e vai em procura do proximo menor
ordena_poss(Poss, [P|R], Pos_inicial, Pos_final):-	descobre_menor(Poss, P, Pos_inicial, Pos_final),%descobre o elemento que satisfaz as melhores condicoes relativamente as distancias.
													subtract(Poss, [P], L),
													ordena_poss(L, R, Pos_inicial, Pos_final), !.



%-----------------------------------------------------------------------------------------------------------------
%                                       resolve1
%-----------------------------------------------------------------------------------------------------------------

resolve1(Lab, (L, C), Pos_final, Movs):-	resolve_aux(1, Lab, (L, C), Pos_final, (L, C), [ (i, L, C)], Movs),!.

%-----------------------------------------------------------------------------------------------------------------
%                                       resolve2
%-----------------------------------------------------------------------------------------------------------------


resolve2(Lab, (L, C), Pos_final, Movs):-	resolve_aux(2, Lab, (L, C), Pos_final, (L, C), [ (i, L, C)], Movs),!.




%-----------------------------------------------------------------------------------------------------------------
%                                     PREDICADOS AUXILIARES
%-----------------------------------------------------------------------------------------------------------------


%-----------------------------------------------------------------------------------------------------------------
% resolve_aux(Tipo_de_resolve, Labirinto, Posicao_inicial, Posicao_final, Posicao_atual, acumulador, Output)
%
%  resolve_aux afirma que Output e uma solucao para o Labirinto.
%
%-----------------------------------------------------------------------------------------------------------------

resolve_aux(_, _, _, Pos_final, Pos_final, Movs, Movs).

%Tipo 1-> Apenas segue as regras de nao voltar para tras e ordem das direcoes c-b-e-d(resolve1)
%Tipo 2-> Tenta resolver sempre da forma mais rapida (resolve2)
resolve_aux(Tipo, Lab, (L, C), Pos_final, (L_atual, C_atual), Ac, Movs):-	movs_possiveis(Lab, (L_atual, C_atual), Ac, Poss),
																			(Tipo =:= 1 ,!, (append([], Poss, L_ord), !);
							       											ordena_poss(Poss, L_ord, (L, C), Pos_final)),
															 	   			member((D, L1, C1), L_ord), %Tenta resolver o labirinto com todos os elementos resultantes de movs_possiveis (ordenados ou nao)
															 	    		append(Ac, [(D, L1, C1)], Caminho_seguinte),
															 				resolve_aux(Tipo, Lab, (L, C), Pos_final, (L1, C1), Caminho_seguinte, Movs).


%-------------------------------------------------------------------------------------------------------------------------------------
% descobre_menor(Lista_de_movs_possiveis, Output, posicao_inicial, posicao_final)
%
%  descobre_menor afirma que Output e o elemento da lista que melhor satisfaz as condicoes para o proximo movimento no labirinto.
%
%--------------------------------------------------------------------------------------------------------------------------------------

descobre_menor([P|[]], P, _, _).

descobre_menor([P|R], Menor, I, F):-	descobre_menor(R, P1, I, F),
										%calculo das distancias relativas ao fim e ao inicio do labirinto
										distancia_aux(P, F, Dist),
										distancia_aux(P1, F, Dist1),
										distancia_aux(P, I, Dist2),
			     						distancia_aux(P1, I, Dist3),
										descobre_menor_aux(P, P1, Dist, Dist1, Dist2, Dist3, Menor).


descobre_menor_aux(P, _, Dist, Dist1, _, _, P):- Dist < Dist1.
descobre_menor_aux(_, P1, Dist, Dist1, _, _, P1):- Dist > Dist1.
descobre_menor_aux(P, _, Dist, Dist, Dist2, Dist2, P). %distancias em relacao ao fim sao iguais e distancias em relacao ao inicio tambem sao iguais
descobre_menor_aux(P, P1, Dist, Dist, Dist2, Dist3, Menor):- descobre_menor_aux(P, P1, Dist3, Dist2, _, _, Menor). %criterio de desempate caso as distancias ao fim sejam iguais




%---------------------------------------------------------------------------------------------------------------------------------
% lista_possiveis(Direcao, Poss, Coordenada, Movs)
%
%  lista_possiveis afirma que Poss e a lista que resulta de juntar a Direcao com a Coordenada( alterada conforme a Direcao), tal
%  que nao haja nenhum membro na lista Movs com a mesma Coordenada.
%
%--------------------------------------------------------------------------------------------------------------------------------

%L: Linha, C: Coluna
lista_possiveis(d, Poss, (L, C), Movs):-	Aux is C+1,
										    lista_possiveis_aux((d, L, Aux), Poss, Movs).

lista_possiveis(e, Poss, (L, C), Movs):-	Aux is C-1,
										    lista_possiveis_aux((e, L, Aux), Poss, Movs).

lista_possiveis(b,Poss, (L, C), Movs):-		Aux is L+1,
										    lista_possiveis_aux((b, Aux, C), Poss, Movs).

lista_possiveis(c,Poss, (L, C), Movs):-		Aux is L-1,
										    lista_possiveis_aux((c, Aux, C), Poss, Movs).

lista_possiveis_aux((Dir, L, C), Poss, Movs):-	(\+ member((_, L, C), Movs),!,append([], [(Dir, L, C)], Poss)); %Se nao tiver sido visitado, adiciona ao conjunto de movimentos
												append([], [], Poss).


%---------------------------------------------------------------------------------------------------------------------------------
% seleciona_celula(Labirinto,Coordenada,Out)
%
% seleciona_celula afirma que Out e a celula do Labirinto na dada Coordenada
%--------------------------------------------------------------------------------------------------------------------------------

seleciona_celula([P|R], (L, C), Out):- 	nth1(L, [P|R], Out1),
									 	nth1(C, Out1, Out).
