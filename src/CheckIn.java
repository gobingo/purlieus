/**
 * @author ssatapathy
 */

public class CheckIn {
public String name;
public String country;
public String city;
public Double latitude;
public Double longitude;
public int likes;
public int comments;
public String timeStamp;
public User user;


public int getComments() {
	return comments;
}
public void setComments(int comments) {
	this.comments = comments;
}
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
public String getCountry() {
	return country;
}
public void setCountry(String country) {
	this.country = country;
}
public String getCity() {
	return city;
}
public void setCity(String city) {
	this.city = city;
}
public Double getLatitude() {
	return latitude;
}
public void setLatitude(Double latitude) {
	this.latitude = latitude;
}
public Double getLongitude() {
	return longitude;
}
public void setLongitude(Double longitude) {
	this.longitude = longitude;
}
public int getLikes() {
	return likes;
}
public void setLikes(int likes) {
	this.likes = likes;
}
public String getTimeStamp() {
	return timeStamp;
}
public void setTimeStamp(String timeStamp) {
	this.timeStamp = timeStamp;
}
}
