//Proper values to be inserted in place of empty strings.
//Multiple environment modes (with approppriate values) can be added to the Configuration Object.
//ENV_MODE indicates the environment mode using which the application would be built and run.

const ENV_MODE = "TEST";

const configObj = {
  TEST: {
    FIREBASE_CONFIG: {
      apiKey: "",
      authDomain: "",
      databaseURL: "",
      projectId: "",
      storageBucket: "",
      messagingSenderId: "",
    },
    BASE_URL: "",
  }
};

export default configObj[ENV_MODE];
