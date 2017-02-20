package department.ui.controller;

import department.model.IDepartmentModel;
import department.model.IMasterModel;
import department.model.form.MasterCreateForm;
import department.ui.controller.model.DepartmentViewModel;
import department.utils.RxUtils;
import department.utils.TextUtils;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.java.Log;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Level;

/**
 * Created by Максим on 2/7/2017.
 */
@Controller
@Log
public final class CreateMasterController extends MasterBaseController {

    private final IMasterModel model;
    private final IDepartmentModel departmentModel;

    @Autowired
    public CreateMasterController(IMasterModel model, IDepartmentModel departmentModel) {
        this.model = model;
        this.departmentModel = departmentModel;
    }

    @Override
    protected void initialize() {

        departmentComboBox.setConverter(new StringConverter<DepartmentViewModel>() {
            @Override
            public String toString(DepartmentViewModel object) {
                return String.format("%d %s", object.getId(), object.getName());
            }

            @Override
            public DepartmentViewModel fromString(String string) {
                return null;
            }
        });

        departmentModel.fetchDepartments(0, Integer.MAX_VALUE)
                .subscribe(departments -> departmentComboBox.getItems().addAll(departments)
                        , th -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setContentText("Не вдалося завантажити список кафедр");
                            alert.showAndWait();
                            log.log(Level.WARNING, "Failed to fetch departments", th);
                        }
                );
    }

    @Override
    protected void onCreate() {

        val department = departmentComboBox.valueProperty().get();

        if (department == null) {
            showWarning("Не обрано кафедру", "Для того, аби продовжити, виберіть кафедру зі списку");
            departmentComboBox.requestFocus();
            return;
        }

        val name = fullNameField.getText();

        if (TextUtils.isEmpty(name)) {
            showWarning(null, "ФІБ не вказано");
            fullNameField.requestFocus();
            return;
        }

        val start = startDatePicker.getValue();

        if (start == null) {
            showWarning(null, "Дату вступу не вказано");
            startDatePicker.requestFocus();
            return;
        }

        val end = endDatePicker.getValue();

        val form = new MasterCreateForm();

        form.setDepartment(department.getId());
        form.setName(name);
        form.setPhone(phoneField.getText());
        form.setStartDate(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        form.setEndDate(end == null ? null : Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        model.create(form).subscribe(master -> {
            log.log(Level.SEVERE, "Model created");

            if (viewRoot.getScene() == null) {
                RxUtils.fromProperty(viewRoot.sceneProperty())
                        .filter(scene -> scene != null)
                        .flatMap(scene -> RxUtils.fromProperty(scene.windowProperty()))
                        .filter(window -> window != null && window instanceof Stage)
                        .take(1)
                        .map(window -> (Stage) window)
                        .subscribe(Stage::close);
            } else {
                ((Stage) viewRoot.getScene().getWindow()).close();
            }
        }, th -> {
            log.log(Level.SEVERE, "Failed to create model", th);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Помилка");
            alert.setContentText("Не вдалося створити магістра");
            alert.showAndWait();
        });
    }

}
