package job.roomManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

enum requestFields{
	EconomyRoomsNumber("EconomyRoomsNumber"),
	PremiumRoomsNumber("PremiumRoomsNumber"),
	CustomersPrices("CustomersPrices");

	private String text;
	requestFields(String text) {
		this.text = text;
	}
	public String text(){
		return text;
	};
}
enum ResponseFields{
	PremiumRoomsPrices("PremiumRoomsPrices"),
	EconomyRoomsPrices("EconomyRoomsPrices"),
	RejectedPrices("RejectedPrices"),
	TotalIncome("TotalIncome"),
	ErrorMsg("ErrorMsg");

	private String text;
	ResponseFields(String text) {
		this.text = text;
	}
	public String text(){
		return text;
	};
}

@RequestMapping
@RestController
public class Controller {
	@PostMapping(value = "/assignRooms")
	public ResponseEntity<String> assignRoomsMapping(@RequestBody String requestJsonBodyStr){
		String errorStr = getErrorInputString(requestJsonBodyStr);
		if(errorStr != null){
			JSONObject badRequestBody = new JSONObject();
			badRequestBody.put(ResponseFields.ErrorMsg.text(), errorStr);
			ResponseEntity<String> badResponse = new ResponseEntity<String>(badRequestBody.toString(), HttpStatus.BAD_REQUEST);
			return badResponse;
		}

		JSONObject inputJson = new JSONObject(requestJsonBodyStr);
		JSONArray prices = inputJson.getJSONArray(requestFields.CustomersPrices.text());
		List<Double> pricesArray = pricesArrayToDoubleList(prices);
		int premiumRoomsNumber = inputJson.getInt(requestFields.PremiumRoomsNumber.text());
		int economyRoomsNumber = inputJson.getInt(requestFields.EconomyRoomsNumber.text());
		JSONObject assignedRoomsJson = assignRooms(economyRoomsNumber, premiumRoomsNumber, pricesArray);

		ResponseEntity<String> response =new ResponseEntity<>(assignedRoomsJson.toString(), HttpStatus.OK) ;
		return response;
	}

	private String getErrorInputString (String input){
		try {
			JSONObject inputObj = new JSONObject(input);

			if(!inputObj.has(requestFields.CustomersPrices.text()))
				throw new IllegalArgumentException("No " + requestFields.CustomersPrices.text() + " field");
			JSONArray prices = inputObj.getJSONArray(requestFields.CustomersPrices.text());
			if(prices.length() == 0)
				throw new IllegalArgumentException(requestFields.CustomersPrices.text() + " field is empty.");
			if(pricesArrayToDoubleList(prices) == null)
				throw new IllegalArgumentException(requestFields.CustomersPrices.text() + " has a object that is not a number.");;


			if(!inputObj.has(requestFields.EconomyRoomsNumber.text()))
				throw new IllegalArgumentException("No " + requestFields.EconomyRoomsNumber.text() + " field");
			int economyRoomsNumber = inputObj.getInt(requestFields.EconomyRoomsNumber.text());
			if(economyRoomsNumber < 0)
				throw new IllegalArgumentException(requestFields.EconomyRoomsNumber.text() + " field is less than zero.");


			if(!inputObj.has(requestFields.PremiumRoomsNumber.text()))
				throw new IllegalArgumentException("No " + requestFields.PremiumRoomsNumber.text() + " field.");
			int premiumRoomsNumber = inputObj.getInt(requestFields.PremiumRoomsNumber.text());
			if(premiumRoomsNumber < 0)
				throw new IllegalArgumentException(requestFields.PremiumRoomsNumber.text() + " field is less than zero.");

			return null;
		}catch (Exception e){
			return e.getMessage();
		}
	}
	private JSONObject assignRooms(int economyRoomsNumber, int premiumRoomsNumber, List<Double> listOfPrices){
		Collections.sort(listOfPrices);

		JSONArray pricesForPremiumRooms = new JSONArray();
		JSONArray pricesForEconomyRooms = new JSONArray();
		JSONArray rejectedPrices = new JSONArray();

		final int minimalPriceForPremiumRoom = 100;
		for(int i = listOfPrices.size() - 1; i >= 0; i--){
			double currentPrice = listOfPrices.get(i);
			boolean premiumRoomAvailable = pricesForPremiumRooms.length() < premiumRoomsNumber;
			boolean economyRoomAvailable = pricesForEconomyRooms.length() < economyRoomsNumber;
			boolean customerPayEnough = currentPrice >= minimalPriceForPremiumRoom;

			if(customerPayEnough){
				if(premiumRoomAvailable)
					pricesForPremiumRooms.put(currentPrice);
				else
					rejectedPrices.put(currentPrice);
			}
			else{
				boolean allEconomyRoomsWillBeOccupied = (i + 1) > economyRoomsNumber;
				if(premiumRoomAvailable && allEconomyRoomsWillBeOccupied)
					pricesForPremiumRooms.put(currentPrice);
				else if(economyRoomAvailable)
					pricesForEconomyRooms.put(currentPrice);
				else
					rejectedPrices.put(currentPrice);
			}
		}
		return constructReturnJsonObj(pricesForPremiumRooms, pricesForEconomyRooms, rejectedPrices);
	}
	private List<Double> pricesArrayToDoubleList(JSONArray array){
		try {
			List<Double> listOfCustomerPrices = new ArrayList<>();
			array.forEach(it -> {
				Double toAdd = new Double(it.toString());
				listOfCustomerPrices.add(toAdd);
			});
			return listOfCustomerPrices;
		}
		catch (Exception e){
			return null;
		}
	}
	private JSONObject constructReturnJsonObj(JSONArray pricesForPremiumRooms, JSONArray pricesForEconomyRooms, JSONArray rejectedPrices){
		double totalIncome = 0.0;
		for (Object price : pricesForPremiumRooms) {
			totalIncome += (double) price;
		}
		for (Object price : pricesForEconomyRooms) {
			totalIncome += (double) price;
		}

		JSONObject jsonToReturnToClient = new JSONObject();
		jsonToReturnToClient.put(ResponseFields.TotalIncome.text(), totalIncome);
		jsonToReturnToClient.put(ResponseFields.PremiumRoomsPrices.text(), pricesForPremiumRooms);
		jsonToReturnToClient.put(ResponseFields.EconomyRoomsPrices.text(), pricesForEconomyRooms);
		jsonToReturnToClient.put(ResponseFields.RejectedPrices.text(), rejectedPrices);
		return jsonToReturnToClient;
	}
}

