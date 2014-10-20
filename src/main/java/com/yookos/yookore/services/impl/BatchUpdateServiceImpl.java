package com.yookos.yookore.services.impl;

import com.mongodb.*;
import com.yookos.yookore.services.BatchUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jome on 2014/10/01.
 */

@Service
public class BatchUpdateServiceImpl implements BatchUpdateService {
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MongoClient client;


    @Override
    public void createAndUpdateGroups(List<String> rows) {
        ProcessGroupMemberships groupMemberships = new ProcessGroupMemberships(rows);
        new Thread(groupMemberships).start();
    }

    @Override
    public void createAndUpdatePages(List<String> rows) {
        ProcessPages pages = new ProcessPages(rows);
        new Thread(pages).start();
    }

    @Override
    public Map<String, Object> getPlaceId(String placetype, String displayname) {
        Map<String, Object> result = new HashMap<>();

        if (placetype.trim().equals("page")) {
            DBCollection pages = client.getDB("yookosreco").getCollection("pages");
            DBObject foundPage = pages.findOne(new BasicDBObject("displayname", displayname));
            if (foundPage != null) {
                int pageid = (int) foundPage.get("spaceid");
                result.put("pageId", pageid);
                result.put("displayname", displayname);
                result.put("placetype", placetype);
                result.put("browseid", foundPage.get("browseid"));
                return result;
            } else {
                result.put("errorcode", 1001);
                result.put("message", "The requested page could not be found");
                return result;
            }
        }

        if (placetype.trim().equals("group")) {
            DBCollection groups = client.getDB("yookosreco").getCollection("socialgroups");
            DBObject foundGroup = groups.findOne(new BasicDBObject("displayname", displayname));
            if (foundGroup != null) {
                long placeid = (long) foundGroup.get("groupid");
                result.put("groupId", placeid);
                result.put("displayname", displayname);
                result.put("placetype", placetype);
                result.put("browseid", foundGroup.get("browseid"));
                return result;
            } else {
                result.put("errorcode", 1001);
                result.put("message", "The requested group could not be found");
                return result;
            }
        }
        return result;
    }

    @Override
    public void processGroupBrowseIds(List<String> rows) {
        ProcessBrowseIds browseIds = new ProcessBrowseIds(rows);
        new Thread(browseIds).start();
    }

    @Override
    public void processBlogBrowseIds(List<String> rows) {
        ProcessBrowseBlogs browseblogs = new ProcessBrowseBlogs(rows);
        new Thread(browseblogs).start();
    }


    //Implementation specific methods and classes

    class ProcessBrowseBlogs implements Runnable {
        List<String> rows;

        public ProcessBrowseBlogs(List<String> rows) {
            this.rows = rows;
        }

        @Override
        public void run() {
            DBCollection blogs;
            if (client.getDB("yookosreco").collectionExists("blogs")) {
                blogs = client.getDB("yookosreco").getCollection("blogs");
            } else {
                blogs = client.getDB("yookosreco").createCollection("blogs", null);
            }


            int rowCount = 0;

            for (String r : rows) {
                String[] rd = r.split(";");
                if (rd.length == 5) {
                    DBObject blog = new BasicDBObject("browseid", Integer.parseInt(rd[0]))
                            .append("blogid", Integer.parseInt(rd[1]))
                            .append("displayname", rd[2])
                            .append("name", rd[3])
                            .append("containertype", Integer.parseInt(rd[4]));

                    try {
                        WriteResult savedblog = blogs.save(blog);
                        log.info("Saved info: {}", savedblog);
                        rowCount++;

                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                }
            }
        }
    }


    class ProcessGroupMemberships implements Runnable {
        List<String> rows;

        public ProcessGroupMemberships(List<String> rows) {
            this.rows = rows;
        }

        @Override
        public void run() {
            DBCollection groups;
            if (client.getDB("yookosreco").collectionExists("socialgroups")) {
                groups = client.getDB("yookosreco").getCollection("socialgroups");
            } else {
                groups = client.getDB("yookosreco").createCollection("socialgroups", null);
            }


            int rowCount = 0;
            List<Map<String, Object>> holder = new ArrayList<>();
            List<Integer> missedRows = new ArrayList<>();

            Map<String, Object> params = new HashMap<>();

            for (String r : rows) {
                String[] rd = r.split(";");
                System.out.println("Row count: " + (rowCount + 1) + "| " + r);
                System.out.println("Row count: " + (rowCount + 1)
                        + "| Array Length: " + rd.length);
                if (rd.length == 8) {
                    Map<String, Object> props = new HashMap<>();

                    DBObject group = new BasicDBObject("groupid", Long.parseLong(rd[0]))
                            .append("grouptype", Integer.parseInt(rd[1]))
                            .append("name", rd[2])
                            .append("displayname", rd[3])
                            .append("userid", Long.parseLong(rd[4]))
                            .append("creationdate", Long.parseLong(rd[5]))
                            .append("modificationdate", Long.parseLong(rd[6]))
                            .append("status", Integer.parseInt(rd[7]));
                    try {
                        WriteResult savedgroup = groups.save(group);
                        log.info("Saved info: {}", savedgroup);
                        rowCount++;

                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                }

            }
        }
    }

    class ProcessPages implements Runnable {
        List<String> rows;

        public ProcessPages(List<String> rows) {
            this.rows = rows;
        }

        @Override
        public void run() {
            DBCollection pages;
            if (client.getDB("yookosreco").collectionExists("pages")) {
                pages = client.getDB("yookosreco").getCollection("pages");
            } else {
                pages = client.getDB("yookosreco").createCollection("pages", null);
            }


            int rowCount = 0;

            Map<String, Object> params = new HashMap<>();

            for (String r : rows) {
                String[] rd = r.split(";");
                System.out.println("Row count: " + (rowCount + 1) + "| " + r);
                System.out.println("Row count: " + (rowCount + 1)
                        + "| Array Length: " + rd.length);
                DBObject page = new BasicDBObject("browseid", Long.parseLong(rd[0]))
                        .append("spaceid", Integer.parseInt(rd[1]))
                        .append("name", rd[2])
                        .append("displayname", rd[3])
                        .append("creationdate", Long.parseLong(rd[4]));

                try {
                    WriteResult savedPage = pages.save(page);
                    log.info("Saved info: {}", savedPage);

                    //holder.add(props);
                    rowCount++;
                } catch (Exception e) {
                    log.error(e.toString());
                }

            }
        }
    }

    class ProcessBrowseIds implements Runnable {
        List<String> rows;

        public ProcessBrowseIds(List<String> rows) {
            this.rows = rows;
        }

        @Override
        public void run() {
            DBCollection groups = client.getDB("yookosreco").getCollection("socialgroups");

            int rowCount = 0;

            Map<String, Object> params = new HashMap<>();

            for (String r : rows) {
                String[] rd = r.split(";");

                try {
                    WriteResult update = groups.update(new BasicDBObject("displayname", rd[1]), new BasicDBObject("$set", new BasicDBObject("browseid", rd[0])));

                    log.info("Row {}: '{}'  Saved info: {}", rowCount, rd[1], update);

                    //holder.add(props);

                    rowCount++;
                } catch (Exception e) {
                    log.error(e.toString());
                }

            }
        }
    }

}
