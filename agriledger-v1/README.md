
# HFFVC Backend - Blockchain

## Overview
The project represents the blockchain component of the backend solution for HFFVC, and is comprised of the code for the CorDapps as well as the overlying API layer.

The project's components can be broadly listed down as below:

* contracts - The states and the associated smart contracts.
* workflows - Corda workflows, which define the transactions to be initiated and recorded in the system.
* clients - The API Layer, which specifies the APIs required for interacting with the Corda Network.

## Configuration
Here are the steps for configuring the solution, before setting up the nodes, and the API services.

### clients
* Update the settings.json file with appropriate values.
* Update the test-firebase-adminsdk.json file with the details for connecting to a Firebase instance.

### workflows
* Check for the fields tagged as "TO BE SET" inside the test_data.json (present in workflows/src/test/resources) and update the values accordingly. This step would be required only if we are planning to run the flow tests.

## Building and running the nodes
1. Deploy the nodes.
   ```
   gradlew clean deployNodes
   ```
2. Run the nodes.
    ```
    build\nodes\runnodes
    ```

## Building and deploying the API Layer
1. Start the Spring Boot Server for each party using the appropriate gradle task.
For instance, SAE API Server can be started using the below command:
   ```
   gradlew.bat runSAEServer
   ```

2. Validate the API layer by loading the Swagger URLs for different nodes. For instance, the Swagger UI for SAE can be loaded using the below URL:
   
   http://localhost:50005/swagger-ui.html


3. APIs can be tested using a valid Firebase token. It has to be noted that the Firebase instance used for generating the token should be the same as the one provided in the configuration section. The Firebase token  should be provided in the request header as a bearer token.
   ```
   Bearer <Firebase Token>
   ```
