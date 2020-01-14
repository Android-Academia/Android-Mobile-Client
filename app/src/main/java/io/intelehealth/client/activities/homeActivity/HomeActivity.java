package io.intelehealth.client.activities.homeActivity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.WorkManager;

import java.io.File;
import java.util.Locale;

import io.intelehealth.client.R;
import io.intelehealth.client.activities.activePatientsActivity.ActivePatientActivity;
import io.intelehealth.client.activities.identificationActivity.IdentificationActivity;
import io.intelehealth.client.activities.loginActivity.LoginActivity;
import io.intelehealth.client.activities.privacyNoticeActivity.PrivacyNotice_Activity;
import io.intelehealth.client.activities.searchPatientActivity.SearchPatientActivity;
import io.intelehealth.client.activities.settingsActivity.SettingsActivity;
import io.intelehealth.client.activities.todayPatientActivity.TodayPatientActivity;
import io.intelehealth.client.activities.videoLibraryActivity.VideoLibraryActivity;
import io.intelehealth.client.app.AppConstants;
import io.intelehealth.client.models.DownloadMindMapRes;
import io.intelehealth.client.networkApiCalls.ApiClient;
import io.intelehealth.client.networkApiCalls.ApiInterface;
import io.intelehealth.client.services.DownloadProtocolsTask;
import io.intelehealth.client.syncModule.SyncUtils;
import io.intelehealth.client.utilities.ConfigUtils;
import io.intelehealth.client.utilities.DownloadMindMaps;
import io.intelehealth.client.utilities.Logger;
import io.intelehealth.client.utilities.OfflineLogin;
import io.intelehealth.client.utilities.SessionManager;
import io.intelehealth.client.widget.materialprogressbar.CustomProgressDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static io.intelehealth.client.app.AppConstants.UNIQUE_WORK_NAME;

/**
 * Home Screen
 */

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();
    SessionManager sessionManager = null;
    ProgressDialog TempDialog;
    CountDownTimer CDT;
    int i = 5;

    TextView lastSyncTextView;
    Button manualSyncButton;
    IntentFilter filter;
    Myreceiver reMyreceive;
    SyncUtils syncUtils = new SyncUtils();
    CardView c1, c2, c3, c4, c5;
    private String key = null;
    private String licenseUrl = null;

    Context context;
    CustomProgressDialog customProgressDialog;
    private String mindmapURL = "";
    private DownloadMindMaps mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sessionManager = new SessionManager(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String language = sessionManager.getAppLanguage();
        if (!language.equalsIgnoreCase("")) {
            Locale locale = new Locale(language);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }
        setTitle(R.string.title_activity_login);
        context = HomeActivity.this;
        customProgressDialog = new CustomProgressDialog(context);
        reMyreceive = new Myreceiver();
        filter = new IntentFilter("lasysync");

        Logger.logD(TAG, "onCreate: " + getFilesDir().toString());
        lastSyncTextView = findViewById(R.id.lastsynctextview);
        manualSyncButton = findViewById(R.id.manualsyncbutton);
        manualSyncButton.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        c1 = findViewById(R.id.cardview_newpat);
        c2 = findViewById(R.id.cardview_find_patient);
        c3 = findViewById(R.id.cardview_today_patient);
        c4 = findViewById(R.id.cardview_active_patients);
        c5 = findViewById(R.id.cardview_video_libraby);
        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Loads the config file values and check for the boolean value of privacy key.
                ConfigUtils configUtils = new ConfigUtils(HomeActivity.this);
                if (configUtils.privacy_notice()) {
                    Intent intent = new Intent(HomeActivity.this, PrivacyNotice_Activity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(HomeActivity.this, IdentificationActivity.class);
                    startActivity(intent);
                }
            }
        });
        c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SearchPatientActivity.class);
                startActivity(intent);
            }
        });
        c3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, TodayPatientActivity.class);
                startActivity(intent);
            }
        });
        c4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ActivePatientActivity.class);
                startActivity(intent);
            }
        });
        c5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, VideoLibraryActivity.class);
                startActivity(intent);
            }
        });

        lastSyncTextView.setText(R.string.last_synced + sessionManager.getLastPulledDateTime());
        manualSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                syncUtils.syncForeground();
            }
        });
        WorkManager.getInstance().enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, AppConstants.PERIODIC_WORK_REQUEST);
        if (sessionManager.isFirstTimeLaunched()) {
            TempDialog = new ProgressDialog(HomeActivity.this, R.style.AlertDialogStyle); //thats how to add a style!
            TempDialog.setTitle(R.string.syncInProgress);
            TempDialog.setCancelable(false);
            TempDialog.setProgress(i);

            TempDialog.show();

            CDT = new CountDownTimer(7000, 1000) {
                public void onTick(long millisUntilFinished) {
                    TempDialog.setTitle(getString(R.string.syncInProgress));
                    TempDialog.setMessage(getString(R.string.please_wait));
                    i--;
                }

                public void onFinish() {
                    TempDialog.dismiss();
                    //Your Code ...
                    sessionManager.setFirstTimeLaunched(false);
                    sessionManager.setMigration(true);
                }
            }.start();

        }
        sessionManager.setMigration(true);

        if (sessionManager.isReturningUser()) {
            syncUtils.syncForeground();
        }


    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.syncOption:
