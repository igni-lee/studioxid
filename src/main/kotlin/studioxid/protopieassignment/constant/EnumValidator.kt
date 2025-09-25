package studioxid.protopieassignment.constant

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class EnumValidator : ConstraintValidator<ValidEnum, UserRole> {
    override fun isValid(
        value: UserRole?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        return value != null && UserRole.entries.toTypedArray().contains(value)
    }
}
