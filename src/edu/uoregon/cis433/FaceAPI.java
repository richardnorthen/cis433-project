package edu.uoregon.cis433;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

public class FaceAPI {
	// api details
	private static final String API_KEY = "8c84dfaa047c440fa17328a67878917e";
	private static final String API_URL = "https://westus.api.cognitive.microsoft.com/face/v1.0/";

	// http client and gson deserializer
	HttpClient client;
	Gson gson;

	/**
	 * Creates an instance of FaceAPI to use Microsoft's Cognitive Servers
	 */
	public FaceAPI() {
		// initialize client and deserializer
		client = HttpClients.createDefault();
		gson = new Gson();
	}
	
	/**
	 * Class for deserializing the detect response
	 */
	public class DetectResponse {
		public DetectResponse() {}
		String faceId;
		FaceAttributes faceAttributes;
		
		public class FaceAttributes {
			public FaceAttributes() {}
			double age, smile;
			String gender, glasses;
		}
		// neat printing
		public String asString() {
			return "\tFaceID:\t" + faceId
					+ "\n\tAge:\t" + faceAttributes.age
					+ "\n\tGender:\t" + faceAttributes.gender
					+ "\n\tSmile:\t" + faceAttributes.smile
					+ "\n\tGlasses:" + faceAttributes.glasses;
		}
	}

	/**
	 * Takes an encoded image and uses FaceAPI to detect faces
	 * @param imageBytes The encoded image
	 * @return An array of DetectResponse with details of each face
	 */
	public DetectResponse[] detect(byte[] imageBytes) {
		try {
			// build url
			URIBuilder baseUrl = new URIBuilder(API_URL + "detect");
			baseUrl.setParameter("returnFaceId", "true");
			baseUrl.setParameter("returnFaceAttributes", "age,gender,smile,glasses");
			URI url = baseUrl.build();

			// build request
			ByteArrayEntity requestBody = new ByteArrayEntity(imageBytes, ContentType.APPLICATION_OCTET_STREAM);
			HttpPost request = new HttpPost(url);
			request.setHeader("Ocp-Apim-Subscription-Key", API_KEY);
			request.setHeader("Content-Type", "application/octet-stream");
			request.setEntity(requestBody);

			// get response
			HttpResponse response = client.execute(request);
			// check if request was successful
			if (response.getStatusLine().getStatusCode() == 200) {
				// deserialize and return
				String responseJson = EntityUtils.toString(response.getEntity());
				return gson.fromJson(responseJson, DetectResponse[].class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Class for deserializing the verify response
	 */
	public class VerifyResponse {
		public VerifyResponse() {}
		boolean isIdentical;
		double confidence;
	}
	
	/**
	 * Takes two FaceIDs and uses FaceAPI to compare them
	 * @param image1FaceId FaceID of the first image
	 * @param image2FaceId FaceID of the second image
	 * @return Whether the faces match and the confidence
	 */
	public VerifyResponse verify(String image1FaceId, String image2FaceId) {
		try {
			// build url
			URIBuilder baseUrl = new URIBuilder(API_URL + "verify");
			URI url = baseUrl.build();

			// build request
			StringEntity requestBody = new StringEntity("{"
					+ "\"faceId1\":\"" + image1FaceId + "\","
					+ "\"faceId2\":\"" + image2FaceId + "\"}");
			HttpPost request = new HttpPost(url);
			request.setHeader("Ocp-Apim-Subscription-Key", API_KEY);
			request.setHeader("Content-Type", "application/json");
			request.setEntity(requestBody);

			// get response
			HttpResponse response = client.execute(request);
			// check if response was successful
			if (response.getStatusLine().getStatusCode() == 200) {
				// deserialize and return
				String responseJson = EntityUtils.toString(response.getEntity());
				return gson.fromJson(responseJson, VerifyResponse.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
