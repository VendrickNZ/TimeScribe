package nz.ac.uclive.jis48.timescribe.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import nz.ac.uclive.jis48.timescribe.R

@Composable
fun CustomKeyboardOverlay(
    visible: Boolean,
    inputText: String,
    animationDurationMillis: Int = 300,
    onKeyPress: (String) -> Unit,
    onDeletePress: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    bottomPadding: PaddingValues = PaddingValues(0.dp)
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = animationDurationMillis)
        ) + fadeIn(animationSpec = tween(durationMillis = animationDurationMillis)),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(durationMillis = animationDurationMillis)
        ) + fadeOut(animationSpec = tween(durationMillis = animationDurationMillis))
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight.times(0.25f))
                    .background(Color.Black.copy(alpha = 0.85f)) // Black with 85% opacity
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        onDismiss()
                    }
            )

            ConstraintLayout(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight.times(0.75f))
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colors.surface)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    )
                    .padding(bottomPadding)
            ) {
                val (text, keypad, exit) = createRefs()

                Box(
                    modifier = Modifier
                        .constrainAs(exit) {
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                        }
                        .padding(16.dp)
                        .clickable { onDismiss() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.close_icon),
                        contentDescription = "Close",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .constrainAs(text) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(keypad.top)
                        }
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Enter number...",
                            style = MaterialTheme.typography.h5.copy(color = Color.Gray)
                        )
                    } else {
                        Text(
                            text = inputText,
                            style = MaterialTheme.typography.h5
                        )
                    }
                }
                NumericKeypad(
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
}
