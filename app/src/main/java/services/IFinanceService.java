package services;

import java.util.List;

import entities.Cuenta;
import entities.Movimiento;
import retrofit2.Call;
import retrofit2.http.GET;

public interface IFinanceService {
    @GET("/todos-los-movimientos")
    Call<List<Movimiento>> getTodosLosMovimientos();
}
