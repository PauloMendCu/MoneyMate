package services;

import java.util.List;

import entities.Categoria;
import entities.Cuenta;
import entities.Movimiento;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface IFinanceService {
    @GET("/api/v1/movimientos")
    Call<List<Movimiento>> getMovimientos();

    @GET("/api/v1/cuentas")
    Call<List<Cuenta>> getCuentas();


    @GET
    Call<List<Movimiento>> getMovimientosPorURL(@Url String url);
}
