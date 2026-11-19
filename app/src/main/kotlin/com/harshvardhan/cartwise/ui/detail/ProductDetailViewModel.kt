package com.harshvardhan.cartwise.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.harshvardhan.cartwise.data.model.Product
import com.harshvardhan.cartwise.data.repository.CartRepository
import com.harshvardhan.cartwise.data.repository.OfferRepository
import com.harshvardhan.cartwise.data.repository.ProductRepository
import com.harshvardhan.cartwise.ui.ProductRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Error(val message: String) : DetailUiState
    data class Ready(val product: Product, val quantityInCart: Int, val offerBadge: String?, val cartCount: Int) : DetailUiState
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val products: ProductRepository,
    private val cart: CartRepository,
    private val offers: OfferRepository,
) : ViewModel() {

    private val productId = savedStateHandle.toRoute<ProductRoute>().id
    private val loadError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DetailUiState> = combine(
        products.observeProduct(productId),
        cart.quantityOf(productId),
        cart.cart,
        loadError,
    ) { product, quantity, pricedCart, error ->
        when {
            product != null -> DetailUiState.Ready(product, quantity, offers.badgeFor(product.category), pricedCart.itemCount)
            error != null -> DetailUiState.Error(error)
            else -> DetailUiState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState.Loading)

    init {
        load()
    }

    fun load() {
        loadError.value = null
        viewModelScope.launch {
            try {
                products.ensureProduct(productId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                loadError.value = "Couldn't load this product. Check your connection."
            }
        }
    }

    fun addToCart(product: Product) = viewModelScope.launch { cart.add(product) }

    fun changeQuantity(delta: Int) = viewModelScope.launch { cart.changeQuantity(productId, delta) }
}
