package cl.duoc.tallerbicicletas.dao;

import cl.duoc.tallerbicicletas.model.Bicicleta;

import java.io.IOException;
import java.util.List;


public interface BicicletaDao<T extends Bicicleta> {


    List<T> cargar() throws IOException;

    void guardar(List<T> bicicletas) throws IOException;
}
