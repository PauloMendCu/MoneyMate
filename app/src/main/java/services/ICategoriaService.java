package services;

import java.util.List;

import entities.Categoria;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ICategoriaService {
    @GET("/Categoria")
    Call<List<Categoria>> getCategorias();
    @POST("/Categoria")
    Call<Categoria> agregarCategoria(@Body Categoria categoria);

    @PUT("/Categoria/{id}")
    Call<Categoria> actualizarCategoria(@Path("id") int categoriaId, @Body Categoria categoria);

    @PUT("Categoria/{id}")
    Call<Categoria> updateCategoria(@Path("id") int id, @Body Categoria categoria);

    @DELETE("Categoria/{id}")
    Call<Void> deleteCategoria(@Path("id") int id);

}