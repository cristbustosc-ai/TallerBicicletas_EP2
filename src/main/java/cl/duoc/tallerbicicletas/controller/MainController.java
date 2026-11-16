package cl.duoc.tallerbicicletas.controller;

import cl.duoc.tallerbicicletas.dao.BicicletaRepository;
import cl.duoc.tallerbicicletas.model.Bicicleta;
import cl.duoc.tallerbicicletas.model.BicicletaElectrica;
import cl.duoc.tallerbicicletas.model.BicicletaMontanya;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


public class MainController {

    private static final String TIPO_ELECTRICA = "Bicicleta Eléctrica";
    private static final String TIPO_MONTANYA = "Bicicleta de Montaña";

    private static final String ESTILO_EXITO = "-fx-text-fill: #1e7e34; -fx-font-weight: bold;";
    private static final String ESTILO_ERROR = "-fx-text-fill: #c62828; -fx-font-weight: bold;";

    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtAnio;
    @FXML private TextField txtPeso;
    @FXML private TitledPane panelElectrica;
    @FXML private TextField txtAutonomia;
    @FXML private CheckBox chkBateria;
    @FXML private CheckBox chkGarantia;
    @FXML private TitledPane panelMontanya;
    @FXML private TextField txtSuspensiones;
    @FXML private TableView<Bicicleta> tblBicicletas;
    @FXML private TableColumn<Bicicleta, String> colTipo;
    @FXML private TableColumn<Bicicleta, String> colCodigo;
    @FXML private TableColumn<Bicicleta, String> colAnio;
    @FXML private TableColumn<Bicicleta, String> colPeso;
    @FXML private TableColumn<Bicicleta, String> colCosto;
    @FXML private Label lblMensaje;

    private final BicicletaRepository repositorio = new BicicletaRepository(
            Path.of("bicicletas_electricas.json"),
            Path.of("bicicletas_montanya.json"));
    private final ObservableList<Bicicleta> filas = FXCollections.observableArrayList();
    private final DecimalFormat formatoCosto = crearFormatoCosto();

    @FXML
    private void initialize() {
        configurarSelectorTipo();
        configurarTabla();
        actualizarCamposPorTipo();

        try {
            repositorio.cargar();
        } catch (IOException e) {
            mostrarError("Error al leer los archivos: " + e.getMessage());
        }
        refrescarTabla();
    }



    @FXML
    private void onRegistrar() {
        Bicicleta bicicleta;
        try {
            bicicleta = construirBicicleta();
        } catch (IllegalArgumentException e) {
            mostrarError(e.getMessage());
            return;
        }

        try {
            repositorio.agregar(bicicleta);
        } catch (IOException e) {
            mostrarError("Error al guardar los archivos: " + e.getMessage());
            return;
        }

        refrescarTabla();
        limpiarFormulario();
        mostrarExito(bicicleta.getCodigo() + " (" + bicicleta.getTipo() + ") registrada correctamente.");
    }

    @FXML
    private void onEliminar() {
        Bicicleta seleccionada = tblBicicletas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Debe seleccionar una bicicleta de la tabla para eliminarla.");
            return;
        }

        try {
            repositorio.eliminar(seleccionada);
        } catch (IOException e) {
            mostrarError("Error al guardar los archivos: " + e.getMessage());
            return;
        }

        refrescarTabla();
        mostrarExito(seleccionada.getCodigo() + " (" + seleccionada.getTipo() + ") eliminada correctamente.");
    }


    private Bicicleta construirBicicleta() {
        String tipo = cmbTipo.getValue();
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo: debe seleccionar un tipo de bicicleta.");
        }

        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            throw new IllegalArgumentException("Código: no puede estar vacío.");
        }

        int anio = leerEntero(txtAnio, "Año de fabricación", "debe ser un número entero entre 2000 y 2026.");
        if (anio < 2000 || anio > 2026) {
            throw new IllegalArgumentException("Año de fabricación: debe ser un número entero entre 2000 y 2026.");
        }

        double peso = leerPeso();

        if (TIPO_ELECTRICA.equals(tipo)) {
            int autonomia = leerEntero(txtAutonomia, "Autonomía", "debe ser un número entero de kilómetros mayor que cero.");
            if (autonomia <= 0) {
                throw new IllegalArgumentException("Autonomía: debe ser un número entero de kilómetros mayor que cero.");
            }
            BicicletaElectrica electrica = new BicicletaElectrica(codigo, anio, peso, autonomia, chkBateria.isSelected());
            if (chkGarantia.isSelected()) {
                electrica.activarGarantiaExtendida();
            }
            return electrica;
        }

        int suspensiones = leerEntero(txtSuspensiones, "Suspensiones", "debe ser un número entero mayor o igual que cero.");
        if (suspensiones < 0) {
            throw new IllegalArgumentException("Suspensiones: debe ser un número entero mayor o igual que cero.");
        }
        return new BicicletaMontanya(codigo, anio, peso, suspensiones);
    }

    private int leerEntero(TextField campo, String nombre, String motivo) {
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nombre + ": " + motivo);
        }
    }

    private double leerPeso() {
        String texto = txtPeso.getText() == null ? "" : txtPeso.getText().trim();
        String mensaje = "Peso: debe ser un número mayor que cero.";
        if (!texto.matches("\\d+([.,]\\d+)?|[.,]\\d+")) {
            throw new IllegalArgumentException(mensaje);
        }
        double peso = Double.parseDouble(texto.replace(',', '.'));
        if (peso <= 0 || Double.isInfinite(peso)) {
            throw new IllegalArgumentException(mensaje);
        }
        return peso;
    }

    private void configurarSelectorTipo() {
        cmbTipo.setItems(FXCollections.observableArrayList(TIPO_ELECTRICA, TIPO_MONTANYA));
        // Muestra "Seleccione..." también cuando el formulario se limpia.
        cmbTipo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean vacio) {
                super.updateItem(item, vacio);
                setText(vacio || item == null ? cmbTipo.getPromptText() : item);
            }
        });
        cmbTipo.valueProperty().addListener((obs, anterior, nuevo) -> actualizarCamposPorTipo());
    }

    private void configurarTabla() {
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colAnio.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getAnioFabricacion())));
        colPeso.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPeso())));
        colCosto.setCellValueFactory(c ->
                new SimpleStringProperty("$" + formatoCosto.format(c.getValue().calcularCostoMantencion())));
        colAnio.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPeso.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCosto.setStyle("-fx-alignment: CENTER-RIGHT;");
        tblBicicletas.setItems(filas);
    }

    private void actualizarCamposPorTipo() {
        String tipo = cmbTipo.getValue();
        panelElectrica.setDisable(!TIPO_ELECTRICA.equals(tipo));
        panelMontanya.setDisable(!TIPO_MONTANYA.equals(tipo));
    }

    private void refrescarTabla() {
        filas.setAll(repositorio.obtenerTodas());
    }

    private void limpiarFormulario() {
        cmbTipo.setValue(null);
        txtCodigo.clear();
        txtAnio.clear();
        txtPeso.clear();
        txtAutonomia.clear();
        chkBateria.setSelected(false);
        chkGarantia.setSelected(false);
        txtSuspensiones.clear();
    }

    private void mostrarExito(String mensaje) {
        lblMensaje.setStyle(ESTILO_EXITO);
        lblMensaje.setText(mensaje);
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setStyle(ESTILO_ERROR);
        lblMensaje.setText(mensaje);
    }

    private static DecimalFormat crearFormatoCosto() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", simbolos);
    }
}
