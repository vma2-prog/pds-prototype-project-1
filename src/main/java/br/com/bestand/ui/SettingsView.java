package br.com.bestand.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class SettingsView extends VBox {

    public SettingsView() {
        this.getStyleClass().add("page-panel");
        this.setSpacing(18);
        this.setPadding(new Insets(8, 0, 0, 0));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Configurações");
        title.getStyleClass().add("page-title");
        title.setStyle("-fx-font-size: 20px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSalvar = new Button("Salvar alterações");
        btnSalvar.getStyleClass().add("btn-primary");

        header.getChildren().addAll(title, spacer, btnSalvar);

        VBox panel = new VBox(16);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(20));

        Label general = new Label("Preferências do sistema");
        general.setStyle("-fx-text-fill: #f2ebff; -fx-font-weight: bold; -fx-font-size: 15px;");

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(16);
        grid.setPrefWidth(900);

        int row = 0;
        grid.add(createSettingRow("Tema do painel", new ComboBox<>(javafx.collections.FXCollections.observableArrayList("Dark Purple", "Midnight", "Classic"))), 0, row++);
        grid.add(createSettingRow("Notificações", new CheckBox("Ativar alertas de estoque")), 0, row++);
        grid.add(createSettingRow("Backup automático", new CheckBox("Sincronizar backups diários")), 0, row++);
        grid.add(createSettingRow("Idioma do sistema", new ComboBox<>(javafx.collections.FXCollections.observableArrayList("Português (BR)", "English", "Español"))), 0, row++);

        VBox right = new VBox(10);
        right.setPadding(new Insets(0, 0, 0, 12));
        right.getChildren().addAll(
            new Label("Segurança e acesso"),
            createSecurityRow("Autenticação em duas etapas", new CheckBox("Ativada")),
            createSecurityRow("Sessão expira em", new ComboBox<>(javafx.collections.FXCollections.observableArrayList("30 minutos", "1 hora", "8 horas"))),
            createSecurityRow("Logs de auditoria", new CheckBox("Registrar alterações"))
        );

        HBox body = new HBox(18);
        body.setAlignment(Pos.TOP_LEFT);
        body.getChildren().addAll(grid, new Separator(), right);

        panel.getChildren().addAll(general, body);

        this.getChildren().addAll(header, panel);
    }

    private HBox createSettingRow(String labelText, javafx.scene.Node control) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 0, 8, 0));

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #d9d0f6; -fx-font-size: 13px;");

        Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        row.getChildren().addAll(label, filler, control);
        return row;
    }

    private HBox createSecurityRow(String labelText, javafx.scene.Node control) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #d9d0f6; -fx-font-size: 13px;");

        Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);

        row.getChildren().addAll(label, filler, control);
        return row;
    }
}
