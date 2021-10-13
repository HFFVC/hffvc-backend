<template>
  <div class="main-window main-container">
    <div v-if="seen" class="loading">Loading&#8230;</div>
    <div class="dashboard-header">
      <h2 class="main-head">HFFVC</h2>
      <h5 class="dashboard-sub-head">Blockchain Browser</h5>
      <input
        class="dashboard-search"
        placeholder="Search"
        v-model="searchWord"
        type="text"
      />
      <!-- <p>v-on:keyup.enter="Search()"</p> -->
      <div class="search-options">
        <b-dropdown id="dropdown-1" text="Search By">
          <b-dropdown-item @click="Search()"
            >Service Request Id</b-dropdown-item
          >
          <b-dropdown-item @click="changeSearchBy('batchId')"
            >Batch Id</b-dropdown-item
          >
          <b-dropdown-item @click="changeSearchBy('saleId')"
            >Sale Id</b-dropdown-item
          >
        </b-dropdown>
      </div>
      <div>
        <div>
          <b-modal
            id="searchResultsModal"
            ref="searchResultsModal"
            hide-backdrop
            no-close-on-backdrop
            hide-footer
            data-backdrop="static"
            centered
            title="Search Result:"
          >
            <p v-if="searchResultError" class="my-4">{{ searchResultError }}</p>
            <table class="col-md-12" v-if="searchResultsData != null">
              <tr>
                <th>Created On</th>
                <th>Id</th>
              </tr>
              <tr
                v-for="(item, index) in searchResultsData"
                :key="index"
                @click="
                  searchWord = item.id;
                  changeSearchBy('serviceRequestId');
                "
              >
                <td>{{ item.createdon | moment }}</td>
                <td>{{ item.id }}</td>
              </tr>
            </table>
          </b-modal>
        </div>
      </div>
      <div class="logout-wrap">
        <span class="logout" @click="Logout()">Logout</span>
      </div>
    </div>
    <div class="dashboard-body">
      <div class="dashboard-body-left">
        <div class="dashboard-inner-head">SERVICE REQUESTS (Tx)</div>
        <div class="dashboard-inner-body">
          <table class="service-request-dashboard-table">
            <thead>
              <th>Date</th>
              <th>SR ID</th>
            </thead>
          </table>
          <perfect-scrollbar class="ps-sr-table">
            <table
              class="service-request-dashboard-table sr-table-inner left-table"
            >
              <tbody>
                <tr
                  v-for="item in serviceRequests"
                  :key="item.id"
                  @click="
                    searchWord = item.displayId;
                    Search();
                  "
                >
                  <td>{{ item.createdOn | moment }}</td>
                  <td>{{ item.displayId }}</td>
                </tr>
              </tbody>
            </table>
          </perfect-scrollbar>
        </div>
      </div>
      <div class="dashboard-body-right">
        <div class="dashboard-inner-first">
          <h2 class="select-text" v-if="selectedServiceRequestId == null">
            Select from the list
            <br />OR <br />Search with a Service Request / Batch / Sale ID
          </h2>
          <div
            class="dashboard-text"
            v-if="
              selectedServiceRequestId != null &&
              currentView == 'serviceRequest'
            "
          >
            <div class="text-inner">
              Service Request ID: {{ serviceRequestData.id }}
            </div>
            <div>
              <button
                @click="downloadServiceRequestDetails()"
                class="download-btn"
              >
                <img src="../assets/images/download-button.png" alt srcset />
                Download
              </button>
            </div>
            <div class="text-inner">
              Participants:
              <span v-for="(item, index) in participants" :key="item">
                {{ item }}
                <span v-if="participants.length > index + 1">,</span>
              </span>
            </div>
            <div class="text-inner">
              Signatories:
              <span>
                SAE
                <span v-if="serviceRequestData.acceptData != null">, LSP</span>
                <span v-if="serviceRequestData.enrouteToProducerData != null"
                  >, Collector</span
                >
                <span v-if="serviceRequestData.batchId != null">, Broker</span>
              </span>
            </div>
          </div>
          <div
            v-if="
              selectedServiceRequestId != null &&
              currentView == 'serviceRequest'
            "
            class="current-state"
          >
            <h2>Service Request State</h2>
          </div>
          <div
            class="dashboard-text"
            v-if="selectedServiceRequestId != null && currentView == 'batch'"
          >
            <div class="text-inner" v-if="batchData && batchData.batchId">
              Batch ID: {{ batchData.batchId }}
            </div>
            <div>
              <button @click="downloadBatchDetails()" class="download-btn">
                <img src="../assets/images/download-button.png" alt srcset />
                Download
              </button>
            </div>
            <div class="text-inner">
              Participants:
              <span v-for="(item, index) in participants" :key="item">
                {{ item }}
                <span v-if="participants.length > index + 1">,</span>
              </span>
            </div>
            <div class="text-inner">
              Signatories:
              <span v-for="(item, index) in participants" :key="item">
                {{ item }}
                <span v-if="participants.length > index + 1">,</span>
              </span>
            </div>
            <!-- <div class="current-state">
              <h2>Batch State</h2>
            </div>-->
          </div>
          <div
            v-if="selectedServiceRequestId != null && currentView == 'batch'"
            class="current-state"
          >
            <h2>Batch State</h2>
          </div>
          <div
            class="dashboard-text"
            v-if="selectedServiceRequestId != null && currentView == 'sale'"
          >
            <div class="text-inner" v-if="salesData && salesData.saleId">
              Sale ID: {{ salesData.saleId }}
            </div>
            <div>
              <button @click="downloadSaleDetails()" class="download-btn">
                <img src="../assets/images/download-button.png" alt srcset />
                Download
              </button>
            </div>
            <div class="text-inner">
              Participants:
              <span v-for="(item, index) in participants" :key="item">
                {{ item }}
                <span v-if="participants.length > index + 1">,</span>
              </span>
            </div>
            <div class="text-inner">
              Signatories:
              <span v-for="(item, index) in participants" :key="item">
                {{ item }}
                <span v-if="participants.length > index + 1">,</span>
              </span>
            </div>
          </div>
          <div
            v-if="selectedServiceRequestId != null && currentView == 'sale'"
            class="current-state"
          >
            <h2>Sale State</h2>
          </div>
        </div>
        <div class="dashboard-inner-second">
          <table
            class="service-request-dashboard-table sr-table-inner right-table"
            v-if="
              selectedServiceRequestId != null &&
              currentView != 'serviceRequest'
            "
          >
            <thead>
              <th>Attribute</th>
              <th>Value</th>
            </thead>
          </table>

          <perfect-scrollbar class="ps-attribute-table">
            <table
              class="service-request-dashboard-table sr-table-inner"
              v-if="
                selectedServiceRequestId != null &&
                currentView == 'serviceRequest'
              "
            >
              <tbody>
                <tr>
                  <td colspan="2">
                    <b-button
                      v-b-toggle.collapse-1
                      variant="primary"
                      class="table-menu-header"
                      >Creation Details</b-button
                    >
                    <b-collapse id="collapse-1" class="mt-2">
                      <table class="sr-table-inner col-md-12">
                        <tr>
                          <th>Attribute</th>
                          <th>Value</th>
                        </tr>
                        <tr>
                          <td>Requested On</td>
                          <td>
                            {{
                              serviceRequestData.creationData.requestedOn
                                | moment
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Farmer Id</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>Farmer Name</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>Phone No.</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>Farmer's cin</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>Farmer's NIF</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>Farmer's Location</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>Farmer's Town</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>Product</td>
                          <td>{{ serviceRequestData.creationData.product }}</td>
                        </tr>
                        <tr>
                          <td>Department</td>
                          <td>
                            {{ serviceRequestData.creationData.department }}
                          </td>
                        </tr>
                      </table>
                    </b-collapse>
                  </td>
                </tr>

                <tr>
                  <td colspan="2">
                    <b-button
                      v-b-toggle.collapse-2
                      variant="primary"
                      class="table-menu-header"
                      >Scheduling Details</b-button
                    >
                    <b-collapse id="collapse-2" class="mt-2">
                      <p v-if="serviceRequestData.acceptData == null">
                        No Data Available
                      </p>
                      <table
                        class="sr-table-inner col-md-12"
                        v-if="serviceRequestData.acceptData != null"
                      >
                        <tr>
                          <th>Attribute</th>
                          <th>Value</th>
                        </tr>
                        <tr>
                          <td>Pickup added on</td>
                          <td>
                            {{
                              serviceRequestData.acceptData.pickupAddedOn
                                | moment
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Collector Name</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>Collection Point</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>Dropoff at Packhouse</td>
                          <td>
                            {{
                              serviceRequestData.acceptData.dropOffAtPackhouse
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Date of Pickup</td>
                          <td>
                            {{
                              serviceRequestData.acceptData.dateOfPickup
                                | moment
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Scheduled After</td>
                          <td>
                            {{ serviceRequestData.acceptData.scheduledAfter }}
                            Days
                          </td>
                        </tr>
                      </table>
                    </b-collapse>
                  </td>
                </tr>

                <tr>
                  <td colspan="2">
                    <b-button
                      v-b-toggle.collapse-3
                      variant="primary"
                      class="table-menu-header"
                      >Enroute Producer Details</b-button
                    >
                    <b-collapse id="collapse-3" class="mt-2">
                      <p
                        v-if="serviceRequestData.enrouteToProducerData == null"
                      >
                        No Data Available.
                      </p>
                      <table
                        class="sr-table-inner col-md-12"
                        v-if="serviceRequestData.enrouteToProducerData != null"
                      >
                        <tr>
                          <th>Attribute</th>
                          <th>Value</th>
                        </tr>
                        <tr>
                          <td>Pickup Planned on</td>
                          <td>
                            {{
                              serviceRequestData.enrouteToProducerData
                                .plannedPickUpAddedOn | moment
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Notes</td>
                          <td>
                            {{
                              serviceRequestData.enrouteToProducerData
                                .enrouteAdditionalNote
                            }}
                          </td>
                        </tr>
                      </table>
                    </b-collapse>
                  </td>
                </tr>

                <tr>
                  <td colspan="2">
                    <b-button
                      v-b-toggle.collapse-4
                      variant="primary"
                      class="table-menu-header"
                      >With Producer Details</b-button
                    >
                    <b-collapse id="collapse-4" class="mt-2">
                      <p v-if="serviceRequestData.withProducerData == null">
                        No Data Available.
                      </p>
                      <table
                        class="sr-table-inner col-md-12"
                        v-if="serviceRequestData.withProducerData != null"
                      >
                        <tr>
                          <th>Attribute</th>
                          <th>Value</th>
                        </tr>
                        <tr>
                          <td>Temperature</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData.temperature
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Ambient Temperature</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData.ambientTemp
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Crates</td>
                          <td>
                            {{ serviceRequestData.withProducerData.crates }}
                          </td>
                        </tr>
                        <tr>
                          <td>Fruits Harvested</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData
                                .fruitsHarvested
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Fruits Rejected</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData.fruitRejected
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Advance Given</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData.advanceGiven
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Currency</td>
                          <td>
                            {{ serviceRequestData.withProducerData.currency }}
                          </td>
                        </tr>
                        <tr>
                          <td>LSP Advance Given</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData
                                .lspAdvanceGiven
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>LSP Advance Given Currency</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData
                                .lspAdvanceGivenCurrency
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Staring QR Code</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData.startingQRCode
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Ending QR Code</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData.endingQRCode
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Payment Method</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData.paymentMethod
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.withProducerData
                              .cashPaymentData != null
                          "
                        >
                          <td>Cash Payment Data</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.withProducerData
                              .wirePaymentData != null
                          "
                        >
                          <td>Wire Payment Data</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.withProducerData
                              .mobilePaymentData != null
                          "
                        >
                          <td>Mobile Payment Data</td>
                          <td>{{ anonymized }}</td>
                        </tr>
                        <tr>
                          <td>QR Code Files</td>
                          <td>
                            <span
                              v-for="(item, index) in serviceRequestData
                                .withProducerData.withProducerQrCodeFile"
                              :key="index"
                              >{{ item }}</span
                            >
                          </td>
                        </tr>
                        <tr>
                          <td>Timestamp</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData
                                .withProducerTimeStamp | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="serviceRequestData.withProducerData.ngo != null"
                        >
                          <td>NGO Name</td>
                          <td>{{ serviceRequestData.withProducerData.ngo }}</td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.withProducerData
                              .conversionRate != null
                          "
                        >
                          <td>Conversion Rate</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData.conversionRate
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.withProducerData
                              .conversionCurrency != null
                          "
                        >
                          <td>Conversion Currency</td>
                          <td>
                            {{
                              serviceRequestData.withProducerData
                                .conversionCurrency
                            }}
                          </td>
                        </tr>
                      </table>
                    </b-collapse>
                  </td>
                </tr>

                <tr>
                  <td colspan="2">
                    <b-button
                      v-b-toggle.collapse-5
                      variant="primary"
                      class="table-menu-header"
                      >Enroute Packhouse Details</b-button
                    >
                    <b-collapse id="collapse-5" class="mt-2">
                      <p
                        v-if="serviceRequestData.enrouteToPackhouseData == null"
                      >
                        No Data Available.
                      </p>
                      <table
                        class="sr-table-inner col-md-12"
                        v-if="serviceRequestData.enrouteToPackhouseData != null"
                      >
                        <tr>
                          <th>Attribute</th>
                          <th>Value</th>
                        </tr>
                        <tr>
                          <td>Enroute Packhouse Data Added On</td>
                          <td>
                            {{
                              serviceRequestData.enrouteToPackhouseData
                                .enroutePackhouseAddedOn | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.enrouteToPackhouseData
                              .additionalNotes
                          "
                        >
                          <td>Additional Notes</td>
                          <td>
                            {{
                              serviceRequestData.enrouteToPackhouseData
                                .additionalNotes
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Dropoff At Packhouse</td>
                          <td>
                            {{
                              serviceRequestData.enrouteToPackhouseData
                                .dropOffAtPackhouse
                            }}
                          </td>
                        </tr>
                      </table>
                    </b-collapse>
                  </td>
                </tr>

                <tr>
                  <td
                    colspan="2"
                    v-if="serviceRequestData.arrivedAtPackhouseData != null"
                  >
                    <b-button
                      v-b-toggle.collapse-6
                      variant="primary"
                      class="table-menu-header"
                      >Arrived at Packhouse Details</b-button
                    >

                    <b-collapse id="collapse-6" class="mt-2">
                      <p
                        v-if="serviceRequestData.arrivedAtPackhouseData == null"
                      >
                        No Data Available.
                      </p>
                      <table
                        class="sr-table-inner col-md-12"
                        v-if="serviceRequestData.arrivedAtPackhouseData != null"
                      >
                        <tr>
                          <th>Attribute</th>
                          <th>Value</th>
                        </tr>
                        <tr>
                          <td>Arrived At Packhouse on</td>
                          <td>
                            {{
                              serviceRequestData.arrivedAtPackhouseData
                                .arrivedAtPackhouseAddedOn | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.arrivedAtPackhouseData
                              .transportCostArrivedAtPackhouse
                          "
                        >
                          <td>Transport Cost</td>
                          <td>
                            {{
                              serviceRequestData.arrivedAtPackhouseData
                                .transportCostArrivedAtPackhouse
                            }}
                          </td>
                        </tr>
                        <tr>
                          <td>Time of Admittance</td>
                          <td>
                            {{
                              serviceRequestData.arrivedAtPackhouseData
                                .timeOfAdmittance
                            }}
                          </td>
                        </tr>
                      </table>
                    </b-collapse>
                  </td>
                </tr>

                <tr>
                  <td colspan="2">
                    <b-button
                      v-b-toggle.collapse-7
                      variant="primary"
                      class="table-menu-header"
                      >Fruit Processing Details</b-button
                    >
                    <b-collapse id="collapse-7" class="mt-2">
                      <p v-if="serviceRequestData.fruitFlowData == null">
                        No Data Found.
                      </p>
                      <table
                        class="sr-table-inner col-md-12"
                        v-if="serviceRequestData.fruitFlowData != null"
                      >
                        <tr>
                          <th>Attribute</th>
                          <th>Value</th>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .larvaeTestingData != null
                          "
                        >
                          <td class="table-sub-heading">Larvae Testing Data</td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .larvaeTestingData != null
                          "
                        >
                          <td>Larvae Testing Results</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData.larvaeTestingData
                                .larvaeTesting
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .larvaeTestingData != null &&
                            serviceRequestData.fruitFlowData.larvaeTestingData
                              .rejectReason
                          "
                        >
                          <td>Reject Reason</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData.larvaeTestingData
                                .rejectReason
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .temperatureMeasurementData
                          "
                        >
                          <td class="table-sub-heading">
                            Temperature Measurement
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .temperatureMeasurementData
                          "
                        >
                          <td>Ambient Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .temperatureMeasurementData.ambientTemp
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .temperatureMeasurementData
                          "
                        >
                          <td>Internal Fruit Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .temperatureMeasurementData.internalFruitTemp
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .qualityInspectionData
                          "
                        >
                          <td class="table-sub-heading">
                            Quality Inspection Data
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .qualityInspectionData
                          "
                        >
                          <td>Fruits Accepted</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .qualityInspectionData.fruitsAccepted
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .qualityInspectionData
                          "
                        >
                          <td>Fruits Rejected</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .qualityInspectionData.fruitsRejected
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .qualityInspectionData
                          "
                        >
                          <td>Fruits Rejected</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .qualityInspectionData.qualityInspectionResults
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .cleaningAndTrimmingData
                          "
                        >
                          <td class="table-sub-heading">
                            Cleaning And Trimming Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .cleaningAndTrimmingData
                          "
                        >
                          <td>Cleaned and Trimmed On</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .cleaningAndTrimmingData
                                .dateTimeCleaningTrimming | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.fruitWashingData
                          "
                        >
                          <td class="table-sub-heading">Fruit Washing Data</td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.fruitWashingData
                          "
                        >
                          <td>Water Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData.fruitWashingData
                                .waterTemperature
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.fruitWashingData
                          "
                        >
                          <td>PH Level</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData.fruitWashingData
                                .phLevel
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.fruitWashingData
                          "
                        >
                          <td>Chlorine Level</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData.fruitWashingData
                                .chlorineLevel
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .feedPackingLineConveyorData
                          "
                        >
                          <td class="table-sub-heading">
                            Feed Packing Line Conveyor Belt Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .feedPackingLineConveyorData
                          "
                        >
                          <td>Time of Entry</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .feedPackingLineConveyorData
                                .packingLineTimeofEntry | moment
                            }}
                          </td>
                        </tr>
                        <tr v-if="serviceRequestData.fruitFlowData.gradingData">
                          <td class="table-sub-heading">Grading Data</td>
                          <td></td>
                        </tr>
                        <tr v-if="serviceRequestData.fruitFlowData.gradingData">
                          <td>Grading Results</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData.gradingData
                                .gradingResults
                            }}
                          </td>
                        </tr>
                        <tr v-if="serviceRequestData.fruitFlowData.gradingData">
                          <td>Weight Of the Fruits Removed</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData.gradingData
                                .weightofRemovedFruit
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.secondSizingData
                          "
                        >
                          <td class="table-sub-heading">Second Sizing Data</td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.secondSizingData
                          "
                        >
                          <td>Second Sizing Results</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData.secondSizingData
                                .secondSizingResults
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .cartonFillingAndPalletizingData
                          "
                        >
                          <td class="table-sub-heading">
                            Carton Filling And Palletizing Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .cartonFillingAndPalletizingData
                          "
                        >
                          <td>
                            <div
                              v-for="(item, index) in serviceRequestData
                                .fruitFlowData.cartonFillingAndPalletizingData"
                              :key="index"
                              class="carton-filling"
                            >
                              <span>
                                Start Fruit QR Code: {{ item.startQRCodeFruits
                                }}<br
                              /></span>
                              <span>
                                End Fruit QR Code: {{ item.endQRCodeFruits
                                }}<br
                              /></span>
                              <span>
                                Start Box QR Code: {{ item.startQRCodeBoxes
                                }}<br
                              /></span>
                              <span>
                                End Box QR Code: {{ item.endQRCodeBoxes }}<br
                              /></span>
                            </div>
                          </td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .cartonFillingAndPalletizingData
                                .startQRCodeBoxes
                            }}
                          </td>
                        </tr>
                        <!-- <tr v-if="serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData">
                          <td>End QR Code Boxes</td>
                          <td>{{ serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData.endQRCodeBoxes }}</td>
                        </tr>
                        <tr v-if="serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData">
                          <td>Start QR Code Fruits</td>
                          <td>{{ serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData.startQRCodeFruits }}</td>
                        </tr>
                        <tr v-if="serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData">
                          <td>End QR Code Fruits</td>
                          <td>{{ serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData.endQRCodeFruits }}</td>
                        </tr>
                        <tr v-if="serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData">
                          <td>Total Boxes</td>
                          <td>{{ serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData.totalBoxes }}</td>
                        </tr>
                        <tr v-if="serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData">
                          <td>Total Fruits</td>
                          <td>{{ serviceRequestData.fruitFlowData.cartonFillingAndPalletizingData.totalFruits }}</td>
                        </tr> -->
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .temperatureReadingPackedLotData
                          "
                        >
                          <td class="table-sub-heading">
                            Temperature Reading Packed Lot Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .temperatureReadingPackedLotData
                          "
                        >
                          <td>Ambient Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .temperatureReadingPackedLotData
                                .ambienttemperaturePacked
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .temperatureReadingPackedLotData
                          "
                        >
                          <td>Internal Fruit Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .temperatureReadingPackedLotData
                                .internalFruitTemperaturePacked
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .forcedAirCoolingEntryData
                          "
                        >
                          <td class="table-sub-heading">
                            Forced Cooling Entry Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .forcedAirCoolingEntryData
                          "
                        >
                          <td>Entry Time</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .forcedAirCoolingEntryData
                                .entryTimeForcedCooling | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .forcedAirCoolingEntryData
                          "
                        >
                          <td>Fruit Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .forcedAirCoolingEntryData
                                .fruitTemperatureEntryForcedCooling
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .forcedAirCoolingEntryData
                          "
                        >
                          <td>Airflow Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .forcedAirCoolingEntryData.airflowTemperatureRH
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .forcedAirCoolingRemovalData
                          "
                        >
                          <td class="table-sub-heading">
                            Forced Air Cooling Exit Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .forcedAirCoolingRemovalData
                          "
                        >
                          <td>Time of Removal</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .forcedAirCoolingRemovalData
                                .removalTimeForcedCooling | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .forcedAirCoolingRemovalData
                          "
                        >
                          <td>Fruit Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .forcedAirCoolingRemovalData
                                .fruitTemperatureRemovalForcedCooling
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldRoomStorageInData
                          "
                        >
                          <td class="table-sub-heading">
                            Cold Room Storage in Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldRoomStorageInData
                          "
                        >
                          <td>Fruit Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .coldRoomStorageInData
                                .fruitTemperatureColdStorage
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldRoomStorageInData
                          "
                        >
                          <td>Airflow Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .coldRoomStorageInData
                                .airflowTemperatureRHColdStorage
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldRoomStorageInData
                          "
                        >
                          <td>Storage Time in</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .coldRoomStorageInData.storageTimeIn | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldRoomStorageOutData
                          "
                        >
                          <td class="table-sub-heading">
                            Cold Room Storage Out Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldRoomStorageOutData
                          "
                        >
                          <td>Storage Time out</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .coldRoomStorageOutData.storageTimeOut
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldRoomStorageOutData
                          "
                        >
                          <td>Fruit temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .coldRoomStorageOutData
                                .fruitTemperatureColdStorageOut
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.shippingDetailsData
                          "
                        >
                          <td class="table-sub-heading">Shipping Details</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .shippingDetailsData.shippingDetails
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.samplingDetailsData
                          "
                        >
                          <td class="table-sub-heading">Sampling Details</td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.samplingDetailsData
                          "
                        >
                          <td>Samples Taken</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .samplingDetailsData.samplesTaken
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.samplingDetailsData
                          "
                        >
                          <td>Sampling Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .samplingDetailsData.samplingTemperature
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.samplingDetailsData
                          "
                        >
                          <td>Date and Time of Sampling</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .samplingDetailsData.dateAndTimeofSampling
                                | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .preCoolingReefersData
                          "
                        >
                          <td>Pre Cooling Reefer wall Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .preCoolingReefersData.reeferWallTemperature
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldTunnelLoadingData
                          "
                        >
                          <td class="table-sub-heading">
                            Cold Tunnel Loading Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldTunnelLoadingData
                          "
                        >
                          <td>Loaded On</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .coldTunnelLoadingData.coldTunnelLoadingData
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .coldTunnelLoadingData
                          "
                        >
                          <td>Reefer Wall Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .coldTunnelLoadingData
                                .reeferWallTemperatureColdTunnelLoading
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData.firstSizingData
                          "
                        >
                          <td>First Sizing Results</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData.firstSizingData
                                .firstSizingResults
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hotWaterTreatmentEntryData
                          "
                        >
                          <td class="table-sub-heading">
                            Hot Water Treatment Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hotWaterTreatmentEntryData
                          "
                        >
                          <td>Hot Water Treatment Entry</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hotWaterTreatmentEntryData
                                .timeofEntryHotWaterTreatment | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hotWaterTreatmentEntryData
                          "
                        >
                          <td>Water Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hotWaterTreatmentEntryData
                                .waterTemperatureHotWaterTreatment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hotWaterTreatmentEntryData
                          "
                        >
                          <td>PH Level</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hotWaterTreatmentEntryData
                                .phLevelHotWaterTreatment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hotWaterTreatmentEntryData
                          "
                        >
                          <td>Chlorine Level</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hotWaterTreatmentEntryData
                                .chlorineLevelHotWaterTreatment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hotWaterTreatmentExitData
                          "
                        >
                          <td class="table-sub-heading">
                            Hot Water Treatment Exit Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hotWaterTreatmentExitData
                          "
                        >
                          <td>Duration of Treatment</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hotWaterTreatmentExitData
                                .timeofExitHotWaterTreatment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hotWaterTreatmentExitData
                          "
                        >
                          <td>Duration of Treatment</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hotWaterTreatmentExitData
                                .timeofExitHotWaterTreatment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hydroCoolingEntryData
                          "
                        >
                          <td class="table-sub-heading">
                            Hydro Cooling Entry Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hydroCoolingEntryData
                          "
                        >
                          <td>Time of Entry</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hydroCoolingEntryData.timeofEntryHydroCooling
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hydroCoolingEntryData
                          "
                        >
                          <td>Water Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hydroCoolingEntryData
                                .waterTemperatureHydroCooling
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hydroCoolingEntryData
                          "
                        >
                          <td>PH Level</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hydroCoolingEntryData.phLevelHydroCooling
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hydroCoolingEntryData
                          "
                        >
                          <td>Chlorine Level</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hydroCoolingEntryData.chlorineLevelHydroCooling
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hydroCoolingExitData
                          "
                        >
                          <td class="table-sub-heading">
                            Hydro Cooling Exit Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hydroCoolingExitData
                          "
                        >
                          <td>Duration</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hydroCoolingExitData.durationHydroCooling
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hydroCoolingExitData
                          "
                        >
                          <td>Time of Exit</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hydroCoolingExitData.timeofExitHydroCooling
                                | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .hydroCoolingExitData
                          "
                        >
                          <td>Internal Fruit Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .hydroCoolingExitData
                                .internalFruitTempHydroCooling
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transferTocoldStorageData
                          "
                        >
                          <td class="table-sub-heading">
                            Transfer to Cold Storage Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transferTocoldStorageData
                          "
                        >
                          <td>Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transferTocoldStorageData.temperatureTransfer
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transferTocoldStorageData
                          "
                        >
                          <td>Time in</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transferTocoldStorageData.coldStorageTimeIn
                                | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .removalFromColdStorageData
                          "
                        >
                          <td class="table-sub-heading">
                            Removal from Cold Storage Data
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .removalFromColdStorageData
                          "
                        >
                          <td>Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .removalFromColdStorageData.temperatureRemoval
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transportDetailsData
                          "
                        >
                          <td class="table-sub-heading">
                            Transportation Details
                          </td>
                          <td></td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transportDetailsData
                          "
                        >
                          <td>Departure date and time</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transportDetailsData.departureDateTimeTransport
                                | moment
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transportDetailsData
                          "
                        >
                          <td>Transport Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transportDetailsData.transportTemperature
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transportDetailsData
                          "
                        >
                          <td>Transport Temperature</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transportDetailsData.transportTemperature
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transportDetailsData
                          "
                        >
                          <td>Destination</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transportDetailsData.destinationTransport
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transportDetailsData
                          "
                        >
                          <td>Transport Conditions</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transportDetailsData.transportConditions
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transportDetailsData
                          "
                        >
                          <td>Transport Cost</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transportDetailsData.transportCost
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transportDetailsData
                          "
                        >
                          <td>Transport Currency</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transportDetailsData.transportCurrency
                            }}
                          </td>
                        </tr>
                        <tr
                          v-if="
                            serviceRequestData.fruitFlowData
                              .transportDetailsData
                          "
                        >
                          <td>Fruit Flow Completed On</td>
                          <td>
                            {{
                              serviceRequestData.fruitFlowData
                                .transportDetailsData.fruitFlowCompletedOn
                                | moment
                            }}
                          </td>
                        </tr>
                      </table>
                    </b-collapse>
                  </td>
                </tr>

                <tr>
                  <td colspan="2">
                    <b-button
                      v-b-toggle.collapse-8
                      variant="primary"
                      class="table-menu-header"
                      >Batch Details</b-button
                    >
                    <b-collapse id="collapse-8" class="mt-2">
                      <p v-if="serviceRequestData.batchId == null">
                        No Data Available
                      </p>
                      <table
                        class="sr-table-inner col-md-12"
                        v-if="serviceRequestData.batchId != null"
                      >
                        <tr
                          class="clickable"
                          @click="getBatchDetails(serviceRequestData.batchId)"
                        >
                          <td>Batch Id</td>
                          <td>{{ serviceRequestData.batchId }}</td>
                        </tr>
                      </table>
                    </b-collapse>
                  </td>
                </tr>
                <tr>
                  <td colspan="2">
                    <b-button
                      v-b-toggle.collapse-10
                      variant="primary"
                      class="table-menu-header"
                      >Sale Details</b-button
                    >
                    <b-collapse id="collapse-10" class="mt-2">
                      <p v-if="serviceRequestData.saleIds.length == 0">
                        No Data Available.
                      </p>
                      <table
                        class="sr-table-inner col-md-12"
                        v-if="serviceRequestData.saleIds.length"
                      >
                        <tr>
                          <th>Sale Id</th>
                          <th>Boxes Sold</th>
                        </tr>
                        <tr
                          class="clickable"
                          v-for="(item, index) in serviceRequestData.saleIds"
                          @click="getSalesDetails(item.id)"
                          :key="index"
                        >
                          <td>{{ item.id }}</td>
                          <td>{{ item.boxesSold }}</td>
                        </tr>
                        <!-- <tr>
                          <td>Sale Ids</td>
                          <td>
                            <span
                              class="clickable"
                              v-for="(item, index) in serviceRequestData.saleIds"
                              @click="getSalesDetails(item.id)"
                              :key="index"
                            >
                              {{ item.id }} ( {{ item.boxesSold }} Boxes )
                              <span
                                v-if="serviceRequestData.saleIds.length>(index+1)"
                              >, &nbsp;</span>
                            </span>
                          </td>
                        </tr>-->
                      </table>
                    </b-collapse>
                  </td>
                </tr>
              </tbody>
            </table>

            <table
              class="service-request-dashboard-table sr-table-inner"
              v-if="batchData != null && currentView == 'batch'"
            >
              <tbody>
                <tr>
                  <td>Batch Id</td>
                  <td>{{ batchData.batchId }}</td>
                </tr>
                <tr>
                  <td>Batch Created On</td>
                  <td>{{ batchData.batchCreatedAt | moment }}</td>
                </tr>
                <tr>
                  <td>Pallets Start QR Code</td>
                  <td>{{ batchData.palletStartQRCode }}</td>
                </tr>
                <tr>
                  <td>Pallets End QR Code</td>
                  <td>{{ batchData.palletEndQRCode }}</td>
                </tr>

                <tr
                  v-for="(item, index) in batchData.saleIds"
                  :key="index"
                  class="clickable"
                  @click="getSalesDetails(item)"
                >
                  <td>Sale Id</td>
                  <td>{{ item }}</td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.arrivalAndDestinationData != null
                  "
                >
                  <td class="table-sub-heading">
                    Arrival and Destination Data
                  </td>
                  <td></td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.arrivalAndDestinationData != null
                  "
                >
                  <td>Arrival On</td>
                  <td>
                    {{
                      batchData.batchUpdateData.arrivalAndDestinationData
                        .arrivalTimestamp | moment
                    }}
                  </td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.arrivalAndDestinationData != null
                  "
                >
                  <td>Destination</td>
                  <td>
                    {{
                      batchData.batchUpdateData.arrivalAndDestinationData
                        .destination
                    }}
                  </td>
                </tr>

                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.coldStorageData != null
                  "
                >
                  <td class="table-sub-heading">Cold Storage Data</td>
                  <td></td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.coldStorageData != null
                  "
                >
                  <td>CO2 Level</td>
                  <td>
                    {{ batchData.batchUpdateData.coldStorageData.co2Level }}
                  </td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.coldStorageData != null
                  "
                >
                  <td>Ethylene Level</td>
                  <td>
                    {{
                      batchData.batchUpdateData.coldStorageData.ethyleneLevel
                    }}
                  </td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.coldStorageData != null
                  "
                >
                  <td>PH Level</td>
                  <td>
                    {{ batchData.batchUpdateData.coldStorageData.phLevel }}
                  </td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.coldStorageData != null
                  "
                >
                  <td>Temperature</td>
                  <td>
                    {{ batchData.batchUpdateData.coldStorageData.temperature }}
                  </td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.coldStorageData != null
                  "
                >
                  <td>Coldroom Storage in at</td>
                  <td>
                    {{
                      batchData.batchUpdateData.coldStorageData
                        .coldStorageInTimestamp | moment
                    }}
                  </td>
                </tr>

                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.costOfMaturationData != null
                  "
                >
                  <td class="table-sub-heading">Cost of Matutation Data</td>
                  <td></td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.costOfMaturationData != null
                  "
                >
                  <td>Cost of Maturation</td>
                  <td>
                    {{
                      batchData.batchUpdateData.costOfMaturationData
                        .costOfMaturation
                    }}
                  </td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.costOfMaturationData != null
                  "
                >
                  <td>Currency</td>
                  <td>
                    {{
                      batchData.batchUpdateData.costOfMaturationData.currency
                    }}
                  </td>
                </tr>

                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.qualityInspectionData != null
                  "
                >
                  <td class="table-sub-heading">Quality Inspection Data</td>
                  <td></td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.qualityInspectionData != null
                  "
                >
                  <td>Fruits Rejected</td>
                  <td>
                    {{
                      batchData.batchUpdateData.qualityInspectionData
                        .fruitsRejected
                    }}
                  </td>
                </tr>
                <tr
                  v-if="
                    batchData.batchUpdateData != null &&
                    batchData.batchUpdateData.qualityInspectionData != null &&
                    batchData.batchUpdateData.qualityInspectionData
                      .additionalComments != null
                  "
                >
                  <td>Notes</td>
                  <td>
                    {{
                      batchData.batchUpdateData.qualityInspectionData
                        .additionalComments
                    }}
                  </td>
                </tr>

                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Invoice Created On</td>
                  <td>
                    {{
                      batchData.proformaInvoiceData.proFormaUpdatedOn | moment
                    }}
                  </td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Proforma Invoice Number</td>
                  <td>
                    {{ batchData.proformaInvoiceData.batchProforma_ProformaNo }}
                  </td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Name</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Email</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Address Line 1</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Address Line 2</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Address Line 3</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Shipping Address Line 1</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Shipping Address Line 2</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Shipping Address Line 3</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Unit Price</td>
                  <td>
                    {{
                      batchData.proformaInvoiceData
                        .batchProforma_Shipping_UnitPrice
                    }}
                  </td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Shipping Currency</td>
                  <td>
                    {{
                      batchData.proformaInvoiceData
                        .batchProforma_Shipping_Currency
                    }}
                  </td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Drop Location</td>
                  <td>{{ batchData.proformaInvoiceData.dropLocation }}</td>
                </tr>
                <tr v-if="batchData.proformaInvoiceData != null">
                  <td>Seller Name</td>
                  <td>{{ batchData.proformaInvoiceData.sellerName }}</td>
                </tr>
              </tbody>
            </table>

            <table
              class="service-request-dashboard-table sr-table-inner"
              v-if="salesData != null && currentView == 'sale'"
            >
              <tbody>
                <tr>
                  <td>Sale Id</td>
                  <td>{{ salesData.saleId }}</td>
                </tr>
                <tr
                  @click="getBatchDetails(salesData.batchId)"
                  class="clickable"
                >
                  <td>Batch Id</td>
                  <td>{{ salesData.batchId }}</td>
                </tr>
                <tr>
                  <td>Buyer Name</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr>
                  <td>Buyer Address</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr>
                  <td>Buyer Organization</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr>
                  <td>Buyer Contact Details</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr>
                  <td>Buyer Email Address</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr>
                  <td>Transaction Date</td>
                  <td>
                    {{ salesData.creationData.saleTransactionDate | moment }}
                  </td>
                </tr>
                <tr>
                  <td>Boxes Sold</td>
                  <td>{{ salesData.creationData.totalNoOfBoxesSold }}</td>
                </tr>
                <tr>
                  <td>Product Sold</td>
                  <td>{{ salesData.creationData.soldProduct }}</td>
                </tr>
                <tr>
                  <td>Price Per Kg.</td>
                  <td>{{ salesData.creationData.salesPricePerKg }}</td>
                </tr>
                <tr>
                  <td>Weight Per Box</td>
                  <td>{{ salesData.creationData.salesWeightPerBox }}</td>
                </tr>
                <tr>
                  <td>Currency</td>
                  <td>{{ salesData.creationData.salesCurrency }}</td>
                </tr>
                <tr>
                  <td>Broker Margin</td>
                  <td>{{ salesData.creationData.salesBrokerMargin }}</td>
                </tr>

                <tr>
                  <td>Sale Details</td>
                </tr>
                <tr
                  v-for="(item, index) in salesData.lotsSold"
                  :key="index"
                  class="clickable"
                  @click="
                    searchWord = item.id;
                    changeSearchBy('serviceRequestId');
                  "
                >
                  <td>Lot Id: {{ item.id }}</td>
                  <td>Boxes sold {{ item.boxesSold }}</td>
                </tr>

                <tr v-if="salesData.shipOrderData != null">
                  <td>Data and Time of Loading</td>
                  <td>
                    {{ salesData.shipOrderData.dateAndTimeOfLoading | moment }}
                  </td>
                </tr>
                <tr v-if="salesData.shipOrderData != null">
                  <td>Total Number of Boxes Loaded</td>
                  <td>{{ salesData.shipOrderData.totalNoofBoxesLoaded }}</td>
                </tr>
                <tr v-if="salesData.shipOrderData != null">
                  <td>Cost of Transportation</td>
                  <td>
                    {{ salesData.shipOrderData.sellCostOfTransportation }}
                  </td>
                </tr>
                <tr v-if="salesData.shipOrderData != null">
                  <td>Buyer Location</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="salesData.shipOrderData != null">
                  <td>Currency</td>
                  <td>{{ salesData.shipOrderData.shipOrderCurrency }}</td>
                </tr>

                <tr v-if="salesData.unloadingAtBuyerData != null">
                  <td>Data and Time of Unloading</td>
                  <td>
                    {{
                      salesData.unloadingAtBuyerData.dateAndTimeOfUnLoading
                        | moment
                    }}
                  </td>
                </tr>
                <tr v-if="salesData.unloadingAtBuyerData != null">
                  <td>Total Number of Boxes Unloaded</td>
                  <td>
                    {{ salesData.unloadingAtBuyerData.totalNoofBoxesUnLoaded }}
                  </td>
                </tr>

                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Invoice Number</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Billing Date</td>
                  <td>{{ salesData.salesInvoiceData.billingDate | moment }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Broker Name</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Broker Organization</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Buyer Name</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Buyer Organization</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Boxes Purchased</td>
                  <td>{{ salesData.salesInvoiceData.noofBoxesPurchased }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Product</td>
                  <td>{{ salesData.salesInvoiceData.product }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Price per Kg.</td>
                  <td>{{ salesData.salesInvoiceData.pricePerKg }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Currency</td>
                  <td>{{ salesData.salesInvoiceData.pricePerKgCurrency }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>GI Currency</td>
                  <td>{{ salesData.salesInvoiceData.GICurrency }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Net Sales</td>
                  <td>{{ salesData.salesInvoiceData.netSales }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Approximate weight of the fruits sold</td>
                  <td>
                    {{ salesData.salesInvoiceData.aproximateWeightOfProduct }}
                  </td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Broker Percentage</td>
                  <td>{{ salesData.salesInvoiceData.brokerPercent }}</td>
                </tr>
                <tr v-if="salesData.salesInvoiceData != null">
                  <td>Broker Transport Flat Fee</td>
                  <td>
                    {{ salesData.salesInvoiceData.brokerTransportFlatFee }}
                  </td>
                </tr>

                <tr v-if="salesData.confirmPaymentData != null">
                  <td>Payment confirmed On</td>
                  <td>
                    {{
                      salesData.confirmPaymentData.confirmPaymentDate | moment
                    }}
                  </td>
                </tr>
                <tr v-if="salesData.confirmPaymentData != null">
                  <td>Net Receivables</td>
                  <td>{{ salesData.confirmPaymentData.netReceivables }}</td>
                </tr>
                <tr v-if="salesData.confirmPaymentData != null">
                  <td>Currency</td>
                  <td>
                    {{
                      salesData.confirmPaymentData.paymentNetReceivablesCurrency
                    }}
                  </td>
                </tr>
                <tr
                  v-if="
                    salesData.salesInvoiceData != null &&
                    salesData.confirmPaymentData != null
                  "
                >
                  <td>Was Factored</td>
                  <td>{{ salesData.confirmPaymentData.wasFactored }}</td>
                </tr>
                <tr
                  v-if="
                    salesData.salesInvoiceData != null &&
                    salesData.confirmPaymentData != null &&
                    salesData.confirmPaymentData.amountFactored != null
                  "
                >
                  <td>Amount Factored</td>
                  <td>{{ salesData.confirmPaymentData.amountFactored }}</td>
                </tr>
                <tr
                  v-if="
                    salesData.salesInvoiceData != null &&
                    salesData.confirmPaymentData != null &&
                    salesData.confirmPaymentData.factoringEntity
                  "
                >
                  <td>Factoring Entity</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr
                  v-if="
                    salesData.salesInvoiceData != null &&
                    salesData.confirmPaymentData != null &&
                    salesData.confirmPaymentData.factoringAgent
                  "
                >
                  <td>Boxes Purchased</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr
                  v-if="
                    salesData.salesInvoiceData != null &&
                    salesData.confirmPaymentData != null &&
                    salesData.confirmPaymentData.factoringContactDetails
                  "
                >
                  <td>Factoring Contact Details</td>
                  <td>{{ anonymized }}</td>
                </tr>
                <tr
                  v-if="
                    salesData.salesInvoiceData != null &&
                    salesData.confirmPaymentData != null &&
                    salesData.salesInvoiceData.factoringCharges
                  "
                >
                  <td>Factoring Charges.</td>
                  <td>{{ salesData.salesInvoiceData.factoringCharges }}</td>
                </tr>
              </tbody>
            </table>
          </perfect-scrollbar>
        </div>
      </div>
    </div>
    <!-- <div class="footer">Powered by Agriledger</div> -->
  </div>
</template>
<script>
import Vue from "vue";
import "../assets/css/main.css";
import axios from "axios";
import { PerfectScrollbar } from "vue2-perfect-scrollbar";
import "vue2-perfect-scrollbar/dist/vue2-perfect-scrollbar.css";
import moment from "moment";
import Router from "../routes";
import firebase from "firebase";
import VueToast from "vue-toast-notification";
import "vue-toast-notification/dist/theme-default.css";
import "bootstrap";
import "bootstrap/dist/css/bootstrap.min.css";
import BootstrapVue from "bootstrap-vue";
import { saveAs } from "file-saver";
import configData from "../utils/config-env";
Vue.use(BootstrapVue);
Vue.use(VueToast);

export default {
  name: "dashboard",
  components: {
    PerfectScrollbar,
  },
  filters: {
    moment: function (date) {
      return moment(date).format("DD MMM YYYY");
    },
  },
  data: function () {
    return {
      IDtoken: "",
      searchWord: null,
      serviceRequestData: {},
      selectedServiceRequestId: null,
      serviceRequests: [],
      seen: false,
      searchFlag: false,
      anonymized: "XXXXXXXXXX",
      searchBy: "ServiceRequestId",
      showSearchResults: false,
      searchResults: null,
      searchResultError: null,
      searchResultsData: null,
      batchData: null,
      salesData: null,
      currentView: "",
      participants: ["SAE", "LSP", "Collector", "Broker"],
    };
  },
  mounted() {
    this.seen = true;
    this.IDToken = localStorage.getItem("IDToken");
    axios
      .get(`${configData().BASE_URL}/api/v1.0/service-requests-recent`, {
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + localStorage.IDtoken,
        },
      })
      .then((response) => {
        this.seen = false;
        this.serviceRequests = response.data.serviceRequests;        
      })
      .catch((error) => {
        this.seen = false;
        localStorage.removeItem("IDtoken");
        Router.push("/login");
        console.log(error);
      });
  },
  methods: {
    Logout() {
      localStorage.removeItem("IDtoken");
      Router.push("/login");
    },
    Search() {
      var vm = this;
      this.searchFlag = true;
      if (!isNaN(Number(this.searchWord))) {
        this.seen = true;
        let timer = setTimeout(() => {
          vm.seen = false;
          if (!vm.selectedServiceRequestId) {
            Vue.$toast.open({
              message: `Service Request Not Found.`,
              type: "error",
              position: "top",
            });
          }
          vm.searchWord = null;
        }, 7000);
        var dbRef = firebase.database().ref("serviceRequest/");
        dbRef
          .orderByChild("displayId")
          .equalTo(Number(this.searchWord))
          .on("child_added", (data) => {
            vm.seen = false;
            vm.searchWord = data.val().serviceRequestId;
            vm.selectedServiceRequestId = data.val().serviceRequestId;
            clearTimeout(timer);
            vm.changeSearchBy("serviceRequestId");
          });
      } else {
        Vue.$toast.open({
          message: "Please enter a valid Service Request Id.",
          type: "error",
          position: "top",
        });
      }
    },
    GetServiceRequestById(id, searchFlag) {
      this.seen = true;
      this.searchFlag = searchFlag;
      if (this.searchFlag == false) {
        this.searchWord = null;
      }
      this.selectedServiceRequestId = id;
      axios
        .get(
          `${configData().BASE_URL}/api/v1.0/service-request-by-id?id=${
            this.selectedServiceRequestId
          }`,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: "Bearer " + localStorage.IDtoken,
            },
          }
        )
        .then((response) => {
          this.seen = false;

          this.serviceRequestData = response.data;
          //console.log(response);
        })
        .catch(function () {
          this.seen = false;

          //console.log(error)
        });
    },
    changeSearchBy(searchBy) {
      this.batchData = null;
      this.salesData = null;
      this.$refs["searchResultsModal"].hide();
      console.log(searchBy);
      this.searchBy = searchBy;
      this.selectedServiceRequestId = id;
      const id = this.searchWord;
      this.searchWord = null;
      const vm = this;
      if (searchBy == "serviceRequestId") {
        vm.seen = true;
        axios
          .get(
            `${
              configData().BASE_URL
            }/api/v1.0/node-explorer/service-request-details?id=${id}`,
            {
              headers: {
                "Content-Type": "application/json",
                Authorization: "Bearer " + localStorage.IDtoken,
              },
            }
          )
          .then((response) => {
            console.log(response);
            vm.searchFlag = false;
            vm.currentView = "serviceRequest";
            vm.selectedServiceRequestId = response.data.id;
            vm.serviceRequestData = response.data;
            vm.seen = false;
          })
          .catch((err) => {
            if (vm.isTokenValid(err)) {
              vm.seen = false;
              Vue.$toast.open({
                message: "Invalid Service Request Id",
                type: "error",
                position: "top",
              });
              console.log(err);
            }
          });
      } else if (searchBy == "batchId") {
        vm.seen = true;
        axios
          .get(
            `${
              configData().BASE_URL
            }/api/v1.0/node-explorer/batch-details?batchId=${id}`,
            {
              headers: {
                "Content-Type": "application/json",
                Authorization: "Bearer " + localStorage.IDtoken,
              },
            }
          )
          .then((response) => {
            console.log(response);
            vm.searchFlag = false;
            vm.batchData = response.data;
            vm.selectedServiceRequestId = id;
            vm.salesData = null;
            vm.searchWord = null;
            vm.currentView = "batch";
            // vm.searchResultsData = response.data;
            vm.seen = false;
            // vm.$refs["searchResultsModal"].show();
          })
          .catch((err) => {
            vm.seen = false;
            if (vm.isTokenValid(err)) {
              Vue.$toast.open({
                message: "Invalid Batch Id",
                type: "error",
                position: "top",
              });
              console.log(err);
            }
          });
      } else if (searchBy == "saleId") {
        vm.seen = true;
        axios
          .get(
            `${
              configData().BASE_URL
            }/api/v1.0/node-explorer/sales-details?saleId=${id}`,
            {
              headers: {
                "Content-Type": "application/json",
                Authorization: "Bearer " + localStorage.IDtoken,
              },
            }
          )
          .then((response) => {
            console.log(response);
            vm.searchFlag = false;
            vm.selectedServiceRequestId = id;
            vm.salesData = null;
            vm.searchWord = null;
            vm.currentView = "sale";
            vm.searchFlag = false;
            vm.salesData = response.data;
            // vm.searchResultsData = response.data;
            vm.seen = false;
            // vm.$refs["searchResultsModal"].show();
          })
          .catch((err) => {
            vm.seen = false;
            if (vm.isTokenValid(err)) {
              Vue.$toast.open({
                message: "Invaid Sale ID.",
                type: "error",
                position: "top",
              });
              console.log(err);
            }
          });
      } else {
        console.log("Invalid choice");
      }
    },
    getBatchDetails(batchId) {
      this.seen = true;
      console.log(batchId);
      this.selectedServiceRequestId = batchId;
      this.salesData = null;
      this.searchWord = null;

      const vm = this;

      axios
        .get(
          `${
            configData().BASE_URL
          }/api/v1.0/node-explorer/batch-details?batchId=${batchId}`,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: "Bearer " + localStorage.IDtoken,
            },
          }
        )
        .then((response) => {
          vm.currentView = "batch";
          vm.batchData = response.data;
          vm.seen = false;
        })
        .catch((error) => {
          if (vm.isTokenValid(error)) {
            console.log(error);
          }
          vm.seen = false;
        });
    },
    getSalesDetails(saleId) {
      this.seen = true;
      console.log(saleId);
      this.selectedServiceRequestId = saleId;
      this.batchData = null;
      this.searchWord = null;

      const vm = this;

      axios
        .get(
          `${
            configData().BASE_URL
          }/api/v1.0/node-explorer/sales-details?saleId=${saleId}`,
          {
            headers: {
              "Content-Type": "application/json",
              Authorization: "Bearer " + localStorage.IDtoken,
            },
          }
        )
        .then((response) => {
          vm.currentView = "sale";
          vm.salesData = response.data;
          vm.seen = false;
        })
        .catch((error) => {
          if (vm.isTokenValid(error)) {
            console.log(error);
          }
          vm.seen = false;
        });
    },
    downloadServiceRequestDetails() {
      const vm = this;
      console.log(vm.selectedServiceRequestId);
      axios
        .get(
          `${
            configData().BASE_URL
          }/api/v1.0/node-explorer/download-service-request?id=${
            vm.selectedServiceRequestId
          }`,
          {
            headers: {
              "Content-Type": "application/vnd.ms-excel",
              Authorization: "Bearer " + localStorage.IDtoken,
            },
            responseType: "blob",
          }
        )
        .then((response) => {
          saveAs(
            response.data,
            `service_request_${vm.selectedServiceRequestId}.xlsx`
          );
        })
        .catch((failed) => {
          vm.isTokenValid();
          console.log(failed);
        });
    },
    downloadBatchDetails() {
      const vm = this;
      console.log(vm.selectedServiceRequestId);
      axios
        .get(
          `${
            configData().BASE_URL
          }/api/v1.0/node-explorer/download-batch-details?batchId=${
            vm.selectedServiceRequestId
          }`,
          {
            headers: {
              "Content-Type": "application/vnd.ms-excel",
              Authorization: "Bearer " + localStorage.IDtoken,
            },
            responseType: "blob",
          }
        )
        .then((response) => {
          saveAs(response.data, `batch_${vm.selectedServiceRequestId}.xlsx`);
        })
        .catch((failed) => {
          vm.isTokenValid();
          console.log(failed);
        });
    },
    downloadSaleDetails() {
      const vm = this;
      console.log(vm.selectedServiceRequestId);
      axios
        .get(
          `${
            configData().BASE_URL
          }/api/v1.0/node-explorer/download-sale-details?saleId=${
            vm.selectedServiceRequestId
          }`,
          {
            headers: {
              "Content-Type": "application/vnd.ms-excel",
              Authorization: "Bearer " + localStorage.IDtoken,
            },
            responseType: "blob",
          }
        )
        .then((response) => {
          saveAs(response.data, `sale_${vm.selectedServiceRequestId}.xlsx`);
        })
        .catch((failed) => {
          if (vm.isTokenValid()) {
            console.log("Internal error");
          }
          console.log(failed);
        });
    },

    isTokenValid(error) {
      let errorObject = JSON.parse(JSON.stringify(error));
      if (errorObject.message.indexOf("401") > -1) {
        Router.push("/login");
        Vue.$toast.open({
          message: "Please login again.",
          type: "error",
          position: "top",
        });
        return false;
      }
      return true;
    },
  },
};
</script>