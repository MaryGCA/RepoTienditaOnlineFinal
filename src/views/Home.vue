<template>
  <v-container fluid class="home-page">
    <div class="home-shell" :class="{ 'panel-open': panelVisible }">
      <!-- CONTENIDO PRINCIPAL del addToCart cd ~/Descargas/grocery-vue-project-main -->
      <div class="home-main">
        <!-- HERO -->
        <v-card class="hero-card" flat>
          <v-img
            src="/cover2.png"
            class="hero-img"
            gradient="to right, rgba(73,217,160,0.35), rgba(73,217,160,0.15), rgba(255,255,255,0)"
          >
            <div class="hero-overlay">
              <div class="hero-content">
                <div class="hero-badge">#Envío gratuito</div>

                <h1 class="hero-title">Haz tu pedido diario de comestibles</h1>

                <p class="hero-subtitle">
                  Frutas y verduras frescas, y compras inteligentes en un solo lugar.
                </p>

                <div class="hero-search">
                  <v-text-field
                    v-model="busqueda"
                    label="Search your daily groceries"
                    prepend-inner-icon="mdi-magnify"
                    solo
                    rounded
                    flat
                    clearable
                    hide-details
                    class="search-field"
                  />
                </div>
              </div>
            </div>
          </v-img>
        </v-card>

        <!-- RESULTADOS DE BÚSQUEDA -->
        <div v-if="busqueda" class="mt-6">
          <v-toolbar flat color="transparent" class="section-toolbar px-0">
            <v-toolbar-title class="section-title">
              Resultados de búsqueda
            </v-toolbar-title>
          </v-toolbar>

          <v-row class="product-grid" v-if="productosFiltrados.length > 0">
            <v-col
              v-for="item in productosFiltrados"
              :key="item.id"
              cols="12"
              sm="6"
              md="4"
              lg="3"
            >
              <v-card class="pa-4 product-card" outlined>
                <div class="product-image-wrap">
                  <v-img :src="item.img" height="140" contain class="mb-2" />
                </div>

                <div class="text-subtitle-1 font-weight-medium product-name">
                  {{ item.name }}
                </div>

                <div class="text-caption grey--text mb-2 product-weight">
                  {{ item.weight }}
                </div>

                <div class="text-h6 font-weight-bold mb-2 product-price">
                  ${{ item.price }}
                </div>

                <div class="mb-3">
                  <v-chip small dark :color="getStockColor(item)">
                    {{ getStockText(item) }}
                  </v-chip>
                </div>

                <div class="d-flex align-center justify-space-between product-actions">
                  <div class="d-flex align-center qty-box">
                    <v-btn
                      icon
                      small
                      @click="decrease(item)"
                      :disabled="item.stock <= 0"
                    >
                      <v-icon>mdi-minus</v-icon>
                    </v-btn>

                    <span class="mx-2 qty-text">{{ item.qty }}</span>

                    <v-btn
                      icon
                      small
                      @click="increase(item)"
                      :disabled="item.stock <= 0 || item.qty >= item.stock"
                    >
                      <v-icon color="green">mdi-plus</v-icon>
                    </v-btn>
                  </div>

                  <div class="d-flex action-buttons">
                    <v-btn fab small class="wishlist-btn" @click="addToWishlist(item)">
                      <v-icon color="#FF6D59">mdi-heart</v-icon>
                    </v-btn>

                    <v-btn
                      color="green"
                      dark
                      fab
                      small
                      class="cart-btn"
                      @click="addToCart(item)"
                      :disabled="item.stock <= 0"
                    >
                      <v-icon>mdi-cart</v-icon>
                    </v-btn>
                  </div>
                </div>
              </v-card>
            </v-col>
          </v-row>

          <v-alert v-else type="info" text color="green" class="mt-2">
            No se encontraron productos con esa búsqueda.
          </v-alert>
        </div>

        <!-- CATEGORÍAS -->
        <v-toolbar flat color="transparent" class="section-toolbar mt-6 px-0">
          <v-toolbar-title class="section-title">Categoría</v-toolbar-title>
        </v-toolbar>

        <div :class="busqueda ? 'categoria-mini' : ''">
          <Category @select-category="seleccionarCategoria" />
        </div>

        <!-- PRODUCTOS -->
        <v-card
          v-if="!busqueda"
          flat
          color="#E2F2E5"
          class="popular-section mt-6"
        >
          <v-toolbar flat color="transparent" class="section-toolbar mb-4 px-0">
            <v-toolbar-title class="section-title">
              {{ tituloSeccion }}
            </v-toolbar-title>
          </v-toolbar>

          <v-row class="product-grid" v-if="productosSeccion.length > 0">
            <v-col
              v-for="item in productosSeccion"
              :key="'section-' + item.id"
              cols="12"
              sm="6"
              md="4"
              lg="3"
            >
              <v-card class="pa-4 product-card" outlined>
                <div class="product-image-wrap">
                  <v-img :src="item.img" height="140" contain />
                </div>

                <div class="text-subtitle-1 font-weight-medium product-name">
                  {{ item.name }}
                </div>

                <div class="text-caption grey--text mb-2 product-weight">
                  {{ item.weight }}
                </div>

                <div class="text-h6 font-weight-bold mb-2 product-price">
                  ${{ item.price }}
                </div>

                <div class="mb-3">
                  <v-chip small dark :color="getStockColor(item)">
                    {{ getStockText(item) }}
                  </v-chip>
                </div>

                <div class="d-flex align-center justify-space-between product-actions">
                  <div class="d-flex align-center qty-box">
                    <v-btn
                      icon
                      small
                      @click="decrease(item)"
                      :disabled="item.stock <= 0"
                    >
                      <v-icon>mdi-minus</v-icon>
                    </v-btn>

                    <span class="mx-2 qty-text">{{ item.qty }}</span>

                    <v-btn
                      icon
                      small
                      @click="increase(item)"
                      :disabled="item.stock <= 0 || item.qty >= item.stock"
                    >
                      <v-icon color="green">mdi-plus</v-icon>
                    </v-btn>
                  </div>

                  <div class="d-flex action-buttons">
                    <v-btn fab small class="wishlist-btn" @click="addToWishlist(item)">
                      <v-icon color="#FF6D59">mdi-heart</v-icon>
                    </v-btn>

                    <v-btn
                      color="green"
                      dark
                      fab
                      small
                      class="cart-btn"
                      @click="addToCart(item)"
                      :disabled="item.stock <= 0"
                    >
                      <v-icon>mdi-cart</v-icon>
                    </v-btn>
                  </div>
                </div>
              </v-card>
            </v-col>
          </v-row>

          <v-alert v-else type="info" text color="green" class="mt-2">
            No se encontraron productos para esta categoría.
          </v-alert>
        </v-card>

        <Client />
        <Partner />
        <Footer />
      </div>

      <!-- PANEL LATERAL DERECHO -->
      <transition name="slide-panel">
        <aside v-if="panelVisible" class="side-panel">
          <div class="panel-header">
            <div class="panel-title">{{ tituloPanel }}</div>

            <v-btn icon @click="cerrarPanel">
              <v-icon>mdi-close</v-icon>
            </v-btn>
          </div>

          <div class="panel-body">
            <Wishlist v-if="panelActivo === 'wishlist'" />
            <Cart
              v-if="panelActivo === 'cart'"
              :embedded="true"
              @go-checkout="abrirPanel('checkout')"
            />
            <Pedidos v-if="panelActivo === 'pedidos'" />
            <Checkout v-if="panelActivo === 'checkout'" />
          </div>
        </aside>
      </transition>
    </div>

    <!-- MODAL -->
    <v-dialog v-model="dialog" max-width="400">
      <v-card class="pa-6 text-center" color="#e8f5e9" outlined>
        <v-icon size="64" color="green">mdi-check-circle</v-icon>
        <h2 class="mt-4">{{ dialogMessage }}</h2>
        <v-btn color="green" class="mt-4" @click="dialog = false">
          Cerrar
        </v-btn>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import Footer from "../components/Footer";
