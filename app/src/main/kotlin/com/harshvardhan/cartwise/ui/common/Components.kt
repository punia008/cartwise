package com.harshvardhan.cartwise.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun OfflineBanner(isOffline: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = isOffline, modifier = modifier) {
        Text(
            text = "You're offline — showing saved products",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(vertical = 6.dp),
        )
    }
}

@Composable
fun CartAction(itemCount: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BadgedBox(badge = { if (itemCount > 0) Badge { Text("$itemCount") } }) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart, $itemCount items")
        }
    }
}

/** − qty + stepper. Uses text glyphs so we don't pull in the extended icon set. */
@Composable
fun QuantityStepper(quantity: Int, onDecrease: () -> Unit, onIncrease: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(onClick = onDecrease, modifier = Modifier.size(40.dp), contentPadding = PaddingValues(0.dp)) {
            Text("−", style = MaterialTheme.typography.titleMedium)
        }
        Text(
            text = "$quantity",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        OutlinedButton(onClick = onIncrease, modifier = Modifier.size(40.dp), contentPadding = PaddingValues(0.dp)) {
            Text("+", style = MaterialTheme.typography.titleMedium)
        }
    }
}
