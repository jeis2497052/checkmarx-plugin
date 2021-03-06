package com.checkmarx.jenkins.opensourceanalysis;
import com.checkmarx.jenkins.OsaScanResult;
import com.checkmarx.jenkins.web.client.OsaScanClient;
import com.checkmarx.jenkins.web.model.CreateScanResponse;
import com.checkmarx.jenkins.web.model.GetOpenSourceSummaryResponse;
import com.checkmarx.jenkins.web.model.ScanDetails;

import java.util.List;


/**
 * Created by tsahib on 9/12/2016.
 */
public class ScanSender {

    private OsaScanClient osaScanClient;
    private long projectId;

    public ScanSender(OsaScanClient scanClient, long projectId) {
        this.osaScanClient = scanClient;
        this.projectId = projectId;
    }

    public OsaScanResult sendAsync(String osaDependenciesJson, LibrariesAndCVEsExtractor librariesAndCVEsExtractor) throws Exception {
        createScan(osaDependenciesJson);
        ScanDetails latestScanDetails = osaScanClient.getLatestScanId(projectId);

        if(latestScanDetails != null) {
            GetOpenSourceSummaryResponse getOpenSourceSummaryResponse = getOpenSourceSummary(latestScanDetails.getId());
            OsaScanResult osaScanResult = new OsaScanResult();
            osaScanResult.setScanId(latestScanDetails.getId());
            osaScanResult.setOsaScanStartAndEndTimes(latestScanDetails);
            osaScanResult.setOsaResults(getOpenSourceSummaryResponse);
            osaScanResult.setOsaLicense(true);
            librariesAndCVEsExtractor.getAndSetLibrariesAndCVEsToScanResult(osaScanResult);
            return osaScanResult;
        }
        return null;
    }

    public OsaScanResult sendOsaScanAndGetResults(String osaDependenciesJson) throws Exception {
        OsaScanResult osaScanResult = new OsaScanResult();
        CreateScanResponse createScanResponse = createScan(osaDependenciesJson);
        osaScanResult.setScanId(createScanResponse.getScanId());
        ScanDetails scanDetails = waitForScanToFinish(createScanResponse.getScanId());
        GetOpenSourceSummaryResponse getOpenSourceSummaryResponse = getOpenSourceSummary(createScanResponse.getScanId());
        osaScanResult.setOsaScanStartAndEndTimes(scanDetails);
        osaScanResult.setOsaResults(getOpenSourceSummaryResponse);
        return osaScanResult;
    }

    private CreateScanResponse createScan(String osaDependenciesJson) throws Exception {
        return osaScanClient.createScan(projectId, osaDependenciesJson);
    }

    private ScanDetails waitForScanToFinish(String scanId) throws InterruptedException {
        return osaScanClient.waitForScanToFinish(scanId);
    }

    private GetOpenSourceSummaryResponse getOpenSourceSummary(String scanId) throws Exception {
        return osaScanClient.getOpenSourceSummary(scanId);
    }

}
