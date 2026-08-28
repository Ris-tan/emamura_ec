package emamura_ec.form;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class AdminProductFormValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void priceMustNotBeNegative() {
        AdminProductForm form = validForm();
        form.setPrice(-1);

        Set<ConstraintViolation<AdminProductForm>> violations = validator.validate(form);

        assertTrue(violations.stream().anyMatch(violation -> "price".equals(violation.getPropertyPath().toString())));
    }

    @Test
    void stockMustNotBeNegative() {
        AdminProductForm form = validForm();
        form.setStock(-1);

        Set<ConstraintViolation<AdminProductForm>> violations = validator.validate(form);

        assertTrue(violations.stream().anyMatch(violation -> "stock".equals(violation.getPropertyPath().toString())));
    }

    private AdminProductForm validForm() {
        AdminProductForm form = new AdminProductForm();
        form.setProductName("商品");
        form.setCategoryId(1L);
        form.setPrice(1000);
        form.setStock(1);
        form.setIsActive(true);
        return form;
    }
}