import Category from "../components/Category.vue";
import Client from "../components/Client.vue";
import Partner from "../components/Partner.vue";
import Wishlist from "./Wishlist.vue";
import Cart from "./Cart.vue";
import Pedidos from "./Pedidos.vue";
import Checkout from "./Checkout.vue";

export default {
  name: "Home",

  components: {
    Footer,
    Category,
    Client,
    Partner,
    Wishlist,
    Cart,
    Pedidos,
    Checkout
  },

  data() {
    return {
      busqueda: "",
      productos: [],
      categoriaSeleccionada: null,
      panelVisible: false,
      panelActivo: null,
      dialog: false,
      dialogMessage: ""
    };
  },

  mounted() {
    this.cargarProductos();
    window.addEventListener("stockActualizado", this.cargarProductos);
    window.addEventListener("abrirPanelLateral", this.recibirEventoPanel);
  },

  beforeDestroy() {
    window.removeEventListener("stockActualizado", this.cargarProductos);
    window.removeEventListener("abrirPanelLateral", this.recibirEventoPanel);
  },

  computed: {
    productosFiltrados() {
      const texto = this.busqueda.trim().toLowerCase();

      if (!texto) return [];

      return this.productos.filter(
        p =>
          (p.name || "").toLowerCase().includes(texto) ||
          (p.weight || "").toLowerCase().includes(texto)
      );
    },

    productosRandom() {
      return [...this.productos]
        .sort(() => Math.random() - 0.5)
        .slice(0, 12);
    },

    productosSeccion() {
      if (!this.categoriaSeleccionada) {
        return this.productosRandom;
      }

      return this.productos.filter(
        p => Number(p.categoriaId) === Number(this.categoriaSeleccionada.id)
      );
    },

    tituloSeccion() {
      return this.categoriaSeleccionada
        ? this.categoriaSeleccionada.title
        : "Productos populares";
    },

    tituloPanel() {
      const titulos = {
        wishlist: "Mis favoritos",
        cart: "Mi carrito",
        pedidos: "Mis pedidos",
        checkout: "Pagar"
      };

      return titulos[this.panelActivo] || "Panel";
    }
  },

  methods: {
    recibirEventoPanel(event) {
      const panel = event?.detail?.panel;
      if (panel) {
        this.abrirPanel(panel);
      }
    },

    abrirPanel(panel) {
      this.panelActivo = panel;
      this.panelVisible = true;
    },

    cerrarPanel() {
      this.panelVisible = false;
      this.panelActivo = null;
    },

    normalizarProductos(data) {
      return data.map(product => ({
        id: product.id,
        name: product.nombre,
        weight: product.variedad || "Sin variedad",
        price: product.precio,
        stock: product.stock || 0,
        qty: (product.stock || 0) > 0 ? 1 : 0,
        img: product.imagen || "/default.webp",
        categoriaId: product.categoriaId
      }));
    },

    async cargarProductos() {
      try {
        const response = await fetch("http://localhost:8081/api/productos");
        if (!response.ok) throw new Error("Error API");

        const data = await response.json();
        this.productos = this.normalizarProductos(data);
      } catch (error) {
        console.error("Error cargando productos:", error);
        this.productos = [];
      }
    },

    seleccionarCategoria(category) {
      this.categoriaSeleccionada = category;
    },

    getStockText(item) {
      if (item.stock <= 0) return "Agotado";
      if (item.stock <= 3) return "Últimas unidades";
      return "Disponible";
    },

    getStockColor(item) {
      if (item.stock <= 0) return "red";
      if (item.stock <= 3) return "orange";
      return "green";
    },

    increase(item) {
      if (item.stock <= 0) return;

      if (item.qty < item.stock) {
        item.qty++;
      } else {
        this.dialogMessage = "⚠️ No hay más stock disponible";
        this.dialog = true;
      }
    },

    decrease(item) {
      if (item.stock <= 0) return;

      if (item.qty > 1) {
        item.qty--;
      }
    },

    async addToCart(item) {
      const user = JSON.parse(localStorage.getItem("user"));

      if (!user) {
        this.dialogMessage = "⚠️ Debes iniciar sesión";
        this.dialog = true;
        return;
      }

      if (item.stock <= 0) {
        this.dialogMessage = "❌ Este producto está agotado";
        this.dialog = true;
        return;
      }

      try {
        const response = await fetch("http://localhost:8081/api/carrito/agregar", {
          method: "POST",
          headers: {
            "Content-Type": "application/x-www-form-urlencoded"
          },
          body: new URLSearchParams({
            nombre: item.name,
            variedad: item.weight,
            cantidad: item.qty,
            usuario: user.id || user.email
          })
        });

        const rawText = await response.text();
        let data = {};

        try {
          data = rawText ? JSON.parse(rawText) : {};
        } catch (e) {
          data = {};
        }

        if (!response.ok) {
          throw new Error(data.message || "Error agregando al carrito");
        }

        this.dialogMessage = data.message || "🛒 Producto agregado al carrito";
        this.dialog = true;
        item.qty = 1;

        await this.cargarProductos();
        window.dispatchEvent(new Event("carritoActualizado"));
        window.dispatchEvent(new Event("stockActualizado"));
      } catch (error) {
        console.error("Error carrito", error);
        this.dialogMessage = error.message || "❌ Error al agregar al carrito";
        this.dialog = true;
      }
    },

    addToWishlist(item) {
      const user = JSON.parse(localStorage.getItem("user"));

      if (!user) {
        this.dialogMessage = "⚠️ Debes iniciar sesión";
        this.dialog = true;
        return;
      }

      const key = `wishlist_${user.id || user.email}`;
      let wishlist = JSON.parse(localStorage.getItem(key)) || [];

      const existe = wishlist.find(p => p.id === item.id);

      if (!existe) {
        wishlist.push({
          id: item.id,
          name: item.name,
          price: item.price,
          weight: item.weight,
          img: item.img
        });

        localStorage.setItem(key, JSON.stringify(wishlist));
        this.dialogMessage = "❤️ Producto agregado a favoritos";
      } else {
        this.dialogMessage = "❤️ Este producto ya está en favoritos";
      }

      this.dialog = true;
    }
  }
};
</script>

