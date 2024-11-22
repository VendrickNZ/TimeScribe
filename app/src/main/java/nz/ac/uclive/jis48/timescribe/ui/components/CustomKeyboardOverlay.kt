package nz.ac.uclive.jis48.timescribe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout

@Composable
fun CustomKeyboardOverlay(
    inputText: String,
    onKeyPress: (String) -> Unit,
    onDeletePress: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0x80000000)) //TODO: Add this to as a colour resource
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            onDismiss()
        }
    ) {

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight.times(0.75f))
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colors.surface)
                .padding(bottomPadding)
        ) {
            val (text, keypad) = createRefs()

            Text(
                text = inputText,
                style = MaterialTheme.typography.h5,
                modifier = Modifier
                    .constrainAs(text) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(16.dp)
            )
            NumericKeypad(
                inputText = inputText,
                onKeyPress = onKeyPress,
                onDeletePress = onDeletePress,
                onConfirm = onConfirm,
                modifier = Modifier
                    .constrainAs(keypad) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(8.dp)
            )
        }
    }
}