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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Main extends Application {
	
	private static final String API_KEY = "8c84dfaa047c440fa17328a67878917e";
	private static final String API_DETECT_URL = "https://westus.api.cognitive.microsoft.com/face/v1.0/detect";
	private static final String APP_TITLE = "FRAS v1";
	private static final String APP_SELECT_IMG = "Select a face";
	private static final String APP_VERIFY_BUTTON = "Verify";
	
	private static final File DEFAULT_DIR = new File(System.getProperty("user.dir"));
	private static final FileChooser.ExtensionFilter[] EXT_FILTERS = new FileChooser.ExtensionFilter[] {
			new FileChooser.ExtensionFilter("JPG", "*.jpg"),
			new FileChooser.ExtensionFilter("PNG", "*.png")
			};
	
	private static final double APP_MIN_HEIGHT = 800;
	private static final double APP_MIN_WIDTH = 1000;
	private static final double APP_IMAGE_SIZE = 400;

	Stage stage;
	File image1File, image2File;
	
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
		layout.setGridLinesVisible(true);
		layout.setHgap(20);
		layout.setVgap(20);
		layout.setPadding(new Insets(10, 10, 10, 10));
		Scene scene = new Scene(layout, APP_MIN_WIDTH, APP_MIN_HEIGHT);

		// gui objects
		TextField image1Path = new TextField(),
				image2Path = new TextField();
		ImageView image1View = new ImageView(),
				image2View = new ImageView();
		Button verifyButton = new Button();

		// object properties
		image1Path.setEditable(false);
		image1Path.setOnMouseClicked(event -> {
			image1File = getImageFile();
			if (image1File != null) {
				updateImageObjects(image1File, image1Path, image1View);
			}
		});
		GridPane.setConstraints(image1Path, 0, 0, 1, 1, HPos.CENTER, VPos.BOTTOM);

		//image1View.setFitWidth(APP_IMAGE_SIZE);
		GridPane.setConstraints(image1View, 0, 1, 1, 1, HPos.CENTER, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS);

		image2Path.setEditable(false);
		image2Path.setOnMouseClicked(event -> {
			image2File = getImageFile();
			if (image2File != null) {
				updateImageObjects(image2File, image2Path, image2View);
			}
		});
		GridPane.setConstraints(image2Path, 1, 0, 1, 1, HPos.CENTER, VPos.BOTTOM);

		//image2View.setFitWidth(APP_IMAGE_SIZE);
		GridPane.setConstraints(image2View, 1, 1, 1, 1, HPos.CENTER, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS);

		verifyButton.setText(APP_VERIFY_BUTTON);
		verifyButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				verifyImages();
			}
		});
		GridPane.setConstraints(verifyButton, 0, 2, 2, 1, HPos.CENTER, VPos.CENTER);
		
		// show window
		layout.getChildren().addAll(image1Path, image1View, image2Path, image2View, verifyButton);
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
		// detect images
		HttpClient client = HttpClients.createDefault();
		try {
			// API url
			URIBuilder builder = new URIBuilder(API_DETECT_URL);
			builder.setParameter("returnFaceId", "true");
			URI uri = builder.build();
			
			// byte-stream of file
			System.out.println("bytes " + Paths.get(image1File.getPath()));
			byte[] img = Files.readAllBytes(Paths.get(image1File.getAbsolutePath()));
			System.out.println("entity");
			ByteArrayEntity entity = new ByteArrayEntity(img, ContentType.APPLICATION_OCTET_STREAM);
			
			// POST request
			HttpPost request = new HttpPost(uri);
			request.setHeader("Ocp-Apim-Subscription-Key", API_KEY);
			request.setHeader("Content-Type", "application/octet-stream");
			request.setEntity(entity);
			
			// response
			HttpResponse response = client.execute(request);
			HttpEntity result = response.getEntity();
			
			System.out.println("got");
			System.out.println(EntityUtils.toString(result));
		} catch (Exception e) {
			
		}
	}
}
