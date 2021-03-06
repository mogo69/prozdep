package department.ui.controller.view;

import department.model.IDepartmentModel;
import department.model.IMasterModel;
import department.ui.controller.DefaultProgressMessage;
import department.ui.controller.MainController;
import department.ui.controller.edit.EditMasterController;
import department.ui.controller.model.MasterViewModel;
import department.ui.utils.UiConstants;
import department.ui.utils.UiUtils;
import department.utils.RxUtils;
import department.utils.TextUtils;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;
import lombok.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import rx.Observable;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

import static department.ui.utils.UiUtils.DATE_FLD_MAPPER;
import static department.ui.utils.UiUtils.NULLABLE_FLD_MAPPER;

/**
 * Created by Максим on 2/1/2017.
 */
@Log
@Value
@EqualsAndHashCode(callSuper = false)
@Getter(value = AccessLevel.NONE)
@Controller
public final class MasterTabController extends ListTabController<MasterViewModel> {

    IMasterModel model;
    IDepartmentModel departmentModel;
    MainController mainController;

    @Autowired
    public MasterTabController(IMasterModel model, MainController mainController, IDepartmentModel departmentModel) {
        this.departmentModel = departmentModel;
        this.model = model;
        this.mainController = mainController;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initSubclasses() {
        searchField.setPromptText("Знайти магістрів");

        final TableColumn<MasterViewModel, String> firstNameCol = new TableColumn<>("Ім'я"),
                departmentCol = new TableColumn<>("Кафедра"),
                phoneCol = new TableColumn<>("Телефон"), topicCol = new TableColumn<>("Тема"),
                startDateCol = new TableColumn<>("Вступ"), endDateCol = new TableColumn<>("Випуск");

        firstNameCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getFirstNameObs()));
        firstNameCol.setMinWidth(100.0);
        departmentCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getDepartmentNameObs().map(NULLABLE_FLD_MAPPER)));
        phoneCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getPhoneObs().map(NULLABLE_FLD_MAPPER)));
        topicCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getTopicObs().map(NULLABLE_FLD_MAPPER)));
        startDateCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getStartDateObs().map(DATE_FLD_MAPPER)));
        endDateCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getEndDateObs().map(DATE_FLD_MAPPER)));
        // setup table columns and content
        tableView.getColumns().addAll(firstNameCol, departmentCol, phoneCol, topicCol, startDateCol, endDateCol);

        tableView.setRowFactory(tv -> {
            TableRow<MasterViewModel> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {

                    val stage = new Stage();
                    val loader = UiUtils.newLoader("/view/partials/_formEditMaster.fxml", EditMasterController.class);

                    try {
                        stage.setScene(new Scene(loader.load()));

                        EditMasterController controller = loader.getController();

                        controller.setDataModel(row.getItem());
                        stage.centerOnScreen();
                        stage.show();
                        stage.sizeToScene();
                    } catch (final IOException e) {
                        log.log(Level.SEVERE, "Failed to open form", e);
                    }
                }
            });
            return row;
        });

        val size = tableView.getColumns().size();

       /* for (val column : tableView.getColumns()) {
            column.prefWidthProperty().bind(tableView.widthProperty().divide(size).add(-4.));
        }*/
        val progress = new DefaultProgressMessage(mainController);
        loadData(new ProgressCallback() {
                     @Override
                     public void onShow() {
                         progress.showProgress("Завантаження списку магістрів...");
                     }

                     @Override
                     public void onHide() {
                         progress.hideProgress();
                     }

                     @Override
                     public void onFailure(Throwable th) {
                         processError(th);
                     }
                 }, model.fetchMasters(0, UiConstants.RESULTS_PER_PAGE), model.count()
        );
    }

    @Override
    protected void onRefresh() {
        val indx = pagination.currentPageIndexProperty().get();

        if(indx >= 0) {
            doLoad(searchField.getText(), indx);
        }
    }

    @Override
    protected void onSearch(String query) {
        doLoad(query, 0);
    }

    @Override
    protected void onNewPageIndexSelected(int oldIndex, int newIndex) {
        doLoad(searchField.getText(), newIndex);
    }

    private void doLoad(String query, int indx) {
        val toastId = mainController.showProgress("Завантаження списку магістрів...");
        final Observable<Collection<? extends MasterViewModel>> observable;

        if (TextUtils.isEmpty(query)) {
            observable = model.fetchMasters(indx * UiConstants.RESULTS_PER_PAGE, UiConstants.RESULTS_PER_PAGE);
        } else {
            observable = model.fetchMasters(query, indx * UiConstants.RESULTS_PER_PAGE, UiConstants.RESULTS_PER_PAGE);
        }

        observable.doOnTerminate(() -> mainController.hideProgress(toastId))
                .subscribe(this::setTableContent, this::processError);
    }

    private void processError(Throwable th) {
        log.log(Level.WARNING, "Failed to fetch masters", th);
        mainController.showError("Не вдалося завантажити список магістрів...", UiConstants.DURATION_NORMAL);
    }

}
