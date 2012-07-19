package de.blinkt.openvpn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ProfileManager {
	private static final String PREFS_NAME =  "VPNList";



	private static ProfileManager instance;
	private HashMap<String,VpnProfile> profiles=new HashMap<String, VpnProfile>();


	public static VpnProfile get(String key) {
		if(instance==null)
			return null;
		return instance.profiles.get(key);
		
	}


	
	private ProfileManager() { }
	
	private static void checkInstance(Context context) {
		if(instance == null) {
			instance = new ProfileManager();
			instance.loadVPNList(context);
		}
	}

	public static ProfileManager getInstance(Context context) {
		checkInstance(context);
		return instance;
	}
	
	
	
	public Collection<VpnProfile> getProfiles() {
		return profiles.values();
	}
	
	public VpnProfile getProfileByName(String name) {
		for (VpnProfile vpnp : profiles.values()) {
			if(vpnp.getName().equals(name)) {
				return vpnp;
			}
		}
		return null;			
	}

	public void saveProfileList(Context context) {
		SharedPreferences sharedprefs = context.getSharedPreferences(PREFS_NAME,Activity.MODE_PRIVATE);
		Editor editor = sharedprefs.edit();
		editor.putStringSet("vpnlist", profiles.keySet());
		
		// For reasing I do not understand at all 
		// Android saves my prefs file only one time 
		// if I remove the debug code below :(
		int counter = sharedprefs.getInt("counter", 0);
		editor.putInt("counter", counter+1);
		editor.apply();

	}

	public void addProfile(VpnProfile profile) {
		profiles.put(profile.getUUID().toString(),profile);
		
	}
	
	
	public void saveProfile(Context context,VpnProfile profile) {
		// First let basic settings save its state
		
		ObjectOutputStream vpnfile;
		try {
			vpnfile = new ObjectOutputStream(context.openFileOutput((profile.getUUID().toString() + ".vp"),Activity.MODE_PRIVATE));

			vpnfile.writeObject(profile);
			vpnfile.flush();
			vpnfile.close();
		} catch (FileNotFoundException e) {

			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {

			e.printStackTrace();
			throw new RuntimeException(e);

		}
	}
	
	
	private void loadVPNList(Context context) {
		profiles = new HashMap<String, VpnProfile>();
		SharedPreferences listpref = context.getSharedPreferences(PREFS_NAME,Activity.MODE_PRIVATE);
		Set<String> vlist = listpref.getStringSet("vpnlist", null);
		Exception exp =null;
		if(vlist==null){
			vlist = new HashSet<String>();
		}

		for (String vpnentry : vlist) {
			try {
				ObjectInputStream vpnfile = new ObjectInputStream(context.openFileInput(vpnentry + ".vp"));
				VpnProfile vp = ((VpnProfile) vpnfile.readObject());

				profiles.put(vp.getUUID().toString(), vp);

			} catch (StreamCorruptedException e) {
				exp=e;
			} catch (FileNotFoundException e) {
				exp=e;
			} catch (IOException e) {
				exp=e;
			} catch (ClassNotFoundException e) { 
				exp=e;
			}
			if(exp!=null) {
				exp.printStackTrace();
			}
		}
	}

	public int getNumberOfProfiles() {
		return profiles.size();
	}



	public void removeProfile(Context context,VpnProfile profile) {
		String vpnentry = profile.getUUID().toString();
		profiles.remove(vpnentry);
		saveProfileList(context);
		context.deleteFile(vpnentry + ".vp");
		
	}



	public static VpnProfile get(Context context, String profileUUID) {
		checkInstance(context);
		return get(profileUUID);
	}

}