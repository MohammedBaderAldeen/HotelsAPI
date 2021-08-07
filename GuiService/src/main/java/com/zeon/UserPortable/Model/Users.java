package com.zeon.UserPortable.Model;



import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import io.micrometer.core.annotation.Counted;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Users implements Serializable{

	private int id;

	private String email;

	private String name;
	private String password;
	private String lastName;
	private int active;
	private String phoneNumber;

	private int version;

	private Set<Role> roles = new HashSet<Role>();

	private Set<UserBank> userBank = new HashSet<UserBank>();

	public Users() {
	}

	public Users(int id, String email, String name, String password, String lastName, int active, String phoneNumber,
			Set<Role> roles, Set<UserBank> userBank) {
		super();
		this.id = id;
		this.email = email;
		this.name = name;
		this.password = password;
		this.lastName = lastName;
		this.active = active;
		this.phoneNumber = phoneNumber;
		this.roles = roles;
		this.userBank = userBank;
	}

	public Users(int id, String email, String name, String password, String lastName, int active, String phoneNumber,
			int version, Set<Role> roles, Set<UserBank> userBank) {
		super();
		this.id = id;
		this.email = email;
		this.name = name;
		this.password = password;
		this.lastName = lastName;
		this.active = active;
		this.phoneNumber = phoneNumber;
		this.version = version;
		this.roles = roles;
		this.userBank = userBank;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	public Set<UserBank> getUserBank() {
		return userBank;
	}

	public void setUserBank(Set<UserBank> userBank) {
		this.userBank = userBank;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "Users [id=" + id + ", email=" + email + ", name=" + name + ", password=" + password + ", lastName="
				+ lastName + ", active=" + active + ", phoneNumber=" + phoneNumber + ", version=" + version + ", roles="
				+ roles + ", userBank=" + userBank + "]";
	}

}
