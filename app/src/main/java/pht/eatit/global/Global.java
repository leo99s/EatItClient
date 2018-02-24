package pht.eatit.global;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import pht.eatit.model.User;
import pht.eatit.remote.FCMService;
import pht.eatit.remote.MapService;
import pht.eatit.remote.RetrofitClient;

public class Global {

    public static User activeUser;
    public static final String DELETE = "Delete";
    public static final String PHONE = "Phone";
    public static final String PASSWORD = "Password";
    public static final String BASE_FCM_URL = "https://fcm.googleapis.com";
    public static final String BASE_MAP_URL = "https://maps.googleapis.com";

    public static FCMService getFCMAPI(){
        return RetrofitClient.getFCMClient(BASE_FCM_URL).create(FCMService.class);
    }

    public static MapService getMapAPI(){
        return RetrofitClient.getMapClient(BASE_MAP_URL).create(MapService.class);
    }

    public static String getDeliveryStatus(String status) {
        if(status.equals("0")){
            return "Placed";
        }
        else if(status.equals("1")){
            return "On my way";
        }
        else {
            return "Shipped";
        }
    }

    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager != null){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();

            if(info != null){
                for(int i = 0; i < info.length; i++){
                    if(info[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }

        return false;
    }
} 