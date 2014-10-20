package com.yookos.yookore.controllers;

import com.yookos.yookore.domain.AndroidDeviceRegistration;
import com.yookos.yookore.domain.UserRelationship;
import com.yookos.yookore.helpers.HelperUtils;
import com.yookos.yookore.services.BatchUpdateService;
import com.yookos.yookore.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Autowired
    BatchUpdateService batchUpdateService;

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
                notificationService.addToUserRelationshipBatch(recipients);
            }

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

    /**
     * Process notifications block list
     * @return
     */
    @RequestMapping("processblocks")
    public HttpEntity processBlockLists() {
        notificationService.processBlockList();
        return new ResponseEntity("In process...", HttpStatus.ACCEPTED);
    }

    /**
     * Process groups storage in Mongo
     *
     */
    @RequestMapping(value = "processgroups", method = RequestMethod.POST)
    public ResponseEntity<Void> createGroupmemberships(
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Starting csv processing...");
            List<String> rows = new ArrayList<>();

            if (!file.isEmpty()) {
                String row;
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        file.getInputStream()));
                while ((row = br.readLine()) != null) {
                    rows.add(row);
                }
                br.close();
                System.out.println(rows.get(0));
                rows.remove(0); // Just to get csv working... removing the
                // header row...
                System.out.println(rows.get(0));

                batchUpdateService.createAndUpdateGroups(rows);

                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

    /**
     * Proceess page storage in mongo
     * @param file
     * @return
     */
    @RequestMapping(value = "processpages", method = RequestMethod.POST)
    public ResponseEntity<Void> addSpacesFromCSV(
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Starting csv processing...");
            List<String> rows = new ArrayList<>();

            if (!file.isEmpty()) {
                String row;
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        file.getInputStream()));
                while ((row = br.readLine()) != null) {
                    rows.add(row);
                }
                br.close();
                System.out.println(rows.get(0));
                rows.remove(0); // Just to get csv working... removing the
                // header row...
                System.out.println(rows.get(0));

                batchUpdateService.createAndUpdatePages(rows);

                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

    /**
     * Add browseid to groups in mongo
     */
    @RequestMapping(value = "processgroupbrowseid", method = RequestMethod.POST)
    public ResponseEntity<Void> processBorowseIds(
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Starting csv processing...");
            List<String> rows = new ArrayList<>();

            if (!file.isEmpty()) {
                String row;
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        file.getInputStream()));
                while ((row = br.readLine()) != null) {
                    rows.add(row);
                }
                br.close();
                System.out.println(rows.get(0));
                rows.remove(0); // Just to get csv working... removing the
                // header row...
                System.out.println(rows.get(0));

                batchUpdateService.processGroupBrowseIds(rows);

                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

    /**
     * Add blogbrowseids to mongo
     */
    @RequestMapping(value = "processblogs", method = RequestMethod.POST)
    public ResponseEntity<Void> processBlogs(
            @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Starting csv processing...");
            List<String> rows = new ArrayList<>();

            if (!file.isEmpty()) {
                String row;
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        file.getInputStream()));
                while ((row = br.readLine()) != null) {
                    rows.add(row);
                }
                br.close();
                System.out.println(rows.get(0));
                rows.remove(0); // Just to get csv working... removing the
                // header row...
                System.out.println(rows.get(0));

                batchUpdateService.processBlogBrowseIds(rows);

                return new ResponseEntity<>(HttpStatus.CREATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    }

    /**
     * Adhoc stuff for mobile callback links
     * Given a page or a group displayname, return the group or page id
     */
   @RequestMapping(value = "getplaceid/{placetype}/{displayname}", method = RequestMethod.GET)
    public HttpEntity getPlaceId(@PathVariable("placetype") String placetype, @PathVariable("displayname") String displayname){
       Map<String, Object> result = batchUpdateService.getPlaceId(placetype, displayname);

       if (result.containsKey("errorcode")){
           return new ResponseEntity(result, HttpStatus.OK);
       }else{
           return new ResponseEntity(result, HttpStatus.NOT_FOUND);
       }
   }
}
