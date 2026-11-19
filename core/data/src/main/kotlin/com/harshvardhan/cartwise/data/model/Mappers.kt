package com.harshvardhan.cartwise.data.model

import com.harshvardhan.cartwise.data.database.CartItemEntity
import com.harshvardhan.cartwise.data.database.ProductEntity
import com.harshvardhan.cartwise.data.network.NetworkProduct
import com.harshvardhan.cartwise.pricing.CartLine
import com.harshvardhan.cartwise.pricing.Money
import java.math.BigDecimal
import java.math.RoundingMode

/** 19.99 -> 1999. Goes through the decimal string so 0.29 doesn't become 28 cents. */
internal fun Double.toCents(): Long =
    BigDecimal(toString()).setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact()

internal fun NetworkProduct.toEntity(position: Int) = ProductEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    brand = brand,
    priceCents = price.toCents(),
    rating = rating,
    stock = stock,
    thumbnail = thumbnail,
    images = images,
    position = position,
)

internal fun ProductEntity.toModel() = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    brand = brand,
    price = Money(priceCents),
    rating = rating,
    stock = stock,
    thumbnail = thumbnail,
    images = images,
)

internal fun CartItemEntity.toCartLine() = CartLine(
    productId = productId,
    title = title,
    category = category,
    unitPrice = Money(unitPriceCents),
    quantity = quantity,
)
