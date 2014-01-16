package com.carnegiemellonracing.racetimer;

import com.carnegiemellonracing.racetimer.R;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
  private TextView currentLapTimeText_, currentLapNumText_, currentSessionTimeText_;
  private int currentLapNum_ = 1;
  private long startTime_, lastLapTime_;
  private Handler timerHandler = new Handler();
  
  private RelativeLayout timerBox_;
  private boolean isRunning_ = false;
  /**
   * Initialize the GUI.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_timing);
    status_ = (TextView) findViewById(R.id.status);
    currentLapTimeText_ = (TextView) findViewById(R.id.lapTime);
    currentLapNumText_ = (TextView) findViewById(R.id.lap);
    timerBox_ = (RelativeLayout) findViewById(R.id.timerBox);

    // Define what happens when you click the timer box.
    timerBox_.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        isRunning_ = !isRunning_;
        if (isRunning_) {
          status_.setText(R.string.touch_stop);
          // TODO: clear list and run the thread
        } else {
          status_.setText(R.string.touch_start);
          // TODO: stop the thread
        }
      }
    });
  }

  // New Runnable that updates the UI on its own thread
  private Runnable updateTotalTimerThread = new Runnable() {
    public void run() {   
      long deltaTime_ = SystemClock.uptimeMillis() - startTime_;
      currentSessionTimeText_.setText(parseTime(deltaTime_));
      timerHandler.postDelayed(this, 0);
    }
  };
  
  private Runnable updateLapTimerThread = new Runnable() {
    public void run() {
      long deltaTime_ = SystemClock.uptimeMillis() - lastLapTime_;
      currentLapTimeText_.setText(parseTime(deltaTime_));
      timerHandler.postDelayed(this, 0);
    }
  };

  /**
   * This is the thread on which all the IOIO activity happens. It will be run
   * every time the application is resumed and aborted when it is paused. The
   * method setup() will be called right after a connection with the IOIO has
   * been established (which might happen several times!). Then, loop() will
   * be called repetitively until the IOIO gets disconnected.
   */
  class Looper extends BaseIOIOLooper {
    /** The on-board LED. */
    private DigitalOutput led_;
    private DigitalInput lightSensor_;
    private boolean isLit_;

    /**
     * Called every time a connection with IOIO has been established.
     * Typically used to open pins.
     * @throws ConnectionLostException
     *             When IOIO connection is lost.
     */
    @Override
    public void setup() throws ConnectionLostException {
      led_ = ioio_.openDigitalOutput(0, true);
      lightSensor_ = ioio_.openDigitalInput(2);
    }

    /**
     * Called repetitively while the IOIO is connected.
     * @throws ConnectionLostException
     *             When IOIO connection is lost.
     */
    @Override
    public void loop() throws ConnectionLostException {
      if(isRunning_) {
        try {
          isLit_ = !lightSensor_.read();
          led_.write(isLit_);
          if (isLit_) {
            setStatus("Light On");
          } else {
            setStatus("Light Off");
          }
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
  
  // A method to create a new lap card.
  private void lap() {
    
  }
  
  private String parseTime(long t) {
    int secs = (int) (t / 1000);
    int mins = secs / 60;
    secs = secs % 60;
    int hrs = mins / 60;
    mins = mins % 60;
    int milliseconds = (int) (t % 1000);
    String out = ""+ mins + ":"
                  + String.format("%02d", secs) + "."
                  + String.format("%03d", milliseconds);
    if (hrs > 0) {
      out = "" + hrs + ":" + out;
    }
    return out;
  }
  
  private void setStatus(final String str) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        status_.setText(str);
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