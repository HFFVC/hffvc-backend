<template>
  <div class="main-window">
    <div v-if="seen" class="loading">Loading&#8230;</div>

    <div class="login-container" v-on:keyup.enter="onEnter">
      <div class="login-header">
        <h2 class="main-head">HFFVC</h2>
        <h6 class="header-login">NODE EXPLORER LOGIN</h6>
      </div>
      <div class="login-body">
        <input
          type="text"
          class="inp-user"
          v-model="username"
          placeholder="Super Admin User ID/Email Address"
        />

        <input type="password" class="inp-password" v-model="password" placeholder="Password" />
        <input v-on:click="login" type="button" class="btn-login" value="Login" />
      </div>
    </div>
    <div class="footer">Powered by Agriledger</div>
  </div>
</template>
<script>
import Vue from "vue";
import "../assets/css/main.css";
import VueToast from "vue-toast-notification";
import "vue-toast-notification/dist/theme-default.css";
import firebase from "firebase";
import Router from "../routes";
Vue.use(VueToast);

export default {
  name: "login",
  data: function() {
    return {
      username: "",
      password: "",
      errorMessage: "",
      seen: false,
      producerList: [],
      IDtoken: ""
    };
  },
  mounted() {
    if (
      localStorage.getItem("IDtoken") != undefined &&
      localStorage.getItem("IDtoken") != null
    ) {
      Router.push("dashboard");
    }
  },
  methods: {
    onEnter: function() {
      this.login();
    },
    login: function() {
      this.seen = true;
      //push producerData to local Storage
      if (this.username == null || this.username == "") {
        this.seen = false;
        Vue.$toast.open({
          message: "Please enter the email address.",
          type: "error",
          position: "top"
        });
      } else if (this.password == null || this.password == "") {
        this.seen = false;
        Vue.$toast.open({
          message: "Please enter the password.",
          type: "error",
          position: "top"
        });
      } else {
        var currentval = this;
        firebase
          .auth()
          .signInWithEmailAndPassword(this.username.trim(), this.password)
          .then(function() {
            firebase
              .auth()
              .currentUser.getIdToken(true)
              .then(idtoken => {
                localStorage.IDtoken = idtoken;
                currentval.seen = false;
                Router.push("dashboard");
              });
          })
          .catch(function(error) {
            currentval.seen = false;
            Vue.$toast.open({
              message: error.message,
              type: "error",
              position: "top"
            });
          });
      }
    }
  }
};
</script>