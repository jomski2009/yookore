package com.yookos.yookore.controllers;

import com.yookos.yookore.domain.AndroidDeviceRegistration;
import com.yookos.yookore.domain.UserRelationship;
import com.yookos.yookore.helpers.HelperUtils;
import com.yookos.yookore.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * All endpoints to process batch data
 * such as updating devices, upload activities
 *
 * @author Jome Akpoduado.
 */

@RestController
@RequestMapping(value = "batch")
public class BatchUpdateController {
    Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    HelperUtils helper;

    @Autowired
    NotificationService notificationService;

    @RequestMapping(value = "updatedevices", method = RequestMethod.POST)
    public ResponseEntity<Void> updateUserDevicesFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Starting update device csv processing...");
            List<AndroidDeviceRegistration> rows = new ArrayList<>();

            if (!file.isEmpty()) {
                String row;
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        file.getInputStream()));
                while ((row = br.readLine()) != null) {
                    //log.info("Row: {}",row);
                    String[] sp = row.split(";");
                    //log.info("Split string: {}", sp.toString());
                    if (!helper.sanitize(sp[0]).trim().equals("null")) {
                        //System.out.println(sp[0]);
                        int user_id = Integer.parseInt(helper.sanitize(sp[0]));
                        String regid = sp[1];
                        AndroidDeviceRegistration uadr = new AndroidDeviceRegistration();
                        uadr.setGcm_regid(regid);
                        uadr.setUserid(user_id);
                        rows.add(uadr);
                    }
                }
                br.close();


                notificationService.addDeviceToUserRelationship(rows);

                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

    @RequestMapping(value = "addrelationships", method = RequestMethod.POST)
    public ResponseEntity<Void> addUsersFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Starting csv processing...");
            List<String> rows = new ArrayList<>();
            List<UserRelationship> recipients = new ArrayList<>();

            if (!file.isEmpty()) {
                String row;
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        file.getInputStream()));
                while ((row = br.readLine()) != null) {
                    rows.add(row);
                }
                br.close();
                rows.remove(0); //Just to get csv working... removing the header row...
                int totalrows = rows.size();
                int rowcount = 1;
                for (String r : rows) {

                    log.info("Processing row {} of {}", rowcount, totalrows);
                    String[] recipientData = r.split(";");
                    UserRelationship userrel = new UserRelationship();
                    userrel.setActorid(Integer.parseInt(recipientData[1]));
                    userrel.setFollowerid(Integer.parseInt(recipientData[2]));
                    userrel.setUsername(recipientData[3]);
                    userrel.setEmail(recipientData[4]);
                    userrel.setCreationdate(Long.parseLong(recipientData[5]));
                    userrel.setHasdevice(false);
                    userrel.setRelationshipType(0);

                    System.out.println("Adding : " + userrel.getActorid());
                    recipients.add(userrel);
                    rowcount++;
                }
                notificationService.addToUserRelationship(recipients);
            }

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

}