//                refreshDatabases();
//                return true;
            case R.id.settingsOption:
                settings();
                return true;
            case R.id.updateProtocolsOption: {

                if (!sessionManager.getLicenseKey().isEmpty()) {

                    String licenseUrl = sessionManager.getMindMapServerUrl();
                    String licenseKey = sessionManager.getLicenseKey();
                    getMindmapDownloadURL("http://" + licenseUrl + ":3004/", licenseKey);

                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    LayoutInflater li = LayoutInflater.from(this);
                    View promptsView = li.inflate(R.layout.dialog_mindmap_cred, null);
                    dialog.setTitle(getString(R.string.enter_license_key))
                            .setView(promptsView)
                            .setPositiveButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Dialog d = (Dialog) dialog;

                                    EditText etURL = d.findViewById(R.id.licenseurl);
                                    EditText etKey = d.findViewById(R.id.licensekey);
                                    String url = etURL.getText().toString().trim();
                                    String key = etKey.getText().toString().trim();

                                    if (url.isEmpty()) {
                                        etURL.setError(getResources().getString(R.string.enter_server_url));
                                        etURL.requestFocus();
                                        return;
                                    }
                                    if (url.contains(":")){
                                        etURL.setError(getResources().getString(R.string.invalid_url));
                                        etURL.requestFocus();
                                        return;
                                    }
                                    if (key.isEmpty()) {
                                        etKey.setError(getResources().getString(R.string.enter_license_key));
                                        etKey.requestFocus();
                                        return;
                                    }

                                    sessionManager.setMindMapServerUrl(url);
                                    getMindmapDownloadURL("http://" + url + ":3004/", key);

                                }
                            })
                            .setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    dialog.create().show();

                }
                return true;
            }
            case R.id.sync:
//                pullDataDAO.pullData(this);
//                pullDataDAO.pushDataApi();
                boolean isSynced = syncUtils.syncForeground();
                AppConstants.notificationUtils.showNotifications(getString(R.string.sync_notif_title), getString(R.string.sync_background_completed), 1, this);
//                boolean i = imagesPushDAO.patientProfileImagesPush();
//                boolean o = imagesPushDAO.obsImagesPush();
                if (isSynced)
                    AppConstants.notificationUtils.showNotifications_noProgress(getString(R.string.sync_not_available), getString(R.string.please_connect_to_internet), getApplicationContext());
                else
                    AppConstants.notificationUtils.showNotifications(getString(R.string.image_upload), getString(R.string.image_upload_failed), 4, this);
                return true;
//            case R.id.backupOption:
//                manageBackup(true, false);  // to backup app data at any time of the day
//                return true;
//
//            case R.id.restoreOption:
//                manageBackup(false, false); // to restore app data if db is empty
//                return true;

            case R.id.logoutOption:
