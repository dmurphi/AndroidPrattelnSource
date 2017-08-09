package com.cityparking.pratteln.webservices.entity;

public class ProfileResponseBody {
	private String lastName;
	private Object call_signature;
	private String firstName;
	private String email;
	private String userGuid;

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Object getCall_signature() {
		return call_signature;
	}

	public void setCall_signature(Object call_signature) {
		this.call_signature = call_signature;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserGuid() {
		return userGuid;
	}

	public void setUserGuid(String userGuid) {
		this.userGuid = userGuid;
	}

	@Override
	public String toString() {
		return "ProfileResponseBody [lastName=" + lastName + ", call_signature=" + call_signature + ", firstName=" + firstName + ", email=" + email + ", userGuid=" + userGuid + "]";
	}

}
