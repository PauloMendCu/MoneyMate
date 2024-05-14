package entities;

public class TarjetaCredito {

    private int id;
    private double lineaCredito;
    private double saldoConsumido;
    private double saldoDisponible;
    private double proxPago;
    private double pagoMinimo;
    private String nombre;
    private String fechaFacturacion;
    private String fechaVencimiento;

    public TarjetaCredito(int id, double lineaCredito, double saldoConsumido, double saldoDisponible, double proxPago, double pagoMinimo, String nombre, String fechaFacturacion, String fechaVencimiento) {
        this.id = id;
        this.lineaCredito = lineaCredito;
        this.saldoConsumido = saldoConsumido;
        this.saldoDisponible = saldoDisponible;
        this.proxPago = proxPago;
        this.pagoMinimo = pagoMinimo;
        this.nombre = nombre;
        this.fechaFacturacion = fechaFacturacion;
        this.fechaVencimiento = fechaVencimiento;
    }

    public TarjetaCredito(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLineaCredito() {
        return lineaCredito;
    }

    public void setLineaCredito(double lineaCredito) {
        this.lineaCredito = lineaCredito;
    }

    public double getSaldoConsumido() {
        return saldoConsumido;
    }

    public void setSaldoConsumido(double saldoConsumido) {
        this.saldoConsumido = saldoConsumido;
    }

    public double getSaldoDisponible() {
        return saldoDisponible;
    }

    public void setSaldoDisponible(double saldoDisponible) {
        this.saldoDisponible = saldoDisponible;
    }

    public double getProxPago() {
        return proxPago;
    }

    public void setProxPago(double proxPago) {
        this.proxPago = proxPago;
    }

    public double getPagoMinimo() {
        return pagoMinimo;
    }

    public void setPagoMinimo(double pagoMinimo) {
        this.pagoMinimo = pagoMinimo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFechaFacturacion() {
        return fechaFacturacion;
    }

    public void setFechaFacturacion(String fechaFacturacion) {
        this.fechaFacturacion = fechaFacturacion;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
}
