<template>
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
        <v-card
          :color="selectedCategoryId === category.id ? '#D5F0DB' : 'white'"
          :class="selectedCategoryId === category.id ? 'borderme' : 'borderout'"
          class="d-flex align-center rounded-lg mx-2 pa-4 category-card"
          height="250"
          @click="selectCategory(category)"
          flat
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
                :class="selectedCategoryId === category.id ? 'green--text' : 'black--text'"
                class="subtitle-1 mt-4 font-weight-medium"
              >
                {{ category.title }}
              </v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  data: () => ({
    selectedCategoryId: null,
    categories: [
      { img: "01.png", title: "Frutas", slug: "frutas", id: 1 },
      { img: "02.png", title: "Vegetales", slug: "vegetales", id: 2 }
    ]
  }),

  methods: {
    selectCategory(category) {
      if (this.selectedCategoryId === category.id) {
        this.selectedCategoryId = null;
        this.$emit("select-category", null);
        return;
      }

      this.selectedCategoryId = category.id;
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