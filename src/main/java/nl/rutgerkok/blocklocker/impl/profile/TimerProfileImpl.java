package nl.rutgerkok.blocklocker.impl.profile;

import java.util.Date;

import nl.rutgerkok.blocklocker.profile.Profile;
import nl.rutgerkok.blocklocker.profile.TimerProfile;

import org.json.simple.JSONObject;

class TimerProfileImpl implements TimerProfile {

    static final String TIME_KEY = "t";

    private final int seconds;
    private String timerTag;

    TimerProfileImpl(String timerTag, int secondsOpen) {
        this.timerTag = timerTag;

        if (secondsOpen < 1) {
            secondsOpen = 1;
        } else if (secondsOpen > 9) {
            secondsOpen = 9;
        }

        this.seconds = secondsOpen;
    }

    @Override
    public String getDisplayName() {
        return "[" + timerTag + ":" + seconds + "]";
    }

    @Override
    public int getOpenSeconds() {
        return seconds;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject getSaveObject() {
        JSONObject object = new JSONObject();
        object.put(TIME_KEY, seconds);
        return object;
    }

    @Override
    public boolean includes(Profile other) {
        // Includes nobody
        return false;
    }

    @Override
    public boolean isExpired(Date cutoffDate) {
        // These never expire
        return false;
    }

	@Override
	public Profile changeTag(String tag) {
		this.timerTag = tag;
		return this;
	}

}
