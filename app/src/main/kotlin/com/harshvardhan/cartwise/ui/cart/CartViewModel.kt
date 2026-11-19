package com.harshvardhan.cartwise.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harshvardhan.cartwise.data.repository.CartRepository
import com.harshvardhan.cartwise.pricing.PricedCart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CartUiState {
    data object Loading : CartUiState
    data object Empty : CartUiState
    data class Filled(val cart: PricedCart) : CartUiState
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cart: CartRepository,
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = cart.cart
        .map { if (it.lines.isEmpty()) CartUiState.Empty else CartUiState.Filled(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState.Loading)

    fun increase(productId: Long) = viewModelScope.launch { cart.changeQuantity(productId, +1) }

    fun decrease(productId: Long) = viewModelScope.launch { cart.changeQuantity(productId, -1) }

    fun remove(productId: Long) = viewModelScope.launch { cart.remove(productId) }
}
