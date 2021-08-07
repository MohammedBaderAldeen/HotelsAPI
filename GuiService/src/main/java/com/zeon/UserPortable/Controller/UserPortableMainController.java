package com.zeon.UserPortable.Controller;

import java.io.IOException; 
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.HotelOffer;
import com.amadeus.resources.HotelOffer.MediaURI;
import com.amadeus.resources.HotelOffer.Offer;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.appinfo.InstanceInfo;

import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.zeon.UserPortable.MessageQueue.RabbitMQSender;
import com.zeon.UserPortable.Model.AddressHotel;
import com.zeon.UserPortable.Model.Contact;
import com.zeon.UserPortable.Model.Hotel;
import com.zeon.UserPortable.Model.SingleRoom;
import com.zeon.UserPortable.Model.User;
import com.zeon.UserPortable.Model.UserBank;
import com.zeon.UserPortable.Model.UserHotel;
import com.zeon.UserPortable.Model.Users;
import com.zeon.UserPortable.Service.Oparations;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class UserPortableMainController {

	Logger logger= LoggerFactory.getLogger(UserPortableMainController.class);
	
	@Autowired
	EurekaClient eurakClient;

	@Autowired
	private AmqpTemplate amqp;


	@Autowired
	RestTemplateBuilder restTemplateBuilder;

	@Autowired
	EurekaDiscoveryClient dicovery;

	@Autowired
	private AmqpTemplate amqpTemplate;

	@Autowired
	private RabbitMQSender MQSernder;

	Oparations ops = new Oparations();

	private User[] userPar = new User[20];

	@GetMapping("/index")
	public String IndexPage(HttpServletRequest request) {

		return "index";
	}
	////////////////// for login

	@GetMapping("/login-user")
	public String LoginUser(HttpServletRequest request) {
		return "loginPage";
	}

	///////////////////// for register

	@GetMapping("/register-user")
	public String registerUser() {
		return "register";
	}

///////////////                 The Hotel Operation					///////////////

	///////////////////////////////////////////////////////////////// Get all hotel
	///////////////////////////////////////////////////////////////// from the API
	@RequestMapping(value = "/get-all-hotel-amd", method = RequestMethod.GET)
	@ResponseBody
	public String getAllHotelAPI(HttpServletRequest request) throws IOException {

		RestTemplate restTemplate = restTemplateBuilder.build();

		// InstanceInfo info =
		// eurakClient.getNextServerFromEureka("HOTELRESEVATION-SERVICE", false);
		InstanceInfo info = eurakClient.getNextServerFromEureka("ZUUL-SERVER", false);
		String baseUrl = info.getHomePageUrl();

		System.out.println("The baseurl :" + baseUrl);

		HttpEntity entity = new HttpEntity(getHeaders());

		Hotel[] hotel = new Hotel[200];

		System.out.println("The entity Body is :" + entity.getBody());

		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(baseUrl + "/hotelReserveion/get-hotel-api", HttpMethod.GET, entity,
					String.class);
			System.out.println("The Response is " + response.getBody());
			hotel = new Gson().fromJson(response.getBody(), Hotel[].class);

			for (int i = 0; i < hotel.length; i++) {
				System.out.println("The Response of hotel is : >>>>>>>>>>>>>>>>" + hotel[i].getName());
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		if (response.getStatusCode() == HttpStatus.OK) {

			System.out.println("The Services is OK ................. :" + response.getStatusCode());
		}

		System.out.println(response.getBody());

		request.setAttribute("hotels", hotel);
		return response.getBody();
	}

/////////////////////////////////////////////////////////////////////////////////search hotel

	@GetMapping("/get-search-page")
	public String getSearchPage() {

		System.out.println("getSearchPage Service is Called .......");
		return "search_hotel";
	}

	@RequestMapping("/search-hotel-city")
	@HystrixCommand(fallbackMethod = "getFallbackSearchHotel")
	@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "false")
	public String searchHotel(@RequestParam String city, HttpServletRequest request, HttpSession session) {

		RestTemplate restTemplate = restTemplateBuilder.build();

		InstanceInfo zuulInfo = eurakClient.getNextServerFromEureka("ZUUL-SERVER", false);

		String baseUrl = zuulInfo.getHomePageUrl();

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		List<String> listInfo = new ArrayList<String>();
		listInfo.add(city);

		Hotel[] hotel = new Hotel[1000];
		ResponseEntity<String> response = null;
		HttpEntity<List<String>> requestEntity = new HttpEntity<>(listInfo, headers);

		try {

			response = restTemplate.exchange(baseUrl + "/hotelReserveion/get-request-hotel-city", HttpMethod.POST,
					requestEntity, String.class);
			
			logger.info("The response   ................... : "+new ObjectMapper().writeValueAsString(response));

			hotel = new Gson().fromJson(response.getBody(), Hotel[].class);

		} catch (Exception e) {
			System.out.println(e);
		}

		if (response.getBody() == null) {
			return "stop";
		} else if (response.getStatusCode() == HttpStatus.OK && hotel.length != 0) {
			List<Hotel> hotels = ops.getHotels(hotel);
			session.setAttribute("allHotels", hotels);
			request.setAttribute("hotels", hotels);

			return "showallhotel";
		} else {
			return "noresult";
		}

	}

	@RequestMapping(value = "/hotel-info", method = RequestMethod.GET)
	@HystrixCommand(fallbackMethod = "getFallbackShowHotelInfo")
	@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "false")
	public String ShowHotelInfo(@RequestParam int id, HttpServletRequest request, HttpSession session)
			throws IOException {

		RestTemplate restTemplate = restTemplateBuilder.build();
		// InstanceInfo info =
		// eurakClient.getNextServerFromEureka("HOTELRESEVATION-SERVICE", false);
		InstanceInfo zuulInfo = eurakClient.getNextServerFromEureka("ZUUL-SERVER", false);
		String baseUrl = zuulInfo.getHomePageUrl();

		System.out.println("The baseurl :" + baseUrl);

		HttpEntity entity = new HttpEntity(getHeaders());
		Hotel[] hotels = new Hotel[500];
		Hotel hotel = new Hotel();

		System.out.println("The entity Body is :" + entity.getBody());

		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(baseUrl + "/hotelReserveion/get-request-hotel-show", HttpMethod.GET,
					entity, String.class);
			
			logger.info("The response   ................... : "+new ObjectMapper().writeValueAsString(response));
			
			hotels = new Gson().fromJson(response.getBody(), Hotel[].class);

			System.out.println("The Response of hotel is : >>>>>>>>>>>>>>>>" + hotels[0].getName());
		} catch (Exception e) {
			System.out.println(e);
		}

		if (response.getStatusCode() == HttpStatus.OK) {

			System.out.println("The Services is OK ................. :" + response.getStatusCode());
		}
		hotel = ops.getSpecificHotels(hotels, id);
		request.setAttribute("hotel", hotel);
		return "showHotelInfo";
	}

	@GetMapping(value = "/producer2")
	@ResponseBody
	public String messagequeue2() {

		List<String> data = new ArrayList();
		
		String ex = "direct-exchange";
		String key = "BankReservationRoute";

		amqp.convertAndSend(ex, key, data);

		return "MSG SENT";

	}

	@RequestMapping(value = "/after-payment", method = RequestMethod.GET)
	@HystrixCommand(fallbackMethod = "getFallGetCheckBankInfo")
	@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "false")
	public String getCheckBankInfo(@RequestParam String price, @RequestParam int hotel_id, @RequestParam int roomId,
			@RequestParam String roomType, @RequestParam String useremail, @RequestParam long accountId,
			@RequestParam String username, @RequestParam String password, @RequestParam String book,
			@RequestParam String leave, @RequestParam String city, HttpServletRequest request, HttpSession s)
			throws IOException, ParseException {
		int n = 0;

		SimpleDateFormat formatter2 = new SimpleDateFormat("MM/dd/yyyy");
		Date date1 = formatter2.parse(book);
		Date date2 = formatter2.parse(leave);
		int num = ops.calckNumberOfDays(date1, date2);

		float p = Float.parseFloat(price);
		float totalPrice = Math.abs(p * num);

		String priceString = String.valueOf(totalPrice);

		RestTemplate restTemplate = restTemplateBuilder.build();

		InstanceInfo info = eurakClient.getNextServerFromEureka("BANKSYSTEMRESEVATION-SERVICE", false);
		String baseUrl = info.getHomePageUrl();

		List<String> res = new ArrayList<String>();
		List<String> listInfo = new ArrayList<String>();
		listInfo.add(useremail);
		listInfo.add(password);
		listInfo.add(Long.toString(accountId));
		listInfo.add(priceString);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		MessageQueueProducer(listInfo);

		ResponseEntity<String> response = null;
		HttpEntity<List<String>> requestEntity = new HttpEntity<>(listInfo, headers);

		UserBank userBank = new UserBank();

		try {
			// for request
			response = restTemplate.exchange(baseUrl + "/check-bank-account", HttpMethod.POST, requestEntity,
					String.class);
			
			logger.info("The response   ................... : "+new ObjectMapper().writeValueAsString(response));

			res = new Gson().fromJson(response.getBody(), ArrayList.class);
			System.out.println("The Answer is ........" + res.toString());

		} catch (Exception e) {
			System.out.println(e);
		}

		if (response.getBody() == null) {
			System.out.println("The User not found ................. :" + response.getStatusCode());
			return "stop"; ////////// here I need GUI
		} else if (response.getStatusCode() == HttpStatus.OK) {

			System.out.println("Value is ..... :" + Integer.valueOf(res.get(0)));

			if (Integer.valueOf(res.get(0)) != 0) {

				UserHotel userHotel = new UserHotel();
				userHotel.setUserName(useremail);
				userHotel.setHotelId(hotel_id);
				userHotel.setRoomId(roomId);
				userHotel.setTotalCost(totalPrice);
				userHotel.setStartdate(date1);
				userHotel.setEndDate(date2);
				userHotel.setRoomType(roomType);
				userHotel.setAvalible(1);
				userHotel.setAccountId(accountId);
				userHotel.setCityName(city);

				System.out.println("The response is after: ......" + response.getBody());

				if (n == 0) {
					userHotelPost(userHotel);
				}

				n++;

				request.setAttribute("userbankinfo", userBank);
				return "index";
			} else {
				System.out.println("The Account not found ..............");
				return "noresult";
			}
		} else {

			System.out.println("The Services is OK ................. :" + response.getStatusCode());
			return "403";
		}
	}

	@GetMapping(value = "/message-queue")
	@ResponseBody
	public String MessageQueueProducer(List<String> userPlane) {

		MQSernder.sendData(userPlane);

		return "Send To message queue successful !";

	}

	// for User Hotel Register in data base
	@GetMapping("/user-hotel-post")
	@HystrixCommand(fallbackMethod = "getFallbackUserHotelPost")
	@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "false")
	public String userHotelPost(UserHotel userHotel) {

		UserHotel userHotel2 = new UserHotel();

		RestTemplate restTemplate = restTemplateBuilder.build();

		HttpHeaders headers2 = new HttpHeaders();
		headers2.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		ResponseEntity<String> response2 = null;
		HttpEntity<UserHotel> requestEntity2 = new HttpEntity<>(userHotel, headers2);

		// InstanceInfo info2 =
		// eurakClient.getNextServerFromEureka("HOTELRESEVATION-SERVICE", false);
		InstanceInfo zuulInfo = eurakClient.getNextServerFromEureka("ZUUL-SERVER", false);
		String baseUrl2 = zuulInfo.getHomePageUrl();

		List<String> listInfo2 = new ArrayList<String>();

		try {

			getFallbackUserHotelPost(userHotel);
			response2 = restTemplate.exchange(baseUrl2 + "/hotelReserveion/save-user-hotel", HttpMethod.POST,
					requestEntity2, String.class);

		} catch (Exception e) {
			System.out.println(e);
		}

		if (response2.getBody() == null) {
			return "403";
		} else if (response2.getStatusCode() == HttpStatus.OK) {
			return "index";
		} else {
			return "403";
		}

	}

	@GetMapping("/get-all-user-reserve")
	public String getAllUserReserve(HttpServletRequest request, HttpSession session, Principal principle) {

		HttpSession s = request.getSession();
		Users user = new Users();
		user = (Users) s.getAttribute("userLogin");

		String userName = user.getEmail();
		System.out.println("The userName is .............:" + userName);
		UserHotel userHotel2 = new UserHotel();

		List<String> param = new ArrayList<String>();
		param.add(userName);

		RestTemplate restTemplate = restTemplateBuilder.build();

		HttpHeaders headers2 = new HttpHeaders();
		headers2.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		ResponseEntity<String> response2 = null;
		HttpEntity<List<String>> requestEntity2 = new HttpEntity<>(param, headers2);

		InstanceInfo info2 = eurakClient.getNextServerFromEureka("ZUUL-SERVER", false);
		// InstanceInfo info2 =
		// eurakClient.getNextServerFromEureka("HOTELRESEVATION-SERVICE", false);
		String baseUrl2 = info2.getHomePageUrl();

		UserHotel[] userHotels = new UserHotel[500];
		UserHotel userHotel = new UserHotel();

		List<String> listInfo2 = new ArrayList<String>();

		try {
			// for request
			int res2;
			response2 = restTemplate.exchange(baseUrl2 + "/hotelReserveion/get-all-reserved-user", HttpMethod.POST,
					requestEntity2, String.class);
			userHotels = new Gson().fromJson(response2.getBody(), UserHotel[].class);
			// int answer =Integer.valueOf(response.getBody());
			System.out.println("After getAll UserHotel of user ..............." + response2.getBody());

			System.out.println("The all user hotel size is ............:" + userHotels.length);

		} catch (Exception e) {
			System.out.println(e);
		}

		if (response2.getBody() == null) {
			System.out.println("The User Hotel  not Create ................. :" + response2.getStatusCode());
			return "403";
		} else if (response2.getStatusCode() == HttpStatus.OK) {
			request.setAttribute("reserved", userHotels);
			session.setAttribute("userHotelExport", userHotels);

			return "showalluserreserve";
		} else {
			return "403";
		}

	}

	@RequestMapping("/delete-reserve")
	@HystrixCommand(fallbackMethod = "getFallbackDeleteReserve")
	@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "false")
	public String deletereserve(@RequestParam long id, HttpServletRequest request) {

		RestTemplate restTemplate = restTemplateBuilder.build();

		HttpHeaders headers2 = new HttpHeaders();
		headers2.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		ResponseEntity<String> response2 = null;
		HttpEntity<Long> requestEntity2 = new HttpEntity<>(id, headers2);

		InstanceInfo info2 = eurakClient.getNextServerFromEureka("ZUUL-SERVER", false);
		String baseUrl2 = info2.getHomePageUrl();

		try {
			// for request
			int res2;
			response2 = restTemplate.exchange(baseUrl2 + "/hotelReserveion/canceled-reserved", HttpMethod.POST,
					requestEntity2, String.class);
			res2 = new Gson().fromJson(response2.getBody(), Integer.class);

		} catch (Exception e) {
			System.out.println(e);
		}

		if (response2.getBody() == null) {
			return "403";
		} else if (response2.getStatusCode() == HttpStatus.OK) {
			return "index";
		} else {
			return "403";
		}
	}

	@RequestMapping("/order-hotel-by-rate-des")
	public String orderHotelByRateDes(HttpServletRequest req, HttpServletResponse res, HttpServletRequest request) {

		List<Hotel> hotels = new ArrayList<Hotel>();
		HttpSession s = req.getSession();
		hotels = (List<Hotel>) s.getAttribute("allHotels");

		Collections.sort(hotels, Hotel.HotelRattingComparatorDis);
		request.setAttribute("hotels", hotels);

		return "showallhotel";
	}

	@RequestMapping("/order-hotel-by-rate-pro")
	public String orderHotelByRatePro(HttpServletRequest req, HttpServletResponse res, HttpServletRequest request) {

		List<Hotel> hotels = new ArrayList<Hotel>();
		HttpSession s = req.getSession();
		hotels = (List<Hotel>) s.getAttribute("allHotels");

		Collections.sort(hotels, Hotel.HotelRattingComparatorPro);
		request.setAttribute("hotels", hotels);

		return "showallhotel";
	}

	@RequestMapping("/order-hotel-by-name")
	public String orderHotelByName(HttpServletRequest req, HttpServletResponse res, HttpServletRequest request) {

		List<Hotel> hotels = new ArrayList<Hotel>();
		HttpSession s = req.getSession();
		hotels = (List<Hotel>) s.getAttribute("allHotels");

		Collections.sort(hotels, Hotel.HotelNameComparator);
		request.setAttribute("hotels", hotels);

		return "showallhotel";
	}

	@RequestMapping("/order-hotel-by-price")
	public String orderHotelByPrice(HttpServletRequest req, HttpServletResponse res, HttpServletRequest request) {

		List<Hotel> hotels = new ArrayList<Hotel>();
		HttpSession s = req.getSession();
		hotels = (List<Hotel>) s.getAttribute("allHotels");

		Collections.sort(hotels, Hotel.HotelPriceComparator);
		request.setAttribute("hotels", hotels);

		return "showallhotel";
	}

