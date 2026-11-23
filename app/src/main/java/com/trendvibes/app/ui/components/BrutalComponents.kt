package com.trendvibes.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trendvibes.app.ui.theme.*

/**
 * Custom Modifier for Hard Neo-Brutalist Drop Shadow (solid rectangle behind).
 */
fun Modifier.brutalShadow(
    shadowColor: Color = BrandBorder,
    offsetX: Dp = 4.dp,
    offsetY: Dp = 4.dp
): Modifier = this.drawBehind {
    drawRect(
        color = shadowColor,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = size
    )
}

/**
 * Neo-Brutalist Button with hard shadow and press feedback.
 */
@Composable
fun BrutalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrandRed,
    textColor: Color = BrandWhite,
    borderColor: Color = BrandBorder,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHighContrast = LocalHighContrast.current
    val isDyslexia = LocalDyslexiaFont.current
    val appColors = LocalAppColors.current

    val effectiveBg = if (isHighContrast && backgroundColor == BrandRed) {
        appColors.buttonBackground
    } else {
        backgroundColor
    }

    val effectiveText = if (isHighContrast && backgroundColor == BrandRed) {
        appColors.buttonTextColor
    } else {
        textColor
    }

    val effectiveBorder = if (isHighContrast && borderColor == BrandBorder) {
        appColors.border
    } else {
        borderColor
    }

    val effectiveBorderWidth = if (isHighContrast) appColors.borderWidth else borderWidth
    val currentOffset = if (isPressed) 0.dp else shadowOffset

    Box(
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
            .brutalShadow(shadowColor = effectiveBorder, offsetX = currentOffset, offsetY = currentOffset)
            .offset(x = if (isPressed) shadowOffset else 0.dp, y = if (isPressed) shadowOffset else 0.dp)
            .border(effectiveBorderWidth, effectiveBorder)
            .background(if (enabled) effectiveBg else BrandGrayDark)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClickLabel = text,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                color = effectiveText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                letterSpacing = if (isDyslexia) 1.5.sp else 1.sp
            )
        }
    }
}

/**
 * Neo-Brutalist Card container.
 */
@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrandWhite,
    borderColor: Color = BrandBorder,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isHighContrast = LocalHighContrast.current
    val appColors = LocalAppColors.current

    val effectiveBg = if (isHighContrast && backgroundColor == BrandWhite) {
        appColors.cardBackground
    } else {
        backgroundColor
    }

    val effectiveBorder = if (isHighContrast && borderColor == BrandBorder) {
        appColors.border
    } else {
        borderColor
    }

    val effectiveBorderWidth = if (isHighContrast) appColors.borderWidth else borderWidth

    Box(
        modifier = modifier
            .padding(end = shadowOffset, bottom = shadowOffset)
            .brutalShadow(shadowColor = effectiveBorder, offsetX = shadowOffset, offsetY = shadowOffset)
            .border(effectiveBorderWidth, effectiveBorder)
            .background(effectiveBg)
            .padding(14.dp)
    ) {
        Column(content = content)
    }
}

/**
 * Neo-Brutalist Badge / Chip.
 */
@Composable
fun BrutalBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrandYellow,
    textColor: Color = BrandBlack,
    borderColor: Color = BrandBorder
) {
    val isHighContrast = LocalHighContrast.current
    val isDyslexia = LocalDyslexiaFont.current
    val appColors = LocalAppColors.current

    val effectiveBorder = if (isHighContrast && borderColor == BrandBorder) {
        appColors.border
    } else {
        borderColor
    }

    Box(
        modifier = modifier
            .border(if (isHighContrast) 2.dp else 1.5.dp, effectiveBorder)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            letterSpacing = if (isDyslexia) 1.sp else 0.5.sp
        )
    }
}

/**
 * Neo-Brutalist Text Input Field.
 */
@Composable
fun BrutalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    val isHighContrast = LocalHighContrast.current
    val isDyslexia = LocalDyslexiaFont.current
    val appColors = LocalAppColors.current

    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = if (isHighContrast) appColors.textSecondary else BrandWhite,
            letterSpacing = if (isDyslexia) 1.sp else 0.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp, bottom = 4.dp)
                .brutalShadow(shadowColor = if (isHighContrast) appColors.border else BrandBorder, offsetX = 4.dp, offsetY = 4.dp)
                .border(if (isHighContrast) appColors.borderWidth else 2.dp, if (isHighContrast) appColors.border else BrandBorder)
                .background(if (isHighContrast) Color(0xFF111111) else BrandWhite)
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = if (isHighContrast) Color.LightGray else Color.Gray,
                        letterSpacing = if (isDyslexia) 0.8.sp else 0.sp
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                trailingIcon = trailingIcon,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = if (isHighContrast) Color.White else BrandBlack,
                    unfocusedTextColor = if (isHighContrast) Color.White else BrandBlack,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = if (isHighContrast) BrandYellow else BrandRed
                )
            )
        }
    }
}
