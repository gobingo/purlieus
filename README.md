purlieus - A Recommendation engine that suggests a configurable list of top hangout spots for a user based on his or her social interaction and connections over an online social network.
========

API:
processSocialData()  	// This processes user data collected from Google App Engine to compile training data

buildClassifier(classLabels)	// This builds the Classification and Regression Tree model

createTestData()		// This can be used to create test examples

generateRecommendations(classLabels) // This generates top 5 hangout recommendations