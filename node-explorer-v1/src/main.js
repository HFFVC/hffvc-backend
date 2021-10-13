import Vue from "vue";
import App from "./App.vue";
import router from "./routes";
import "bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
Vue.config.productionTip = false;
import firebase from "firebase";
import "firebase/firestore";
import configData from "./utils/config-env";

const config = configData().FIREBASE_CONFIG;

firebase.initializeApp(config);
firebase.firestore().enablePersistence(true);

new Vue({
  router,
  render: (h) => h(App),
}).$mount("#app");
