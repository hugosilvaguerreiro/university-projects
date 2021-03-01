import nltk
from nltk.stem.lancaster import LancasterStemmer
import string
import sys
from sklearn.ensemble import RandomForestClassifier
from sklearn.feature_extraction.text import TfidfTransformer
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.pipeline import Pipeline

CORPUS_PATH = sys.argv[1]
TEST_PATH = sys.argv[2]
MOVIE_PATH = "recursos/list_movies.txt"
CHARACTERS_PATH = "recursos/list_characters.txt"
COMPANIES_PATH = "recursos/list_companies.txt"
GENRES_PATH = "recursos/list_genres.txt"
JOBS_PATH = "recursos/list_jobs.txt"
KEYWORDS_PATH = "recursos/list_keywords.txt"
PEOPLE_PATH = "recursos/list_movies.txt"

categories_index = {'runtime': 0, 
							'overview': 1, 
							'budget': 2, 
							'original_language': 3, 
							'release_date': 4, 
							'revenue': 5, 
							'genre': 6, 
							'keyword': 7, 
							'person_name': 8, 
							'vote_avg': 9, 
							'actor_name':10, 
							'character_name': 11, 
							'production_company': 12, 
							'production_country':13, 
							'spoken_language':14, 
							'original_title':15}

categories = ['runtime', 'overview', 'budget', 'original_language', 'release_date', 'revenue', 'genre', 'keyword', 'person_name', 'vote_avg', 'actor_name', 'character_name', 'production_company', 'production_country', 'spoken_language', 'original_title']


def remove_punctuation_and_lower(question):
		translator = str.maketrans('','',string.punctuation)
		return question.lower().translate(translator)

def load_word_list(path):
		result = []
		with open(path, "r") as file:
				for word in file:
						result.append(remove_punctuation_and_lower(word[:-1].strip(" ")))
		return result

characters_list = load_word_list(CHARACTERS_PATH)
movies_list = load_word_list(MOVIE_PATH)
companies_list = load_word_list(COMPANIES_PATH)
genres_list = load_word_list(GENRES_PATH)
jobs_list = load_word_list(JOBS_PATH)
keywords_list = load_word_list(KEYWORDS_PATH)
people_list = load_word_list(PEOPLE_PATH)



def stem(phrase, st=None):
		if st == None:
				st = LancasterStemmer()
		result = []
		for word in phrase.split():
				result += [st.stem(word)]
		return " ".join(result)

def change_words(question, replacement,  resources_list):
		''' question comes clean '''
		question = " " + question + " "
		for key_word in resources_list:
				index = question.find(" " + key_word + " ")
				if index != -1:
						question = question[:index] + " " + replacement + " " + question[index+len(key_word)+2:]
		return question.strip(" ")


def clean(iterating_list):
		result_questions = []
		for question in iterating_list:
				question = remove_punctuation_and_lower(question)
				result = change_words(question, "_MOVIE_", movies_list)
				result = change_words(result, "_CHARACTER_", characters_list)
				result = change_words(result, "_COMPANY_", companies_list)
				result = change_words(result, "_GENRES_", genres_list)
				result = change_words(result, "_JOBS_", jobs_list)
				result = change_words(result, "_PEOPLE_", people_list)
				result_questions.append(result)
		return result_questions




parsed_corpus = {}
with open(CORPUS_PATH, "r") as corpus:
		for question in corpus:
				l = parsed_corpus.pop(question.split()[0], [])
				parsed_corpus[question.split()[0]] = l + [" ".join(question.split()[1:])] 

				
testset = []
with open(TEST_PATH, "r") as question_file:
		for question in question_file:
				testset.append(question[:-1])


questions = []
tags_index = []
for k, v in parsed_corpus.items():
		for question in v:
				questions += [question]
				tags_index += [categories_index[k]]
				
result_questions = clean(questions)
result_questions = [i for i in map(stem, result_questions)]
result_test_questions = clean(testset)
result_test_questions = [i for i in map(stem, result_test_questions)]




text_clf = Pipeline([('vect', TfidfVectorizer(stop_words="english")),
										 ('tfidf', TfidfTransformer()),
										 ('clf', RandomForestClassifier(n_estimators=100,
																										min_samples_leaf=1, 
																										bootstrap=True, 
																										class_weight="balanced_subsample"))])
text_clf = text_clf.fit(result_questions, tags_index)


predictions = text_clf.predict(result_test_questions)

for i in predictions:
	print(categories[i] + " ")
