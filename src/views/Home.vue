<template>
  <v-app>
    <Navbar @open-panel="abrirPanel" />

    <!-- PANEL LATERAL -->
    <v-navigation-drawer
      v-model="drawer"
      right
      temporary
      fixed
      :width="$vuetify.breakpoint.smAndDown ? '100%' : 470"
      class="panel-drawer"
    >
      <div class="panel-header">
        <div class="panel-title">{{ tituloPanel }}</div>
        <v-btn icon @click="drawer = false">
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
        <Checkout
          v-if="panelActivo === 'checkout'"
          :embedded="true"
          @back-cart="abrirPanel('cart')"
          @pago-exitoso="manejarPagoExitoso"
        />
      </div>
    </v-navigation-drawer>

    <v-container fluid class="home-container">
      <v-row>
        <v-col cols="12">
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
                    <v-img
                      :src="item.img"
                      height="140"
                      contain
                      class="mb-2"
                    />
                  </div>

                  <div class="text-subtitle-1 font-weight-medium product-name">
                    {{ item.name }}
                  </div>

                  <div class="text-caption grey--text mb-2 product-weight">
                    {{ item.weight }}
                  </div>

                  <div class="text-h6 font-weight-bold mb-3 product-price">
                    ${{ item.price }}
                  </div>

                  <div class="d-flex align-center justify-space-between product-actions">
                    <div class="d-flex align-center qty-box">
                      <v-btn icon small @click="decrease(item)">
                        <v-icon>mdi-minus</v-icon>
                      </v-btn>

                      <span class="mx-2 qty-text">{{ item.qty }}</span>

                      <v-btn icon small @click="increase(item)">
                        <v-icon color="green">mdi-plus</v-icon>
                      </v-btn>
                    </div>

                    <div class="d-flex action-buttons">
                      <v-btn fab small class="wishlist-btn" @click="addToWishlist(item)">
                        <v-icon color="#FF6D59">mdi-heart</v-icon>
                      </v-btn>

                      <v-btn color="green" dark fab small class="cart-btn" @click="addToCart(item)">
                        <v-icon>mdi-cart</v-icon>
                      </v-btn>
                    </div>
                  </div>
                </v-card>
              </v-col>
            </v-row>

            <v-alert
              v-else
              type="info"
              text
              color="green"
              class="mt-2"
            >
              No se encontraron productos con esa búsqueda.
            </v-alert>
          </div>

          <!-- CATEGORÍAS -->
          <v-toolbar flat color="transparent" class="section-toolbar mt-6 px-0">
            <v-toolbar-title class="section-title">Categoría</v-toolbar-title>
            <v-spacer></v-spacer>
          </v-toolbar>

          <div :class="busqueda ? 'categoria-mini' : ''">
            <Category @select-category="seleccionarCategoria" />
          </div>

          <!-- PRODUCTOS POPULARES / CATEGORÍA -->
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
                    <v-img
                      :src="item.img"
                      height="140"
                      contain
                    />
                  </div>

                  <div class="text-subtitle-1 font-weight-medium product-name">
                    {{ item.name }}
                  </div>

                  <div class="text-caption grey--text mb-2 product-weight">
                    {{ item.weight }}
                  </div>

                  <div class="text-h6 font-weight-bold mb-3 product-price">
                    ${{ item.price }}
                  </div>

                  <div class="d-flex align-center justify-space-between product-actions">
                    <div class="d-flex align-center qty-box">
                      <v-btn icon small @click="decrease(item)">
                        <v-icon>mdi-minus</v-icon>
                      </v-btn>

                      <span class="mx-2 qty-text">{{ item.qty }}</span>

                      <v-btn icon small @click="increase(item)">
                        <v-icon color="green">mdi-plus</v-icon>
                      </v-btn>
                    </div>

                    <div class="d-flex action-buttons">
                      <v-btn fab small class="wishlist-btn" @click="addToWishlist(item)">
                        <v-icon color="#FF6D59">mdi-heart</v-icon>
                      </v-btn>

                      <v-btn color="green" dark fab small class="cart-btn" @click="addToCart(item)">
                        <v-icon>mdi-cart</v-icon>
                      </v-btn>
                    </div>
                  </div>
                </v-card>
              </v-col>
            </v-row>

            <v-alert
              v-else
              type="info"
              text
              color="green"
              class="mt-2"
            >
              No se encontraron productos para esta categoría.
            </v-alert>
          </v-card>

          <Client />
          <Partner />
        </v-col>
      </v-row>
    </v-container>

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

    <Footer />
  </v-app>
</template>

<script>
import Navbar from "../components/Navbar";
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
    Navbar,
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
      drawer: false,
      panelActivo: null,
      dialog: false,
      dialogMessage: ""
    };
  },

  mounted() {
    this.cargarProductos();
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
        cart: "Carrito",
        pedidos: "Mis pedidos",
        checkout: "Pago"
      };

      return titulos[this.panelActivo] || "Panel";
    }
  },

  methods: {
    normalizarProductos(data) {
      return data.map(product => ({
        id: product.id,
        name: product.nombre,
        weight: product.variedad || "Sin variedad",
        price: product.precio,
        stock: product.stock || 0,
        qty: 1,
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

    abrirPanel(panel) {
      this.panelActivo = panel;
      this.drawer = true;
    },

    manejarPagoExitoso() {
      this.drawer = false;
      this.dialogMessage = "✅ Pago realizado con éxito";
      this.dialog = true;
    },

    increase(item) {
      if (item.qty < item.stock) {
        item.qty++;
      } else {
        alert("No hay más stock disponible");
      }
    },

    decrease(item) {
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

        if (!response.ok) throw new Error("Error agregando al carrito");

        this.dialogMessage = "🛒 Producto agregado al carrito";
        this.dialog = true;
        item.qty = 1;
        window.dispatchEvent(new Event("carritoActualizado"));
      } catch (error) {
        console.error("Error carrito", error);
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
        this.dialogMessage = "❤️ Producto agregado a wishlist";
      } else {
        this.dialogMessage = "❤️ Este producto ya está en tu wishlist";
      }

      this.dialog = true;
    }
  }
};
</script>

<style scoped>
.panel-drawer {
  z-index: 1200;
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
}

.categoria-mini {
  transform: scale(0.85);
  opacity: 0.7;
  margin-top: -20px;
}

.home-container {
  padding-top: 8px;
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
  .home-container {
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

  .panel-body {
    padding: 6px;
  }
}
</style>