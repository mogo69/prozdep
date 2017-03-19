package department.ui.controller.view;

import department.model.ITopicModel;
import department.ui.controller.DefaultProgressMessage;
import department.ui.controller.MainController;
import department.ui.controller.edit.EditTopicController;
import department.ui.controller.model.TopicViewModel;
import department.ui.utils.UiConstants;
import department.ui.utils.UiUtils;
import department.utils.RxUtils;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;
import lombok.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.logging.Level;

import static department.ui.utils.UiConstants.RESULTS_PER_PAGE;
import static department.ui.utils.UiUtils.DATE_FLD_MAPPER;
import static department.ui.utils.UiUtils.NULLABLE_FLD_MAPPER;

/**
 * Created by Максим on 2/20/2017.
 */
@Log
@Value
@EqualsAndHashCode(callSuper = false)
@Getter(value = AccessLevel.NONE)
@Controller
public class TopicController extends ListTabController<TopicViewModel> {

    ITopicModel topicModel;
    MainController mainController;

    @Autowired
    public TopicController(ITopicModel topicModel, MainController mainController) {
        this.topicModel = topicModel;
        this.mainController = mainController;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void initSubclasses() {

        final TableColumn<TopicViewModel, String> titleCol = new TableColumn<>("Назва"),
                chiefCol = new TableColumn<>("Керівник"), startDateCol = new TableColumn<>("Початок"),
                endDateCol = new TableColumn<>("Кінець"), departmentCol = new TableColumn<>("Кафедра");

        titleCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getNameObs()));
        chiefCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getChiefScientistNameObs().map(NULLABLE_FLD_MAPPER)));
        startDateCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getStartDateObs().map(DATE_FLD_MAPPER)));
        endDateCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getEndDateObs().map(DATE_FLD_MAPPER)));
        departmentCol.setCellValueFactory(param -> RxUtils.fromRx(param.getValue().getDepartmentTitleObs().map(NULLABLE_FLD_MAPPER)));
        // setup table columns and content
        tableView.getColumns().addAll(titleCol, chiefCol, departmentCol, startDateCol, endDateCol);

        tableView.setRowFactory(tv -> {
            val row = new TableRow<TopicViewModel>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {

                    val stage = new Stage();
                    val loader = UiUtils.newLoader("/view/partials/_formEditTopic.fxml", EditTopicController.class);

                    try {
                        stage.setScene(new Scene(loader.load()));

                        EditTopicController controller = loader.getController();

                        controller.setModel(row.getItem());
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

        for (val column : tableView.getColumns()) {
            column.prefWidthProperty().bind(tableView.widthProperty().divide(size));
        }

        val progress = new DefaultProgressMessage(mainController);
        loadData(new ProgressCallback() {
                     @Override
                     public void onShow() {
                         progress.showProgress("Завантаження списку наукових тем...");
                     }

                     @Override
                     public void onHide() {
                         progress.hideProgress();
                     }

                     @Override
                     public void onFailure(Throwable th) {
                         processError(th);
                     }
                 }, topicModel.fetchTopics(0, RESULTS_PER_PAGE), topicModel.count()
        );
    }

    @Override
    protected void onNewPageIndexSelected(int oldIndex, int newIndex) {
        doLoad(newIndex);
    }

    @Override
    protected void onRefresh() {
        val indx = pagination.currentPageIndexProperty().get();

        if (indx >= 0) {
            doLoad(indx);
        }
    }

    private void doLoad(int indx) {
        val toastId = mainController.showProgress("Завантаження списку наукових тем...");

        topicModel.fetchTopics(indx * RESULTS_PER_PAGE, RESULTS_PER_PAGE)
                .doOnTerminate(() -> mainController.hideProgress(toastId))
                .subscribe(this::setTableContent, this::processError);
    }

    private void processError(Throwable th) {
        log.log(Level.WARNING, "Failed to fetch masters", th);
        mainController.showError("Не вдалося завантажити список наукових тем...", UiConstants.DURATION_NORMAL);
    }

}
