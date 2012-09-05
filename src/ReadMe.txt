/*
Author: Som Satapathy
*/


1.HangoutRecommender.java implements the following API:
processSocialData()		// This processes user data collected from Google App Engine to compile training data
buildClassifier(classLabels)	// This builds the Classification and Regression Tree model
createTestData()		// This can be used to create test examples
generateRecommendations(classLabels) // This generates top 5 hangout recommendations

2.GenerateData.java will parse the raw data

3.CheckIn.java is the class representing user check-in information giving a set of locations that a user has been to

4.PhotoAlbum.java is the class representing user photo album information also giving a set of locations that a user has been to

5.Event.java is the class representing user events information

6.User.java is the class representing a user

7.Movies.java is the class representing movies liked by a user

8.Music.java is the class representing music liked by a user

9.Books.java is the class representing books liked by a user