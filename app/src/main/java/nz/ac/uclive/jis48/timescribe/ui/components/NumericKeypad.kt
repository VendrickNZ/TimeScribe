package nz.ac.uclive.jis48.timescribe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import nz.ac.uclive.jis48.timescribe.R

@Composable
fun NumericKeypad(
    onKeyPress: (String) -> Unit,
    onDeletePress: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val buttonHeight = 60.dp
        val buttonWidth = 80.dp
        val spacing = 6.dp

        val (button1, button2, button3, button4, button5, button6, button7, button8, button9, button0, buttonDelete, buttonConfirm) = createRefs()

        KeyButton("1",
            onClick = { onKeyPress("1") },
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(button1) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                })

        KeyButton("2",
            onClick = { onKeyPress("2") },
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(button2) {
                    top.linkTo(button1.top)
                    start.linkTo(button1.end, spacing)
                })

        KeyButton("3",
            onClick = { onKeyPress("3") },
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(button3) {
                    top.linkTo(button1.top)
                    start.linkTo(button2.end, spacing)
                })

        KeyButton(imageRes = R.drawable.backspace_icon,
            onClick = onDeletePress,
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(buttonDelete) {
                    top.linkTo(button1.top)
                    start.linkTo(button3.end, spacing)
                })

        KeyButton("4",
            onClick = { onKeyPress("4") },
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(button4) {
                    top.linkTo(button1.bottom, spacing)
                    start.linkTo(parent.start)
                })

        KeyButton("5",
            onClick = { onKeyPress("5") },
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(button5) {
                    top.linkTo(button4.top)
                    start.linkTo(button4.end, spacing)
                })

        KeyButton("6",
            onClick = { onKeyPress("6") },
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(button6) {
                    top.linkTo(button4.top)
                    start.linkTo(button5.end, spacing)
                })


        KeyButton(imageRes = R.drawable.continue_icon,
            onClick = onConfirm,
            modifier = Modifier
                .width(buttonWidth)
                .height(buttonHeight * 3 + (spacing * 2))
                .constrainAs(buttonConfirm) {
                    top.linkTo(buttonDelete.bottom, spacing)
                    start.linkTo(button6.end, spacing)
                    bottom.linkTo(button0.bottom)
                })

        KeyButton("7",
            onClick = { onKeyPress("7") },
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(button7) {
                    top.linkTo(button4.bottom, spacing)
                    start.linkTo(parent.start)
                })

        KeyButton("8",
            onClick = { onKeyPress("8") },
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(button8) {
                    top.linkTo(button7.top)
                    start.linkTo(button7.end, spacing)
                })

        KeyButton("9",
            onClick = { onKeyPress("9") },
            modifier = Modifier
                .height(buttonHeight)
                .width(buttonWidth)
                .constrainAs(button9) {
                    top.linkTo(button7.top)
                    start.linkTo(button8.end, spacing)
                })

        KeyButton("0",
            onClick = { onKeyPress("0") },
            modifier = Modifier
                .width(buttonWidth * 3 + (spacing * 2))
                .height(buttonHeight)
                .constrainAs(button0) {
                    top.linkTo(button7.bottom, spacing)
                    start.linkTo(parent.start)
                })
    }
}


@Composable
fun KeyButton(
    label: String? = null,
    imageRes: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick, modifier = modifier
    ) {
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label ?: "Button",
                modifier = Modifier.fillMaxSize()
            )
        } else if (label != null) {
            Text(text = label, style = MaterialTheme.typography.h6)
        }
    }
}