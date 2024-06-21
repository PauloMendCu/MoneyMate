package entities;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static Retrofit retrofitCategorias;
    private static final String BASE_URL = "https://666d0dfd7a3738f7cacb54ac.mockapi.io";
    private static final String BASE_URL_CATEGORIAS = "https://662da416a7dda1fa378afbe0.mockapi.io";

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getInstanceCategorias() {
        if (retrofitCategorias == null) {
            retrofitCategorias = new Retrofit.Builder()
                    .baseUrl(BASE_URL_CATEGORIAS)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitCategorias;
    }
}
