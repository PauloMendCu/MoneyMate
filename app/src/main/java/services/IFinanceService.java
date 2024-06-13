package services;

import java.util.List;

import entities.Cuenta;
import entities.Movimiento;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IFinanceService {
    @GET("/api/v1/movimientos")
    Call<List<Movimiento>> getMovimientos();

    @GET("/api/v1/cuentas")
    Call<List<Cuenta>> getCuentas();

    @GET("/api/v1/movimientos")
    Call<List<Movimiento>> getMovimientosPorCuenta(@Query("cuentaId") int cuentaId);

    @POST("/api/v1/movimientos")
    Call<Movimiento> agregarMovimiento(@Body Movimiento movimiento);

    @GET("/api/v1/cuentas/{id}")
    Call<Cuenta> getCuentaById(@Path("id") int cuentaId);

    @PUT("/api/v1/cuentas/{id}")
    Call<Cuenta> actualizarCuenta(@Path("id") int cuentaId, @Body Cuenta cuenta);

    @PUT("/api/v1/movimientos/{id}")
    Call<Movimiento> actualizarMovimiento(@Path("id") int movimientoId, @Body Movimiento movimiento);


    @POST("/api/v1/cuentas")
    Call<Cuenta> crearCuenta(@Body Cuenta cuenta);
}
