package it.unitn.APCM.ACME.ServerCommon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;

/**
 * Class used to convert JSON Array to Array
 * The class represent an ArrayList
 */
public class JSONToArray extends ArrayList<String> {

	/**
	 * Instantiates a new Json to array.
	 *
	 * @param str the string representing the JSON list
	 * @throws JsonProcessingException the json processing exception
	 */
	public JSONToArray(String str) throws JsonProcessingException {
		if (str != null) {
			// Convert JSON object to ArrayList of strings
			ObjectMapper objectMapper = new ObjectMapper();
			TypeReference<ArrayList<String>> typeReference = new TypeReference<>() {};
			try {
				// Add all the elements of the object to the arraylist
				this.addAll(objectMapper.readValue(str, typeReference));
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		} else {
			this.clear();
		}
	}
}
