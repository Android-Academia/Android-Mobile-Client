package io.intelehealth.client.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SessionManager {
    // Shared preferences file name
    private static final String PREF_NAME = "Intelehealth";
    private static final String VISIT_ID = "visitID";
    private static final String BASE_URL = "base_url";
    private static final String ENCODED = "encoded";
    private static final String PULL_EXECUTED_TIME = "pullexecutedtime";
    public static final String KEY_PREF_SETUP_COMPLETE = "setup";
    private static final String APP_LANGUAGE = "Language";
    private static final String SESSION_ID = "sessionid";
    private static final String CREATOR_ID = "creatorid";
    private static final String PROVIDER_ID = "providerid";
    private static final String CHWNAME = "chwname";
    public static final String KEY_PREF_SERVER_URL_REST = "serverurl";
    public static final String KEY_PREF_SERVER_URL = "url";
    public static final String KEY_PREF_SERVER_URL_BASE = "serverurlbase";
    public static final String KEY_PREF_LOCATION_UUID = "locationuuid";
    public static final String KEY_PREF_LOCATION_NAME = "locationname";
    public static final String KEY_PREF_LOCATION_DESCRIPTION = "locationdesc";
    private static final String LICENSE_KEY = "licensekey";
    private static final String DATE = "date";
    private static final String TIME = "time";
    private static final String FIRST_TIME_SYNC_EXECUTE = "FIRST_TIME_SYNC_EXECUTE";
    private static final String LICENSE = "license";

    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();
    // Shared Preferences
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context _context;
    // Shared pref mode
    private int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public String getVisitId() {
        return pref.getString(VISIT_ID, "");
    }

    public void setVisitId(String token) {
        editor.putString(VISIT_ID, token);
        editor.commit();
    }

    public String getBaseUrl() {
        return pref.getString(BASE_URL, "http://openmrs.intelehealth.io");
    }

    public void setBaseUrl(String baseUrl) {
        editor.putString(BASE_URL, baseUrl);
        editor.commit();
    }

    public String getEncoded() {
        return pref.getString(ENCODED, "");
    }

    public void setEncoded(String encoded) {
        editor.putString(ENCODED, encoded);
        editor.commit();
    }

    public String getPullExcutedTime() {
        return pref.getString(PULL_EXECUTED_TIME, "2006-08-22 22:21:48 ");
    }

    public void setPullExcutedTime(String pullExcutedTime) {
        editor.putString(PULL_EXECUTED_TIME, pullExcutedTime);
        editor.commit();
    }

    public String getAppLanguage() {
        return pref.getString(APP_LANGUAGE, "");
    }

    public void setAppLanguage(String pullExcutedTime) {
        editor.putString(APP_LANGUAGE, pullExcutedTime);
        editor.commit();
    }

    public boolean isSetupComplete() {
        return pref.getBoolean(KEY_PREF_SETUP_COMPLETE, false);
    }

    public void setSetupComplete(Boolean setupComplete) {
        editor.putBoolean(KEY_PREF_SETUP_COMPLETE, setupComplete);
        editor.commit();
    }

    public String getSessionID() {
        return pref.getString(SESSION_ID, "");
    }

    public void setSessionID(String sessionID) {
        editor.putString(SESSION_ID, sessionID);
        editor.commit();
    }

    public String getProviderID() {
        return pref.getString(PROVIDER_ID, "");
    }

    public void setProviderID(String providerID) {
        editor.putString(PROVIDER_ID, providerID);
        editor.commit();
    }

    public String getCreatorID() {
        return pref.getString(CREATOR_ID, "");
    }

    public void setCreatorID(String creatorID) {
        editor.putString(CREATOR_ID, creatorID);
        editor.commit();
    }

    public String getChwname() {
        return pref.getString(CHWNAME, "");
    }

    public void setChwname(String chwname) {
        editor.putString(CHWNAME, chwname);
        editor.commit();
    }

    public String getLocationName() {
        return pref.getString(KEY_PREF_LOCATION_NAME, "");
    }

    public void setLocationName(String locationName) {
        editor.putString(KEY_PREF_LOCATION_NAME, locationName);
        editor.commit();
    }

    public String getLocationUuid() {
        return pref.getString(KEY_PREF_LOCATION_UUID, "");
    }

    public void setLocationUuid(String locationUuid) {
        editor.putString(KEY_PREF_LOCATION_UUID, locationUuid);
        editor.commit();
    }

    public String getLocationDescription() {
        return pref.getString(KEY_PREF_LOCATION_DESCRIPTION, "");
    }

    public void setLocationDescription(String locationDescription) {
        editor.putString(KEY_PREF_LOCATION_DESCRIPTION, locationDescription);
        editor.commit();
    }

    public String getServerUrl() {
        return pref.getString(KEY_PREF_SERVER_URL, "");
    }

    public void setServerUrl(String serverUrl) {
        editor.putString(KEY_PREF_SERVER_URL, serverUrl);
        editor.commit();
    }

    public String getServerUrlRest() {
        return pref.getString(KEY_PREF_SERVER_URL_REST, "");
    }

    public void setServerUrlRest(String serverUrlRest) {
        editor.putString(KEY_PREF_SERVER_URL_REST, serverUrlRest);
        editor.commit();
    }

    public String getServerUrlBase() {
        return pref.getString(KEY_PREF_SERVER_URL_BASE, "");
    }

    public void setServerUrlBase(String serverUrlBase) {
        editor.putString(KEY_PREF_SERVER_URL_BASE, serverUrlBase);
        editor.commit();
    }

    public String getLicenseKey() {
        return pref.getString(LICENSE_KEY, "");
    }

    public void setLicenseKey(String licenseKey) {
        editor.putString(LICENSE_KEY, licenseKey);
        editor.commit();
    }

    public void deleteLicensekey() {
        editor.remove(LICENSE_KEY);
        editor.commit();
    }

    public String getDate() {
        return pref.getString(DATE, "");
    }

    public void setDate(String date) {
        editor.putString(DATE, date);
        editor.commit();
    }

    public String getTime() {
        return pref.getString(TIME, "");
    }

    public void setTime(String time) {
        editor.putString(TIME, time);
        editor.commit();
    }

    public boolean valueContains(String value) {
        boolean hasvalue = false;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
        hasvalue = sharedPreferences.contains(value);
        return hasvalue;
    }

    public boolean isFirstTimeSyncExcuted() {
        return pref.getBoolean(FIRST_TIME_SYNC_EXECUTE, true);
    }

    public void setFirstTimeSyncExecute(Boolean firstTimeSyncExecute1) {
        editor.putBoolean(FIRST_TIME_SYNC_EXECUTE, firstTimeSyncExecute1);
        editor.commit();
    }

}