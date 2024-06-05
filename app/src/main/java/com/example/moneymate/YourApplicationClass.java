package com.example.moneymate;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.widget.Toast;

import java.util.List;

import entities.AppDatabase;
import entities.Cuenta;
import entities.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.IFinanceService;

public class YourApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Registrar el receptor de conectividad
        registerReceiver(new ConnectivityReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    public void syncDataWithServer() {
        AppDatabase db = AppDatabase.getInstance(this);
        List<Cuenta> cuentasNoSincronizadas = db.cuentaDao().getCuentasNoSincronizadas();

        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);

        for (Cuenta cuenta : cuentasNoSincronizadas) {
            api.crearCuenta(cuenta).enqueue(new Callback<Cuenta>() {
                @Override
                public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                    if (response.isSuccessful()) {
                        cuenta.setIsSynced(true);
                        db.cuentaDao().update(cuenta);
                    }
                }

                @Override
                public void onFailure(Call<Cuenta> call, Throwable t) {
                    Toast.makeText(YourApplicationClass.this, "No se pudo conectar al servidor para la sincronizacion", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
