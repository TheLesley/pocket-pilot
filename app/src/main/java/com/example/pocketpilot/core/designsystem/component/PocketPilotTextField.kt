package com.example.pocketpilot.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.pocketpilot.core.designsystem.theme.PocketPilotTheme

@Composable
fun PocketPilotTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    supportingText: String? = null,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val isError = errorMessage != null
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        isError = isError,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        supportingText = {
            when {
                isError -> Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                supportingText != null -> Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        shape = MaterialTheme.shapes.medium
    )
}

@Preview
@Composable
private fun TextFieldPreview() {
    PocketPilotTheme {
        PocketPilotTextField(
            value = "12.50",
            onValueChange = {},
            label = "Amount",
            supportingText = "Enter a positive value"
        )
    }
}

@Preview
@Composable
private fun TextFieldErrorPreview() {
    PocketPilotTheme {
        PocketPilotTextField(
            value = "abc",
            onValueChange = {},
            label = "Amount",
            errorMessage = "Amount must be a number"
        )
    }
}
