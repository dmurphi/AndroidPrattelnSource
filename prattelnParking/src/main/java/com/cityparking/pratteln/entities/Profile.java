package com.cityparking.pratteln.entities;

public class Profile {

	String first_name, last_name, email, userGuid;

	public Profile() {
		super();

	}

	public Profile(String first_name, String last_name, String email, String guid) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.userGuid = guid;
	}

	@Override
	public String toString() {
		return "Profile [first_name=" + first_name + ", last_name=" + last_name + ", email=" + email + "]";
	}

	public String getUserGuid() {
		return userGuid;
	}

	public void setUserGuid(String userGuid) {
		this.userGuid = userGuid;
	}

	public String getFirst_name() {
		return first_name;
	}

	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}

	public String getLast_name() {
		return last_name;
	}

	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
