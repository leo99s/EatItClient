package pht.eatit.remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface MapService {

    @GET
    Call<String> getAddress(@Url String url);
} 