<style scoped>
.home-page {
  padding-top: 8px;
}

.home-shell {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  transition: all 0.3s ease;
}

.home-main {
  flex: 1;
  min-width: 0;
  transition: all 0.3s ease;
}

.home-shell.panel-open .home-main {
  width: calc(100% - 420px);
}

.side-panel {
  width: 400px;
  min-width: 400px;
  max-width: 400px;
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.12);
  position: sticky;
  top: 88px;
  max-height: calc(100vh - 110px);
  overflow: hidden;
  z-index: 5;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid #eeeeee;
  background: white;
  position: sticky;
  top: 0;
  z-index: 2;
}

.panel-title {
  font-size: 1.15rem;
  font-weight: 700;
  color: #1f2937;
}

.panel-body {
  padding: 8px 10px 20px 10px;
  overflow-y: auto;
  max-height: calc(100vh - 170px);
}

.categoria-mini {
  transform: scale(0.88);
  opacity: 0.8;
  transform-origin: top center;
}

.hero-card {
  border-radius: 24px;
  overflow: hidden;
  margin-top: 8px;
}

.hero-img {
  height: 360px;
  background-position: center center;
  background-size: cover;
}

.hero-overlay {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 32px 40px;
}

.hero-content {
  max-width: 500px;
}

.hero-badge {
  display: inline-block;
  background: rgba(73, 217, 160, 0.22);
  color: #0d8a5b;
  font-weight: 700;
  font-size: 0.9rem;
  padding: 8px 14px;
  border-radius: 999px;
  margin-bottom: 14px;
}

