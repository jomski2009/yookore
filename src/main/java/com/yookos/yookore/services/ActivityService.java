package com.yookos.yookore.services;

import com.yookos.yookore.domain.Activity;

import java.util.List;

/**
 * Created by jome on 2014/09/08.
 */

public interface ActivityService {
    void addActivityObjectToGraph(List<Activity> activities);

}
