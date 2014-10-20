package com.yookos.yookore.services;

import java.util.List;
import java.util.Map;

/**
 * Created by jome on 2014/10/01.
 */
public interface BatchUpdateService {
    void createAndUpdateGroups(List<String> rows);
    void createAndUpdatePages(List<String> rows);

    Map<String, Object> getPlaceId(String placetype, String displayname);

    void processGroupBrowseIds(List<String> rows);
    void processBlogBrowseIds(List<String> rows);
}
