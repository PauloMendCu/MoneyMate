package entities;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Cuenta {
    private int id;
    private String nombre;
    private double saldo;
    private int tipo;

    public Cuenta(int id, String nombre, double saldo, int tipo) {
        this.id = id;
        this.nombre = nombre;
        this.saldo = saldo;
        this.tipo = tipo;
    }

    public Cuenta() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }
}