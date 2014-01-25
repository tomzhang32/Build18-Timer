package com.carnegiemellonracing.racetimer;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LapListAdapter extends ArrayAdapter<Long> {
  private final Context context;
  // ArrayList of all the times that the laser was triggered.
  // Need to take difference to get actual lap times.
  private final ArrayList<Long> lapTimes; // Maybe don't need final if plan to change it later
  private long startTime = 0;

  public LapListAdapter(Context c, ArrayList<Long> values) {
    super(c, R.layout.lapcard, values);
    context = c;
    lapTimes = values;
  }
  
  public void setStartTime (long t) {
    startTime = t;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View lapCard = inflater.inflate(R.layout.lapcard, parent, false);
    TextView lapNumTV = (TextView) lapCard.findViewById(R.id.lapcardlap);
    TextView lapTimeTV = (TextView) lapCard.findViewById(R.id.lapcardtime);
    TextView deleteButton = (TextView) lapCard.findViewById(R.id.deleteButton);

    // Remove this element if long click the X
    deleteButton.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        remove(lapTimes.get(position));
        return true;
      }
    });

    int lapNum = position + 1;
    lapNumTV.setText("Lap " + lapNum);
    
    // Calculate the elapsed time since last trigger
    long elapsed;
    if (position == 0) {
      elapsed = lapTimes.get(position) - startTime;
    } else {
      elapsed = lapTimes.get(position) - lapTimes.get(position - 1);
    }
    lapTimeTV.setText(Utils.parseTime(elapsed));

    return lapCard;
  }
}
