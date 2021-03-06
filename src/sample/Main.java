package sample;

import com.sun.glass.events.WheelEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static sample.Ai.*;
import static sample.Ai.getCol;

public class Main extends Application {
	private static Stage gameWindow;

	//<editor-fold defaultstate="collapsed" desc="Images">
	static final byte CELL_DIMENSION = 60;
	private static final Image white_piece = new Image("images/blue_piece.png", CELL_DIMENSION, CELL_DIMENSION, true, true, true);
	private static final Image black_piece = new Image("images/red_piece.png", CELL_DIMENSION, CELL_DIMENSION, true, true, true);
	//</editor-fold>

	////// Game settings  /////////////
	static boolean isAi = true;
	static byte boardSize = 8; // or 10
	static final byte WHITE = 0, BLACK = 1, NONE = 2;
	static byte whoseMove = NONE;
	static final String[] stateName = {"White", "Black", "None"};
	static final Image[] stateImage = {white_piece, black_piece};
	static boolean[] stateIsHuman = {true, false};
	static   Move newMove;
	/////////////////////////////

	//<editor-fold defaultstate="collapsed" desc="GameState">
	static byte turn = BLACK, selectedRow = -1, selectedCol = -1;
	static byte[][] states = new byte[boardSize][boardSize]; // WHITE / BLACK / NONE, 0-based indexing, states[i][j] -> state of cell at row i, column j
	//</editor-fold>

	static byte opponent() {
		return turn == WHITE ? BLACK : WHITE;
	}

	static boolean isInside(int row,int col) {
		if(row >= boardSize ) return false;
		if(row < 0) return false;
		if(col >= boardSize ) return false;
		if(col < 0) return false;

		return true;
	}

	private static void selectCell(byte row, byte col) {
		selectedRow = row;
		selectedCol = col;
	}

	private static boolean isAdj(Pair<Integer,Integer> one,Pair<Integer,Integer> two) {
		if( (Math.abs(one.getKey()-two.getKey())<=1)&&(Math.abs(one.getValue()-two.getValue())<=1) )
		{
			System.out.println(one.getKey()+","+one.getValue()+" and "+two.getKey()+","+two.getValue()+" are adj");
			return true;
		}
		else
			return false;
	}

	private static boolean checkWin(byte player) {
		ArrayList<Pair<Integer,Integer>> all = new ArrayList<>();
		ArrayList<Pair<Integer,Integer>> connected = new ArrayList<>();
		ArrayList<Pair<Integer,Integer>> temp = new ArrayList<>();
		ArrayList<Pair<Integer,Integer>> temp2 = new ArrayList<>();

		for(int i=0;i<boardSize;i++) {
			for(int j=0;j<boardSize;j++) {
				if(states[i][j]==player){
					all.add( new Pair<>(i,j) );
				}
			}
		}

		int N = all.size();
		if(all.size()==1) return true;

		connected.add( all.get(0) );
		all.remove(0);
		temp = (ArrayList<Pair<Integer,Integer>>) all.clone();

		for(int k=0;k<N;k++) {


			for (Pair<Integer, Integer> con : connected) {

				for (Pair<Integer, Integer> check : all) {
					if (isAdj(con, check)) {
						temp2.add(check);
						temp.remove(check);
					}
				}
				all = (ArrayList<Pair<Integer,Integer>>) temp.clone();

			}

			connected.addAll(temp2);
			temp2.clear();

		}

		//System.out.println("connected "+stateName[player]+" : "+connected.size());

		if(connected.size()==N) return true;
		else return false;

	}



