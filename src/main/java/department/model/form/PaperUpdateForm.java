/**
 * 
 */
package department.model.form;

import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * @author Nikolay
 *
 */
@Data
@Validated
public class PaperUpdateForm {
	private Integer id;
    private String name;
    private String type;
    private Integer year;
}
