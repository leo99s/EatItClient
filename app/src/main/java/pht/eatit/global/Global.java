package pht.eatit.global;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import pht.eatit.model.User;
import pht.eatit.remote.APIService;
import pht.eatit.remote.RetrofitClient;
import retrofit2.Retrofit;

public class Global {

    public static User activeUser;
    public static final String DELETE = "Delete";
    public static final String PHONE = "Phone";
    public static final String PASSWORD = "Password";
    public static final String BASE_FCM_URL = "https://fcm.googleapis.com/";

    public static APIService getFCMService(){
        return RetrofitClient.getClient(BASE_FCM_URL).create(APIService.class);
    }

    public static String convertCodeToStatus(String status) {
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