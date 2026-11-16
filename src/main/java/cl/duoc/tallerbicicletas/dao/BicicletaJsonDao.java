package cl.duoc.tallerbicicletas.dao;

import cl.duoc.tallerbicicletas.model.Bicicleta;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO que almacena un tipo concreto de bicicleta en un archivo JSON usando Gson.
 * Se usa un archivo por tipo para no tener que serializar la clase abstracta.
 *
 * @param <T> tipo concreto de bicicleta
 */
public class BicicletaJsonDao<T extends Bicicleta> implements BicicletaDao<T> {

    private final Path archivo;
    private final Type tipoLista;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public BicicletaJsonDao(Path archivo, Class<T> clase) {
        this.archivo = archivo;
        this.tipoLista = TypeToken.getParameterized(List.class, clase).getType();
    }

    @Override
    public List<T> cargar() throws IOException {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            List<T> leidas = gson.fromJson(lector, tipoLista);
            return leidas == null ? new ArrayList<>() : new ArrayList<>(leidas);
        } catch (JsonParseException e) {
            throw new IOException("el contenido de " + archivo.getFileName() + " no es válido.", e);
        }
    }

    @Override
    public void guardar(List<T> bicicletas) throws IOException {
        // Se escribe en un archivo temporal y luego se reemplaza el original,
        // para no dejar el archivo a medio escribir si ocurre un error.
        Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        try (Writer escritor = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
            gson.toJson(bicicletas, tipoLista, escritor);
        }
        Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
    }
}