	static boolean[][] destinations(byte[][] states,byte selectedRow,byte selectedCol) {
		assert selectedRow!=-1 && selectedCol!=-1;
		boolean[][] validTo = new boolean[boardSize][boardSize];

		if(states[selectedRow][selectedCol] != NONE) {

			// for left and right move
			int col_offset = 0;
			for(int c=0;c<boardSize;c++) {
				if(states[selectedRow][c] != NONE) col_offset++;
			}

			if( isInside( selectedRow,selectedCol-col_offset)  ) {
				if(states[selectedRow][selectedCol] != states[selectedRow][selectedCol-col_offset])
					validTo[selectedRow][selectedCol-col_offset] = true;
			}
			if( isInside( selectedRow,selectedCol+col_offset) ) {
				if(states[selectedRow][selectedCol] != states[selectedRow][selectedCol+col_offset])
				validTo[selectedRow][selectedCol+col_offset] = true;
			}

			// for up and down move
			int row_offset = 0;
			for(int r=0;r<boardSize;r++) {
				if(states[r][selectedCol] != NONE) row_offset++;
			}

			if( isInside( selectedRow-row_offset,selectedCol) ) {

				if(states[selectedRow][selectedCol] != states[selectedRow-row_offset][selectedCol])
				validTo[selectedRow-row_offset][selectedCol] = true;
			}
			if( isInside( selectedRow+row_offset,selectedCol) ) {
				if(states[selectedRow][selectedCol] != states[selectedRow+row_offset][selectedCol])
				validTo[selectedRow+row_offset][selectedCol] = true;
			}

			// for 00 to 99 diagonal type 1
			int  offset = 0;
			for(int r=selectedRow,c=selectedCol; isInside(r,c) ;r--,c--) {
				if(states[r][c] != NONE) offset++;
			}

			for(int r=selectedRow+1,c=selectedCol+1; isInside(r,c) ;r++,c++) {
				if(states[r][c] != NONE) offset++;
			}


			if( isInside( selectedRow-offset,selectedCol-offset) ) {
				if(states[selectedRow][selectedCol] != states[selectedRow-offset][selectedCol-offset])
				validTo[selectedRow-offset][selectedCol-offset] = true;
			}
			if( isInside( selectedRow+offset,selectedCol+offset) ) {
				if(states[selectedRow][selectedCol] != states[selectedRow+offset][selectedCol+offset])
				validTo[selectedRow+offset][selectedCol+offset] = true;
			}

			// for 09 to 90 diagonal type 2
			offset = 0;
			for(int r=selectedRow,c=selectedCol; isInside(r,c) ;r--,c++) {
				if(states[r][c] != NONE) offset++;
			}

			for(int r=selectedRow+1,c=selectedCol-1; isInside(r,c) ;r++,c--) {
				if(states[r][c] != NONE) offset++;
			}


			if( isInside( selectedRow-offset,selectedCol+offset) ) {
				if(states[selectedRow][selectedCol] != states[selectedRow-offset][selectedCol+offset])
				validTo[selectedRow-offset][selectedCol+offset] = true;
			}
			if( isInside( selectedRow+offset,selectedCol-offset) ) {
				if(states[selectedRow][selectedCol] != states[selectedRow+offset][selectedCol-offset])
				validTo[selectedRow+offset][selectedCol-offset] = true;
			}



		}


		return validTo;
	}


	private static void changeTurn() {


		if(checkWin(turn)==true)
			win(turn);

		turn = opponent();

		if(turn==WHITE && isAi==true) {

			newMove = getRandomMove();
			selectCell(newMove.first_row,newMove.first_col);
			click(newMove.first_row,newMove.first_col);
			whoseMove = WHITE;
			click(newMove.sec_row,newMove.sec_col);
			System.out.println(newMove.sec_row+" "+newMove.sec_col);

		}

	}



	static void click(byte row, byte col) {

		// normal click


		// is this a move?
		if (whoseMove != NONE) {
			boolean[][] dest = destinations(states,selectedRow,selectedCol);

			if (dest[row][col] == true && states[selectedRow][selectedCol] == whoseMove) {
				states[selectedRow][selectedCol] = NONE;
				states[row][col] = whoseMove;
				whoseMove = NONE;
				changeTurn();

			}

			return;
		}

		// click on empty cell
		if (states[row][col] == NONE) {
			return;
		}


		// click on white cell
		if (states[row][col] == WHITE && turn == WHITE) {
			whoseMove = WHITE;
			selectCell(row, col);
		}

		// click on black cell
		else if (states[row][col] == BLACK && turn == BLACK) {
			whoseMove = BLACK;
			selectCell(row, col);
		}

		else return;




	}

	private static Object setScene(Stage window, String sceneFile) {
		try {
			FXMLLoader loader = new FXMLLoader(Main.class.getResource(sceneFile));
			window.setScene(new Scene(loader.load()));
			return loader.getController();
		} catch (IOException e) {
			System.out.println("fxml file " + sceneFile + " could not be loaded");
			e.printStackTrace(System.out);
			return null;
		}
	}

	private static void win(byte player) {
		Platform.runLater(()->{
			Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(gameWindow);
			dialog.setOnCloseRequest(event -> System.exit (1));
			dialog.show();
			Finishedscene finishedscene = (Finishedscene) setScene(dialog, "finishedscene.fxml");
			assert finishedscene != null;
			finishedscene.result.setText(stateName[player] + " won");
		});

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		gameWindow = primaryStage;
		if(boardSize==10) {
			gameWindow.setTitle("LOA");
			gameWindow.setHeight(735);
			gameWindow.setWidth(625);
		}
		else {
			gameWindow.setTitle("LOA");
			gameWindow.setHeight(587);
			gameWindow.setWidth(505);
		}
		gameWindow.setResizable(true);
		gameWindow.setOnCloseRequest(e -> System.exit(1));
		gameWindow.show();
		setScene(gameWindow, "mainscene.fxml");

	}

	public static void main(String[] args) {
		launch(args);
	}
}
