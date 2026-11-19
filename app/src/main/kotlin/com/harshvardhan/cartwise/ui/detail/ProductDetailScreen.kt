package com.harshvardhan.cartwise.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.harshvardhan.cartwise.data.model.Product
import com.harshvardhan.cartwise.ui.common.CartAction
import com.harshvardhan.cartwise.ui.common.QuantityStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { CartAction((state as? DetailUiState.Ready)?.cartCount ?: 0, onCartClick) },
            )
        },
        bottomBar = {
            (state as? DetailUiState.Ready)?.let { ready ->
                AddToCartBar(
                    ready = ready,
                    onAdd = { viewModel.addToCart(ready.product) },
                    onChange = viewModel::changeQuantity,
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                DetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DetailUiState.Error -> Column(
                    Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(s.message)
                    Button(onClick = viewModel::load, modifier = Modifier.padding(top = 16.dp)) { Text("Retry") }
                }
                is DetailUiState.Ready -> ProductDetails(s.product, s.offerBadge)
            }
        }
    }
}

@Composable
private fun ProductDetails(product: Product, offerBadge: String?) {
    val images = product.images.ifEmpty { listOf(product.thumbnail) }
    var selected by remember(product.id) { mutableStateOf(images.first()) }

    Column(Modifier.verticalScroll(rememberScrollState())) {
        AsyncImage(
            model = selected,
            contentDescription = product.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant),
        )
        if (images.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(images) { url ->
                    val shape = RoundedCornerShape(8.dp)
                    AsyncImage(
                        model = url,
                        contentDescription = "Show image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(shape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = if (url == selected) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = shape,
                            )
                            .clickable { selected = url },
                    )
                }
            }
        }
        Column(Modifier.padding(16.dp)) {
            if (offerBadge != null) {
                Text(
                    offerBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Text(product.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                listOfNotNull(product.brand, product.category.replaceFirstChar { it.uppercase() }).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(product.price.format(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                Text("★ ${"%.1f".format(product.rating)}", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                if (product.stock > 0) "${product.stock} in stock" else "Out of stock",
                style = MaterialTheme.typography.labelLarge,
                color = if (product.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(12.dp))
            Text(product.description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun AddToCartBar(ready: DetailUiState.Ready, onAdd: () -> Unit, onChange: (Int) -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (ready.quantityInCart == 0) {
                Button(
                    onClick = onAdd,
                    enabled = ready.product.stock > 0,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) { Text("Add to cart") }
            } else {
                QuantityStepper(
                    quantity = ready.quantityInCart,
                    onDecrease = { onChange(-1) },
                    onIncrease = { onChange(+1) },
                )
            }
        }
    }
}
