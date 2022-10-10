package job.roomManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

class InfoFromResponse{
	Double totalIncome;
	int occupiedRoomsPremium;
	int occupiedRoomsEconomy;
}

class ControllerTest {
	//In task description is said "how much money they will make in total".
	// I assumed that I should test only amount of sum from premium and economy rooms, not each of them separately.

	Controller controller = new Controller();

	InfoFromResponse getNeededDataFromServer(int freeEconomyRoomsNumber, int freePremiumRoomsNumber){
		final JSONArray prices = new JSONArray("[23, 45, 155, 374, 22, 99.99, 100, 101, 115, 209]");
		JSONObject request = new JSONObject();
		request.put(requestFields.EconomyRoomsNumber.text(), freeEconomyRoomsNumber);
		request.put(requestFields.PremiumRoomsNumber.text(), freePremiumRoomsNumber);
		request.put(requestFields.CustomersPrices.text(), prices);
		ResponseEntity<String> responseFromServer = controller.assignRoomsMapping(request.toString());
		JSONObject responseBody = new JSONObject(responseFromServer.getBody());

		InfoFromResponse data = new InfoFromResponse();
		data.totalIncome = responseBody.getDouble(ResponseFields.TotalIncome.text());
		data.occupiedRoomsPremium = responseBody.getJSONArray(ResponseFields.PremiumRoomsPrices.text()).length();
		data.occupiedRoomsEconomy = responseBody.getJSONArray(ResponseFields.EconomyRoomsPrices.text()).length();
		return data;
	}

	@Test
	void firstTaskFromPdf(){
		InfoFromResponse data = getNeededDataFromServer(3,3);
		assertEquals(738 + 167.99, data.totalIncome);
		assertEquals(3, data.occupiedRoomsPremium);
		assertEquals(3, data.occupiedRoomsEconomy);
	}

	@Test
	void secondTaskFromPdf(){
		InfoFromResponse data = getNeededDataFromServer(5,7);
		assertEquals(1054 + 189.99, data.totalIncome);
		assertEquals(6, data.occupiedRoomsPremium);
		assertEquals(4, data.occupiedRoomsEconomy);
	}

	@Test
	void thirdTaskFromPdf(){
		InfoFromResponse data = getNeededDataFromServer(7,2);
		assertEquals(583 + 189.99, data.totalIncome);
		assertEquals(2, data.occupiedRoomsPremium);
		assertEquals(4, data.occupiedRoomsEconomy);
	}

	@Test
	void fourthTaskFromPdf(){
		InfoFromResponse data = getNeededDataFromServer(1,7);
		assertEquals(1153 + 45.99, data.totalIncome);
		assertEquals(7, data.occupiedRoomsPremium);
		assertEquals(1, data.occupiedRoomsEconomy);
	}
}