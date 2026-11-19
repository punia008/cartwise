package com.harshvardhan.cartwise.ui.cart

import com.harshvardhan.cartwise.data.model.Product
import com.harshvardhan.cartwise.data.repository.CartRepository
import com.harshvardhan.cartwise.pricing.CartLine
import com.harshvardhan.cartwise.pricing.Money
import com.harshvardhan.cartwise.pricing.PricedCart
import com.harshvardhan.cartwise.pricing.PricingEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private val repository = FakeCartRepository()

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `empty cart shows the empty state`() = runTest {
        val viewModel = CartViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(CartUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `decreasing the last unit removes the line`() = runTest {
        repository.lines.value = listOf(CartLine(1, "Mascara", "beauty", Money.of(10), 1))
        val viewModel = CartViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertTrue(viewModel.uiState.value is CartUiState.Filled)

        viewModel.decrease(1)

        assertEquals(CartUiState.Empty, viewModel.uiState.value)
    }

    private class FakeCartRepository : CartRepository {
        val lines = MutableStateFlow<List<CartLine>>(emptyList())
        private val engine = PricingEngine()

        override val cart: Flow<PricedCart> = lines.map { engine.price(it, emptyList()) }
        override fun quantityOf(productId: Long) = lines.map { l -> l.firstOrNull { it.productId == productId }?.quantity ?: 0 }
        override suspend fun add(product: Product) = Unit
        override suspend fun changeQuantity(productId: Long, delta: Int) {
            lines.value = lines.value.mapNotNull { line ->
                if (line.productId != productId) line
                else (line.quantity + delta).takeIf { it > 0 }?.let { line.copy(quantity = it) }
            }
        }
        override suspend fun remove(productId: Long) {
            lines.value = lines.value.filterNot { it.productId == productId }
        }
        override suspend fun clear() {
            lines.value = emptyList()
        }
    }
}
