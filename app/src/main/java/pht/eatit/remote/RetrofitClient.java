package pht.eatit.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofitFCM = null;
    private static Retrofit retrofitMap = null;

    public static Retrofit getFCMClient(String baseUrl){
        if(retrofitFCM == null){
            retrofitFCM = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofitFCM;
    }

    public static Retrofit getMapClient(String baseUrl){
        if(retrofitMap == null){
            retrofitMap = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }

        return retrofitMap;
    }
} 