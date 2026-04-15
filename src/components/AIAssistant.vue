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
      class="assistant-fab"
      @click="toggleChat"
    >
      <v-icon>mdi-robot</v-icon>
    </v-btn>

    <!-- Ventana chat -->
    <v-dialog v-model="open" width="420" persistent>
      <v-card class="assistant-card">
        <v-card-title class="assistant-header">
          <div class="header-content">
            <div>
              <div class="assistant-title">Asistente Virtual</div>
              <div class="assistant-subtitle">Frutas, vegetales y recetas</div>
            </div>

            <v-btn icon dark @click="open = false">
              <v-icon>mdi-close</v-icon>
            </v-btn>
          </div>
        </v-card-title>

        <v-card-text
          ref="chatBox"
          class="chat-body"
        >
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="message-row"
            :class="msg.type"
          >
            <div class="message-bubble" :class="msg.type">
              <div class="message-author">
                {{ msg.author }}
              </div>

              <div class="message-text">
                {{ msg.text }}
              </div>
            </div>
          </div>

          <div v-if="isTyping" class="message-row bot">
            <div class="message-bubble bot typing-bubble">
              <div class="message-author">Asistente</div>
              <div class="typing-text">Escribiendo...</div>
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
            class="chat-input"
            @keyup.enter="sendMessage"
          ></v-text-field>

          <v-btn
            color="green"
            dark
            class="send-btn"
            :loading="isTyping"
            @click="sendMessage"
          >
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
      isTyping: false,
      messages: [
        {
          type: "bot",
          author: "Asistente",
          text: "¡Hola! 👋 Soy tu asistente virtual. Puedo ayudarte a buscar productos, agregar artículos al carrito y sugerirte recetas con frutas y verduras."
        }
      ]
    };
  },

  methods: {
    toggleChat() {
      this.open = true;
      this.scrollToBottom();
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const chatBox = this.$refs.chatBox;
        if (chatBox) {
          chatBox.scrollTop = chatBox.scrollHeight;
        }
      });
    },

    async sendMessage() {
      if (!this.input.trim() || this.isTyping) return;

      const user = JSON.parse(localStorage.getItem("user"));

      if (!user) {
        this.messages.push({
          type: "system",
          author: "Sistema",
          text: "⚠️ Debes iniciar sesión para usar el asistente."
        });
        this.scrollToBottom();
        return;
      }

      const texto = this.input.trim();
      const usuario = user.id || user.email;

      this.messages.push({
        type: "user",
        author: "Tú",
        text: texto
      });

      this.input = "";
      this.isTyping = true;
      this.scrollToBottom();

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
          throw new Error("Respuesta inválida del servidor");
        }

        const data = await response.json();

        this.messages.push({
          type: "bot",
          author: "Asistente",
          text: data.reply || "No pude generar una respuesta en este momento."
        });

        window.dispatchEvent(new Event("carritoActualizado"));
      } catch (error) {
        console.error(error);

        this.messages.push({
          type: "system",
          author: "Sistema",
          text: "❌ Ocurrió un error al conectar con el servidor."
        });
      } finally {
        this.isTyping = false;
        this.scrollToBottom();
      }
    }
  }
};
</script>

<style scoped>
.assistant-fab {
  z-index: 999;
}

.assistant-card {
  border-radius: 18px;
  overflow: hidden;
}

.assistant-header {
  background: linear-gradient(135deg, #2e7d32, #43a047);
  color: white;
  padding: 16px;
}

.header-content {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.assistant-title {
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1.2;
}

.assistant-subtitle {
  font-size: 0.82rem;
  opacity: 0.9;
  margin-top: 2px;
}

.chat-body {
  height: 380px;
  overflow-y: auto;
  background: #f5f7f6;
  padding: 16px;
}

.message-row {
  display: flex;
  margin-bottom: 12px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.bot,
.message-row.system {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 82%;
  padding: 10px 12px;
  border-radius: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-bubble.user {
  background: #2e7d32;
  color: white;
  border-bottom-right-radius: 6px;
}

.message-bubble.bot {
  background: white;
  color: #263238;
  border-bottom-left-radius: 6px;
}

.message-bubble.system {
  background: #fff3e0;
  color: #6d4c41;
  border-bottom-left-radius: 6px;
}

.message-author {
  font-size: 0.76rem;
  font-weight: 700;
  margin-bottom: 4px;
  opacity: 0.85;
}

.message-text {
  white-space: pre-line;
  line-height: 1.45;
  font-size: 0.95rem;
}

.typing-bubble {
  opacity: 0.92;
}

.typing-text {
  font-style: italic;
  color: #607d8b;
}

.chat-actions {
  padding: 12px 16px 16px 16px;
  background: white;
  display: flex;
  gap: 10px;
  align-items: center;
}

.chat-input {
  flex: 1;
}

.send-btn {
  height: 40px;
}
</style>