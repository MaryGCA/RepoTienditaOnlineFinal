<template>
  <div>
    <v-btn
      fab
      fixed
      bottom
      right
      color="green darken-1"
      dark
      class="assistant-fab"
      @click="open = true"
    >
      <v-icon size="34">mdi-robot</v-icon>
    </v-btn>

    <v-dialog v-model="open" width="420">
      <v-card class="assistant-card">
        <v-card-title class="assistant-header">
          Asistente Virtual
        </v-card-title>

        <v-card-text class="chat-box" ref="chatBox">
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="message-row"
            :class="msg.roleClass"
          >
            <div class="message-bubble" :class="msg.roleClass">
              <div class="message-role">{{ msg.role }}</div>
              <div class="message-text">{{ msg.text }}</div>
            </div>
          </div>
        </v-card-text>

        <v-card-actions class="chat-actions">
          <v-text-field
            v-model="input"
            label="Escribe tu mensaje"
            dense
            outlined
            hide-details
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
      messages: [
        {
          role: "Asistente",
          roleClass: "agente",
          text: "¡Hola! 👋 Soy tu asistente virtual. Puedo ayudarte con productos, carrito, recetas y bienestar alimentario."
        }
      ]
    };
  },

  methods: {
    async sendMessage() {
      if (!this.input.trim()) return;

      const user = JSON.parse(localStorage.getItem("user"));

      if (!user) {
        this.messages.push({
          role: "Sistema",
          roleClass: "sistema",
          text: "⚠️ Debes iniciar sesión"
        });
        this.scrollBottom();
        return;
      }

      const userMessage = this.input.trim();

      this.messages.push({
        role: "Tú",
        roleClass: "usuario",
        text: userMessage
      });

      this.input = "";
      this.scrollBottom();

      try {
        const response = await fetch("http://localhost:8081/api/chat", {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            message: userMessage,
            usuario: user.id || user.email
          })
        });

        if (!response.ok) {
          throw new Error("Error del servidor");
        }

        const data = await response.json();

        this.messages.push({
          role: "Asistente",
          roleClass: "agente",
          text: data.reply || "No recibí respuesta en este momento."
        });

        window.dispatchEvent(new Event("carritoActualizado"));
        this.scrollBottom();
      } catch (error) {
        console.error("Error:", error);

        this.messages.push({
          role: "Sistema",
          roleClass: "sistema",
          text: "❌ Error conectando con el servidor"
        });

        this.scrollBottom();
      }
    },

    scrollBottom() {
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
.assistant-fab {
  z-index: 3000 !important;
  width: 72px !important;
  height: 72px !important;
  bottom: 24px !important;
  right: 24px !important;
  box-shadow: 0 10px 28px rgba(46, 125, 50, 0.35) !important;
}

.assistant-card {
  border-radius: 18px;
  overflow: hidden;
}

.assistant-header {
  background: linear-gradient(135deg, #2e7d32, #43a047);
  color: white;
  font-weight: bold;
}

.chat-box {
  height: 360px;
  overflow-y: auto;
  background: #f7faf7;
  padding: 14px;
}

.chat-actions {
  padding: 12px;
}

.message-row {
  display: flex;
  margin-bottom: 10px;
}

.message-row.usuario {
  justify-content: flex-end;
}

.message-row.agente,
.message-row.sistema {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 82%;
  border-radius: 16px;
  padding: 10px 12px;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.08);
}

.message-bubble.usuario {
  background: #2e7d32;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-bubble.agente {
  background: white;
  color: #1f2937;
  border-bottom-left-radius: 4px;
}

.message-bubble.sistema {
  background: #fff3e0;
  color: #7c4d00;
  border-bottom-left-radius: 4px;
}

.message-role {
  font-size: 0.75rem;
  font-weight: 700;
  margin-bottom: 4px;
  opacity: 0.85;
}

.message-text {
  white-space: pre-line;
  line-height: 1.45;
}
</style>