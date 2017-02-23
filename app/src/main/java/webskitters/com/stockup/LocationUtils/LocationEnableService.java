package webskitters.com.stockup.LocationUtils;

import android.app.IntentService;
import android.content.Intent;

public class LocationEnableService extends IntentService
{   
	private LocationProvider mLocationProvider;	
	boolean flag=true;
	
    public LocationEnableService() 
    {
        super("LocationEnableService");
    }
    public static final String TAG = "LocationHandlerService";
 
    @Override
    protected void onHandleIntent(Intent intent) 
    {
    	flag=intent.getBooleanExtra("flag", true);
    	mLocationProvider = new LocationProvider(this, null);
    	if(flag)
    		mLocationProvider.connect();
    	else
    		mLocationProvider.disconnect();
    }
}
