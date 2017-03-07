package edu.uoregon.cis433;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main extends Application {
	private static final String API_KEY = "8c84dfaa047c440fa17328a67878917e";
	private static final String API_URL = "https://westus.api.cognitive.microsoft.com/face/v1.0/";

	private static final String APP_TITLE = "FRAS v1";
	private static final String APP_SELECT_IMG = "Select a face...";
	private static final String APP_VERIFY_BUTTON = "Verify";
	private static final String APP_NO_IMG = "Please select an image";

	private static final File DEFAULT_DIR = new File(System.getProperty("user.dir"));
	private static final FileChooser.ExtensionFilter[] EXT_FILTERS = new FileChooser.ExtensionFilter[] {
			new FileChooser.ExtensionFilter("JPG", "*.jpg"),
			new FileChooser.ExtensionFilter("PNG", "*.png")
	};

	private static final double APP_MIN_HEIGHT = 600;
	private static final double APP_MIN_WIDTH = 800;
	private static final double APP_IMAGE_SIZE = 350;

	// stage and images
	Stage stage;
	File image1File, image2File;
	Label image1Label, image2Label, resultLabel;

	public static void main(String[] args) {
		launch(args);
	}

	public void start(Stage stage) throws Exception {
		// stage
		this.stage = stage;
		stage.setTitle(APP_TITLE);
		stage.setMinHeight(APP_MIN_HEIGHT);
		stage.setMinWidth(APP_MIN_WIDTH);

		// layout
		GridPane layout = new GridPane();
		//layout.setGridLinesVisible(true);
		layout.setHgap(20);
		layout.setVgap(20);
		layout.setPadding(new Insets(10, 10, 10, 10));
		Scene scene = new Scene(layout);

		// gui objects
		TextField image1Path = new TextField(),
				image2Path = new TextField();
		ImageView image1View = new ImageView(),
				image2View = new ImageView();
		image1Label = new Label();
		image2Label = new Label();
		resultLabel = new Label();
		Button verifyButton = new Button();

		// object properties
		image1Path.setText(APP_SELECT_IMG);
		image1Path.setEditable(false);
		image1Path.setOnMouseClicked(event -> {
			image1File = getImageFile();
			if (image1File != null) {
				updateImageObjects(image1File, image1Path, image1View);
			}
		});
		GridPane.setConstraints(image1Path, 0, 0, 1, 1, HPos.CENTER, VPos.BOTTOM);

		GridPane.setConstraints(image1View, 0, 1, 1, 1, HPos.CENTER, VPos.TOP);

		resultLabel.setFont(new Font(18));
		image1Label.setStyle("-fx-background-color: #FFFFFF");
		GridPane.setConstraints(image1Label, 0, 2, 1, 1, HPos.CENTER, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS);
		
		image2Path.setText(APP_SELECT_IMG);
		image2Path.setEditable(false);
		image2Path.setOnMouseClicked(event -> {
			image2File = getImageFile();
			if (image2File != null) {
				updateImageObjects(image2File, image2Path, image2View);
			}
		});
		GridPane.setConstraints(image2Path, 1, 0, 1, 1, HPos.CENTER, VPos.BOTTOM);

		GridPane.setConstraints(image2View, 1, 1, 1, 1, HPos.CENTER, VPos.TOP);

		resultLabel.setFont(new Font(18));
		image2Label.setStyle("-fx-background-color: #FFFFFF");
		GridPane.setConstraints(image2Label, 1, 2, 1, 1, HPos.CENTER, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS);
		
		verifyButton.setText(APP_VERIFY_BUTTON);
		verifyButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				if (image1File == null || image2File == null) {
					image1Label.setText(APP_NO_IMG);
					image2Label.setText(APP_NO_IMG);
					return;
				}
				verifyImages();
			}
		});
		GridPane.setConstraints(verifyButton, 0, 3, 2, 1, HPos.CENTER, VPos.CENTER);

		resultLabel.setFont(new Font(24));
		resultLabel.setStyle("-fx-background-color: #FFFFFF");
		GridPane.setConstraints(resultLabel, 0, 4, 2, 1, HPos.CENTER, VPos.CENTER);
		
		// display window
		ColumnConstraints col1 = new ColumnConstraints(),
				col2 = new ColumnConstraints();
		col1.setPercentWidth(50);
		col2.setPercentWidth(50);
		layout.getColumnConstraints().addAll(col1, col2);
		layout.getChildren().addAll(image1Path, image1View, image1Label, image2Path, image2View, image2Label, verifyButton, resultLabel);
		stage.setScene(scene);
		//stage.sizeToScene();
		stage.show();
	}

	public File getImageFile() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(APP_SELECT_IMG);
		chooser.setInitialDirectory(DEFAULT_DIR);
		chooser.getExtensionFilters().addAll(EXT_FILTERS);
		return chooser.showOpenDialog(stage);
	}

	public void updateImageObjects(File file, TextField tf, ImageView iv) {
		tf.setText(file.getAbsolutePath());
		Image image = new Image(file.toURI().toString(), APP_IMAGE_SIZE, APP_IMAGE_SIZE, true, true, true);
		iv.setImage(image);
	}

	public void verifyImages() {
		// http session
		HttpClient client = HttpClients.createDefault();
		JsonParser parser = new JsonParser();
		try {
			// detect URL
			URIBuilder baseUrl = new URIBuilder(API_URL + "detect");
			baseUrl.setParameter("returnFaceId", "true");
			URI url = baseUrl.build();

			// image1
			byte[] bytes = Files.readAllBytes(Paths.get(image1File.getAbsolutePath()));
			ByteArrayEntity requestBody = new ByteArrayEntity(bytes, ContentType.APPLICATION_OCTET_STREAM);
			// POST request
			HttpPost request = new HttpPost(url);
			request.setHeader("Ocp-Apim-Subscription-Key", API_KEY);
			request.setHeader("Content-Type", "application/octet-stream");
			request.setEntity(requestBody);
			// response
			HttpResponse response = client.execute(request);
			String responseJson = EntityUtils.toString(response.getEntity());
			// faceid
			String image1FaceId;
			JsonObject result = parser.parse(responseJson).getAsJsonArray().get(0).getAsJsonObject();
			if (result.has("faceId")) {
				image1FaceId = result.get("faceId").getAsString();
				image1Label.setText("FaceID: " + image1FaceId);
			} else {
				image1Label.setText("Error: " + result.get("message").getAsString());
				return;
			}
			
			// image2
			bytes = Files.readAllBytes(Paths.get(image2File.getAbsolutePath()));
			requestBody = new ByteArrayEntity(bytes, ContentType.APPLICATION_OCTET_STREAM);
			// POST request
			request = new HttpPost(url);
			request.setHeader("Ocp-Apim-Subscription-Key", API_KEY);
			request.setHeader("Content-Type", "application/octet-stream");
			request.setEntity(requestBody);
			// response
			response = client.execute(request);
			responseJson = EntityUtils.toString(response.getEntity());
			// faceid
			String image2FaceId;
			result = parser.parse(responseJson).getAsJsonArray().get(0).getAsJsonObject();
			if (result.has("faceId")) {
				image2FaceId = result.get("faceId").getAsString();
				image2Label.setText("FaceID: " + image2FaceId);
			} else {
				image2Label.setText("Error: " + result.get("message").getAsString());
				return;
			}
			
			// verify URL
			baseUrl = new URIBuilder(API_URL + "verify");
			baseUrl.setParameter("returnFaceId", "true");
			url = baseUrl.build();
			// faceids
			StringEntity faceIds = new StringEntity("{"
					+ "\"faceId1\":\"" + image1FaceId + "\","
					+ "\"faceId2\":\"" + image2FaceId + "\"}");
			// POST request
			request = new HttpPost(url);
			request.setHeader("Ocp-Apim-Subscription-Key", API_KEY);
			request.setHeader("Content-Type", "application/json");
			request.setEntity(faceIds);
			// response
			response = client.execute(request);
			responseJson = EntityUtils.toString(response.getEntity());
			// result
			result = parser.parse(responseJson).getAsJsonObject();
			String resultString;
			if (result.get("isIdentical").getAsBoolean()) {
				resultString = "The faces are the same";
			} else {
				resultString = "The faces are not the same";
			}
			int c = (int) (result.get("confidence").getAsDouble() * 100);
			resultString += " (" + c + "% confidence).";
			resultLabel.setText(resultString);
			System.out.println(responseJson);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
