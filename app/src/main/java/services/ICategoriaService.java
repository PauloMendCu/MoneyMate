package services;

import java.util.List;

import entities.Categoria;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ICategoriaService {
    @GET("/Categoria")
    Call<List<Categoria>> getCategorias();
}