 
from search import *
from utils import *
import copy



# TAI color
# sem cor = 0
# com cor > 0
def get_no_color():
 return 0
def no_color (c):
 return c==0
def color (c):
 return c > 0

# TAI pos
# Tuplo (l, c)
def make_pos (l, c):
 return (l, c)
def pos_l (pos):
 return pos[0]
def pos_c (pos):
 return pos[1]



# TAI grupo
# Lista [pos1,..., posN]
def group_add_element(group, element):
	outputGroup += [element]
	return outputGroup
def group_get_element(group, position):
	return group[position]

def group_get_number_of_elements(group):
	return len(group)
#This functions returns the beggining and ending of a group in a given column
def group_find_end_by_column(group, column):
	minCol = -1
	maxCol = -1
	for pos in group:
		if pos_c(pos) == column:
			if minCol == -1:
				minCol = pos_l(pos)
			if pos_l(pos) > maxCol:
				maxCol = pos_l(pos)
			if pos_l(pos) < minCol:
				minCol = pos_l(pos)
	return (minCol, maxCol)


#TAI board
#Lista [Linha1, ..., LinhN]
#Linha : Lista [cor1, ..., corN]
def board_find_groups(board):
	def apply_functions(group, GroupColor, pos, nLinhas, nColunas, board, marked, functionsToApply, positions):
		group.append(pos)
		marked[pos_l(pos)][pos_c(pos)] = True
		for i in range(0,3):
			functions[functionsToApply[i]](group, GroupColor, positions[i], nLinhas, nColunas, board, marked)

	def find_right_color_group(group, GroupColor, pos, nLinhas, nColunas, board, marked):
		if(pos_c(pos) < nColunas):
			if( color(board_get_element(board, pos)) and board[pos_l(pos)][pos_c(pos)] == GroupColor and marked[pos_l(pos)][pos_c(pos)] == False):
				positions = [make_pos(pos_l(pos)+1, pos_c(pos)), make_pos(pos_l(pos)-1, pos_c(pos)), make_pos(pos_l(pos), pos_c(pos)+1) ]
				apply_functions(group, GroupColor, pos, nLinhas, nColunas, board, marked, ["down", "up", "right"], positions)

	def find_left_color_group(group, GroupColor, pos, nLinhas, nColunas, board, marked):
		if(pos_c(pos) >= 0):
			if(color(board_get_element(board, pos)) and board[pos_l(pos)][pos_c(pos)] == GroupColor and marked[pos_l(pos)][pos_c(pos)] == False):
				positions = [make_pos(pos_l(pos)+1, pos_c(pos)), make_pos(pos_l(pos)-1, pos_c(pos)),  make_pos(pos_l(pos), pos_c(pos)-1) ]
				apply_functions(group, GroupColor, pos, nLinhas, nColunas, board, marked, ["down", "up", "left"], positions)

	def find_down_color_group(group, GroupColor, pos, nLinhas, nColunas, board, marked):
		if(pos_l(pos) < nLinhas):
			if(color(board_get_element(board, pos)) and board[pos_l(pos)][pos_c(pos)] == GroupColor and marked[pos_l(pos)][pos_c(pos)] == False):
				positions = [make_pos(pos_l(pos), pos_c(pos)+1), make_pos(pos_l(pos)+1, pos_c(pos)), make_pos(pos_l(pos), pos_c(pos)-1) ]
				apply_functions(group, GroupColor, pos, nLinhas, nColunas, board, marked, ["right", "down", "left"], positions)

	def find_up_color_group(group, GroupColor, pos, nLinhas, nColunas, board, marked):
		if(pos_l(pos) >= 0):
			if(color(board_get_element(board, pos)) and board[pos_l(pos)][pos_c(pos)] == GroupColor and marked[pos_l(pos)][pos_c(pos)] == False):
				positions = [make_pos(pos_l(pos), pos_c(pos)+1), make_pos(pos_l(pos)-1, pos_c(pos)), make_pos(pos_l(pos), pos_c(pos)-1) ]
				apply_functions(group, GroupColor,pos, nLinhas, nColunas, board, marked, ["right", "up", "left"], positions)


	functions = {"right": find_right_color_group, "left": find_left_color_group, "down": find_down_color_group, "up": find_up_color_group}
	nLinhas = board_get_nr_lines(board)
	nColunas = board_get_nr_cols(board)
	marked = list()

	for i in range(0, nLinhas):
		coluna = list()
		for c in range(0, nColunas):
			coluna.append(False)
		marked.append(coluna)

	groups = []

	iLinha = 0
	iColuna = 0
	group = []
	for l in range(0, nLinhas):
		for c in range(0, nColunas):
			find_right_color_group(group, board[l][c], make_pos(l, c), nLinhas, nColunas, board, marked)
			if(group != []):
				groups.append(group)
				group = []
	return groups

