package nl.rutgerkok.blocklocker.profile;

public interface TagProfile extends Profile {
	
	/**
	 * Get profile with another tag
	 * 
	 * @param tag
	 * 			The tag for the profile
	 * @return The profile itself
	 */
	Profile fromTag(String tag);

}
