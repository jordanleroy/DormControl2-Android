package com.fouadchamoun.dormcontrol2;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener{


    private SwipeRefreshLayout refreshLayout;
    private LinearLayout layoutNextBus;
    private TextView labelTemperature, labelNextBus;
    private boolean stateDeskLed, stateDeskLamp, stateBigLamp, stateBedLamp;
    private ImageButton img0, img1, img2, img3, img4, img5;
    private int stateMasterSwitch;
    private LinearLayout seekLayout;
    private Animation animationFadeIn, animationFadeOut;
    private SeekBar seekLed;
    private int brightness;
    private Handler mHandler;


    @Override
    public void onRefresh() {
        //refreshContent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);

        mHandler = new Handler();

        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);


        layoutNextBus = (LinearLayout) findViewById(R.id.layoutNextBus);
        labelNextBus = (TextView) findViewById(R.id.labelNextBus);


        labelTemperature = (TextView) findViewById(R.id.labelTemperature);

        img0 = (ImageButton) findViewById(R.id.img0);
        img1 = (ImageButton) findViewById(R.id.img1);
        img2 = (ImageButton) findViewById(R.id.img2);
        img3 = (ImageButton) findViewById(R.id.img3);
        img4 = (ImageButton) findViewById(R.id.img4);
        img5 = (ImageButton) findViewById(R.id.img5);


        seekLayout = (LinearLayout) findViewById(R.id.seekLayout);
        seekLed=(SeekBar) findViewById(R.id.seekLed);




        seekLed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                runOnUiThread(new Runnable(){
                    public void run() {
                        brightness = seekLed.getProgress() + 10;
                        String message = "LED Brightness: " + calculateBrightnessPercentage(brightness) + "%";
                        Toast.makeText(getApplicationContext(), message , Toast.LENGTH_SHORT).show();
                    }
                });
                new POSTdeskLed("http://www.fouadchamoun.com:8000/fadeLed/" + brightness).execute();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }
        });



        img0.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                callGETtempMethod();
            }
        });

        img1.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stateDeskLed)
                    new POSTdeskLed("http://www.fouadchamoun.com:8000/fadeLed/175").execute();
                else
                    new POSTdeskLed("http://www.fouadchamoun.com:8000/fadeLed/0").execute();
            }
        });

        img2.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stateDeskLamp)
                    new POSTdeskLamp("http://www.fouadchamoun.com:8000/desklamp/1").execute();
                else
                    new POSTdeskLamp("http://www.fouadchamoun.com:8000/desklamp/0").execute();
            }
        });

        img3.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stateBigLamp)
                    new POSTbigLamp("http://www.fouadchamoun.com:8000/biglamp/1").execute();
                else
                    new POSTbigLamp("http://www.fouadchamoun.com:8000/biglamp/0").execute();
            }
        });

        img4.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stateBedLamp)
                    new POSTbedLamp("http://www.fouadchamoun.com:8000/bedlamp/1").execute();
                else
                    new POSTbedLamp("http://www.fouadchamoun.com:8000/bedlamp/0").execute();
            }
        });

        img5.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stateMasterSwitch == 0){
                    new POSTallLights("http://www.fouadchamoun.com:8000/all/1").execute();
                } else {
                    new POSTallLights("http://www.fouadchamoun.com:8000/all/0").execute();
                }
            }
        });


    }

    @Override
    protected void onResume(){
        super.onResume();

        getStateAll();

        callGETtempMethod();

        //Display next bus in the morning
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour == 7)
            startRepeatingTask();
        //startRepeatingTask();

    }

    @Override
    protected void onPause(){
        super.onPause();
        stopRepeatingTask();
    }

    private void getStateAll(){
       new GETall("http://www.fouadchamoun.com:8000/getall").execute();
    }

    private void callGETtempMethod(){
        new GETtemp("http://www.fouadchamoun.com:8000/temp").execute();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            /*//Display next bus in the morning
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);


            layoutNextBus.setVisibility(View.VISIBLE);

            callGETBusMethod(hour, minute);

            //updateStatus(); //this function can change value of mInterval.
            mHandler.postDelayed(mStatusChecker, 30000);*/
            System.out.println(refreshLayout.isRefreshing());
            mHandler.postDelayed(mStatusChecker, 1000);
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        layoutNextBus.setVisibility(View.INVISIBLE);
        mHandler.removeCallbacks(mStatusChecker);
    }



    private void callGETBusMethod(int hour, int minute){
        String m_minute = String.valueOf(minute);
        if (minute < 10)
            m_minute = "0"+m_minute;
        String m_hour = String.valueOf(hour);
        if (hour < 10)
            m_hour = "0"+m_hour;
        new GETbus("https://api.navitia.io/v1" +
            "/coverage/fr-nw" +
            "/stop_points/stop_point:ORE:SP:2011" +
            "/departures" +
            "?from_datetime=20150128T" +
            m_hour + //HOUR
            m_minute + //MINUTE
            "00" + //SECOND
            "&start_page=0" +
            "&count=1").execute();
    }

    private String getBusDataToDisplay(int busHour, int busMinute, int remainingHours, int remainingMinutes){
        String m_minute = String.valueOf(busMinute);
        if (busMinute < 10)
            m_minute = "0"+m_minute;
        StringBuilder displayedData = new StringBuilder();

        displayedData.append(busHour).append("h").append(m_minute).append(" (");
        if (remainingHours !=0)
            displayedData.append(remainingHours).append("h").append(remainingMinutes).append(")");
        else
            displayedData.append(remainingMinutes).append(" min)");

        return displayedData.toString();
    }

    private void updateSeekBar(){
        seekLed.setProgress(brightness-10);
    }

    private int calculateBrightnessPercentage(int rawBrightness){
        return (rawBrightness * 100) / 350;
    }


    private void updateIcons(){
        updateMasterIcon();
        updateIcon1();
        updateIcon2();
        updateIcon3();
        updateIcon4();
    }


    private void updateMasterSwitchState(){
        if (!stateDeskLed && !stateDeskLamp && !stateBigLamp && !stateBedLamp)
            stateMasterSwitch=0;
        else if(stateDeskLed && stateDeskLamp && stateBigLamp && stateBedLamp)
            stateMasterSwitch=2;
        else
            stateMasterSwitch=1;

        updateMasterIcon();
    }

    private void updateMasterIcon(){
        switch (stateMasterSwitch){
            case 0:
            {
                img5.setBackgroundColor(getResources().getColor(R.color.white));
                img5.setColorFilter(getResources().getColor(R.color.black));
                img5.setAlpha((float) 1);
                break;
            }
            case 1:
            {
                img5.setBackgroundColor(getResources().getColor(R.color.orange));
                img5.setColorFilter(getResources().getColor(R.color.white));
                img5.setAlpha((float) 0.87);
                break;
            }
            case 2:
            {
                img5.setBackgroundColor(getResources().getColor(R.color.green));
                img5.setColorFilter(getResources().getColor(R.color.white));
                img5.setAlpha((float) 0.87);
                break;
            }
        }
    }




    private void updateIcon1(){
        img1.setActivated(stateDeskLed);
        if(stateDeskLed) {
            if(seekLayout.getVisibility() == View.INVISIBLE){
                seekLayout.setVisibility(View.VISIBLE);
                seekLayout.startAnimation(animationFadeIn);
            }
            img1.setColorFilter(getResources().getColor(R.color.pink));
            img1.setAlpha((float) 1);
            img1.setElevation((float) 6);
        } else {
            if(seekLayout.getVisibility() == View.VISIBLE){
                seekLayout.startAnimation(animationFadeOut);
                seekLayout.setVisibility(View.INVISIBLE);
            }
            img1.setColorFilter(getResources().getColor(R.color.black));
            img1.setAlpha((float) 0.54);
            img1.setElevation((float) 0);
        }
    }

    private void updateIcon2(){
        img2.setActivated(stateDeskLamp);
        if(stateDeskLamp) {
            img2.setColorFilter(getResources().getColor(R.color.pink));
            img2.setAlpha((float) 1);
            img2.setElevation((float) 6);
        } else {
            img2.setColorFilter(getResources().getColor(R.color.black));
            img2.setAlpha((float) 0.54);
            img2.setElevation((float) 0);
        }
    }

    private void updateIcon3(){
        img3.setActivated(stateBigLamp);
        if(stateBigLamp) {
            img3.setColorFilter(getResources().getColor(R.color.pink));
            img3.setAlpha((float) 1);
            img3.setElevation((float) 6);
        } else {
            img3.setColorFilter(getResources().getColor(R.color.black));
            img3.setAlpha((float) 0.54);
            img3.setElevation((float) 0);
        }
    }

    private void updateIcon4(){
        img4.setActivated(stateBedLamp);
        if(stateBedLamp) {
            img4.setColorFilter(getResources().getColor(R.color.pink));
            img4.setAlpha((float) 1);
            img4.setElevation((float) 6);
        } else {
            img4.setColorFilter(getResources().getColor(R.color.black));
            img4.setAlpha((float) 0.54);
            img4.setElevation((float) 0);
        }
    }


    private void updateBusLabel(String displayedData, int remainingHours, int remainingMinutes){
        int remainingTime = remainingHours * 60 + remainingMinutes;
        labelNextBus.setText(displayedData);
        labelNextBus.setTextColor(getResources().getColor(R.color.green));
        if (remainingTime < 10)
            labelNextBus.setTextColor(getResources().getColor(R.color.orange));
        if (remainingTime < 5)
            labelNextBus.setTextColor(getResources().getColor(R.color.red));
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_refresh) {
        //    new GETtemp("http://www.fouadchamoun.com:8000/temp").execute();
        //}

        return super.onOptionsItemSelected(item);
    }







    private class GETall extends AsyncTask<Void, JSONObject, JSONObject> {
        private String url;
        AndrestClient rest = new AndrestClient();

        public GETall(String url){
            this.url = url;
        }

        @Override
        protected JSONObject doInBackground(Void... arg0) {
            try {
                return rest.request(url, "POST", null); // Do request
            } catch (Exception e) {
                System.out.println(e.getMessage());
                runOnUiThread(new Runnable() {
                    public void run() {
                        //error_flag = true;
                        Toast.makeText(getApplicationContext(), "error get all", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                public void run() {
                    //error_flag = true;
                    Toast.makeText(getApplicationContext(), "Updating state", Toast.LENGTH_SHORT).show();
                }
            });
            //refreshLayout.setEnabled(false);
            //refreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(JSONObject data){
            super.onPostExecute(data);
            try {
                brightness = data.getInt("brightness");
                stateDeskLed = brightness != 0;
                stateDeskLamp = data.getBoolean("stateDeskLamp");
                stateBigLamp = data.getBoolean("stateBigLamp");
                stateBedLamp = data.getBoolean("stateBedLamp");

                updateMasterSwitchState();
                updateIcons();
                updateSeekBar();
                //refreshLayout.setRefreshing(false);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "State updated", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    private class GETtemp extends AsyncTask<Void, String, String> {
        private boolean error_flag=false;
        private String url;
        AndrestClient rest = new AndrestClient();

        public GETtemp(String url){
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                return rest.request(url, "GET"); // Do request
            } catch (Exception e) {
                runOnUiThread(new Runnable(){
                    public void run() {
                        error_flag = true;
                        Toast.makeText(getApplicationContext(), "error temperature", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            img0.setAlpha((float) 1);
            img0.setColorFilter(getResources().getColor(R.color.pink));

            labelTemperature.setTextSize(1, 20);
            labelTemperature.setText("refreshing");
        }

        @Override
        protected void onPostExecute(String data){
            super.onPostExecute(data);
            try {
                if(data != null){
                    if (!data.substring(3,3).equals("0"))
                        labelTemperature.setText(data.substring(0,4) + "°C");
                    else
                        labelTemperature.setText(data.substring(0,2) + "°C");


                    labelTemperature.setTextSize(1, 40);
                    img0.setAlpha((float) 0.54);
                    img0.setColorFilter(getResources().getColor(R.color.black));
                }
                else{
                    if (!error_flag)
                        callGETtempMethod();
                    else{
                        labelTemperature.setText("Connection Error");
                        img0.setAlpha((float) 0.54);
                        img0.setColorFilter(getResources().getColor(R.color.black));
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    private class POSTdeskLed extends AsyncTask<Void, String, String> {

        private String url;
        AndrestClient rest = new AndrestClient();

        public POSTdeskLed(String url){
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                return rest.request(url, "POST"); // Do request
            } catch (Exception e) {
                runOnUiThread(new Runnable(){
                    public void run() {
                        Toast.makeText(getApplicationContext(), "error desk led", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data){
            super.onPostExecute(data);
            try {
                brightness = Integer.parseInt(data);
                stateDeskLed = brightness != 0;

                updateMasterSwitchState();
                updateIcon1();
                updateSeekBar();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private class POSTdeskLamp extends AsyncTask<Void, String, String> {

        private String url;
        AndrestClient rest = new AndrestClient();

        public POSTdeskLamp(String url){
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                return rest.request(url, "POST"); // Do request
            } catch (Exception e) {
                runOnUiThread(new Runnable(){
                    public void run() {
                        Toast.makeText(getApplicationContext(), "error desk lamp", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data){
            super.onPostExecute(data);
            try {
                stateDeskLamp=data.equals("1");
                updateMasterSwitchState();
                updateIcon2();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    private class POSTbigLamp extends AsyncTask<Void, String, String> {

        private String url;
        AndrestClient rest = new AndrestClient();

        public POSTbigLamp(String url){
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                return rest.request(url, "POST"); // Do request
            } catch (Exception e) {
                runOnUiThread(new Runnable(){
                    public void run() {
                        Toast.makeText(getApplicationContext(), "error big lamp", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data){
            super.onPostExecute(data);
            try {
                stateBigLamp=data.equals("1");
                updateMasterSwitchState();
                updateIcon3();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    private class POSTbedLamp extends AsyncTask<Void, String, String> {

        private String url;
        AndrestClient rest = new AndrestClient();

        public POSTbedLamp(String url){
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                return rest.request(url, "POST"); // Do request
            } catch (Exception e) {
                runOnUiThread(new Runnable(){
                    public void run() {
                        Toast.makeText(getApplicationContext(), "error bed lamp", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(String data){
            super.onPostExecute(data);
            try {
                stateBedLamp=data.equals("1");
                updateMasterSwitchState();
                updateIcon4();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    private class POSTallLights extends AsyncTask<Void, String, String> {

        private String url;
        AndrestClient rest = new AndrestClient();

        public POSTallLights(String url){
            this.url = url;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                return rest.request(url, "POST"); // Do request
            } catch (Exception e) {
                runOnUiThread(new Runnable(){
                    public void run() {
                        Toast.makeText(getApplicationContext(), "error all lights", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "switching all lights", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        protected void onPostExecute(String data){
            super.onPostExecute(data);
            try {
                boolean actualState = data.equals("1");
                stateDeskLed = actualState;
                stateDeskLamp = actualState;
                stateBigLamp = actualState;
                stateBedLamp = actualState;
                brightness = 175;
                updateMasterSwitchState();
                updateIcons();
                updateSeekBar();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        }
    }

    private class GETbus extends AsyncTask<Void, JSONObject, JSONObject> {

        private String url;
        AndrestClient rest = new AndrestClient();

        public GETbus(String url) {
            this.url = url;
            System.out.println(url);
        }

        @Override
        protected JSONObject doInBackground(Void... arg0) {
            try {
                Map<String, String> data = new HashMap<>();

                data.put("Authorization", "a6682fd7-7d9c-4d02-98e5-659bc19c3f0b");

                return rest.request(url, "GET", data); // Do request

            } catch (Exception e) {
                System.out.println(e.getMessage());
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "error bus", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            labelNextBus.setTextColor(getResources().getColor(R.color.black));
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "fetching bus data", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        protected void onPostExecute(JSONObject data) {
            super.onPostExecute(data);
            try {
                //Get RAW data from API
                String extractedData = data.getJSONArray("departures").getJSONObject(0).getJSONObject("stop_date_time").getString("arrival_date_time");

                //Extract the time of the next bus: Hour and Minute
                int busHour = Integer.parseInt(extractedData.substring(9, 11));
                int busMinute = Integer.parseInt(extractedData.substring(11, 13));

                //Calculate Remaining Hours and Minutes
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                if (hour == 0)
                    hour = 24;
                int minute = calendar.get(Calendar.MINUTE);
                int remainingHours = busHour - hour;
                int remainingMinutes = busMinute - minute;


                //Generate the final data to display in application
                String displayedData = getBusDataToDisplay(busHour, busMinute, remainingHours, remainingMinutes);

                //Update the TextView with the final data
                updateBusLabel(displayedData, remainingHours, remainingMinutes);


            } catch (Exception e) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "bus data error", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }
    }




}
