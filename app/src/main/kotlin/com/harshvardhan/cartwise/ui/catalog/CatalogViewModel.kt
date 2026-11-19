package com.harshvardhan.cartwise.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.harshvardhan.cartwise.data.connectivity.ConnectivityObserver
import com.harshvardhan.cartwise.data.model.Product
import com.harshvardhan.cartwise.data.repository.CartRepository
import com.harshvardhan.cartwise.data.repository.OfferRepository
import com.harshvardhan.cartwise.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    products: ProductRepository,
    cart: CartRepository,
    connectivity: ConnectivityObserver,
    private val offers: OfferRepository,
) : ViewModel() {

    // cachedIn keeps loaded pages across configuration changes.
    val catalog: Flow<PagingData<Product>> = products.catalog().cachedIn(viewModelScope)

    val cartCount: StateFlow<Int> = cart.cart.map { it.itemCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val isOffline: StateFlow<Boolean> = connectivity.isOnline.map { !it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun offerBadge(category: String): String? = offers.badgeFor(category)
}
