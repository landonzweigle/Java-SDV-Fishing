
/* By Landon Zweigle, Parker Segelhorst, and Zach Giuliano
 * Java Final Project; recreation of Stardew Valley's Fishing MiniGame See: https://www.youtube.com/watch?v=wFF-eBrzusM
 * Cast a line into the water by left clicking. when you hear a sound left click. Attempt to keep the bar behind the fish in order to raise the orange bar on the left all the way.
 * 	When it raises, you capture the fish. Game Mode traditional makes it so if you lose three fish you lose the game. freemode you play for forever.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class FishGame extends Application {

	/*Definitions:
	 *  Mode_Idle_out = time when the player has the line casted out (click to pull line, if not in Leniate_period, mode changes to Mode_Idle_in).
	 *  Mode_Idle_in = time when the player has the line in (nothing is happening at all, just the background is animating). When
	 *  																	the Wait_Time is reached, the Leniate_Period begins and a noise is played.
	 *  Mode_Base = time when the fish, capture area, capture bar, and frame are displayed (player attempts to capture fish here) background animation stops.
	 *  Leniate_period = time when the player hears the sound that they have a fish hooked (must click within 'x' amount of seconds. if they miss this period
	 *  																													another random wait_time is generated again.)
	 *  Wait_time = time required to wait, from the begining of Idle_out to the begining off the Leniate_period.
	 *  Case_Win = if the user captures the fish, the Mode_Base display disapears and the fish that they captured is displayed and they are awarded the fish (wait for click to
	 *  																																		go to Mode_idle_in).
	 *  Case_Lose: if the user loses the fish, the Base_mode display disapears and mode is set to Mode_Idle_in.
	 */

	// ---------------VARIABLES---------------//
	
// Physics stuff (all constants or canstantly changing).
	
	static Scanner input = new Scanner(System.in);
		//Time of last frame.
	public static double lastTime = System.nanoTime();
		// The Capture areas gravity rate.
	public static double GRAV = -40;
		// Acceleration rate.
	public static double MOTOR = 50;
		// Fishes max speed.
	public static double MAXSPEED = 1000;
		// fishes min speed.
	public static double MINSPEED = -1000;
		// Rate that the bar raises.
	public static double CAPRATEUP = .25;
		// rate the bar lowers.
	public static double CAPRATEDOWN = .5;
		// the time required to change fishes direction
	public static double ttc;
		// The last time of the frame.
	public static double lt = System.currentTimeMillis();
		// rate the fish slows down.
	public static double DECRATE = 1;
		// Fish minimum speed.
	public static double MINFISH = 10;
		// Fish max speed.
	public static double MAXFISH = 2500;
		// Determines if player is clicking (for the animation loop).
	public static boolean isClicked = false;
		// Points required to capture fish.
	public static double CAPTUREPOINTS = 100;
		// Highest y value of the bar.
	public static double CAPHEIGHT = 660;
		// Height of the capture area.
	public static int CA_HEIGHT = 150;
		// The idle background
	public static Image IDLE;
		// The minigame framework.
	public static Image MINIGAME;
		// the randomly generated number for the fish.
	public static int fishNum;
		// Time required to wait to catch the fish.
	public static double waitTime;
		// All the fish.
	public static Fish ourFish = new Fish();
		// array of images that makes the animation for the player casting a line.
	public static Image[] castAnim;
		//2D array of the positions for the fish when he is pulled out of the water.
	public static int [][]bobberPos = new int[][] {{576,371},{526,226},{269,166},{146,240}};
		// Image display for winning a fish.
	public static Image FISHWON;
		// Image display for losing a fish.
	public static Image FISHLOSE;
		// Time between frames for the animation.
	public static Image GAMEOVER;
		//This is the image for when you lose the game.
	public static double frameRate = 175;
		// the next time (in milliseconds) that the next frame will play.
	public static double animTime = 0;
		// the index for the frame of the animation.
	public static int animPos = 0;
		// amount of fish lost.
	public static int fishLost = 0;
		// amount of fish caught.
	public static int fishCaught = 0;
		// true if the sound hasnt played yet, false if otherwise (so the sound doesn'trepeat).
	public static boolean firstNoise = true;
		//Current max wait time (reaction based portion).
	public static double maxWaitTime = 0.0;
		// how long the fish will stay on the hook before the fish runs away.
	public static double MAXWAITTIMEHOLD = 750.0;
		// Fish of the image you are trying to catch.
	public static Image fishImage;
		//scaled version of the fish.
	public static Image scaledFish;
		//The fish's name
	public static String fishName = "OH NO, I'M BURNING!!! I'M BUURRRNIIINNNGG!!!1!!!1 \n Something went wrong :P";
		//maditory wait time.
	public double manditoryWait=1000.0;
		// manditory wait time added to the current time (later on).
	public double actualManditoryWait=0.0;
		//The type of game the user chosses.
	public static int playMode;
		// position to display the name
	public static int []namePos = new int[] {769,400};
		//positino to display the difficulty.
	public static int []difPos = new int[] {769,551};
		//The record amount of fish that have ever been caught.
	public static int recordFish = 0;
		//Whether or not the amount of caught fish beats the old record
	public static boolean isRecord = false;
