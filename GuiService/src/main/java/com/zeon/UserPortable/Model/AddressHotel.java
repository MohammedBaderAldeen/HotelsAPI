package com.zeon.UserPortable.Model;



public class AddressHotel {
	
	private String address;
	private String countryCode;
	
	public AddressHotel() {
		
	}
	
	public AddressHotel(String address, String countryCode) {
		super();
		this.address = address;
		this.countryCode = countryCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	@Override
	public String toString() {
		return "AddressHotel [address=" + address + ", countryCode=" + countryCode + "]";
	}
	
	

}
