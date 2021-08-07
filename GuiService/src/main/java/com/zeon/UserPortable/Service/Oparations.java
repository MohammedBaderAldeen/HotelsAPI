package com.zeon.UserPortable.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.zeon.UserPortable.Model.Hotel;
import com.zeon.UserPortable.Model.SingleRoom;
import com.zeon.UserPortable.Model.User;

public class Oparations {

	public Oparations() {

	}

	public User findUserById(User[] user, long id) {

		User userObj = new User();

		for (int i = 0; i < user.length; i++) {

			if (user[i].getId() == id) {
				System.out.println("I Find The User :" + id);
				System.out.println(user[i].toString());
				userObj = user[i];
				break;
			} else {
				System.out.println("The user not here .......");
			}
		}
		return userObj;
	}

	public List<Hotel> getHotels(Hotel[] hotels) {
		List<Hotel> allHotels = new ArrayList<Hotel>();
		for (int k = 0; k < hotels.length; k++) {
			if (hotels[k] == null) {
				break;
			}
			allHotels.add(hotels[k]);
		}
		return allHotels;
	}

	public Hotel getSpecificHotels(Hotel[] hotels, int id) {
		Hotel hotel = new Hotel();
		for (int i = 0; i < hotels.length; i++) {
			if (hotels[i].getid() == id) {
				hotel = hotels[i];
			}
		}
		return hotel;
	}
	
	public int calckNumberOfDays(Date book, Date leave) throws ParseException {

		Calendar cal = Calendar.getInstance();
		cal.setTime(book);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(leave);
		
		int dayOfMonth1 = cal.get(Calendar.DAY_OF_MONTH);
		int dayOfMonth2 = cal2.get(Calendar.DAY_OF_MONTH);

		int num = dayOfMonth2 - dayOfMonth1;
		return num;
	}

}
