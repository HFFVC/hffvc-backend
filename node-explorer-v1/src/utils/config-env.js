import configVars from "./config-vars";

function configData() {
  var appConfig = {
    FIREBASE_CONFIG: configVars.FIREBASE_CONFIG,
    BASE_URL: configVars.BASE_URL,
  };
  return appConfig;
}

export default configData;