.hero-title {
  font-size: 3rem;
  line-height: 1.08;
  font-weight: 800;
  color: #1f2937;
  margin-bottom: 12px;
}

.hero-subtitle {
  font-size: 1.05rem;
  color: #4b5563;
  margin-bottom: 22px;
  max-width: 420px;
}

.hero-search {
  max-width: 420px;
}

.search-field {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 999px;
}

.section-toolbar {
  min-height: auto;
}

.section-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: #1f2937;
}

.popular-section {
  border-radius: 24px;
  padding: 24px;
}

.product-grid {
  margin-top: 4px;
}

.product-card {
  height: 100%;
  border-radius: 20px;
  transition: 0.25s;
  border: 1px solid #ebebeb !important;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.04);
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.1);
}

.product-image-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 150px;
  margin-bottom: 8px;
}

.product-name {
  min-height: 30px;
  color: #1f2937;
}

.product-weight {
  min-height: 20px;
}

.product-price {
  color: #111827;
}

.product-actions {
  gap: 8px;
  flex-wrap: wrap;
}

.qty-box {
  background: #f6f7f8;
  border-radius: 999px;
  padding: 2px 6px;
}

.qty-text {
  min-width: 16px;
  text-align: center;
  font-weight: 600;
}

.action-buttons {
  gap: 8px;
}

