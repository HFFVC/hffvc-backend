# Node-Explorer

## Overview
The application serves as a Web-based tool for visualising the data recorded in the node-vaults. Node-Explorer communicates with the Corda Network and fetches the ledger data by interacting with API Layer.

The features available in the Node-Explorer include:
* Listing of Service Requests
* Search by Service Request-Id, Batch-Id and Sale-Id
* Navigation by Ids
* Ledger visualization at multiple levels
* Export of States in Excel format

## Configuration
The solution has to be configured by setting the appropriate values inside the config-vars.js file (present in src\utils).

## Running the Node Explorer
* Set up the project
    ```
    npm install
    ```

* Compile and run the solution
    ```
    npm run serve
    ```
  
* Compile and minify for production
    ```
    npm run build
    ```