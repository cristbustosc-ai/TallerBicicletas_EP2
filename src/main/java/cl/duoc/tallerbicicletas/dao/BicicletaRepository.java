package cl.duoc.tallerbicicletas.dao;

import cl.duoc.tallerbicicletas.model.Bicicleta;
import cl.duoc.tallerbicicletas.model.BicicletaElectrica;
import cl.duoc.tallerbicicletas.model.BicicletaMontanya;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class BicicletaRepository {

    private final BicicletaDao<BicicletaElectrica> daoElectricas;
    private final BicicletaDao<BicicletaMontanya> daoMontanyas;
    private List<BicicletaElectrica> electricas = new ArrayList<>();
    private List<BicicletaMontanya> montanyas = new ArrayList<>();

    public BicicletaRepository(Path archivoElectricas, Path archivoMontanyas) {
        this.daoElectricas = new BicicletaJsonDao<>(archivoElectricas, BicicletaElectrica.class);
        this.daoMontanyas = new BicicletaJsonDao<>(archivoMontanyas, BicicletaMontanya.class);
    }


    public void cargar() throws IOException {
        List<String> errores = new ArrayList<>();
        try {
            electricas = daoElectricas.cargar();
        } catch (IOException e) {
            electricas = new ArrayList<>();
            errores.add(e.getMessage());
        }
        try {
            montanyas = daoMontanyas.cargar();
        } catch (IOException e) {
            montanyas = new ArrayList<>();
            errores.add(e.getMessage());
        }
        if (!errores.isEmpty()) {
            throw new IOException(String.join(" ", errores));
        }
    }


    public List<Bicicleta> obtenerTodas() {
        List<Bicicleta> todas = new ArrayList<>(electricas);
        todas.addAll(montanyas);
        return todas;
    }


    public void agregar(Bicicleta bicicleta) throws IOException {
        if (bicicleta instanceof BicicletaElectrica e) {
            electricas.add(e);
            try {
                daoElectricas.guardar(electricas);
            } catch (IOException ex) {
                electricas.remove(e);
                throw ex;
            }
        } else if (bicicleta instanceof BicicletaMontanya m) {
            montanyas.add(m);
            try {
                daoMontanyas.guardar(montanyas);
            } catch (IOException ex) {
                montanyas.remove(m);
                throw ex;
            }
        } else {
            throw new IllegalArgumentException("Tipo de bicicleta no soportado.");
        }
    }

    public void eliminar(Bicicleta bicicleta) throws IOException {
        if (bicicleta instanceof BicicletaElectrica e) {
            int posicion = electricas.indexOf(e);
            if (posicion < 0) {
                return;
            }
            electricas.remove(posicion);
            try {
                daoElectricas.guardar(electricas);
            } catch (IOException ex) {
                electricas.add(posicion, e);
                throw ex;
            }
        } else if (bicicleta instanceof BicicletaMontanya m) {
            int posicion = montanyas.indexOf(m);
            if (posicion < 0) {
                return;
            }
            montanyas.remove(posicion);
            try {
                daoMontanyas.guardar(montanyas);
            } catch (IOException ex) {
                montanyas.add(posicion, m);
                throw ex;
            }
        }
    }
}
