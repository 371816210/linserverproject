package com.inhuasoft.smart.server;
/*
AudioCallFragment.java
Copyright (C) 2012  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCall.State;

import android.R.raw;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;

/**
 * @author Sylvain Berfini
 */
public class AudioCallFragment extends Fragment implements OnClickListener {	
	private InCallActivity incallActvityInstance;
	
    ImageButton btnHangUp ;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_call_incall_audio, container, false);
        registerCallDurationTimer(view,LinphoneManager.getLc().getCalls()[0]);
        btnHangUp = (ImageButton) view.findViewById(R.id.view_call_incall_audio_imageButton_hang);
        btnHangUp.setOnClickListener(this);
        return view;
    }

	
	
	private void registerCallDurationTimer(View view,LinphoneCall call) {
		int callDuration = call.getDuration();
		if (callDuration == 0 && call.getState() != State.StreamsRunning) {
			return;
		}
		
		Chronometer timer = (Chronometer) view.findViewById(R.id.callTimer_new);
		if (timer == null) {
			throw new IllegalArgumentException("no callee_duration view found");
		}
		
		timer.setBase(SystemClock.elapsedRealtime() - 1000 * callDuration);
		timer.start();
	}
	
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		incallActvityInstance = (InCallActivity) activity;
		
		
		if (incallActvityInstance != null) {
			incallActvityInstance.bindAudioFragment(this);
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();

		// Just to be sure we have incall controls
		if (incallActvityInstance != null) {
			incallActvityInstance.setCallControlsVisibleAndRemoveCallbacks();
		}
	}
	
	class SwipeGestureDetector implements OnTouchListener {
	    static final int MIN_DISTANCE = 100;
	    private float downX, upX;
	    private boolean lock;
	    
		private SwipeListener listener;
		
		public SwipeGestureDetector(SwipeListener swipeListener) {
			super();
			listener = swipeListener;
		}
		
        @Override
    	public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
            	lock = false;
                downX = event.getX();
                return true;
                
            case MotionEvent.ACTION_MOVE:
            	if (lock) {
            		return false;
            	}
                upX = event.getX();

                float deltaX = downX - upX;

                if (Math.abs(deltaX) > MIN_DISTANCE) {
                    lock = true;
                    if (deltaX < 0) { listener.onLeftToRightSwipe(); return true; }
                    if (deltaX > 0) { listener.onRightToLeftSwipe(); return true; }
                }
                break;
            }
            return false;
        }
    }
	
	interface SwipeListener {
		void onRightToLeftSwipe();
		void onLeftToRightSwipe();
	}

	
	private void hangUp() {
		LinphoneCore lc = LinphoneManager.getLc();
		LinphoneCall currentCall = lc.getCurrentCall();
		
		if (currentCall != null) {
			lc.terminateCall(currentCall);
		} else if (lc.isInConference()) {
			lc.terminateConference();
		} else {
			lc.terminateAllCalls();
		}
	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		if(arg0.getId() == R.id.view_call_incall_audio_imageButton_hang)
		{
			hangUp();
		}
	}
}
