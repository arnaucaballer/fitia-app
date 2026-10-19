package com.arnaucaballer.fitia.feature.nutrition.data

import com.arnaucaballer.fitia.feature.nutrition.domain.FoodItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OpenFoodFactsClient {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
    }

    suspend fun search(query: String, pageSize: Int = 20): List<FoodItem> {
        if (query.isBlank()) return emptyList()
        val response: SearchResponse = client.get("https://world.openfoodfacts.org/cgi/search.pl") {
            parameter("search_terms", query.trim())
            parameter("search_simple", 1)
            parameter("action", "process")
            parameter("json", 1)
            parameter("page_size", pageSize)
            parameter("fields", "product_name,brands,code,nutriments,serving_quantity,serving_size")
        }.body()

        return response.products.mapNotNull { product -> product.toFoodItem() }
    }

    fun close() {
        client.close()
    }
}

@Serializable
private data class SearchResponse(
    val products: List<OffProduct> = emptyList(),
)

@Serializable
private data class OffProduct(
    @SerialName("product_name") val productName: String? = null,
    val brands: String? = null,
    val code: String? = null,
    val nutriments: OffNutriments? = null,
    @SerialName("serving_quantity") val servingQuantity: Double? = null,
)

@Serializable
private data class OffNutriments(
    @SerialName("energy-kcal_100g") val energyKcal100g: Double? = null,
    @SerialName("proteins_100g") val proteins100g: Double? = null,
    @SerialName("carbohydrates_100g") val carbohydrates100g: Double? = null,
    @SerialName("fat_100g") val fat100g: Double? = null,
)

private fun OffProduct.toFoodItem(): FoodItem? {
    val name = productName?.trim().orEmpty()
    if (name.isBlank()) return null
    val nutriments = nutriments ?: return null
    val calories = nutriments.energyKcal100g ?: return null
    val serving = servingQuantity?.takeIf { it > 0 } ?: 100.0
    val factor = serving / 100.0
    return FoodItem(
        name = name,
        brand = brands.orEmpty(),
        servingGrams = serving,
        calories = calories * factor,
        proteinGrams = (nutriments.proteins100g ?: 0.0) * factor,
        carbsGrams = (nutriments.carbohydrates100g ?: 0.0) * factor,
        fatGrams = (nutriments.fat100g ?: 0.0) * factor,
        source = "open_food_facts",
        barcode = code.orEmpty(),
    )
}
