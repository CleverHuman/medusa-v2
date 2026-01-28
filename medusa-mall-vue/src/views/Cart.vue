<template>
  <div class="cart-container">
    <h1 class="cart-title">Shopping Cart</h1>

    <el-table :data="cartItems" style="width: 100%" class="cart-table" v-loading="loading">
      <el-table-column label="Product" width="400">
        <template #default="{row}">
          <div class="product-info">
            <img :src="row.image" class="product-image">
            <div class="product-details">
              <div class="product-name">{{ row.name }}</div>
              <div class="product-spec">{{ row.des }}</div>
              <div class="product-price">
                {{ row.quantity }} × ${{ row.price }} = ${{ (row.quantity * row.price).toFixed(2) }}
              </div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="Quantity" width="200">
        <template #default="{row}">
          <el-input-number
            v-model="row.quantity"
            :min="1"
            :max="row.inventory"
            size="small"
            @change="handleQuantityChange(row)"
          ></el-input-number>
        </template>
      </el-table-column>

      <el-table-column label="Action" width="120">
        <template #default="{row}">
          <el-button
            type="danger"
            size="small"
            @click="removeCartItem(row.productId)"
            class="remove-btn"
          >
            Remove
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="summary-section">
      <div class="summary-left">
        <div class="summary-row">
          <span>Subtotal:</span>
          <span>${{ subtotal.toFixed(2) }}</span>
        </div>
        <div class="summary-row">
          <span>Discount:</span>
          <span>-${{ discount.toFixed(2) }}</span>
        </div>
        <div class="summary-row total-row">
          <span>Total:</span>
          <span>${{ total.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <div class="coupon-section">
      <el-input
        v-model="couponCode"
        placeholder="Enter coupon code"
        class="coupon-input"
      ></el-input>
      <el-button
        type="primary"
        @click="applyCoupon"
        class="apply-coupon-btn"
      >
        Apply Coupon
      </el-button>
    </div>

    <div class="checkout-section">
      <el-button
        type="success"
        @click="proceedToCheckout"
        class="checkout-btn"
        :disabled="cartItems.length === 0"
      >
        Proceed to Checkout
      </el-button>
    </div>
  </div>
</template>

<script>
import { getCartItems, updateCartItem, removeCartItem, applyCartCoupon } from '@/api/cart';
import { listProduct } from '@/api/product';

export default {
  name: "Cart",
  data() {
    return {
      loading: false,
      cartItems: [],
      couponCode: "",
      discount: 0,
      subtotal: 0,
      total: 0
    };
  },
  created() {
    this.fetchCart();
  },
  methods: {
    async fetchCart() {
      this.loading = true;
      try {
        const cartResponse = await getCartItems();
        const cartData = cartResponse.data;

        this.subtotal = Number(cartData.subtotal) || 0;
        this.discount = Number(cartData.discount) || 0;
        this.total = Number(cartData.total) || 0;

        const items = cartData.items || [];
        const productIds = items.map(item => item.productId);
        const productsResponse = await listProduct({ ids: productIds.join(',') });
        const products = productsResponse.rows || [];

        this.cartItems = items.map(item => {
          const product = products.find(p => p.id === item.productId) || {};
          return {
            ...item,
            name: product.name || `Product ${item.productId}`,
            des: product.des || 'No description',
            // image: product.image || require('@/assets/default-product.png'),
            inventory: product.inventory || 99
          };
        });
      } catch (error) {
        console.error("Error fetching cart:", error);
        this.$message.error("Failed to load cart");
      } finally {
        this.loading = false;
      }
    },

    async handleQuantityChange(row) {
      try {
        if (row.quantity > row.inventory) {
          this.$message.warning(`Only ${row.inventory} items available`);
          row.quantity = row.inventory;
          return;
        }

        await updateCartItem({
          productId: row.productId,
          quantity: row.quantity
        });

        await this.fetchCart(); // 自动刷新价格
      } catch (error) {
        console.error("Error updating quantity:", error);
        this.$message.error(error.response?.data?.msg || "Failed to update quantity");
      }
    },

    async removeCartItem(productId) {
      try {
        await this.$confirm("Are you sure to remove this item?", "Warning", {
          confirmButtonText: "OK",
          cancelButtonText: "Cancel",
          type: "warning"
        });

        await removeCartItem(productId);
        await this.fetchCart();
        this.$message.success("Item removed successfully");
      } catch (error) {
        if (error !== "cancel") {
          console.error("Error removing item:", error);
          this.$message.error("Failed to remove item");
        }
      }
    },

    async applyCoupon() {
      if (!this.couponCode) {
        this.$message.warning("Please enter a coupon code");
        return;
      }

      try {
        await applyCartCoupon(this.couponCode);
        await this.fetchCart();
        this.$message.success("Coupon applied successfully");
      } catch (error) {
        console.error("Error applying coupon:", error);
        this.$message.error(error.response?.data?.msg || "Failed to apply coupon");
      }
    },

    proceedToCheckout() {
      if (this.cartItems.length === 0) {
        this.$message.warning("Your cart is empty");
        return;
      }
      this.$router.push("/order/checkout");
    }
  }
};
</script>

<style scoped>
.cart-container {
  padding: 20px;
  width: 100%;
  margin: 0 auto;
}

.cart-title {
  color: white;
  font-size: 28px;
  margin-bottom: 20px;
  font-weight: bold;
}

.cart-table {
  margin-bottom: 30px;
}

.product-info {
  display: flex;
  align-items: center;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin-right: 15px;
  background-color: #f5f5f5;
}

.product-details {
  display: flex;
  flex-direction: column;
}

.product-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.product-spec {
  color: #999;
  font-size: 12px;
  margin-bottom: 5px;
}

.product-price {
  color: #f56c6c;
  font-weight: bold;
}

.remove-btn {
  margin-top: 10px;
}

.summary-section {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #1a1a1a; /* 暗黑背景 */
  color: #fff;
  border-radius: 4px;
}

.summary-left {
  width: 50%;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.total-row {
  font-weight: bold;
  font-size: 16px;
  border-top: 1px solid #eee;
  padding-top: 10px;
}

.coupon-section {
  margin-bottom: 15px;
}

.coupon-input {
  width: 300px;
}

.checkout-btn {
  width: 200px;
  height: 50px;
  font-size: 16px;
}
</style>
