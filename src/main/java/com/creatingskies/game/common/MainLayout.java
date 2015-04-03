package com.creatingskies.game.common;

import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public final class MainLayout {

	private static Stage primaryStage;
	private static BorderPane rootLayout;
	
	public static Stage getPrimaryStage() {
		return primaryStage;
	}
	
	public static void setPrimaryStage(Stage primaryStage) {
		MainLayout.primaryStage = primaryStage;
	}
	
	public static BorderPane getRootLayout() {
		return rootLayout;
	}
	
	public static void setRootLayout(BorderPane rootLayout) {
		MainLayout.rootLayout = rootLayout;
	}
}
