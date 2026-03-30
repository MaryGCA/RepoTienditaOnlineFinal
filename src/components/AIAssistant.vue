<template>
  <div>
    <!-- Botón flotante -->
    <v-btn
      fab
      fixed
      bottom
      right
      color="green"
      dark
      @click="open = true"
    >
      <v-icon>mdi-robot</v-icon>
    </v-btn>

    <!-- Ventana chat -->
    <v-dialog v-model="open" width="400">
      <v-card>

        <v-card-title class="green white--text">
          Asistente Virtual
        </v-card-title>

        <v-card-text
          style="height:300px; overflow-y:auto;"
          ref="chatBox"
        >
          <div v-for="(msg, i) in messages" :key="i">
            <strong>{{ msg.role }}:</strong>
            {{ msg.text }}
          </div>
        </v-card-text>

        <v-card-actions>
          <v-text-field
            v-model="input"
            label="Escribe tu mensaje"
            dense
            @keyup.enter="sendMessage"
          ></v-text-field>

          <v-btn color="green" dark @click="sendMessage">
            Enviar
          </v-btn>
        </v-card-actions>

      </v-card>
    </v-dialog>
  </div>
</template>

<script>
export default {
  name: "AIAssistant",

  data() {
    return {
      open: false,
      input: "",
      messages: []
    };
  },

  methods: {

    async sendMessage() {

      if (!this.input.trim()) return;

      const user = JSON.parse(localStorage.getItem("user"));

      if (!user) {
        this.messages.push({
          role: "Sistema",
          text: "⚠️ Debes iniciar sesión"
        });
        return;
      }

      const texto = this.input;
      const usuario = user.id || user.email;

      // mensaje usuario
      this.messages.push({
        role: "Usuario",
        text: texto
      });

      this.input = "";

      try {
        const response = await fetch("http://localhost:8081/api/chat", {
          method: "POST",
          headers: {
          "Content-Type": "application/json"
        },
          body: JSON.stringify({
            message: texto,
            usuario: usuario
          })
        });

        if (!response.ok) {
          throw new Error("Respuesta inválida");
        }

        const data = await response.json();

        this.messages.push({
          role: "Agente",
          text: data.reply || "Sin respuesta"
        });

        // 🔥 ACTUALIZA CARRITO AUTOMÁTICO
        window.dispatchEvent(new Event("carritoActualizado"));

      } catch (error) {

        console.error(error);

        this.messages.push({
          role: "Sistema",
          text: "❌ Error conectando con servidor"
        });
      }

      // scroll automático
      this.$nextTick(() => {
        const chatBox = this.$refs.chatBox;
        if (chatBox) {
          chatBox.scrollTop = chatBox.scrollHeight;
        }
      });
    }

  }
};
</script>

<style scoped>
</style>