// End Physics stuff.

// Begin Variables to be changed per gamemode change (essentially whenever Mode_Idle_out)
	
		// The capture area.
	public static Sprite CA = new Sprite();
		// The fish
	public static Sprite fish = new Sprite();
		// The bar
	public static Image bar;
		// players current points (to capture fish).
	public static double myPoints = 50;
		// from 1 and up (recommended to be less than 10) Difficulty of fish.
	public static int difficulty = 7;
		// The current mode of the game (see full definitions above) -1 = Mode_idle_int. 0 = Mode_idle_out. 1 = Mode_Base. 2 = Mode_Win 3 = Mode_Lose.Mode 4=Game Over.
	public static int mode = -1;

// End changed variables.
	
	//Landon Zweigle
	//Used whenever we need to reset the sprites.
	//Parameters: none
	//Returns: none.
	public static void startGame() {
			//Set the images.
		FISHLOSE = new Image("resources/You Lost the Fish0.png", 1280, 720, true, false);
		FISHWON = new Image("resources/Capture Screen.png", 1280, 720, true, false);
		GAMEOVER=new Image("resources/Game Over0.png",1280,720,true,false);
		bar = new Image("resources/bar.png",64,0,false,false);
		castAnim = new Image[] { new Image("resources/Casting0.png"), new Image("resources/Casting1.png"), new Image("resources/Casting2.png"), new Image("resources/Casting3.png"), new Image("resources/Casting4.png") };
		MINIGAME = new Image("resources/Capture Area.png", 1280, 720, true, false);
		IDLE = new Image("resources/Ocean.png", 1280, 720, true, false);
		
		CA.setImg(new Image("resources/CA.png", 129, CA_HEIGHT, false, false));
		fish.setImg(new Image("resources/fish.png", 300, 75, true, false));
		
			// Set base Capture area information
		CA.setPos(862, 560);
		CA.setHeight((int) CA.getImg().getHeight());
		CA.setWidth((int) CA.getImg().getWidth());
		CA.setMaxH(687);
		CA.setMinH(35);

			// Set base fish information
		fish.setPos(852, 560);
		fish.setHeight((int) fish.getImg().getHeight());
		fish.setWidth((int) fish.getImg().getWidth());
		fish.setMaxH(687);	
		fish.setMinH ( 35 );

	}

	// Parker T. Segelhorst
	// Main menu that exists in the consoul before the method that calls game. It asks
	// for what the user wants to do, if they would rather begin the game, or rather
	// look at instructions.
	public static void main(String[] args) {
		String fileName = "resources/record.txt";
		
		//String line = null;
		
		try {

			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			recordFish = Integer.valueOf(br.readLine ( ));
			br.close ( );
			
		}catch(FileNotFoundException ex) {
			print("Failed to load a record file! (Does it exist?)");
		}catch(IOException ex) {
			print("Failed to read the file.");
		}
		
		
		int menu;
		print("WELCOME TO FISH'N FREINDS!!!");
		print("Press 1 to Begin");
		print("Press 2 to For Instructions");
		menu = input.nextInt();		
		
		//begin the game call before instructions are desplayed.
		if (menu == 1) {
			print("Press 1 for Traditional Mode");
			print("Press 2 for Freemode");
			playMode=input.nextInt();
			startGame();
			launch(args);
		} else if (menu == 2) {
			print("INSTRUCTIONS"+"\n");
			print("There are two modes, Traditional, and Freemode. In Traditional you can lose up to 3 fish before you lose the game.");
			print("In Freemode you can play as long as you like without ending the game.");
			print("1). Just sit and wait for the fish to grab. You'll useally have to wait within a 8 second window.");
			print("The snaged fish will be indicated by a noise. Be carful to click at the right moment, or else the fish will get away.");
			print("2). Once a fish has been grabbed, the objective is to keep the green highlighted box behind the fish.");
			print("The problem with this is that the fish moves. In order to caputre it, move the box up by rapidley clicking");
			print("the left mouse button, and release the mouse to let the box fall. You'll have captured the fish once the orange bar is full.");
			print("3). There are several levels of difficulty, depending on the level of diffulcty, the fish will be harder to capture.");
			print("You do not have a choice for which fish you capture."+"\n");
			print("GOOD LUCK!!!"+"\n");
			int begin;

			print("Press 1 to Begin");
				//Different Variable to begin the game, no reason to allow for the option to display instructions again.

			begin = input.nextInt();

			
			if (begin == 1) 
			{
				print("Press 1 for Traditional Mode");
				print("Press 2 for Freemode");
				playMode=input.nextInt();
				startGame();
				launch(args);
			}
			else
			{
				print("You did not enter a vaild number, you are banned.");
				return;
			}
			
		} 
		else
		{
			print("You did not enter a vaild number, you are extra banned.");
			return;
		}
	}

	//EveryOne
	@Override
	public void start(Stage stage) throws Exception {

		// ------------TODO------------------//
		/* 1. Implement modes. 
		 * 2. clean up this area (remove redundent stuff).
		 * 3. Formate the "correct way".
		 */
		//set the bobber down sound.
		
		URL location = getClass().getResource("resources/FishBobberDown.wav");
		AudioClip bobberDown = new AudioClip(location.toString());
		URL locationBK = getClass ( ).getResource ( "resources/Ocean Sounds.wav" );
			//Credit for audio goes to: https://www.youtube.com/watch?v=xyA5c-ajXyg
			//I edited the audio and made it loopable.
		AudioClip atmosphericSound = new AudioClip(locationBK.toString ( ));
		atmosphericSound.setCycleCount ( AudioClip.INDEFINITE );
		atmosphericSound.play (.1 );
			// Set stage title
		stage.setTitle("fish game");
			// Instantiate group.
		Group root = new Group();
			// Instantiate the scene.
		Scene sc = new Scene(root);
			// Set the stages scene.
		stage.setScene(sc);
			// Canvas for drawing stuff.
		Canvas can = new Canvas(1280, 720);
			// Instantiate the graphicsContext.
		GraphicsContext gc = can.getGraphicsContext2D();
		gc.drawImage(IDLE, 0, 0);
			// add everything to the console.
		root.getChildren().addAll(can);
		
		
			// update the original fish and Capture area.
		CA.update(0);
		CA.show(gc);
		fish.update(0);
		fish.show(gc);
		animPos = 5;
		
// ---------------------Get Mouse Events-------------------//
		sc.setOnMousePressed(event -> {
			if (event.getButton() == MouseButton.PRIMARY) {
					// Depending on the current mode (see definitions), we set values and call functions Here.

				if (mode == -1) {
						// Generate the random fish.
					fishNum = (int) (Math.random() * (14));
					waitTime = (Math.random() * 5) + 2.5;
					waitTime = (waitTime * 1000) + System.currentTimeMillis();
					maxWaitTime = waitTime + MAXWAITTIMEHOLD;
					fishImage = new Image(ourFish.getIMG(fishNum),255,255,true,false);
					scaledFish = new Image(ourFish.getIMG(fishNum),100,100,true,false);
					difficulty = ourFish.getDiff(fishNum);
					fishName = ourFish.getName(fishNum);
					animPos = 0;
					mode = 0;
					animTime = System.currentTimeMillis();

				} 
					//If the player left clicks within a time window set mode to 1.
					//If the player misses the window (is too late), and the player clicks, set mode to 1.
					//If the player just won or lost, wait manditoryWait amount of seconds untill they can click again.
				else if (mode == 0 && (System.currentTimeMillis() >= waitTime && System.currentTimeMillis() <= maxWaitTime)) 
				{
					mode = 1;
				} 
				else if (mode == 0 && (System.currentTimeMillis() <= waitTime )) 
				{
					animTime = System.currentTimeMillis();
					animPos = 0;
					fishImage = null;
					fishName = null;
					scaledFish = null;
					mode = -1;
				} 
				else if ((mode == 3 || mode == 2) && (System.currentTimeMillis()>=actualManditoryWait)) 
				{
					if(mode == 2)
					{

						animPos = 5;

					}else if (mode == 3) 
					{
						animPos = 0;
					}
					animTime = System.currentTimeMillis();
					fishImage = null;
					fishName = null;
					scaledFish = null;
					mode = -1;
				}
				else if(mode == 4 && System.currentTimeMillis()>=actualManditoryWait + 2500)
				{
					System.exit (-1);
				}
					// if the player is left clicking, set isClicked to true so the fish can raise.
				isClicked = true;
			}
		});
		sc.setOnMouseReleased(event -> {
			isClicked = false;
		});

// ----------------Animation Hub------------------//
			// Instantiate the animation timer
		new AnimationTimer() {
				// This is essentially just a while loop.
			@Override
			public void handle(long now) {

					//clear the screen.
				gc.clearRect(0, 0, 1280, 720);
								
				if (mode == 3) 
				{
						// Image that says "Game Over, you caught # fish". Closes after 5 seconds.
					gc.drawImage(FISHLOSE, 0, 0);
					fishImage = null;					
				}
				if (mode == 2) 
				{
					//Animate the bobber being flung.
					if (System.currentTimeMillis() >= animTime + (animPos * frameRate)) {
						animPos++;
						animPos = (int) clamp(animPos, 0, 5);
					}
					//The fish was successfully caught.
					if (animPos == 5) {
						gc.drawImage(FISHWON, 0, 0);
						gc.setTextAlign(TextAlignment.LEFT);
						gc.setTextBaseline(VPos.TOP);
						
						gc.setFont(new Font("Timesnew Roman", 30));
						gc.setFill ( Color.WHITE );
						gc.fillText("" + fishName, namePos[0],namePos[1]);
						gc.setFill ( Color.CORAL );
						gc.fillText("Difficulty " + difficulty, difPos[0],difPos[1]);
						gc.drawImage(fishImage,837,69);
					} else {
						gc.drawImage(castAnim[4 - animPos], 0, 0);
					}
					if(scaledFish != null && animPos < 5 && animPos >= 1) 
					{
						gc.drawImage ( scaledFish, bobberPos[animPos - 1][0], bobberPos[animPos - 1][1] );
					}
				}
				if(mode==4)
				{
					gc.drawImage(GAMEOVER,0,0);
				}
				if (mode == 0) 
				{
					if (System.currentTimeMillis() >= waitTime && firstNoise == true) 
					{
						bobberDown.play(1);
						firstNoise = false;
					}
					if (System.currentTimeMillis() >= animTime + (animPos * frameRate)) 
					{
						animPos++;
						animPos = (int) clamp(animPos, 0, 4);
					}
					gc.drawImage(castAnim[animPos], 0, 0);
					if(System.currentTimeMillis() >= maxWaitTime) 
					{
						waitTime = (Math.random() * 2.5) + 5;
						waitTime = (waitTime * 1000) + System.currentTimeMillis();
						maxWaitTime = waitTime + MAXWAITTIMEHOLD;
						firstNoise = true;
					}
				}
				if (mode == -1) 
				{
					if (System.currentTimeMillis() >= animTime + (animPos * frameRate)) {
						animPos++;
						animPos = (int) clamp(animPos, 0, 5);
					}
					if (animPos == 5) {
						gc.drawImage(IDLE, 0, 0);
					} else {
						gc.drawImage(castAnim[4 - animPos], 0, 0);
					}
				}
				if (mode == 1) {
						// Display the fish's difficulty (most likely removed in final version).
					gc.drawImage(MINIGAME, 0, 0);
						// Generate time since last frame.
					double deltaT = (now - lastTime) / 1000000000.0;
					lastTime = now;

						// Determine the fishes movement.
					double newVel = detMove();

						// Set the new fishes movement.
					fish.setyVel(clamp(Math.abs(newVel) - DECRATE, MINFISH, MAXFISH) * ((newVel >= 0) ? 1 : -1));

						// if the player is clicking the left mouse button the fish raises.
					if (isClicked == true) {
						CA.setyVel(clamp(CA.getyVel() + MOTOR, MINSPEED, MAXSPEED));
					} else {
						CA.setyVel(clamp(CA.getyVel() + GRAV, MINSPEED, MAXSPEED));
					}

						// Update the fish and the Capture area in order to display its new position.
					CA.update(deltaT);
					CA.show(gc);

					fish.update(deltaT);
					fish.show(gc);

						// determine if fish is inside the colliding area.
					if (fish.collidingWith(CA)) 
					{
						myPoints += CAPRATEUP;
					} 
					else 
					{
						myPoints -= CAPRATEDOWN;
					}
					

						// The size of the bar.
					double ySize = (myPoints / CAPTUREPOINTS) * CAPHEIGHT;
						// draw the bar.
					gc.drawImage(bar, 751, 692, 64, -ySize);
					//Handle capture/loss
					if (myPoints >= CAPTUREPOINTS) 
					{
						mode = 2;
						animPos = 0;
						animTime = System.currentTimeMillis();
						fishCaught++;
						if(fishCaught > recordFish) {
							isRecord = true;
							recordFish = fishCaught;
							//Todo: Save the number.
							saveGame();
						}
						
						myPoints = CAPTUREPOINTS / 2;
						firstNoise = true;
						actualManditoryWait=System.currentTimeMillis()+manditoryWait;
						startGame();
					}
					else if (myPoints <= 0) 
					{
						startGame();
						actualManditoryWait=System.currentTimeMillis()+manditoryWait;
							// go into lose mode method
						fishLost++;
						if(fishLost >= 3 && playMode==1) {
							mode=4;
							actualManditoryWait=System.currentTimeMillis()+manditoryWait;
						}
						else
						{
							mode=3;
							myPoints = CAPTUREPOINTS / 2;
							firstNoise = true;
						}
					}
				}
					// This will try to make fish caught/fish lost display
				gc.setTextAlign(TextAlignment.LEFT);
				gc.setTextBaseline(VPos.TOP);
				gc.setFont(new Font("Timesnew Roman", 45));
				gc.setFill(Color.LAWNGREEN);
				gc.fillText("Caught: " + fishCaught, 10, 0);
				gc.setTextAlign(TextAlignment.RIGHT);
				gc.setTextBaseline(VPos.TOP);
				gc.setFill(Color.RED);
				gc.fillText("Got Away: " + fishLost, 1270, 0);
				gc.setFont(new Font("Timesnew Roman", 30));
				gc.setTextAlign(TextAlignment.LEFT);
				gc.setTextBaseline(VPos.BOTTOM);
				gc.setFill(Color.AQUAMARINE);
				gc.fillText("Record Fish Caught: " + recordFish, 0, 720);
				
				if(isRecord) {
					gc.setFont(new Font("Timesnew Roman", 20));
					gc.setTextAlign(TextAlignment.RIGHT);
					gc.setTextBaseline(VPos.BOTTOM);
					gc.setFill(Color.YELLOW);
					gc.fillText("You have beaten the record!", 1270,720);
				}
					// Update everything so it actually draws.
				root.getChildren().setAll(can);
			}
		}.start();
			// show everything.
		stage.show();
	}
	
	public static boolean saveGame() {
		String file = "resources/record.txt";
		boolean success = false;
		try {
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			
			PrintWriter pw = new PrintWriter(file);
			pw.print ("");
			pw.close ( );
			
			bw.write ("" + recordFish );
			bw.close ( );
			success = true;
			
		}catch(IOException ex) {
			print("Something went wrong saving the record amount of fish.");
			success = false;
		}
		return success;
	}
	
	/*Landon Zweigle
	 * Determines the fishes next velocity using its difficulty. 
	 * parameters: none
	 * returns: the fishes "new" velocity *
	 */
	public static double detMove() {
			// The current time in nanoseconds.
		double cur = System.currentTimeMillis();
			// the current fishes velocity.
		double toRet = fish.getyVel();

			// if the current time is greater than or equal to the time to change.
		if (cur >= (ttc * 1000) + lt) {
				// Set lt (lasttime) to the current time to add it to the number of seconds required to get, so we can have a relative time.
			lt = cur;
				// Milliseconds to wait.
			ttc = ((Math.random()) * 7.5 / difficulty);

				// The rest of the method is just to make the fish movement feel balanced given its difficulty. Mult changes the direction.
			int mult = -1;
			if (Math.random() <= .25) {
				mult = 1;
			}
				// Depending on the fishes current velocity, we change its velocity.
			if (toRet >= 0) {
				toRet = 200 * difficulty * ((Math.random()) - (.5)) * mult;
			} else {
				toRet = 200 * difficulty * ((Math.random()) - (.5)) * mult * -1;
			}
		}
			// Return new velocity.
		return toRet;
	}

		// Landon Zweigle
		// Simple print method.
		//parameters: toPrint = object to be printed
		//returns: None.
	public static void print(Object toPrint) {
		System.out.println (toPrint.toString());
	}
	
	/*
	 * Landon Zweigle Clamps a value to a range 
	 * parameters: val = value to be clamped, min = lower limit, max = upper limit. returns: 
	 * returnedVal = the clamped value.
	 */
	public static double clamp(double val, double min, double max) {
		double returnedVal = val;
		if (val >= max) {
			returnedVal = max;
		} else if (val <= min) {
			returnedVal = min;
		}
		return returnedVal;
	}

}
// Problems: None