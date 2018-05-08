package com.pxs.reaper.agent.runner;

class Thruple {

    String page;
    int fetchTime = 0;
    int fetches = 0;
    int errors = 0;
    volatile transient long start = 0;

}