package com.example.viralclicker.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class PurchaseResult {
    data class Success(val productId: String) : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        const val NO_ADS_SKU = "viral_no_ads"
        const val SKIN_GALAXY_SKU = "viral_skin_galaxy"
        const val SKIN_FIRE_SKU = "viral_skin_fire"
    }

    private val _purchaseResults = MutableSharedFlow<PurchaseResult>(extraBufferCapacity = 10)
    val purchaseResults: SharedFlow<PurchaseResult> = _purchaseResults

    private val scope = CoroutineScope(Dispatchers.IO)

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) { /* ready */ }
            override fun onBillingServiceDisconnected() { /* retry on next launch */ }
        })
    }

    fun launchPurchase(activity: Activity, productId: String) {
        scope.launch {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
            val result = billingClient.queryProductDetails(params)
            val productDetails = result.productDetailsList?.firstOrNull() ?: run {
                _purchaseResults.emit(PurchaseError("Product not found: $productId"))
                return@launch
            }
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                ).build()
            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    scope.launch { acknowledgePurchase(purchase) }
                }
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params)
        }
        purchase.products.forEach { productId ->
            _purchaseResults.emit(PurchaseResult.Success(productId))
        }
    }
}

private fun PurchaseError(message: String) = PurchaseResult.Error(message)
