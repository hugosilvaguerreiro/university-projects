 
 	

#83475 Hugo Rafael Silva Guerreiro / 83558 Rodrigo Domingues Oliveira / Numero Grupo 023


#Numero de deputados em cada circulo eleitoral de indice 0 a 21.
deputados = ( 16, 3, 19, 3,
               4, 9,  3, 9,
               4, 10, 47, 2,
               39, 9, 18, 6,
               5, 9, 5, 6,
               2, 2)
               
#Nome dos partidos de indice 0 a 14 
candidaturas = ( 'PDR\tPartido Democratico Republicano',
                 'PCP-PEV\tCDU - Coligacao Democratica Unitaria',
                 'PPD/PSD-CDS/PP\tPortugal a Frente',
                 'MPT\tPartido da Terra',
                 'L/TDA\tLIVRE/Tempo de Avancar',
                 'PAN\tPessoas-Animais-Natureza',
                 'PTP-MAS\tAgir',
                 'JPP\tJuntos pelo Povo',
                 'PNR\tPartida Nacional Renovador',
                 'PPM\tPartida Popular Monarquico',
                 'NC\tNos, Cidadaos!',
                 'PCTP/MRPP\tPartido Comunista dos Trabalhadores Portugueses',
                 'PS\tPartido Socialista',
                 'B.E.\tBloco de Esquerda',
                 'PURP\tPartido Unido dos Reformados e Pensionistas')
 
 
def maior(lista_votos, lista_divisores):
    """
    Recebe dois argumentos: sendo o primeiro argumento uma lista ou tuplo contendo 
    os votos em cada partido. O segundo argumento pode ser uma lista ou um tuplo que 
    contem os divisores de cada numero da primeira lista, ou pode ser o numero 0 
    quando nao e' necessario verificar o numero de divisoes realizadas.
    Esta funcao devolve o indice do maior numero da lista ou tuplo inserida(o).
    """
    j, empate = 0, 0 #'empate' verifica se ha um empate entre os valores que sao comparados ao maior.
    
    for i in range(1,len(lista_votos)):
        
        if lista_votos[j] < lista_votos[i]:
            j = i
            empate = 0
            
        elif lista_votos[j] == lista_votos[i]:
            if lista_divisores == 0:  #Sem criterio de desempate por metodo de D'Hondt
                empate = 1                
            elif lista_divisores[j] > lista_divisores[i]:
                j = i
                empate = 0
                
    if(empate == 1):
        return -1 #Retorna o indice -1 que, nesta funcao, corresponde ao facto de haver dois valores (maiores) que sao iguais.
    
    return j       
    
def mandatos(nr_mandatos, nr_votos):
    """
    Recebe dois argumentos: um inteiro (numero de mandatos) e um tuplo 
    (numero de votos por cada partido). Devolve um tuplo que contem os mandatos
    distribuidos pelos partidos atraves do metodo DHondt.
    """
    l = len(nr_votos)
    #A multiplicao das listas por 'l' faz com que as listas tenham o mesmo tamanho que a lista 'nr_votos' para evitar IndexError.
    mandatos = [0] * l
    lista_votos = [0] * l
     
    for i in range(l):
        lista_votos[i] = nr_votos[i] / (mandatos[i] + 1) #O divisor do numero e' igual ao numero de mandatos + 1
     
    for mandatos_atribuidos in range(nr_mandatos):
        ind = maior(lista_votos, mandatos)
        
        if(nr_votos[ind] != 0):  #Atribui mandatos se o maior de indice 'ind' for diferente de 0. Se fosse 0 implicaria\
                                 #que todos os elementos da lista 'nr_votos' fossem igual a 0.
            mandatos[ind] = mandatos[ind] + 1
            lista_votos[ind] = nr_votos[ind]/( mandatos[ind] + 1 )
            
    return tuple(mandatos)
  
def assembleia(votacoes):
    """
    Recebe um unico argumento: um tuplo que contem 22 tuplos. Cada um dos 22
    tuplos representa um circulo eleitoral ao qual estao associados 15 valores
    que representam os votos em cada partido.
    Esta funcao devolve um tuplo contendo a distribuicao dos mandatos pelos 15
    partidos.
    """
    
    votos = [0] * 15 #Queremos que 'votos' tenha tamanho 15 (15 partidos).
    
    for i in range(22): #Percorre cada circulo eleitoral (22 circulos eleitorais).
        total_ronda = mandatos(deputados[i],votacoes[i]) #Numero de mandatos atribuidos a cada partido em um circulo eleitoral.
        
        for c in range(15): #Percorre cada partido (15 partidos)
            votos[c] = votos[c] + total_ronda[c] #Adiciona o numero de mandatos atribuidos a cada partido por circulo eleitoral.
    
    return tuple(votos)
      
def max_mandatos(votacoes):
    """
    Recebe um unico argumento: um tuplo que contem 22 tuplos. Cada um dos 22
    tuplos representa um circulo eleitoral ao qual estao associados 15 valores
    que representam os votos em cada partido.
    Devolve o nome e sigla do partido que recebeu o maior numero de mandatos.
    """
     
    assemb = assembleia(votacoes)
    ind = maior(assemb,0) #O segundo argumento e' 0 uma vez que nao e' necessario desempate por metodo de D'Hondt
     
    if(ind == -1): #O facto de o valor 'ind' ser -1 indica que ha dois ou mais valores que sao os maiores do tuplo, com igual valor.
        return 'Empate tecnico' 
        
    return candidaturas[ind]
