<template>
  <v-app-bar app color="white" flat class="app-bar-spaced">
    <template v-if="!isAdmin">
      <v-btn icon to="/">
        <v-badge color="#D5F0DB" dot offset-x="5" offset-y="5">
          <v-img src="1.png" contain width="40"></v-img>
        </v-badge>
      </v-btn>

      <v-toolbar-title class="ml-2">
        <v-btn text to="/">
          <span class="green--text">e</span><strong>Grocery</strong>
        </v-btn>
      </v-toolbar-title>
    </template>

    <v-spacer></v-spacer>

    <!-- Acciones del lado derecho, sin redirección -->
    <template v-if="!isAdmin">
      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-badge color="#FF6D59" overlap content=" " dot class="mr-3">
            <v-avatar color="#FFF0EE" size="44">
              <v-btn icon v-bind="attrs" v-on="on" @click="abrirPanel('wishlist')">
                <v-icon size="28" color="#FF6D59">far fa-heart</v-icon>
              </v-btn>
            </v-avatar>
          </v-badge>
        </template>
        <span>Favoritos</span>
      </v-tooltip>

      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-badge color="#41AB55" overlap content=" " dot class="mr-3">
            <v-avatar color="#ECF7EE" size="44">
              <v-btn icon v-bind="attrs" v-on="on" @click="abrirPanel('cart')">
                <v-icon size="28" color="#41AB55">fas fa-shopping-cart</v-icon>
              </v-btn>
            </v-avatar>
          </v-badge>
        </template>
        <span>Mi carrito</span>
      </v-tooltip>

      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-avatar color="#FFF8E1" size="44" class="mr-3">
            <v-btn icon v-bind="attrs" v-on="on" @click="abrirPanel('pedidos')">
              <v-icon size="28" color="#FFB300">fas fa-box-open</v-icon>
            </v-btn>
          </v-avatar>
        </template>
        <span>Mis pedidos</span>
      </v-tooltip>

      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-avatar color="#E8F5E9" size="44" class="mr-3">
            <v-btn icon v-bind="attrs" v-on="on" @click="abrirPanel('checkout')">
              <v-icon size="28" color="#2E7D32">mdi-credit-card-outline</v-icon>
            </v-btn>
          </v-avatar>
        </template>
        <span>Pagar</span>
      </v-tooltip>
    </template>

    <!-- Perfil -->
    <v-menu
      v-model="menu"
      offset-y
      bottom
      left
      transition="scale-transition"
      min-width="200"
    >
      <template v-slot:activator="{ on, attrs }">
        <v-avatar size="42" v-bind="attrs" v-on="on">
          <v-img src="/user.png" />
        </v-avatar>
      </template>

      <v-card class="pa-4 text-center">
        <div class="font-weight-bold text-subtitle-1 mb-3">
          {{ usuario.nombre || "Invitado" }}
        </div>
        <div class="grey--text text-caption mb-3">
          {{ usuario.email || "" }}
        </div>
        <v-divider class="mb-3"></v-divider>
        <v-btn block color="red" dark @click="logout">🚪 Cerrar sesión</v-btn>
      </v-card>
    </v-menu>
  </v-app-bar>
</template>

<script>
export default {
  name: "Navbar",

  props: {
    isAdmin: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      menu: false,
      usuario: { nombre: "", email: "" }
    };
  },

  mounted() {
    this.cargarUsuario();
    window.addEventListener("storage", this.cargarUsuario);
  },

  beforeDestroy() {
    window.removeEventListener("storage", this.cargarUsuario);
  },

  methods: {
    cargarUsuario() {
      const user =
        this.$store?.state?.usuario || JSON.parse(localStorage.getItem("user"));
      this.usuario = user || { nombre: "Invitado", email: "" };
    },

    abrirPanel(panel) {
      window.dispatchEvent(
        new CustomEvent("abrirPanelLateral", {
          detail: { panel }
        })
      );
    },

    logout() {
      if (this.$store && this.$store.commit) {
        this.$store.commit("logout");
      }

      localStorage.removeItem("user");
      this.menu = false;
      this.$router.push("/login");
    }
  }
};
</script>

<style scoped>
.app-bar-spaced {
  padding-left: 8px;
  padding-right: 12px;
}
</style>
