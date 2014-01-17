package com.carnegiemellonracing.racetimer;

import java.util.ArrayList;

import com.carnegiemellonracing.racetimer.R;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the main activity of the HelloIOIO example application.
 * 
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link AbstractIOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class Timing extends IOIOActivity {
  private TextView status_;
  private TextView currentLapTimeTV_, currentLapNumTV_, currentTotalTimeTV_;
  private View lightIndicator_;
  private RelativeLayout timerBox_;
  private ListView lapList_;
  private TextView testButton_;

  private long startTime_;
  private boolean isRunning_ = false, connectedToIOIO_ = false;

  private ArrayList<Long> lapTimes = new ArrayList<Long>();
  private Handler timerHandler = new Handler();
  private LapListAdapter lla;

  /**
   * Initialize the GUI.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_timing);

    // Find all the UI elements
    status_ = (TextView) findViewById(R.id.status);
    currentLapTimeTV_ = (TextView) findViewById(R.id.lapTime);
    currentLapNumTV_ = (TextView) findViewById(R.id.lap);
    currentTotalTimeTV_ = (TextView) findViewById(R.id.sessionTime);
    lightIndicator_ = (View) findViewById(R.id.lightIcon);
    timerBox_ = (RelativeLayout) findViewById(R.id.timerBox);
    lapList_ = (ListView) findViewById(R.id.lapList);
    lla = new LapListAdapter(this, lapTimes);
    lapList_.setAdapter(lla);

    isRunning_ = false;
    startTime_ = -1;

    // Define what happens when you click the timer box.
    timerBox_.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        isRunning_ = !isRunning_;
        if (isRunning_) {
          reset();
          status_.setText(R.string.touch_stop);
          // TODO: clear list
        } else {
          status_.setText(R.string.touch_start);
          timerHandler.removeCallbacks(updateTotalTimerThread);
          timerHandler.removeCallbacks(updateLapTimerThread);
        }
      }
    });
    
    testButton_ = (TextView) findViewById(R.id.TestButton);
    testButton_.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        lightTriggered();
      }
    });
  }

  /**
   * This is the thread on which all the IOIO activity happens. It will be run
   * every time the application is resumed and aborted when it is paused. The
   * method setup() will be called right after a connection with the IOIO has
   * been established (which might happen several times!). Then, loop() will
   * be called repetitively until the IOIO gets disconnected.
   */
  class Looper extends BaseIOIOLooper {
    private DigitalInput lightSensor_;
    private boolean isLit_;

    /**
     * Called every time a connection with IOIO has been established.
     * @throws ConnectionLostException, When IOIO connection is lost.
     */
    @Override
    public void setup() throws ConnectionLostException {
      lightSensor_ = ioio_.openDigitalInput(2);
      makeToast("Connected to IOIO!");
    }

    /**
     * Called repetitively while the IOIO is connected.
     * @throws ConnectionLostException, When IOIO connection is lost.
     */
    @Override
    public void loop() throws ConnectionLostException {
      // if(isRunning_) {
      if(true) {
        try {
          // Wait for a beam break
          // True is when there's no light
          lightSensor_.waitForValue(true);
          // isLit_ = !lightSensor_.read();
          isLit_ = false;
          setLightStatus(isLit_);
          if(!isLit_) {
            // lightTriggered();
          }
          // Wait for the light to come back
          lightSensor_.waitForValue(false);
          setLightStatus(true);

          Thread.sleep(10);
        } catch (InterruptedException e) {
          makeToast("Connection Interrupted");
        }
      }
    }
      
    @Override
    public void disconnected() {
      // TODO: Implement what happens when lose connection
    }
  }
  
  /**
   * A method to create our IOIO Looper.
   */
  @Override
  protected IOIOLooper createIOIOLooper() {
    return new Looper();
  }


  // New Runnable that updates the UI on its own thread
  private Runnable updateTotalTimerThread = new Runnable() {
    public void run() {
      long deltaTime_ = SystemClock.uptimeMillis() - startTime_;
      currentTotalTimeTV_.setText(Utils.parseTime(deltaTime_));
      timerHandler.postDelayed(this, 0);
    }
  };

  private Runnable updateLapTimerThread = new Runnable() {
    public void run() {
      // Use the last element in the ArrayList in case a lap is deleted
      long lastLap = startTime_;
      if (lapTimes.size() > 0) {
        lastLap = lapTimes.get(lapTimes.size() - 1);
      }
      long deltaTime_ = SystemClock.uptimeMillis() - lastLap;
      currentLapTimeTV_.setText(Utils.parseTime(deltaTime_));
      timerHandler.postDelayed(this, 0);
    }
  };

  // A method to start the timer
  private void lightTriggered() {
    if (isRunning_) {
      if (startTime_ == -1) {
        startTime_ = SystemClock.uptimeMillis();
        lla.setStartTime(startTime_);
        timerHandler.postDelayed(updateTotalTimerThread, 0);
        timerHandler.postDelayed(updateLapTimerThread, 0);
      } else {
        // Automatically updates lapTimes as well.
        lla.add(SystemClock.uptimeMillis());
        currentLapNumTV_.setText("Lap " + lla.getCount());
      }
      // updateUI();
    }
  }
  
  private void reset() {
    startTime_ = -1;
    lla.clear();
    currentTotalTimeTV_.setText(R.string.default_time);
    currentLapTimeTV_.setText(R.string.default_time);
  }
  
  private void setLightStatus(final boolean isLit) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        int rs;
        if (isLit) {
          rs = R.drawable.light_on_icon;
        } else {
          rs = R.drawable.light_off_icon;
        }
        lightIndicator_.setBackgroundResource(rs);
      }
    });
  }
  
  private void makeToast(final String str) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
      }
    });
  }

}