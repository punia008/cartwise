package com.harshvardhan.cartwise.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.harshvardhan.cartwise.ui.cart.CartScreen
import com.harshvardhan.cartwise.ui.catalog.CatalogScreen
import com.harshvardhan.cartwise.ui.detail.ProductDetailScreen
import kotlinx.serialization.Serializable

@Serializable
object CatalogRoute

@Serializable
data class ProductRoute(val id: Long)

@Serializable
object CartRoute

@Composable
fun CartwiseNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = CatalogRoute) {
        composable<CatalogRoute> {
            CatalogScreen(
                onProductClick = { navController.navigate(ProductRoute(it)) },
                onCartClick = { navController.navigate(CartRoute) },
            )
        }
        composable<ProductRoute> {
            ProductDetailScreen(
                onBack = navController::popBackStack,
                onCartClick = { navController.navigate(CartRoute) },
            )
        }
        composable<CartRoute> {
            CartScreen(
                onBack = navController::popBackStack,
                onProductClick = { navController.navigate(ProductRoute(it)) },
            )
        }
    }
}
