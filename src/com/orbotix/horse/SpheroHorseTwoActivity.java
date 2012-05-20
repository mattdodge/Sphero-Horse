package com.orbotix.horse;

import java.util.ArrayList;

import orbotix.macro.Delay;
import orbotix.macro.Fade;
import orbotix.macro.MacroObject;
import orbotix.macro.RGB;
import orbotix.macro.MacroObject.MacroObjectMode;
import orbotix.robot.base.ConfigureCollisionDetectionCommand;
import orbotix.robot.base.DeviceMessenger;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.widgets.ControllerActivity;
import orbotix.robot.widgets.calibration.CalibrationView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SpheroHorseTwoActivity extends ControllerActivity {
	 
    private final static int ENABLE_ROBOT_ADAPTER_ACTIVITY = 1;

    private Robot driverRobot, targetRobot;
    
    private ArrayList<Robot> robotList = new ArrayList<Robot>();
    private ArrayAdapter<Robot> robotListAdapter;
    
    private int connectedRobots = 0;
    
    private CollisionListener driverCollisionListener, targetCollisionListener;
    
    private long startTimeMillis, timeToBeatMillis;
    
    private int currentPlayer, currentTurn,
    			player1Score, player2Score;
    
    private String spheroScoreMap = "sphero";
    
    private boolean playing;
    
    private String player1Name, player2Name;
    
    private final String 	PLAYER_START_GAME_TEXT = "%s's turn. Place the target and press start.",
    						PLAYER_FINISH_COURSE_TEXT = "Time set. Reset the driver sphero and pass the controller to %s, then press Start",
    						PLAYER_BEAT_COURSE_TEXT = "Good job! Reset the driver sphero and pass controller to %s, then press Start",
    						PLAYER_LOSE_COURSE_TEXT = "Nice try. %s's turn to set the course. Place driver and target, then press Start",
    						GAME_OVER_TEXT = "Game over! Reset driver and target and press REMATCH for a rematch! %s is up first";
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startpage);
        
        robotListAdapter = new ArrayAdapter<Robot>(this, R.layout.sphero_list_item, robotList);
        ((ListView) findViewById(R.id.spheroList)).setAdapter(robotListAdapter);
        
        ((EditText)findViewById(R.id.player1NameText)).addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                checkReadyToStart();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        }); 
        
        ((EditText)findViewById(R.id.player2NameText)).addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                checkReadyToStart();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        }); 
        
        this.initConnection();
    }
    
    private void initConnection() {
    	//bluetooth enabled?
        if (RobotProvider.getDefaultProvider().isAdapterEnabled()) {
            //start connection
        	
        	registerReceiver(getDiscoveredReceiver(), new IntentFilter(Robot.ACTION_FOUND));
            registerReceiver(getConnectedReceiver(), new IntentFilter(RobotProvider.ACTION_ROBOT_CONNECT_SUCCESS));
            registerReceiver(getConnectedFailedReceiver(), new IntentFilter(RobotProvider.ACTION_ROBOT_CONNECT_FAILED));
            registerReceiver(getNonePairedReceiver(), new IntentFilter(RobotProvider.ACTION_ROBOT_NONE_FOUND));
        	
        	//get me my robots!
            RobotProvider.getDefaultProvider().setBroadcastContext(this);
            RobotProvider.getDefaultProvider().findRobots();
        	
        	
        } else {
        	Log.w("SpheroHorse", "Bluetooth adapter not enabled, start it?");
        	Intent intent = RobotProvider.getDefaultProvider().getAdapterIntent();
            startActivityForResult(intent, ENABLE_ROBOT_ADAPTER_ACTIVITY);
        }
    }
    
    private BroadcastReceiver getDiscoveredReceiver() {
    	return new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				robotDiscovered(context, intent);
			}
    		
    	};
    }
    private BroadcastReceiver getConnectedReceiver() {
    	return new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				robotConnected(context, intent);
			}
    		
    	};
    }
    private BroadcastReceiver getConnectedFailedReceiver() {
    	return new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				robotConnectedFailed(context, intent);
			}
    		
    	};
    }
    private BroadcastReceiver getNonePairedReceiver() {
    	return new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				robotNonePaired(context, intent);
			}
    		
    	};
    }
    
    private void robotDiscovered(Context context, Intent intent) {
    	Log.d("SpheroHorse","Robot Discovered : "+ getRobotFromIntent(intent).getName());
    	robotList.add(getRobotFromIntent(intent));
    	robotListAdapter.notifyDataSetChanged();
    }
    
    private void robotConnected(Context context, Intent intent) {
    	// we passed, start the next activity
    	Log.d("SpheroHorse","Connection successful!");
    	connectedRobots ++;
    	EditText player1Name = (EditText) findViewById(R.id.player1NameText);
    	EditText player2Name = (EditText) findViewById(R.id.player2NameText);
    	
    	this.player1Name = player1Name.getText().toString();
    	this.player2Name = player2Name.getText().toString();
//    	Intent i = new Intent(getApplicationContext(), SpheroHorsePlayActivity.class);
//
//    	i.putExtra(SpheroHorsePlayActivity.TARGET_ROBOT_INTENT_KEY, targetRobot.getUniqueId());
//    	i.putExtra(SpheroHorsePlayActivity.DRIVER_ROBOT_INTENT_KEY, driverRobot.getUniqueId());
//    	i.putExtra(SpheroHorsePlayActivity.PLAYER_1_NAME_INTENT_KEY, player1Name.getText().toString());
//    	i.putExtra(SpheroHorsePlayActivity.PLAYER_2_NAME_INTENT_KEY, player2Name.getText().toString());
//    	
//    	startActivity(i);
    	
    	if (connectedRobots >= 2) {
	    	setContentView(R.layout.play_game_main);
	    	
	        //Add the CalibrationView as a Controller
	        CalibrationView calibrateView = (CalibrationView)findViewById(R.id.calibration);
	        calibrateView.setRobot(driverRobot);
	        addController(calibrateView);
	        
        	SingleRobotJoystickView joystick = (SingleRobotJoystickView) findViewById(R.id.singleRobotJoystick);
	        joystick.setRobot(driverRobot);
	        addController(joystick);
	        
//	        driverCollisionListener = new CollisionListener(driverRobot);
	        targetCollisionListener = new CollisionListener(targetRobot, this);
	        
//	        DeviceMessenger.getInstance().addAsyncDataListener(driverRobot, driverCollisionListener);
	        DeviceMessenger.getInstance().addAsyncDataListener(targetRobot, targetCollisionListener);

			//// Now send a command to enable streaming collisions
			//// 
//			ConfigureCollisionDetectionCommand.sendCommand(driverRobot, ConfigureCollisionDetectionCommand.DEFAULT_DETECTION_METHOD,
//					45, 45, 100, 100, 100);
			ConfigureCollisionDetectionCommand.sendCommand(targetRobot, ConfigureCollisionDetectionCommand.DEFAULT_DETECTION_METHOD,
					20, 20, 20, 20, 100);
			
	    	((TextView) findViewById(R.id.player1Name)).setText(this.player1Name);
	    	((TextView) findViewById(R.id.player2Name)).setText(this.player2Name);
			
			startGame();
    	}
        

    }
    
    private void setTimer(long millis) {
    	int 	ms = (int) (millis % 1000),
    			s = (int) (millis / 1000) % 60,
    			min = (int) (millis / 60000);
    	
    	TextView timer = (TextView) findViewById(R.id.timer);
    	timer.setText(String.format("%d:%02d.%03d", min, s, ms));
    	timer.setVisibility(View.VISIBLE);
    	findViewById(R.id.gameText).setVisibility(View.GONE);
    }
    
    private void updateTextView(int textViewId, String newText, int hideViewId) {
    	TextView tv = (TextView) findViewById(textViewId);
    	tv.setText(newText);
    	tv.setVisibility(TextView.VISIBLE);
    	findViewById(hideViewId).setVisibility(TextView.GONE);
    }
    
    private void robotConnectedFailed(Context context, Intent intent) {
    	Log.e("SpheroHorse","Could not connect to one of the robots");
    	setLoadingScreenVisible(false);
    	connectedRobots = 0;
		
    	showErrorConnectingToast();
		driverRobot = null;
		targetRobot = null;
		
		findViewById(R.id.choose_driver_button).setVisibility(TextView.VISIBLE);
		findViewById(R.id.choose_target_button).setVisibility(TextView.VISIBLE);
		
		findViewById(R.id.driverText).setVisibility(TextView.GONE);
		findViewById(R.id.targetText).setVisibility(TextView.GONE);
		
		RobotProvider.getDefaultProvider().disconnectControlledRobots();
//		RobotProvider.getDefaultProvider().removeControl(driverRobot);
//		RobotProvider.getDefaultProvider().removeControl(targetRobot);
		
		checkReadyToStart();
    }
    
    private void showErrorConnectingToast() {
    	Toast.makeText(
    		getApplicationContext(), 
    		"Could not connect to those spheros, make sure they are turned on and not paired already. If they are both on, try connecting again.", 
    		Toast.LENGTH_LONG
    	).show();
    }
    
    private void robotNonePaired(Context context, Intent intent) {
    	Log.e("SpheroHorse","No Robots Paired");
    	Toast.makeText(
    		getApplicationContext(), 
    		"You do not have any Spheros paired yet. Go to your Bluetooth settings to pair then restart this app.", 
    		Toast.LENGTH_LONG
    	).show();
    }
    
    private Robot getRobotFromIntent(Intent intent) {
    	Log.d("SpheroHorse","Robot ID is "+intent.getStringExtra(Robot.EXTRA_ROBOT_ID));
    	String id = intent.getStringExtra(Robot.EXTRA_ROBOT_ID);
        return RobotProvider.getDefaultProvider().findRobot(id);
    }

    /**
     * Connect to the robot when the Activity starts
     */
    @Override
    protected void onStart() {
        super.onStart();
    }
    
    private void getTargetRobot() {
    	ListView spheroList = (ListView) findViewById(R.id.spheroList);
		if (targetRobot == null && spheroList.getCheckedItemCount() > 0) {
			Robot selectedRobot = (Robot) (spheroList.getItemAtPosition(spheroList.getCheckedItemPosition()));
    		
			if (driverRobot == null || !selectedRobot.getName().equals(driverRobot.getName())) {
				targetRobot = selectedRobot;
				RobotProvider.getDefaultProvider().control(selectedRobot);
				
				updateTextView(R.id.targetText, "Target: "+targetRobot.getName(), R.id.choose_target_button);
				checkReadyToStart();
			}
    	}
    }
    
    private void getDriverRobot() {
    	ListView spheroList = (ListView) findViewById(R.id.spheroList);
		if (driverRobot == null && spheroList.getCheckedItemCount() > 0) {
			Robot selectedRobot = (Robot) (spheroList.getItemAtPosition(spheroList.getCheckedItemPosition()));
    		
			if (targetRobot == null || !targetRobot.getName().equals(selectedRobot.getName())) {
				driverRobot = selectedRobot;
				RobotProvider.getDefaultProvider().control(selectedRobot);
	
				updateTextView(R.id.driverText, "Driver: "+driverRobot.getName(), R.id.choose_driver_button);
				checkReadyToStart();
			}
    	}
    }
    
    private void startGame() {
    	currentPlayer = 0;
    	timeToBeatMillis = -1;
    	startTimeMillis = -1;
    	currentTurn = 0;
    	
    	player1Score = 0;
    	player2Score = 0;
    	
    	updateTextView(R.id.gameText, String.format(PLAYER_START_GAME_TEXT, getCurrentPlayerName()), R.id.timer);
    	
    	findViewById(R.id.rematchButton).setVisibility(View.GONE);
    	showJoystick(false);
    }
    
    private void showJoystick(boolean showIt) {
    	if (showIt) {
    		findViewById(R.id.singleRobotJoystick).setVisibility(View.VISIBLE);
    		findViewById(R.id.singleRobotJoystick).bringToFront();
    		findViewById(R.id.startButton).setVisibility(View.GONE);
    	} else {
    		findViewById(R.id.singleRobotJoystick).setVisibility(View.GONE);
    		findViewById(R.id.startButton).setVisibility(View.VISIBLE);
    	}
    }
    
    private void updateScores() {
    	String player1ScoreStr = (player1Score == 0) ? "none" : spheroScoreMap.charAt(player1Score - 1) + "";
    	String player2ScoreStr = (player2Score == 0) ? "none" : spheroScoreMap.charAt(player2Score - 1) + "";
    	
    	((ImageView) findViewById(R.id.player1Score))
    		.setImageResource(
    				getResources().getIdentifier("score_"+player1ScoreStr+"_01", "drawable", "com.orbotix.horse"));
    	
    	((ImageView) findViewById(R.id.player2Score))
		.setImageResource(
				getResources().getIdentifier("score_"+player2ScoreStr+"_02", "drawable", "com.orbotix.horse"));
    	
    	if (player1Score >= 6 || player2Score >= 6) {
    		endGame();
    	}
    }
    
    private void endGame() {
    	currentPlayer = 0;
		showText(String.format(GAME_OVER_TEXT, getCurrentPlayerName()));
		
		showJoystick(false);
		findViewById(R.id.startButton).setVisibility(View.GONE);
		findViewById(R.id.rematchButton).setVisibility(View.VISIBLE);
    }
    
    public void onRematchClick(View v) {
    	startGame();
    	updateScores();
    }
    
    private String getCurrentPlayerName() {
    	return (currentPlayer == 0) ? player1Name : player2Name;
    }
    
    public int getCurrentPlayer() {
    	return currentPlayer;
    }
    
    public int getCurrentTurn() {
    	return currentTurn;
    }
    
    public void lose() {
    	playing = false;
    	
    	if (currentPlayer == 0) {
    		player1Score ++;
    	} else {
    		player2Score ++;
    	}
    	
    	currentPlayer = 1 - currentPlayer;
    	timeToBeatMillis = -1;
    	
    	showText(String.format(PLAYER_LOSE_COURSE_TEXT, getCurrentPlayerName()));
    	showJoystick(false);
    	
    	updateScores();
    }
    
    private void checkReadyToStart() {
    	EditText player1Name = (EditText) findViewById(R.id.player1NameText);
    	EditText player2Name = (EditText) findViewById(R.id.player2NameText);
    	
    	if (
    		player1Name.getText().length() > 0 &&
    		player2Name.getText().length() > 0 &&
    		! player1Name.getText().toString().equalsIgnoreCase(player2Name.getText().toString()) &&
    		driverRobot != null &&
    		targetRobot != null
    	) {
    		findViewById(R.id.start_button).setVisibility(View.VISIBLE);
    	} else {
    		findViewById(R.id.start_button).setVisibility(View.GONE);
    	}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(resultCode == RESULT_OK) {
            
        	switch (requestCode) {
        	case ENABLE_ROBOT_ADAPTER_ACTIVITY:
        		this.initConnection();
        		break;
        	}
        	
//        	final String robot_id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
//        	Robot tempRobot;
//        	if(robot_id != null && !robot_id.equals("")){
//                tempRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);
//            } else {
//            	return;
//            }
//        	
//        	if (requestCode == STARTUP_ACTIVITY_DRIVER) {
//        		driverRobot = tempRobot;
//        	} else if (requestCode == STARTUP_ACTIVITY_TARGET) {
//        		targetRobot = tempRobot;
//        	}
        }
    }

    public void onChooseDriverClick(View v){
    	getDriverRobot();
    }
    
    public void onChooseTargetClick(View v) {
    	getTargetRobot();
    }
    
    public void onStartClick(View v) {
    	setLoadingScreenVisible(true);
    	hideKeyboard();
    	RobotProvider.getDefaultProvider().connectControlledRobots();
    }
    
    private void hideKeyboard() {
    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(findViewById(R.id.player1NameText).getWindowToken(), 0);
    }
    
    private void setLoadingScreenVisible(boolean visible) {
    	int visibility = visible ? View.VISIBLE : View.GONE;
    	
    	findViewById(R.id.transparentPanel1).setVisibility(visibility);
    	findViewById(R.id.progressBar1).setVisibility(visibility);
    }
    
    public void onStartPlayClick(View v) {
    	playing = true;
    	startTimeMillis = System.currentTimeMillis();
    	currentTurn++;
    	blinkTarget();
    	if (timeToBeatMillis > 0) {
    		countdownDriver(timeToBeatMillis);
    	} else {
    		countupDriver();
    	}
    	
    	showJoystick(true);
    }
    
    public void targetHit() {
    	Log.d("SpheroHorse","Target robot has been hit, stop timer");
    	
    	if (playing) {
    		playing = false;
    		
    		long nextTime = System.currentTimeMillis() - startTimeMillis;
    		
    		currentPlayer = 1 - currentPlayer;
			
    		final String nextText = (timeToBeatMillis > 0) ? PLAYER_BEAT_COURSE_TEXT : PLAYER_FINISH_COURSE_TEXT;
    		final Handler handler = new Handler();                     
        	handler.postDelayed(new Runnable() {
                public void run() {
                	showText(String.format(nextText, getCurrentPlayerName()));
                }
            }, 2000);
    		
    		MacroObject macro = new MacroObject();
			macro.addCommand(new RGB(0,255,0,0)); 
			macro.setRobot(driverRobot);
			macro.playMacro();
			RGBLEDOutputCommand.sendCommand(driverRobot, 0, 255, 0);
			
			RGBLEDOutputCommand.sendCommand(targetRobot, 0, 0, 255);
			
			timeToBeatMillis = nextTime;
    		startTimeMillis = 0;
    		
    		showJoystick(false);
    	}
    }
    
    private void showText(String text) {
    	updateTextView(R.id.gameText, text, R.id.timer);
    }
    
    private boolean blinkOn = false;
    private void blinkTarget() {
    	if (!playing) {
    		return;
    	}
    	
    	if (blinkOn) {
    		RGBLEDOutputCommand.sendCommand(targetRobot, 0, 0, 0);
    	} else {
    		RGBLEDOutputCommand.sendCommand(targetRobot, 255, 0, 0);
    	}
    	
    	blinkOn = !blinkOn;
    	
    	final Handler handler = new Handler();                     
    	handler.postDelayed(new Runnable() {
            public void run() {
                blinkTarget();
            }
        }, 500);
    }
    
    private void countdownDriver(long timeMillis) {
    	final Handler countdown = new Handler();                      
        countdown.postDelayed(new MissedTimeChecker(this, currentPlayer, currentTurn), timeMillis);
        
        getFadeMacro((int) timeMillis).playMacro();
        RGBLEDOutputCommand.sendCommand(driverRobot, 255, 0, 0);
        countdownTimer();
    }
    
    private void countupDriver() {
    	RGBLEDOutputCommand.sendCommand(driverRobot, 0, 255, 0);
    	countupTimer();
    }
    
    private void countupTimer() {
    	if (!playing) return;
    	
    	setTimer(System.currentTimeMillis() - startTimeMillis);
    	final Handler handler = new Handler();                     
    	handler.postDelayed(new Runnable() {
            public void run() {
                countupTimer();
            }
        }, 23);
    }
    
    private void countdownTimer() {
    	if (!playing) return;
    	
    	setTimer(timeToBeatMillis + startTimeMillis - System.currentTimeMillis());
    	final Handler handler = new Handler();                     
    	handler.postDelayed(new Runnable() {
            public void run() {
                countdownTimer();
            }
        }, 23);
    }
    
    private MacroObject getFadeMacro(int millis) {
    	MacroObject macro = new MacroObject();
		macro.addCommand(new RGB(0,255,0,0)); 
		macro.addCommand(new Fade(255, 0, 0, millis));
		macro.addCommand(new Delay(millis));
		macro.addCommand(new RGB(255,0,0,0));
		macro.setRobot(driverRobot);
		macro.setMode(MacroObjectMode.Normal);
		
		return macro;
    }

    /**
     * Disconnect from the robot when the Activity stops
     */
    @Override
    protected void onStop() {
        super.onStop();

        //disconnect robot
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
        
//        DeviceMessenger.getInstance().removeAsyncDataListener(driverRobot, driverCollisionListener);
        DeviceMessenger.getInstance().removeAsyncDataListener(targetRobot, targetCollisionListener);
    }
}