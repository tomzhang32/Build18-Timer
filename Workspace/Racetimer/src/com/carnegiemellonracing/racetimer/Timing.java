package com.carnegiemellonracing.racetimer;

import com.carnegiemellonracing.racetimer.R;

import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.TextView;
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
	private TextView instructions_;
	private TextView currentTime_;
	private ToggleButton button_;
	/**
	 * Initialize the GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timing);
		instructions_ = (TextView) findViewById(R.id.status);
		button_ = (ToggleButton) findViewById(R.id.ledButton);
		/*
		button_.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!button_.isChecked()) {
					instructions_.setText("Light On");
				} else {
					instructions_.setText("Light Off");
				}
			}
		}
				
		);
		*/
	}

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
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
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
			try {
				isLit_ = !lightSensor_.read();
				led_.write(isLit_);
				if (isLit_) {
					setText("Light On");
				} else {
					setText("Light Off");
				}
				Thread.sleep(10);
			} catch (InterruptedException e) {
				setText("Connection Interrupted");
			}
			/* isLit_ = !button_.isChecked();
			led_.write(isLit_);
			*/
		}
		
		@Override
		public void disconnected() {
			// TODO: Implement what happens when lose connection
		}
	}
	
	private void setText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				instructions_.setText(str);
			}
		});
	}

	/**
	 * A method to create our IOIO Looper.
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
}