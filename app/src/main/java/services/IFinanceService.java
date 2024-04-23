package services;

import java.util.List;

import entities.Cuenta;
import entities.Movimiento;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface IFinanceService {
    @GET("/api/v1/movimientos")
    Call<List<Movimiento>> getMovimientos();

    @GET("/api/v1/cuentas")
    Call<List<Cuenta>> getCuentas();

    @GET("/api/v1/cuentas/{id}/movimientos")
    Call<List<Movimiento>> getMovimientosPorCuenta(@Path("id") int cuentaId);
}
