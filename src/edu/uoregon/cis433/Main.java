package edu.uoregon.cis433;

import java.io.File;
import java.io.FilenameFilter;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Main extends Application {
	private static final String APP_TITLE = "FRAS v1";
	private static final double APP_MIN_HEIGHT = 800;
	private static final double APP_MIN_WIDTH = 1000;
	private static final double APP_THUMB_SIZE = 50;
	private static final double APP_LIST_WIDTH = 160;
	private static final double APP_IMAGE_WIDTH = 300;

	Stage stage;
	
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
		TextField image1Directory = new TextField(),
				image2Directory = new TextField();
		ListView<File> image1List = new ListView<File>(),
				image2List = new ListView<File>();
		ImageView image1View = new ImageView(),
				image2View = new ImageView();
		Button verifyButton = new Button();

		// properties
		image1Directory.setEditable(false);
		image1Directory.setOnMouseClicked(event -> {
			File dir = getDirectory();
			if (dir != null) {
				image1Directory.setText(dir.getAbsolutePath());
				image1List.setItems(getImages(dir));
			}
		});
		GridPane.setConstraints(image1Directory, 0, 0, 2, 1, HPos.CENTER, VPos.BOTTOM);

		image1List.setMaxWidth(APP_LIST_WIDTH);
		image1List.setCellFactory(listView -> new ListCell<File>() {
			@Override public void updateItem(File file, boolean empty) {
				super.updateItem(file, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(file.getName());
					ImageView image = new ImageView();
					image.setImage(new Image(file.toURI().toString(), APP_THUMB_SIZE, APP_THUMB_SIZE, true, true, true));
					setGraphic(image);
				}
			}
		});
		image1List.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
			@Override public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
				Image image = new Image(newValue.toURI().toString(), APP_IMAGE_WIDTH, APP_IMAGE_WIDTH, true, true, true);
				image1View.setImage(image);
			}
		});
		GridPane.setConstraints(image1List, 0, 1, 1, 1, HPos.RIGHT, VPos.TOP, Priority.SOMETIMES, Priority.ALWAYS);

		image1View.setFitWidth(APP_IMAGE_WIDTH);
		GridPane.setConstraints(image1View, 1, 1, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS);

		image2Directory.setEditable(false);
		image2Directory.setOnMouseClicked(event -> {
			File dir = getDirectory();
			if (dir != null) {
				image2Directory.setText(dir.getAbsolutePath());
				image2List.setItems(getImages(dir));
			}
		});
		GridPane.setConstraints(image2Directory, 2, 0, 2, 1, HPos.CENTER, VPos.BOTTOM);

		image2List.setMaxWidth(160);
		image2List.setCellFactory(listView -> new ListCell<File>() {
			@Override public void updateItem(File file, boolean empty) {
				super.updateItem(file, empty);
				if (empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(file.getName());
					ImageView image = new ImageView();
					image.setImage(new Image(file.toURI().toString(), APP_THUMB_SIZE, APP_THUMB_SIZE, true, true, true));
					setGraphic(image);
				}
			}
		});
		image2List.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
			@Override public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
				Image image = new Image(newValue.toURI().toString(), APP_IMAGE_WIDTH, APP_IMAGE_WIDTH, true, true, true);
				image2View.setImage(image);
			}
		});
		GridPane.setConstraints(image2List, 2, 1, 1, 1, HPos.RIGHT, VPos.TOP, Priority.SOMETIMES, Priority.ALWAYS);

		image2View.setFitWidth(APP_IMAGE_WIDTH);
		GridPane.setConstraints(image2View, 3, 1, 1, 1, HPos.LEFT, VPos.TOP, Priority.ALWAYS, Priority.ALWAYS);

		verifyButton.setText("Verify");
		verifyButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent event) {
				verifyImages();
			}
		});
		GridPane.setConstraints(verifyButton, 0, 2, 4, 1, HPos.CENTER, VPos.CENTER);
		
		// show window
		layout.getChildren().addAll(image1Directory, image1List, image1View, image2Directory, image2List, image2View, verifyButton);
		stage.setScene(scene);
		//stage.sizeToScene();
		stage.show();
	}

	public File getDirectory() {
		DirectoryChooser chooser = new DirectoryChooser();
		return chooser.showDialog(stage);
	}
	
	private static final String[] EXTS = new String[] {"jpg", "jpeg", "png"};
	private static final FilenameFilter FILTER = new FilenameFilter() {
		@Override public boolean accept(File dir, String name) {
			for (String ext : EXTS) {
				if (name.endsWith("." + ext)) return true;
			}
			return false;
		}
	};
	public ObservableList<File> getImages(File dir) {
		return FXCollections.observableArrayList(dir.listFiles(FILTER));
	}
	
	public void verifyImages() {
		
	}
}
