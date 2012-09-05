/**
 * @author ssatapathy
 */

public class PhotoAlbum {
public User user;
public String name;
public String location;
public String timeStamp;
public int likes;
public int comments;
public User getUser() {
	return user;
}
public void setUser(User user) {
	this.user = user;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getLocation() {
	return location;
}
public void setLocation(String location) {
	this.location = location;
}

public String getTimeStamp() {
	return timeStamp;
}
public void setTimeStamp(String timeStamp) {
	this.timeStamp = timeStamp;
}
public int getLikes() {
	return likes;
}
public void setLikes(int likes) {
	this.likes = likes;
}
public int getComments() {
	return comments;
}
public void setComments(int comments) {
	this.comments = comments;
}

}
