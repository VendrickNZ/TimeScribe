package nz.ac.uclive.jis48.timescribe.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import nz.ac.uclive.jis48.timescribe.R
import nz.ac.uclive.jis48.timescribe.ui.theme.KeyboardDarkGray
import nz.ac.uclive.jis48.timescribe.ui.theme.NearBlack

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

        val (
            button1, button2, button3, buttonDelete,
            button4, button5, button6, buttonConfirm,
            button7, button8, button9,
            button0
        ) = createRefs()

        KeyButton("1",
            onClick = { onKeyPress("1") },
            modifier = Modifier
                .height(buttonHeight)
                .constrainAs(button1) {
                    top.linkTo(parent.top)
                    width = Dimension.fillToConstraints
                })

        KeyButton("2", onClick = { onKeyPress("2") }, modifier = Modifier
            .height(buttonHeight)
            .constrainAs(button2) {
                top.linkTo(button1.top)
                width = Dimension.fillToConstraints
            })

        KeyButton("3", onClick = { onKeyPress("3") }, modifier = Modifier
            .height(buttonHeight)
            .constrainAs(button3) {
                top.linkTo(button1.top)
                width = Dimension.fillToConstraints
            })

        // backspace button
        KeyButton(
            imageRes = R.drawable.backspace_icon,
            onClick = onDeletePress,
            backgroundColour = NearBlack,
            modifier = Modifier
                .height(buttonHeight)
                .constrainAs(buttonDelete) {
                    top.linkTo(button1.top)
                    width = Dimension.fillToConstraints
                })

        createHorizontalChain(
            button1, button2, button3, buttonDelete,
            chainStyle = ChainStyle.Spread
        )

        constrain(button1) { start.linkTo(parent.start) }
        constrain(buttonDelete) { end.linkTo(parent.end) }

        KeyButton("4", onClick = { onKeyPress("4") }, modifier = Modifier
            .height(buttonHeight)
            .constrainAs(button4) {
                top.linkTo(button1.bottom)
                width = Dimension.fillToConstraints
            })

        KeyButton("5", onClick = { onKeyPress("5") }, modifier = Modifier
            .height(buttonHeight)
            .constrainAs(button5) {
                top.linkTo(button4.top)
                width = Dimension.fillToConstraints
            })

        KeyButton("6", onClick = { onKeyPress("6") }, modifier = Modifier
            .height(buttonHeight)
            .constrainAs(button6) {
                top.linkTo(button4.top)
                width = Dimension.fillToConstraints
            })

        // confirm/continue button
        KeyButton(
            imageRes = R.drawable.continue_icon,
            onClick = onConfirm,
            backgroundColour = NearBlack,
            modifier = Modifier
                .height(buttonHeight * 3)
                .constrainAs(buttonConfirm) {
                    top.linkTo(buttonDelete.bottom)
                    width = Dimension.fillToConstraints
                }
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    drawLine(
                        color = KeyboardDarkGray,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = strokeWidth
                    )
                }
                .zIndex(3f)
        )

        createHorizontalChain(
            button4, button5, button6, buttonConfirm,
            chainStyle = ChainStyle.Spread
        )

        constrain(button4) { start.linkTo(parent.start) }
        constrain(buttonConfirm) { end.linkTo(parent.end) }

        KeyButton("7", onClick = { onKeyPress("7") }, modifier = Modifier
            .height(buttonHeight)
            .constrainAs(button7) {
                top.linkTo(button4.bottom)
                width = Dimension.fillToConstraints
            })

        KeyButton("8", onClick = { onKeyPress("8") }, modifier = Modifier
            .height(buttonHeight)
            .constrainAs(button8) {
                top.linkTo(button7.top)
                width = Dimension.fillToConstraints
            })

        KeyButton("9", onClick = { onKeyPress("9") }, modifier = Modifier
            .height(buttonHeight)
            .constrainAs(button9) {
                top.linkTo(button7.top)
                width = Dimension.fillToConstraints
            })

        createHorizontalChain(
            button7, button8, button9, buttonConfirm,
            chainStyle = ChainStyle.Spread
        )

        constrain(button7) { start.linkTo(parent.start) }
        constrain(button9) { end.linkTo(parent.end) }

        KeyButton("0", onClick = { onKeyPress("0") }, modifier = Modifier
            .height(buttonHeight)
            .constrainAs(button0) {
                top.linkTo(button7.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            })

        createHorizontalChain(
            button0, buttonConfirm,
            chainStyle = ChainStyle.Spread
        )

        constrain(button0) {
            start.linkTo(parent.start)
            end.linkTo(buttonConfirm.start)
        }
        constrain(buttonConfirm) {
            start.linkTo(button0.end)
            end.linkTo(buttonDelete.end)
        }
    }
}


@Composable
fun KeyButton(
    label: String? = null,
    imageRes: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColour: Color = KeyboardDarkGray,
    contentColour: Color = contentColorFor(backgroundColour),
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColour,
            contentColor = contentColour
        ),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = label ?: "Button",
                colorFilter = ColorFilter.tint(contentColour),
                modifier = Modifier.fillMaxSize()
            )
        } else if (label != null) {
            Text(text = label, style = MaterialTheme.typography.h6)
        }
    }
}