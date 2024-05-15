package services;

import java.util.List;

import entities.Categoria;
import entities.TarjetaCredito;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ICategoriaService {
    @GET("/Categoria")
    Call<List<Categoria>> getCategorias();

    @GET("/Credito")
    Call<List<TarjetaCredito>> getCreditos();

    @POST("/Credito")
    Call<TarjetaCredito> registrarTarjetaCredito(@Body TarjetaCredito tarjetaCredito);
}