package department.ui.utils;

import department.di.Injector;
import javafx.fxml.FXMLLoader;
import javafx.util.Callback;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.val;

import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Created by Максим on 1/31/2017.
 */
@Value
@Getter(value = AccessLevel.NONE)
public final class UiUtils {

    /**
     * default callback which takes controllers from app context as beans
     */
    static Callback<Class<?>, Object> DEFAULT_CALLBACK = cl ->
            Objects.requireNonNull(Injector.getInstance().getContext().getBean(cl),
                    String.format("Controller %s wasn't found. Are you missing component registering?", cl));


    private UiUtils() {
        throw new IllegalStateException("shouldn't be called");
    }

    public static FXMLLoader newLoader(String filePath) {
        return UiUtils.newLoader(filePath, null);
    }

    public static FXMLLoader newLoader(String filePath, ResourceBundle bundle) {
        Objects.requireNonNull(filePath, "path to a file wasn't specified!");

        val loader = new FXMLLoader(UiUtils.class.getResource(filePath), bundle);

        loader.setControllerFactory(DEFAULT_CALLBACK);
        return loader;
    }

}
