package edu.uoregon.cis433;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.uoregon.cis433.FaceAPI.DetectResponse;
import edu.uoregon.cis433.FaceAPI.VerifyResponse;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Main extends Application {
	// important variables
	private static final String APP_TITLE = "FRAS v2";
	private static final double APP_MIN_H = 750;
	private static final double APP_MIN_W = 1000;
	private static final double APP_IMG_SIZE = 400;

	private static final String APP_DEF_IMG_URI = "file:face.jpg";
	private static final String APP_DEF_IMG_TEXT = "Select a face...";

	private static final String APP_BTN_VERIFY = "Verify";
	private static final double APP_BTN_VERIFY_H = 36;
	private static final double APP_BTN_VERIFY_W = 72;

	private static final String APP_BTN_CAMERA = "CAM";
	private static final String APP_BTN_DIR = "...";
	private static final double APP_BTN_SMALL_H = 28;
	private static final double APP_BTN_SMALL_W = 46;

	private static final File DEFAULT_DIR = new File(System.getProperty("user.dir"));
	private static final FileChooser.ExtensionFilter[] EXT_FILTERS = new FileChooser.ExtensionFilter[] {
			new FileChooser.ExtensionFilter("JPG", "*.jpg"),
			new FileChooser.ExtensionFilter("PNG", "*.png")
	};

	private static final Format dateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

	// declare stage, image files, and output area
	Stage stage;
	File image1File, image2File;
	TextArea outputTextArea;

	/**
	 * Starts the program and launches the application
	 * @param args unused
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Overrides start to build, set, and display the application
	 * @param stage
	 * @throws Exception
	 */
	@Override
	public void start(Stage stage) throws Exception {
		// set stage
		this.stage = stage;
		stage.setTitle(APP_TITLE);
		stage.setMinHeight(APP_MIN_H);
		stage.setMinWidth(APP_MIN_W);

		// set layout
		GridPane layout = new GridPane();
		//layout.setGridLinesVisible(true);
		layout.setHgap(10);
		layout.setVgap(10);
		layout.setPadding(new Insets(5, 10, 10, 10));
		Scene scene = new Scene(layout);

		// declare gui objects
		Button image1CamBtn = new Button();
		Button image2CamBtn = new Button();
		Button image1DirBtn = new Button();
		Button image2DirBtn = new Button();
		Button verifyBtn = new Button();
		TextField image1Path = new TextField();
		TextField image2Path = new TextField();
		ImageView image1View = new ImageView();
		ImageView image2View = new ImageView();
		outputTextArea = new TextArea();

		// image 1 object properties
		image1CamBtn.setText(APP_BTN_CAMERA);
		image1CamBtn.setPrefSize(APP_BTN_SMALL_W, APP_BTN_SMALL_H);
		image1CamBtn.setOnAction(event -> {
			// TODO take webcam picture
		});
		GridPane.setConstraints(image1CamBtn, 0, 0);

		image1Path.setEditable(false);
		GridPane.setConstraints(image1Path, 1, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);

		image1DirBtn.setText(APP_BTN_DIR);
		image1DirBtn.setPrefSize(APP_BTN_SMALL_W, APP_BTN_SMALL_H);
		image1DirBtn.setOnAction(event -> {
			File selectedFile = getImageFile();
			if (selectedFile != null) {
				image1File = selectedFile;
				updateImageObjects(image1File, image1Path, image1View);
			}
		});
		GridPane.setConstraints(image1DirBtn, 2, 0);

		image1View.setImage(new Image(APP_DEF_IMG_URI, APP_IMG_SIZE, APP_IMG_SIZE, true, true, false));
		GridPane.setConstraints(image1View, 0, 1, 3, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);
		
		// image 2 object properties
		image2CamBtn.setText(APP_BTN_CAMERA);
		image2CamBtn.setPrefSize(APP_BTN_SMALL_W, APP_BTN_SMALL_H);
		image2CamBtn.setOnAction(event -> {
			// TODO take webcam picture
		});
		GridPane.setConstraints(image2CamBtn, 3, 0);

		image2Path.setEditable(false);
		GridPane.setConstraints(image2Path, 4, 0, 1, 1, HPos.CENTER, VPos.CENTER, Priority.ALWAYS, Priority.NEVER);

		image2DirBtn.setText(APP_BTN_DIR);
		image2DirBtn.setPrefSize(APP_BTN_SMALL_W, APP_BTN_SMALL_H);
		image2DirBtn.setOnAction(event -> {
			File selectedFile = getImageFile();
			if (selectedFile != null) {
				image2File = selectedFile;
				updateImageObjects(image2File, image2Path, image2View);
			}
		});
		GridPane.setConstraints(image2DirBtn, 5, 0);

		image2View.setImage(new Image(APP_DEF_IMG_URI, APP_IMG_SIZE, APP_IMG_SIZE, true, true, false));
		GridPane.setConstraints(image2View, 3, 1, 3, 1, HPos.RIGHT, VPos.TOP, Priority.ALWAYS, Priority.NEVER);

		// verify button and output area properties
		verifyBtn.setText(APP_BTN_VERIFY);
		verifyBtn.setPrefSize(APP_BTN_VERIFY_W, APP_BTN_VERIFY_H);
		verifyBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				if (image1File == null || image2File == null) {
					log("ERROR: no image selected");
					return;
				}
				Task<Void> task = new Task<Void>() {
					@Override public Void call() {
						verifyImages();
						return null;
					}
				};
				new Thread(task).start();
			}
		});
		GridPane.setConstraints(verifyBtn, 0, 2, 6, 1, HPos.CENTER, VPos.TOP);

		outputTextArea.setText("[" + dateFormat.format(new Date()) + "] Application started");
		outputTextArea.setEditable(false);
		outputTextArea.setStyle("-fx-font-family:monospace;-fx-font-size:12px");
		GridPane.setConstraints(outputTextArea, 0, 3, 6, 1, HPos.CENTER, VPos.TOP, Priority.NEVER, Priority.ALWAYS);

		// display window
		layout.getChildren().addAll(
				image1CamBtn,
				image1Path,
				image1DirBtn,
				image1View,
				image2CamBtn,
				image2Path,
				image2DirBtn,
				image2View,
				verifyBtn,
				outputTextArea);
		stage.setScene(scene);
		//stage.sizeToScene();
		stage.show();
	}

	/**
	 * Prompts the user to select an image file
	 * @return A File object of the image
	 */
	public File getImageFile() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle(APP_DEF_IMG_TEXT);
		chooser.setInitialDirectory(DEFAULT_DIR);
		chooser.getExtensionFilters().addAll(EXT_FILTERS);
		return chooser.showOpenDialog(stage);
	}

	/**
	 * Updates the TextField and ImageView of an image, as well as the backing File
	 * @param file File associated with the image
	 * @param tf TextField associated with the image
	 * @param iv ImageView associated with the image
	 */
	public void updateImageObjects(File file, TextField tf, ImageView iv) {
		tf.setText(file.getAbsolutePath());
		Image image = new Image(file.toURI().toString(), APP_IMG_SIZE, APP_IMG_SIZE, true, true, true);
		iv.setImage(image);
	}

	/**
	 * Adds a timestamped line to the output area
	 * @param line Text to display
	 */
	public void log(String line) {
		Date date = new Date();
		outputTextArea.appendText("\n[" + dateFormat.format(date) + "] " + line);
        outputTextArea.setScrollTop(Double.MAX_VALUE);
	}

	/** 
	 * Uses the FaceAPI to compare image1File and image2File
	 */
	public void verifyImages() {
		try {
			FaceAPI api = new FaceAPI();
			log("Using image files <" + image1File.getName() + "> and <" + image2File.getName() + ">");
			
			// encode and detect image 1
			log("Encoding image 1 ...");
			byte[] image1Bytes = Files.readAllBytes(Paths.get(image1File.getAbsolutePath()));
			log ("... done");
			log("Detecting face in image 1 ...");
			DetectResponse[] image1Faces = api.detect(image1Bytes);
			if (image1Faces == null) {
				log("ERROR: something went wrong, aborting");
				return;
			} else if (image1Faces.length > 1) {
				log("ERROR: only 1 face per image, aborting");
				return;
			} else if (image1Faces.length < 1) {
				log ("ERROR: no face detected, aborting");
				return;
			}
			log("... found face:\n" + image1Faces[0].asString());
			
			// encode and detect image 2
			log("Encoding image 2 ...");
			byte[] image2Bytes = Files.readAllBytes(Paths.get(image2File.getAbsolutePath()));
			log("... done");
			log("Detecting face in image 2 ...");
			DetectResponse[] image2Faces = api.detect(image2Bytes);;
			if (image2Faces == null) {
				log("ERROR: something went wrong, aborting");
				return;
			} else if (image2Faces.length > 1) {
				log("ERROR: only 1 face per image, aborting");
				return;
			} else if (image2Faces.length < 1) {
				log ("ERROR: no face detected, aborting");
				return;
			}
			log("... found face:\n" + image2Faces[0].asString());
			
			// verify image 1 and 2
			log("Verifying image 1 and 2 ...");
			VerifyResponse result = api.verify(image1Faces[0].faceId, image2Faces[0].faceId);
			if (result.isIdentical) {
				log("RESULT: The faces are identical. (%" + (int)(result.confidence*100) + ")");
			} else {
				log("RESULT: The faces do not match. (%" + (int)(result.confidence*100) + ")");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
