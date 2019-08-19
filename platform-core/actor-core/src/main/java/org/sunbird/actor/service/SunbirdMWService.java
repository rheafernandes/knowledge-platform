package org.sunbird.actor.service;


/** @author Mahesh Kumar Gangula */
public class SunbirdMWService extends BaseMWService {

  public static void init() {
    getActorSystem();
    initRouters();
  }
}
