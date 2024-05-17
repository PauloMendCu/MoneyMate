package com.example.moneymate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import entities.Cuenta;
import entities.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import services.IFinanceService;

public class NuevaCuentaActivity extends AppCompatActivity {
    private EditText etNombreCuenta, etSaldoCuenta;
    private Button btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuevacuenta);


        etNombreCuenta = findViewById(R.id.etMontoCuenta);
        etSaldoCuenta = findViewById(R.id.etSaldoCuenta);
        btnGuardar = findViewById(R.id.btn_guardar);

        ImageButton btnVerTarjetas = findViewById(R.id.btnTarjetas);
        btnVerTarjetas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NuevaCuentaActivity.this, TarjetasCreditoActivity.class);
                startActivity(intent);
            }
        });
        // Botón para ver movimientos
        ImageButton btnVerMovimientos = findViewById(R.id.btn_ver_movimientos);
        btnVerMovimientos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NuevaCuentaActivity.this, MovimientosActivity.class);
                startActivity(intent);
            }
        });

        // Botón para ver cuentas
        ImageButton btnVerCuentas = findViewById(R.id.btn_ver_cuentas);
        btnVerCuentas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NuevaCuentaActivity.this, CuentasActivity.class);
                startActivity(intent);
            }
        });

        // Botón para registrar nuevo movimiento
        ImageButton btnNuevoMovimiento = findViewById(R.id.btn_nuevo_movimiento);
        btnNuevoMovimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NuevaCuentaActivity.this, NuevoMovimientoActivity.class);
                startActivity(intent);
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCuenta();
            }
        });
    }
    private void guardarCuenta() {
        String nombreCuenta = etNombreCuenta.getText().toString().trim();
        String saldoString = etSaldoCuenta.getText().toString().trim();

        // Validaciones
        if (nombreCuenta.isEmpty()) {
            Toast.makeText(this, "Debes ingresar un nombre para la cuenta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (saldoString.isEmpty()) {
            Toast.makeText(this, "Debes ingresar un saldo inicial", Toast.LENGTH_SHORT).show();
            return;
        }

        double saldo;
        try {
            saldo = Double.parseDouble(saldoString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El saldo debe ser un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una nueva instancia de Cuenta
        Cuenta nuevaCuenta = new Cuenta();
        nuevaCuenta.setNombre(nombreCuenta);
        nuevaCuenta.setSaldo(saldo);

        // Llamar al método para guardar la cuenta en el servidor
        guardarCuentaEnServidor(nuevaCuenta);
    }
    private void guardarCuentaEnServidor(Cuenta nuevaCuenta) {
        IFinanceService api = RetrofitClient.getInstance().create(IFinanceService.class);
        api.crearCuenta(nuevaCuenta).enqueue(new Callback<Cuenta>() {
            @Override
            public void onResponse(Call<Cuenta> call, Response<Cuenta> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(NuevaCuentaActivity.this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad actual
                } else {
                    Toast.makeText(NuevaCuentaActivity.this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cuenta> call, Throwable t) {
                Toast.makeText(NuevaCuentaActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
