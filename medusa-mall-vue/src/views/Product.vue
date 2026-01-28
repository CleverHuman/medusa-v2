<template>
  <div class="product-container">
<!--    <h2>product</h2>-->

    <!-- 搜索和筛选区域 -->
    <div class="product-toolbar">
      <el-input
        v-model="searchQuery"
        placeholder="search"
        style="width: 300px"
        clearable
        @clear="getList"
      >
        <template #append>
          <el-button icon="el-icon-search" @click="getList" />
        </template>
      </el-input>
    </div>

    <!-- 产品列表 -->
    <el-row :gutter="20">
      <el-col
        v-for="product in productsWithQuantity"
        :key="product.id"
        :xs="24" :sm="12" :md="8" :lg="6"
        class="product-card"
      >
        <el-card :body-style="{ padding: '0px' }" shadow="hover">
          <router-link :to="`/product/detail/${product.id}`">
            <img :src="product.image" class="product-image" />
          </router-link>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.des }}</p>
            <div class="product-footer">
              <span class="price">¥{{ product.price }}</span>
              <div class="actions">
                <el-input-number
                  v-model="product.quantity"
                  :min="1"
                  :max="99"
                  size="small"
                  controls-position="right"
                  style="width: 120px; margin-right: 8px;"
                ></el-input-number>
                <el-button
                  type="primary"
                  size="small"
                  @click.stop="updateCartItem(product)"
                  :loading="addingToCart"
                  :disabled="addingToCart"
                >
                  {{ addingToCart ? 'Adding...' : 'Add to cart' }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分页 -->
    <el-pagination
      v-show="total > 0"
      layout="total, sizes, prev, pager, next"
      :total="total"
      v-model:current-page="queryParams.pageNum"
      v-model:page-size="queryParams.pageSize"
      @current-change="getList"
    />
  </div>
</template>

<script>
import { listProduct } from "@/api/product";
import { updateCartItem } from "@/api/cart";


export default {
  name: "ProductList",
  data() {
    return {
      loading: true,
      category: this.$route.params.category, // DIGITAL/PHY
      productList: [],
      searchQuery: "",
      total: 0,
      addingToCart: false, // 添加加载状态
      queryParams: {
        pageNum: 1,
        pageSize: 8,
        category: null,
      },
    };
  },
  computed: {
    categoryName() {
      return this.category === "DIGITAL" ? "数字" : "实物";
    },
    productsWithQuantity() {
      return this.productList.map(product => ({
        ...product,
        quantity: product.quantity || 1
      }));
    }
  },
  watch: {
    // 监听路由参数变化
    '$route.params.category': {
      immediate: true,
      handler(newCategory) {
        this.category = newCategory;
        this.queryParams.pageNum = 1; // 重置页码
        this.getList();
      }
    }
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      this.queryParams.category = this.category
      listProduct(this.queryParams).then(response => {
        console.log(response)
        this.productList = response.rows;
        this.total = response.total;

      }).catch(error => {
        console.error('Error fetching product:', error)
        this.$message.error('Failed to load product information')
      })
    },
    async updateCartItem(product) {
      if (this.addingToCart) return; // 防止重复点击

      this.addingToCart = true;
      try {
        const data = {
          productId: product.id,
          quantity: product.quantity || 1 // 使用用户选择的数量，默认为1
        };

        await updateCartItem(data);
        this.$message.success('Added to cart successfully');

      } catch (error) {
        // 错误处理...
      } finally {
        this.addingToCart = false;
      }
    }
  }
};
</script>

<style scoped>
.product-container {
  padding: 20px;
  background-color: #f5f5f5;
}

.product-toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-card {
  margin-bottom: 20px;
}

.product-image {
  width: 100%;
  height: 180px;
  object-fit: cover;
  cursor: pointer;
}

.product-info {
  padding: 15px;
}

.product-name {
  font-size: 16px;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-desc {
  color: #999;
  font-size: 12px;
  height: 36px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.price {
  color: #f56c6c;
  font-weight: bold;
}

.actions {
  display: flex;
  gap: 8px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
