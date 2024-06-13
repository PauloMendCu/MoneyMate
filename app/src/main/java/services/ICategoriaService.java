package services;

import java.util.List;

import entities.Categoria;
import entities.Cuenta;
import entities.Movimiento;
import entities.TarjetaCredito;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ICategoriaService {
    @GET("/Categoria")
    Call<List<Categoria>> getCategorias();

    @GET("/Credito")
    Call<List<TarjetaCredito>> getCreditos();

    @POST("/Categoria")
    Call<Categoria> agregarCategoria(@Body Categoria categoria);

    @PUT("/Categoria/{id}")
    Call<Categoria> actualizarCategoria(@Path("id") int categoriaId, @Body Categoria categoria);

    @POST("/Credito")
    Call<TarjetaCredito> registrarTarjetaCredito(@Body TarjetaCredito tarjetaCredito);
}