package pht.eatit.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface MapService {

    @GET
    Call<String> getAddress(@Url String url);

    @GET("maps/api/geocode/json")
    Call<String> getGeoCode(@Query("address") String address);
} 