////////////////////////////////////////////////////////////////////////////// sercute precker

	@HystrixCommand(fallbackMethod = "getFallbackSearchHotelAmadues") /// yes
	@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "false")
	public String getFallbackSearchHotel(@RequestParam String city, HttpServletRequest request, HttpSession session)
			throws ResponseException {
		Amadeus amadeus = Amadeus.builder("JTd1wtkpTSWdGJXdQjd62m3n1eHVIxF4", "GcQ8tPTuF5RnUcn0").build();

		// "PAR"
		// Get list of hotels by city code
		HotelOffer[] offers = amadeus.shopping.hotelOffers.get(Params.with("cityCode", city));

		if (offers[0].getResponse().getStatusCode() != 200) {
			System.out.println("Wrong status code: " + offers[0].getResponse().getStatusCode());
			System.exit(-1);
		}

		List<Hotel> hotels = new ArrayList<Hotel>();
		;
		System.out.println("88888888888888888 : " + offers[0].getHotel().getHotelDistance().getDistance());
		Offer[] offersHotel = offers[0].getOffers();
		System.out.println("88888888888888888 : " + offersHotel[0]);
		System.out.println(";;;;;;;;;;;;;;;;;;;;;;; ababababa " + offers.length);
		int c = 0;
		for (int j = 0; j < offers.length; j++) {

			Hotel hotelObj = new Hotel();
			Contact contactObj = new Contact();
			AddressHotel addressObj = new AddressHotel();

			MediaURI[] mediaList = offers[j].getHotel().getMedia();

			addressObj.setAddress(offers[j].getHotel().getAddress().getCityName());
			addressObj.setCountryCode(offers[j].getHotel().getAddress().getCountryCode());

			contactObj.setFax(offers[j].getHotel().getContact().getFax());
			contactObj.setPhone(offers[j].getHotel().getContact().getPhone());
			contactObj.setEmail("www.nero98@gmail.com"); // *
			hotelObj.setType(offers[j].getHotel().getType());
			hotelObj.setName(offers[j].getHotel().getName());
			System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&" + offers[j].getHotel().getName());
			hotelObj.setRatting(offers[j].getHotel().getRating()); // *
			hotelObj.setCityCode(offers[j].getHotel().getCityCode());
			hotelObj.setLatitude((float) offers[j].getHotel().getLatitude());
			hotelObj.setLongtitude((float) offers[j].getHotel().getLongitude());
			hotelObj.setAddress(addressObj);
			hotelObj.setContact(contactObj);
			hotelObj.setDescription(offers[0].getHotel().getDescription().getText()); // here there is probleme
			String[] amenitiesList = new String[20];
			amenitiesList = offers[j].getHotel().getAmenities();
			for (int z = 0; z < amenitiesList.length; z++) {
				hotelObj.setAmenitiesIndex(amenitiesList[z]);
			}
			if (mediaList.length != 0) {
				for (int i = 0; i < mediaList.length; i++) {
					hotelObj.setMediaIndex(mediaList[i].getUri()); /// mistak
				}
			}
			hotelObj.setAvalibale(offers[j].isAvailable());
			System.out.println("/////////////////]]]]]]]]]]]]]]]]]]" + hotelObj.toString());
			System.out.println("++++++++++++++" + hotelObj.getName());
			hotels.add(hotelObj);

		}
		System.out.println("size: " + hotels.size());

		for (int i = 0; i < hotels.size(); i++) {
			System.out.println("LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL!!!!!!!!!!!!!!!!!!" + hotels.get(i));
		}

		request.setAttribute("hotels", hotels);
		return "showallhotel";

	}

	public String getFallbackSearchHotelAmadues(@RequestParam String city, HttpServletRequest request,
			HttpSession session) {

		try {

			List<Hotel> hotels = new ArrayList<Hotel>();

			session = request.getSession();

			hotels = (List<Hotel>) session.getAttribute("allHotels");

			if (hotels == null) {
				return "error6";
			} else {
				request.setAttribute("hotels", hotels);
				return "showallhotel";
			}

		} catch (Exception e) {
			return "error6";
		}

	}

	@HystrixProperty(name = "hystrix.command.default.execution.timeout.enabled", value = "false")
	@ResponseBody
	public String getFallbackDeleteReserve(@RequestParam long id, HttpServletRequest request) {

		String queueName = "userRequestDelete";
		String exchange = "direct-exchange";
		String routingKey = "userDelete";
		String s = String.valueOf(id);

		return "error6";

	}

	public String getFallbackShowHotelInfo(@RequestParam int id, HttpServletRequest request, HttpSession session) {

		try {

			session = request.getSession();

			// Hotel[] hotels = new Hotel[500];

			List<Hotel> hotels = new ArrayList<Hotel>();

			Hotel hotel = new Hotel();

			hotels = (List<Hotel>) session.getAttribute("allHotels");

			if (hotels == null) {
				return "error6";
			} else {

				for (int i = 0; i < hotels.size(); i++) {
					if (hotels.get(i).getid() == id) {
						hotel = hotels.get(i);

						Set<SingleRoom> singleroom = new HashSet<SingleRoom>();
						singleroom = hotel.getSingleRoomList();
						Iterator<SingleRoom> it = singleroom.iterator();
						SingleRoom sroom = it.next();
						System.out.println("ssssssssssssssssssssssssssss" + sroom.toString());

					}
				}

				System.out.println("heeeeeeeeeeeeerrrr" + id + "" + hotel);
				request.setAttribute("hotel", hotel);
				return "showHotelInfo";

			}

		} catch (Exception e) {
			return "error6";
		}

	}

	public String getFallbackUserHotelPost(UserHotel userHotel) {

		String queueName = "userHotelPostQueue";
		String exchange = "direct-exchange";
		String routingKey = "userHotelPost";

		amqpTemplate.convertAndSend(exchange, routingKey, userHotel);
		return "error6";

	}

	public String getFallGetCheckBankInfo(@RequestParam String price, @RequestParam int hotel_id,
			@RequestParam int roomId, @RequestParam String roomType, @RequestParam String useremail,
			@RequestParam long accountId, @RequestParam String username, @RequestParam String password,
			@RequestParam String book, @RequestParam String leave, @RequestParam String city,
			HttpServletRequest request, HttpSession s) throws IOException, ParseException {

		SimpleDateFormat formatter2 = new SimpleDateFormat("MM/dd/yyyy");
		Date date1 = formatter2.parse(book);
		Date date2 = formatter2.parse(leave);

		Calendar cal = Calendar.getInstance();
		cal.setTime(date1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);

		int dayOfMonth1 = cal.get(Calendar.DAY_OF_MONTH); // 17

		int dayOfMonth2 = cal2.get(Calendar.DAY_OF_MONTH);

		System.out.println("The dayOfMonth1 is ....... :" + dayOfMonth1);

		System.out.println("The dayOfMonth2 is ....... :" + dayOfMonth2);

		System.out.println("The Date1 day is .........................:" + dayOfMonth1);
		System.out.println("The Date2 day is .........................:" + dayOfMonth2);
		int num = dayOfMonth2 - dayOfMonth1;

		float p = Float.parseFloat(price);

		float totalPrice = Math.abs(p * num);

		UserHotel userHotel = new UserHotel();
		userHotel.setUserName(useremail);
		userHotel.setHotelId(hotel_id);
		userHotel.setRoomId(roomId);
		userHotel.setTotalCost(totalPrice);
		userHotel.setStartdate(date1);
		userHotel.setEndDate(date2);
		userHotel.setRoomType(roomType);
		userHotel.setAvalible(1);
		userHotel.setAccountId(accountId);
		userHotel.setCityName(city);

		String queueName = "userHotelPostQueue";
		String exchange = "direct-exchange";
		String routingKey = "userHotelPost";

		System.out.println("sadasdsadasdas");

		amqpTemplate.convertAndSend(exchange, routingKey, userHotel);

		// rabbitMQSender.sendUserHotelPost(userHotel);
		return "error6";

	}

///////////////                 The Header Request					///////////////

	public static HttpEntity<?> getHeaders() throws IOException { // here I put the properties of the header
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		// headers.set("Accept",MediaType.APPLICATION_JSON_VALUE);
		return new HttpEntity<>(headers);
	}

}
