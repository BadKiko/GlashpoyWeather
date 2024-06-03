package com.example.arcus.ui.components


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.arcus.R
import com.example.arcus.domain.hourStringInTwelveHourFormat
import com.example.arcus.domain.models.weather.PrecipitationProbability

/**
 * A card composable that displays precipitation probabilities in a "vertical progress bar" styled manner.
 * @param precipitationProbabilities The list of precipitation probabilities.
 * @param modifier The modifier to apply to the card.
 */
@Composable
fun PrecipitationProbabilitiesCard(
    precipitationProbabilities: List<PrecipitationProbability>,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Explicitly set tint to Color.Unspecified to ensure that no tint is applied to the vector
            // resource. See documentation of the Icon composable for more information.
            Icon(
                modifier = Modifier
                    .size(32.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_chance_of_rain),
                contentDescription = null,
                tint = Color.Unspecified
            )
            Text(
                text = "Процент осадков",
                style = MaterialTheme.typography.titleMedium
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(precipitationProbabilities) {
                ProbabilityProgressColumn(
                    modifier = Modifier.padding(bottom = 16.dp),
                    precipitationProbability = it
                )
            }
        }
    }
}

@Composable
private fun ProbabilityProgressColumn(
    precipitationProbability: PrecipitationProbability,
    modifier: Modifier = Modifier
) {
    var progressValue by remember { mutableStateOf(0f) }
    val animatedProgressValue by animateFloatAsState(targetValue = progressValue)
    LaunchedEffect(precipitationProbability) {
        // dividing a percentage value by 100 will yield a value that is between 0.0f..1.0f
        progressValue = precipitationProbability.probabilityPercentage / 100f
    }
    val (heightOfProgressBarWhenVertical, widthOfProgressBarWhenVertical) = remember {
        Pair(120.dp, 16.dp)
    }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = precipitationProbability.dateTime.hourStringInTwelveHourFormat.padStart(length = 5),
            style = MaterialTheme.typography.labelLarge
        )
        // Since Modifier.rotate() rotates the composable in a separate graphics layer,
        // other contents of the column will overlap with the progress bar.
        // In order to accommodate for that, use a Box as a spacer for the other
        // composables.
        Box(
            modifier = Modifier.size(
                height = heightOfProgressBarWhenVertical,
                width = widthOfProgressBarWhenVertical
            )
        ) {
            // Since Modifier.rotate() will rotate the composable with it's center point as the
            // pivot, center the progress bar composable to correctly fit the Box composable.
            LinearProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .requiredSize(
                        height = widthOfProgressBarWhenVertical, // after rotating, the width will be the height and vice-versa
                        width = heightOfProgressBarWhenVertical
                    )
                    .rotate(-90f),
                progress = animatedProgressValue,
                strokeCap = StrokeCap.Round,
                trackColor = ProgressIndicatorDefaults.linearColor.copy(alpha = 0.5f)
            )
        }
        Text(
            text = "${precipitationProbability.probabilityPercentage}%".padStart(length = 4),
            style = MaterialTheme.typography.labelLarge
        )
    }
}