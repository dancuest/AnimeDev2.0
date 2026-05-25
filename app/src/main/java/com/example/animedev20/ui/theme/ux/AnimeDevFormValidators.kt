package com.example.animedev20.ui.theme.ux

object AnimeDevFormValidators {
    data class ValidationResult(val isValid: Boolean, val message: String? = null)

    fun loginError(email: String, password: String): String? {
        if (email.isBlank() || password.isBlank()) return AnimeDevCopy.Errors.emptyFields
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return AnimeDevCopy.Errors.invalidEmail
        return null
    }

    fun registerError(displayName: String, email: String, password: String): String? {
        if (displayName.isBlank() || email.isBlank() || password.isBlank()) return AnimeDevCopy.Errors.emptyFields
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return AnimeDevCopy.Errors.invalidEmail
        if (password.length < 6) return AnimeDevCopy.Errors.shortPassword
        return null
    }

    fun forgotPasswordError(email: String): String? {
        if (email.isBlank()) return AnimeDevCopy.Errors.emptyFields
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return AnimeDevCopy.Errors.invalidEmail
        return null
    }

    fun resetPasswordError(password: String, confirm: String, token: String): String? {
        if (password.isBlank() || confirm.isBlank() || token.isBlank()) return AnimeDevCopy.Errors.emptyFields
        if (password != confirm) return AnimeDevCopy.Errors.passwordMismatch
        if (password.length < 6) return AnimeDevCopy.Errors.shortPassword
        return null
    }

    fun validateEmail(email: String): ValidationResult {
        return if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) ValidationResult(true)
        else ValidationResult(false, AnimeDevCopy.Validation.invalidEmail)
    }

    fun validatePassword(password: String): ValidationResult {
        return if (password.length >= 6) ValidationResult(true)
        else ValidationResult(false, AnimeDevCopy.Validation.shortPassword)
    }

    fun validateDisplayName(name: String): ValidationResult {
        return if (name.isNotBlank()) ValidationResult(true)
        else ValidationResult(false, AnimeDevCopy.Validation.required)
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return if (password == confirmPassword && password.isNotBlank()) ValidationResult(true)
        else ValidationResult(false, AnimeDevCopy.Validation.passwordMismatch)
    }

    fun validateResetToken(token: String): ValidationResult {
        return if (token.isNotBlank()) ValidationResult(true)
        else ValidationResult(false, AnimeDevCopy.Validation.requiredToken)
    }

    fun validateNewPassword(password: String): ValidationResult {
        return if (password.length >= 6) ValidationResult(true)
        else ValidationResult(false, AnimeDevCopy.Validation.shortPassword)
    }

    fun canSubmitLogin(email: String, password: String, isLoading: Boolean): Boolean = 
        !isLoading && validateEmail(email).isValid && validatePassword(password).isValid
    
    fun canSubmitForgotPassword(email: String, isLoading: Boolean): Boolean = 
        !isLoading && validateEmail(email).isValid
    
    fun canSubmitResetPassword(email: String, token: String, newPassword: String, confirmPassword: String, isLoading: Boolean): Boolean = 
        !isLoading && 
        validateEmail(email).isValid && 
        validateResetToken(token).isValid && 
        validateNewPassword(newPassword).isValid && 
        validateConfirmPassword(newPassword, confirmPassword).isValid

    object Validation {
        // Obsolete, use AnimeDevCopy.Validation
    }
}