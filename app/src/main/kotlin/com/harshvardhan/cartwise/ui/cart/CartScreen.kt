package com.harshvardhan.cartwise.ui.cart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harshvardhan.cartwise.pricing.Money
import com.harshvardhan.cartwise.pricing.PricedCart
import com.harshvardhan.cartwise.pricing.PricedLine
import com.harshvardhan.cartwise.ui.common.QuantityStepper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onProductClick: (Long) -> Unit,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your cart") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            (state as? CartUiState.Filled)?.let { filled ->
                CheckoutBar(filled.cart.total) {
                    scope.launch { snackbar.showSnackbar("Checkout isn't part of this demo.") }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                CartUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                CartUiState.Empty -> Text(
                    "Your cart is empty",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center),
                )
                is CartUiState.Filled -> CartContent(
                    cart = s.cart,
                    onProductClick = onProductClick,
                    onIncrease = viewModel::increase,
                    onDecrease = viewModel::decrease,
                    onRemove = viewModel::remove,
                )
            }
        }
    }
}

@Composable
private fun CartContent(
    cart: PricedCart,
    onProductClick: (Long) -> Unit,
    onIncrease: (Long) -> Unit,
    onDecrease: (Long) -> Unit,
    onRemove: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        cart.nudge?.let { nudge ->
            item(key = "nudge") {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(
                        "Add ${nudge.amountNeeded.format()} more to get ${nudge.offer.percentOff}% off your order",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
        items(cart.lines, key = { it.line.productId }) { line ->
            CartLineRow(
                line = line,
                onClick = { onProductClick(line.line.productId) },
                onIncrease = { onIncrease(line.line.productId) },
                onDecrease = { onDecrease(line.line.productId) },
                onRemove = { onRemove(line.line.productId) },
            )
        }
        item(key = "summary") { Summary(cart) }
    }
}

@Composable
private fun CartLineRow(
    line: PricedLine,
    onClick: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(line.line.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${line.line.unitPrice.format()} each",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove ${line.line.title}")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuantityStepper(line.line.quantity, onDecrease, onIncrease)
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    if (line.discount > Money.ZERO) {
                        Text(
                            "−${line.discount.format()} offer",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    Text(line.total.format(), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun Summary(cart: PricedCart) {
    Column(Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SummaryRow("Subtotal (${cart.itemCount} items)", cart.subtotal.format())
        cart.appliedOffers.forEach { applied ->
            SummaryRow(applied.offer.label, "−${applied.discount.format()}", highlight = true)
        }
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        SummaryRow("Total", cart.total.format(), bold = true)
        if (cart.totalDiscount > Money.ZERO) {
            Text(
                "You save ${cart.totalDiscount.format()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, highlight: Boolean = false, bold: Boolean = false) {
    val style = if (bold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
    val color = if (highlight) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = style, color = color, modifier = Modifier.weight(1f))
        Text(value, style = style, color = color)
    }
}

@Composable
private fun CheckoutBar(total: Money, onCheckout: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Button(
            onClick = onCheckout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(48.dp),
        ) { Text("Checkout · ${total.format()}") }
    }
}