//                manageBackup(true, false);
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method starts intent to another activity to change settings
     *
     * @return void
     */
    public void settings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Logs out the user. It removes user account using AccountManager.
     *
     * @return void
     */
    public void logout() {

        OfflineLogin.getOfflineLogin().setOfflineLoginStatus(false);

//        parseLogOut();

        AccountManager manager = AccountManager.get(HomeActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Account[] accountList = manager.getAccountsByType("io.intelehealth.openmrs");
        if (accountList.length > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.removeAccount(accountList[0], HomeActivity.this, null, null);
            } else {
                manager.removeAccount(accountList[0], null, null); // Legacy implementation
            }
        }

        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

        SyncUtils syncUtils = new SyncUtils();
        syncUtils.syncBackground();

        sessionManager.setReturningUser(false);
    }


    @Override
    protected void onResume() {
        registerReceiver(reMyreceive, filter);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(reMyreceive);
        super.onDestroy();
    }

    private boolean keyVerified(String key) {
        //TODO: Verify License Key
        return true;
    }
    @Override
    public void onBackPressed() {
        /*new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to EXIT ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        finish();

                    }

                })
                .setNegativeButton("No", null)
                .show();
*/
        AlertDialog.Builder alertdialogBuilder = new AlertDialog.Builder(this);
        alertdialogBuilder.setMessage(R.string.sure_to_exit);
        alertdialogBuilder.setPositiveButton(R.string.generic_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)

            {
                moveTaskToBack(true);
               // finish();
            }
        });
        alertdialogBuilder.setNegativeButton(R.string.generic_no, null);

        AlertDialog alertDialog = alertdialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);

        positiveButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        positiveButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        negativeButton.setTextColor(getResources().getColor(R.color.colorPrimary));
        negativeButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

    }

    public class Myreceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            lastSyncTextView.setText(R.string.last_synced + sessionManager.getLastPulledDateTime());
        }
    }

    private void getMindmapDownloadURL(String url, String key) {
        customProgressDialog.show();
        ApiClient.changeApiBaseUrl(url);
        ApiInterface apiService = ApiClient.createService(ApiInterface.class);
        try {
            Observable<DownloadMindMapRes> resultsObservable = apiService.DOWNLOAD_MIND_MAP_RES_OBSERVABLE(key);
            resultsObservable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<DownloadMindMapRes>() {
                        @Override
                        public void onNext(DownloadMindMapRes res) {
                            customProgressDialog.dismiss();
                            if (res.getMessage() != null && res.getMessage().equalsIgnoreCase("Success")) {

                                Log.e("MindMapURL", "Successfully get MindMap URL");
                                mTask = new DownloadMindMaps(context);
                                mindmapURL = res.getMindmap().trim();
                                sessionManager.setLicenseKey(key);
                                checkExistingMindMaps();

                            } else {
                                Toast.makeText(context, getResources().getString(R.string.no_mindmaps_found), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            customProgressDialog.dismiss();
                            Toast.makeText(context, getResources().getString(R.string.unable_to_get_proper_response), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "changeApiBaseUrl: " + e.getMessage());
            Log.e(TAG, "changeApiBaseUrl: " + e.getStackTrace());
        }
    }

    private void checkExistingMindMaps() {

        //Check is there any existing mindmaps are present, if yes then delete.

        File engines = new File(context.getFilesDir().getAbsolutePath(), "/Engines");
        Log.e(TAG, "Engines folder=" + engines.exists());
        if (engines.exists()) {
            engines.delete();
        }
        File logo = new File(context.getFilesDir().getAbsolutePath(), "/logo");
        Log.e(TAG, "Logo folder=" + logo.exists());
        if (logo.exists()) {
            logo.delete();
        }
        File physicalExam = new File(context.getFilesDir().getAbsolutePath() + "/physExam.json");
        Log.e(TAG, "physExam.json=" + physicalExam.exists());
        if (physicalExam.exists()) {
            physicalExam.delete();
        }
        File familyHistory = new File(context.getFilesDir().getAbsolutePath() + "/famHist.json");
        Log.e(TAG, "famHist.json=" + familyHistory.exists());
        if (familyHistory.exists()) {
            familyHistory.delete();
        }
        File pastMedicalHistory = new File(context.getFilesDir().getAbsolutePath() + "/patHist.json");
        Log.e(TAG, "patHist.json=" + pastMedicalHistory.exists());
        if (pastMedicalHistory.exists()) {
            pastMedicalHistory.delete();
        }
        File config = new File(context.getFilesDir().getAbsolutePath() + "/config.json");
        Log.e(TAG, "config.json=" + config.exists());
        if (config.exists()) {
            config.delete();
        }

        //Start downloading mindmaps
        mTask.execute(mindmapURL, context.getFilesDir().getAbsolutePath() + "/mindmaps.zip");
        Log.e("DOWNLOAD", "isSTARTED");

    }

}