.cart-btn {
  border: 2px solid #e0e0e0;
}

.wishlist-btn {
  border: 2px solid #e0e0e0;
  background: white;
}

.slide-panel-enter-active,
.slide-panel-leave-active {
  transition: all 0.28s ease;
}

.slide-panel-enter,
.slide-panel-leave-to {
  opacity: 0;
  transform: translateX(18px);
}

@media (max-width: 1264px) {
  .hero-title {
    font-size: 2.4rem;
  }

  .hero-img {
    height: 320px;
  }

  .hero-overlay {
    padding: 26px 30px;
  }
}

@media (max-width: 1100px) {
  .home-shell {
    flex-direction: column;
  }

  .home-shell.panel-open .home-main {
    width: 100%;
  }

  .side-panel {
    width: 100%;
    min-width: 100%;
    max-width: 100%;
    position: relative;
    top: 0;
    max-height: none;
  }

  .panel-body {
    max-height: none;
  }
}

@media (max-width: 960px) {
  .hero-img {
    height: 280px;
  }

  .hero-title {
    font-size: 2rem;
  }

  .hero-subtitle {
    font-size: 0.98rem;
  }
}

@media (max-width: 600px) {
  .home-page {
    padding-left: 8px;
    padding-right: 8px;
  }

  .hero-card {
    border-radius: 18px;
  }

  .hero-img {
    height: 220px;
  }

  .hero-overlay {
    padding: 18px 16px;
    align-items: center;
  }

  .hero-content {
    max-width: 100%;
  }

  .hero-title {
    font-size: 1.65rem;
    margin-bottom: 8px;
  }

  .hero-subtitle {
    font-size: 0.9rem;
    margin-bottom: 14px;
    max-width: 100%;
  }

  .hero-badge {
    font-size: 0.76rem;
    padding: 6px 10px;
    margin-bottom: 10px;
  }

  .hero-search {
    max-width: 100%;
  }

  .section-title {
    font-size: 1.1rem;
  }

  .popular-section {
    padding: 16px;
    border-radius: 18px;
  }

  .product-image-wrap {
    min-height: 120px;
  }

  .product-card {
    border-radius: 16px;
  }
}
</style>
