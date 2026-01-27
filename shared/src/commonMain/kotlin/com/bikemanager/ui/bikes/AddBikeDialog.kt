package com.bikemanager.ui.bikes

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import bikemanager.shared.generated.resources.*
import com.bikemanager.ui.components.BottomSheetDialog
import com.bikemanager.ui.components.ButtonPrimary
import com.bikemanager.ui.components.InputField
import com.bikemanager.ui.theme.Dimens
import org.jetbrains.compose.resources.stringResource

/**
 * Premium dialog for adding a bike.
 * Uses BottomSheetDialog with InputField and ButtonPrimary components.
 */
@Composable
fun AddBikeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var bikeName by remember { mutableStateOf("") }

    BottomSheetDialog(
        title = stringResource(Res.string.add_bike_title),
        onDismiss = onDismiss
    ) {
        Spacer(modifier = Modifier.height(Dimens.SpaceLg))

        InputField(
            value = bikeName,
            onValueChange = { bikeName = it },
            label = stringResource(Res.string.my_bikes),
            modifier = Modifier
        )

        Spacer(modifier = Modifier.height(Dimens.Space3xl))

        ButtonPrimary(
            text = stringResource(Res.string.confirm),
            onClick = {
                if (bikeName.isNotBlank()) {
                    onConfirm(bikeName)
                }
            },
            enabled = bikeName.isNotBlank()
        )
    }
}