def board_vertical_compact(board, group, groupSize):

	def move_lines_to_bottom(startingLine, col):
		for line in range(startingLine-1, -1, -1):
			pos = make_pos(line, col)
			newPos = make_pos(line + 1, col)
			posColor = board_get_element(outputBoard, pos)
			board_put_element(outputBoard, newPos, posColor)
			board_remove_pos(outputBoard, pos)

	outputBoard = board
	nrLines = board_get_nr_lines(outputBoard)
	linesToMove = []
	visitedColumns = []
	for i in range(0,groupSize):

		pos = group_get_element(group, i)
		if pos_c(pos) not in visitedColumns:
			visitedColumns += [pos_c(pos)]
			previouslyCol = False
			for j in range(0, nrLines):
				if color(board_get_element(outputBoard, make_pos(j ,pos_c(pos)))):
					previouslyCol = True
				if not color(board_get_element(outputBoard, make_pos(j, pos_c(pos)))) and previouslyCol:
					linesToMove += [j]
		for i in linesToMove:
			move_lines_to_bottom(i, pos_c(pos))
		linesToMove = []
	return outputBoard


def board_horizontal_compact(board, group, groupSize):

	def move_Columns_to_left(startingColumn):
		for col in range(startingColumn + 1, nrOfCollumns):
			for line in range(0, nrOfLines):
				pos = make_pos(line, col)
				newPos = make_pos(line , col -1)
				posColor = board_get_element(outputBoard, pos)
				board_put_element(outputBoard, newPos, posColor)
				board_remove_pos(outputBoard, pos)

	visitedColumns = []
	columnsToMove = []
	nrOfLines = board_get_nr_lines(board)
	nrOfCollumns = board_get_nr_cols(board)
	outputBoard = board

	for i in range(0, groupSize):

		pos = group_get_element(group, i)
		if pos_c(pos) not in visitedColumns:

			emptyColumn = True
			visitedColumns += [pos_c(pos)]
			for j in range(0, nrOfLines):

				newPos = make_pos(j, pos_c(pos))
				if color(board_get_element(outputBoard, newPos)):
					emptyColumn = False
					break;

			if emptyColumn:
				columnsToMove += [pos_c(pos)]

	for i in sorted(columnsToMove, key=int, reverse=True):
		move_Columns_to_left(i)
	return outputBoard


def board_empty_board(nrLines, nrColumns):
	board = []
	for i in range(nrLines):
		board += [[]]
		for j in range(nrColumns):
			board[i] += [0]
	return board
def board_get_nr_lines(board):
	return len(board)

def board_get_nr_cols(board):
	return len(board[0])

def board_remove_pos(board, pos):
	return board_put_element(board, pos, get_no_color())

def board_get_element(board, pos):
	return board[pos_l(pos)][pos_c(pos)]

def board_put_element(board, pos, element):
	outputBoard = board
	outputBoard[pos_l(pos)][pos_c(pos)] = element
	return outputBoard


def board_remove_group(board, group):
	outputBoard = copy.deepcopy(board)
	groupSize = group_get_number_of_elements(group)
	#removes the group
	for i in range(0, groupSize):
		pos = group_get_element(group, i)
		outputBoard = board_remove_pos(outputBoard, pos)
	#compacts the resulting board
	outputBoard = board_vertical_compact(outputBoard, group, groupSize)
	outputBoard = board_horizontal_compact(outputBoard, group, groupSize)
	return outputBoard

#TAI sg_state
class sg_state:
	def __init__(self, board):
		self.board = board
		self.groups = None
		self.emptySpaces = None


	def get_board(self):
		return self.board

	def get_groups(self):
		if self.groups == None:
			self.groups = board_find_groups(self.board)
		return self.groups

	def get_empty_spaces(self):
		if self.emptySpaces == None:
			self.emptySpaces = 0
			for i in range(0, board_get_nr_lines(self.board)):
				for j in range(0, board_get_nr_cols(self.board)):
					pos = make_pos(i,j)
					if no_color(board_get_element(self.board, pos)):
						self.emptySpaces += 1
		return self.emptySpaces

	def __lt__(self, other_sg_state):
		return self.get_empty_spaces() < other_sg_state.get_empty_spaces()


class same_game(Problem):
	def __init__(self, board):
		self.initial = sg_state(board)
		self.goal = sg_state(board_empty_board(board_get_nr_lines(board), board_get_nr_cols(board)))
	def actions(self, state):
		groups = state.get_groups()
		actions = []
		for group in groups:
			if len(group) > 1:
				actions += [group]
		return actions

	def result(self, state, action):
		board = state.get_board()
		board = board_remove_group(board, action)
		return sg_state(board)

	def goal_test(self, state):
		if state.get_board() == self.goal.get_board():
			return True
		else:
			return False

	def path_cost(self, c, state1, action, state2):
			return c + 1

	def h(self, node):
		INVALID_PATH = 10000
		colors = {}
		emptyGroups = 0
		h = 0
		groups = node.state.get_groups()
		board = node.state.get_board()
		for group in groups:
			color = board_get_element(board, group_get_element(group, 0)) 
			if color not in colors:
				colors[color] = [0,[]]
				h += 1
			h += 1
			colors[color][0] += 1
			colors[color][1] += [group]
		for i in colors:
			if colors[i][0] == 1 and len(colors[i][1][0]) == 1:
				h += INVALID_PATH
				break;
		return h
