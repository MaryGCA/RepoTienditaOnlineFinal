<template>
  <v-app-bar app color="white" flat class="app-bar-spaced">
    <template v-if="!isAdmin">
      <v-menu
        v-model="menuNav"
        offset-y
        bottom
        left
        transition="scale-transition"
        min-width="220"
      >
        <template v-slot:activator="{ on, attrs }">
          <v-btn icon class="mr-2" v-bind="attrs" v-on="on">
            <v-icon size="30">mdi-menu</v-icon>
          </v-btn>
        </template>

        <v-card class="pa-2">
          <v-list dense nav>
            <v-list-item @click="abrirPanel('wishlist')">
              <v-list-item-icon>
                <v-icon color="#FF6D59">mdi-heart-outline</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title class="menu-text">Favoritos</v-list-item-title>
              </v-list-item-content>
            </v-list-item>

            <v-list-item @click="abrirPanel('cart')">
              <v-list-item-icon>
                <v-icon color="#41AB55">mdi-cart-outline</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Carrito</v-list-item-title>
              </v-list-item-content>
            </v-list-item>

            <v-list-item @click="abrirPanel('pedidos')">
              <v-list-item-icon>
                <v-icon color="#FFB300">mdi-package-variant-closed</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Mis pedidos</v-list-item-title>
              </v-list-item-content>
            </v-list-item>

            <v-list-item @click="abrirPanel('checkout')">
              <v-list-item-icon>
                <v-icon color="#1976D2">mdi-credit-card-outline</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Pagar</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </v-card>
      </v-menu>

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

    <v-menu
      v-model="menu"
      offset-y
      bottom
      left
      transition="scale-transition"
      min-width="220"
    >
      <template v-slot:activator="{ on, attrs }">
        <v-avatar size="40" v-bind="attrs" v-on="on">
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
  props: {
    isAdmin: {
      type: Boolean,
      default: false
    }
  },

  data() {
    return {
      menu: false,
      menuNav: false,
      usuario: { nombre: "", email: "" }
    };
  },

  mounted() {
    this.cargarUsuario();
  },

  methods: {
    cargarUsuario() {
      const user =
        this.$store.state.usuario || JSON.parse(localStorage.getItem("user"));
      this.usuario = user || { nombre: "Invitado", email: "" };
    },

    abrirPanel(panel) {
      this.menuNav = false;
      this.$emit("open-panel", panel);
    },

    logout() {
      this.$store.commit("logout");
      localStorage.removeItem("user");
      this.menu = false;
      this.$router.push("/login");
    }
  }
};
</script>