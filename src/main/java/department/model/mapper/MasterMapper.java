package department.model.mapper;

import department.model.bo.Master;
import department.ui.controller.model.MasterViewModel;
import department.utils.Preconditions;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by Максим on 2/17/2017.
 */
public final class MasterMapper {

    private MasterMapper() {
        throw new RuntimeException();
    }

    public static MasterViewModel toViewModel(Master master) {
        Preconditions.notNull(master);
        return new MasterViewModel.Builder(master.getId(), master.getName(), master.getStartDate())
                .setTopic(master.getTopic())
                .setEndDate(master.getEndDate())
                .setPhone(master.getPhone())
                .setTeacherId(master.getTeacher() == null ? null : master.getTeacher().getId()).build();
    }

    public static Collection<? extends MasterViewModel> toViewModel(Collection<? extends Master> masters) {
        return masters.stream().map(MasterMapper::toViewModel).collect(Collectors.toList());
    }

}