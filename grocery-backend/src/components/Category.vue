<template>
  <v-item-group v-model="selectedIndex" class="mt-n1">
    <v-container>
      <v-row justify="center" class="space">
        <v-col
          cols="12"
          xs="12"
          sm="6"
          md="3"
          v-for="(category, i) in categories"
          :key="i"
        >
          <v-item v-slot="{ active, toggle }">
            <v-card
              :color="active ? '#D5F0DB' : 'white'"
              :class="active ? 'borderme' : 'borderout'"
              class="d-flex align-center rounded-lg mx-2 pa-4 category-card"
              height="250"
              flat
              @click="handleCategoryClick(category, i, toggle)"
            >
              <v-list-item three-line class="text-center" style="width: 100%">
                <v-list-item-content>
                  <v-responsive :aspect-ratio="1" class="mx-auto" max-width="120">
                    <v-img
                      :src="category.img"
                      contain
                      height="100%"
                      width="100%"
                    ></v-img>
                  </v-responsive>

                  <v-list-item-subtitle
                    :class="active ? 'green--text' : 'black--text'"
                    class="subtitle-1 mt-4 font-weight-medium"
                  >
                    {{ category.title }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-card>
          </v-item>
        </v-col>
      </v-row>
    </v-container>
  </v-item-group>
</template>

<script>
export default {
  name: "Category",

  data() {
    return {
      selectedIndex: null,
      categories: [
        {
          img: "01.png",
          title: "Frutas",
          slug: "frutas",
          id: 1
        },
        {
          img: "02.png",
          title: "Vegetales",
          slug: "vegetales",
          id: 2
        }
      ]
    };
  },

  methods: {
    handleCategoryClick(category, index, toggle) {
      if (this.selectedIndex === index) {
        this.selectedIndex = null;
        this.$emit("select-category", null);
        return;
      }

      toggle();
      this.$emit("select-category", category);
    }
  }
};
</script>

<style scoped>
.borderme {
  border: 2px solid #4caf50 !important;
}

.borderout {
  border: 1px solid #ccc !important;
}

.category-card {
  cursor: pointer;
  transition: all 0.25s ease;
}

.category-card:hover {
  transform: translateY(-4px);
}
</style>