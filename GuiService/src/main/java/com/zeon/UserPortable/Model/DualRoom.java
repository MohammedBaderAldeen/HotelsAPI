package com.zeon.UserPortable.Model; 


import java.util.Date; 

public class DualRoom {


	private int id;
	private String description;
	private String url;
	private int adult;
	private float price; //if you want type currency
//	private int ratting;
	private Boolean reserved;
	private Date checkIn;
	private Date checkOut;
	
	
	
	public DualRoom() {

	}
	
	public DualRoom( String description, int adult, float price, Boolean reserved, Date checkIn,
			Date checkOut) {
		super();
		this.description = description;
		this.adult = adult;
		this.price = price;
		this.reserved = reserved;
		this.checkIn = checkIn;
		this.checkOut = checkOut;
	}
	
	public DualRoom(String description, int adult, float price, Boolean reserved) {
		super();
		this.description = description;
		this.adult = adult;
		this.price = price;
		this.reserved = reserved;
	}
	
	public DualRoom(int id, String description,String url, int adult, float price, Boolean reserved) {
		super();
		this.id = id;
		this.description = description;
		this.price = price;
		this.reserved = reserved;
		this.adult = adult;
		this.url = url;
	}
	
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getAdult() {
		return adult;
	}
	public void setAdult(int adult) {
		this.adult = adult;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public Boolean getReserved() {
		return reserved;
	}
	public void setReserved(Boolean reserved) {
		this.reserved = reserved;
	}
	public Date getCheckIn() {
		return checkIn;
	}
	public void setCheckIn(Date checkIn) {
		this.checkIn = checkIn;
	}
	public Date getCheckOut() {
		return checkOut;
	}
	public void setCheckOut(Date checkOut) {
		this.checkOut = checkOut;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "DualRoom [id=" + id + ", description=" + description + ", url=" + url + ", adult=" + adult
				+ ", price=" + price + ", reserved=" + reserved + ", checkIn=" + checkIn + ", checkOut=" + checkOut
				+ "]";
	}
	
	
	
	
	
}







