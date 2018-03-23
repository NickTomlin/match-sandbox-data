package com.example.hellomatch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mastercard.api.core.ApiConfig;
import com.mastercard.api.core.exception.*;
import com.mastercard.api.core.model.*;
import com.mastercard.api.core.model.map.*;
import com.mastercard.api.core.security.oauth.OAuthAuthentication;
import com.mastercard.api.match.*;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.Map;

public class HelloMatch {

    // I am not using a .p12 file so I need to convert it into a PrivateKey
    // stolen from: https://stackoverflow.com/a/34456838
    public static PrivateKey getPrivateKey() throws Exception {
        String keyString = new String(Files.readAllBytes(Paths.get("/tmp/match-private.key")));
        StringBuilder pkcs8Lines = new StringBuilder();

        BufferedReader rdr = new BufferedReader(new StringReader(keyString));
        String line;
        while ((line = rdr.readLine()) != null) {
            pkcs8Lines.append(line);
        }

        String pkcs8Pem = pkcs8Lines.toString();
        pkcs8Pem = pkcs8Pem.replace("-----BEGIN PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replace("-----END PRIVATE KEY-----", "");
        pkcs8Pem = pkcs8Pem.replaceAll("\\s+", "");

        byte[] pkcs8EncodedBytes = Base64.decode(pkcs8Pem);


        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privKey = kf.generatePrivate(keySpec);
        System.out.println(privKey);

        return privKey;
    }

    public static String getConsumerKey() throws Exception {
        String key =  new String(Files.readAllBytes(Paths.get("/tmp/match-client-id")));
        return key.trim();
    }

    public static void main(String[] args) throws Exception {
        String consumerKey = getConsumerKey();
        PrivateKey privateKey = getPrivateKey();

        ApiConfig.setAuthentication(new OAuthAuthentication(consumerKey, privateKey));
        ApiConfig.setSandbox(true);
        ApiConfig.setEnvironment(Environment.SANDBOX);


        try {
            RequestMap map = new RequestMap();
            map.set("PageOffset", "0");
            map.set("PageLength", "10");
            map.set("TerminationInquiryRequest.AcquirerId", "1996");
            map.set("TerminationInquiryRequest.Merchant.Name", "TERMINATED MERCHANT 12");
            map.set("TerminationInquiryRequest.Merchant.DoingBusinessAsName", "DOING BUSINESS AS TERMINATED MERCHANT 12");
            map.set("TerminationInquiryRequest.Merchant.PhoneNumber", "5555555555");
            map.set("TerminationInquiryRequest.Merchant.Address.Line1", "1823 INDEPENDENCE AVE");
            map.set("TerminationInquiryRequest.Merchant.Address.Line2", "APT 9009");
            map.set("TerminationInquiryRequest.Merchant.Address.City", "CHATSWORTH");
            map.set("TerminationInquiryRequest.Merchant.Address.CountrySubdivision", "CA");
            map.set("TerminationInquiryRequest.Merchant.Address.PostalCode", "55555");
            map.set("TerminationInquiryRequest.Merchant.Address.Country", "USA");

            // This is line 6 from the TERMINATED_PRINCIPALS sandbox data spreadsheet
            map.set("TerminationInquiryRequest.Merchant.Principal[0].FirstName", "GARY");
            map.set("TerminationInquiryRequest.Merchant.Principal[0].LastName", "OWEN");
            map.set("TerminationInquiryRequest.Merchant.Principal[0].PhoneNumber", "5555555555");
            map.set("TerminationInquiryRequest.Merchant.Principal[0].NationalId", "884052747");
            map.set("TerminationInquiryRequest.Merchant.Principal[0].Address.CountrySubdivision", "NY");
            map.set("TerminationInquiryRequest.Merchant.Principal[0].Address.PostalCode", "55555");
            map.set("TerminationInquiryRequest.Merchant.Principal[0].Address.Country", "USA");
            map.set("TerminationInquiryRequest.Merchant.SearchCriteria.SearchAll", "Y");
            map.set("TerminationInquiryRequest.Merchant.SearchCriteria.MinPossibleMatchCount", "1");
            TerminationInquiryRequest response = TerminationInquiryRequest.create(map);

            out(response, "TerminationInquiry.PageOffset");
            out(response, "TerminationInquiry.PossibleMerchantMatches[0].TotalLength");

            for(Map<String,Object> item : (List<Map<String, Object>>) response.get("TerminationInquiry.PossibleMerchantMatches")) {
                JSONArray terminatedMerchants = (JSONArray) item.get("TerminatedMerchant");
                for(Object terminatedMerchant : terminatedMerchants) {
                    JSONObject merchantMatch = (JSONObject) ((JSONObject)terminatedMerchant).get("MerchantMatch");
                    JSONArray principalMatches = (JSONArray) merchantMatch.get("PrincipalMatch");
                    for (Object principalMatch : principalMatches) {
                        JSONObject principal = (JSONObject) principalMatch;
                        // At the moment, this results in M00
                        // for all national ids that I can see
                        // full output here: https://gist.github.com/NickTomlin/7834347aa3b4a35432a601895dcee98b
                        System.out.println(principal.get("NationalId"));
                    }
                }
            }
        } catch (ApiException e) {
            err("HttpStatus: " + e.getHttpStatus());
            err("Message: " + e.getMessage());
            err("ReasonCode: " + e.getReasonCode());
            err("Source: " + e.getSource());
        }
    }

    public static void out(SmartMap response, String key) {
        System.out.println(key + "-->" + response.get(key));
    }

    public static void out(Map<String, Object> map, String key) {
        System.out.println(key + "--->" + map.get(key));
    }

    public static void err(String message) {
        System.err.println(message);
    }
}